package com.youme.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.anser.contant.MsgType;
import com.anser.enums.ActionType;
import com.anser.model.FileModel;
import com.anser.model.FileTransfer_in;
import com.anser.model.FileTransfer_out;
import com.anser.util.BitConvert;
import com.core.server.FunCall;
import com.core.server.TCPSingleton;
import com.google.gson.Gson;
import com.youme.constant.APPFinal;
import com.youme.db.DbHelper;
import com.youme.entity.FileTransfer;
import com.youme.entity.FileTransferType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by leihtg on 2018/11/30 22:25.
 */
public class FileTransferService extends Service {
    private static ExecutorService handle = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(200));

    @Override
    public IBinder onBind(Intent intent) {
        return new FileBinder();
    }

    FunCall<FileTransfer_in, FileTransfer_out> downFun = new FunCall<>();

    List<FileTransfer> list = new ArrayList<>(1000);

    @Override
    public void onCreate() {
        downFun.setFuncResultHandler(handler);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ActionType type = (ActionType) intent.getSerializableExtra("type");
        switch (type) {
            case UP_LOAD:
                doUpload();
                break;
            case DOWN_LOAD:
                FileModel model = (FileModel) intent.getSerializableExtra("model");
                downloadFile(model, 0);
                break;


        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void downloadFile(FileModel model, long pos) {
        FileTransfer_in in = new FileTransfer_in();

        in.setModel(model);
        in.setPos(pos);
        in.setBusType(ActionType.DOWN_LOAD);

        broast(ActionType.DOWN_LOAD, FileTransferType.DOWNLOADING);

        downFun.call(in, FileTransfer_out.class);
    }


    static ConcurrentHashMap<String, RandomAccessFile> map = new ConcurrentHashMap<>();
    @SuppressLint("HandlerLeak")
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FileTransfer_out rd = (FileTransfer_out) msg.obj;
            switch (rd.msgType) {
                case MsgType.SUCC:
                    try {
                        FileModel model = rd.getModel();

                        String name = model.getName();
                        RandomAccessFile rw = map.get(name);
                        if (null == rw) {
                            File file = new File(APPFinal.appDir, name);
                            rw = new RandomAccessFile(file, "rw");
                            rw.setLength(model.getLength());
                            map.put(name, rw);
                        }
                        rw.seek(rd.getPos());
                        rw.write(rd.getBuf());
                        long pos = rd.getPos() + rd.getBuf().length;

                        if (pos == model.getLength()) {
                            rw.close();
                            map.remove(name);
                            File file = new File(APPFinal.appDir, name);
                            file.setLastModified(model.getLastModified());
                            broast(ActionType.DOWN_LOAD, FileTransferType.OVER);
                            Toast.makeText(getApplicationContext(), new File(APPFinal.appDir, name).getAbsolutePath() + ",下载完成", Toast.LENGTH_LONG).show();
                        } else {
                            downloadFile(model, pos);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case MsgType.ERROR:
                    Toast.makeText(getApplicationContext(), rd.msg, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    final DbHelper dbHelper = new DbHelper(this);

    /**
     * 上传
     */
    private void doUpload() {
        handle.execute(new Runnable() {
            @Override
            public void run() {
                beginBack();
            }
        });
        if (!uploadFile.isAlive()) {
            uploadFile.start();
        }
    }

    int dirLen = APPFinal.storageDir.length();
    Gson gson = new Gson();
    Thread uploadFile = new Thread() {
        @Override
        public void run() {
            try {
                Socket socket = new Socket(TCPSingleton.getHostAddr(), 8181);
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                os.write(1);//1上传
                while (true) {
                    try {
                        if (list == null || list.isEmpty()) {
                            list = queryUpload();
                        }
                        synchronized (list) {
                            if (list.isEmpty()) {
                                list.wait();
                            }
                        }
                        FileTransfer take = list.get(0);
                        String path = take.getPath();
                        File file = new File(path);
                        if (!file.exists()) {
                            list.remove(0);
                            continue;
                        }
                        take.setPath(path.substring(dirLen));
                        String fileInfo = gson.toJson(take);
                        byte[] bs = fileInfo.getBytes("utf8");
                        byte[] head = BitConvert.convertToBytes(bs.length, 4);
                        os.write(head);
                        if (file.isDirectory()) {
                            os.write(BitConvert.convertToBytes(0, 4));
                            os.write(bs);
                            dbHelper.finishUpload(file.getAbsolutePath());
                            list.remove(0);
                            continue;
                        }
                        int length = (int) take.getLength();
                        os.write(BitConvert.convertToBytes(length, 4));
                        os.write(bs);
                        sendFile(take, file, os);
                        broast(ActionType.UP_LOAD, FileTransferType.OVER);
                        list.remove(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void beginBack() {
        List<String> list = dbHelper.queryAutoBakPath();
        for (String dir : list) {
            scanDir(dir);
        }
    }

    private void scanDir(String dir) {
        File file = new File(dir);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                scanDir(f.getAbsolutePath());
            }
            addToExec(file);
        } else {
            addToExec(file);
        }
    }

    private void addToExec(final File f) {

        String file = f.getAbsolutePath();
        String path = file;
        boolean b = dbHelper.hasUploaded(path);
        if (b) {
            return;
        }
        FileTransfer model = new FileTransfer();
        model.setPath(path);
        model.setLastModified(f.lastModified());
        model.setLength(f.length());
        model.setDir(f.isDirectory());
        model.setPos(0);
        model.setFlags(FileTransferType.WAITUPLOAD);

        if (list.size() < 1000) {
            list.add(model);
            if (list.size() == 1) {
                synchronized (list) {
                    list.notify();
                }
            }
        }
        dbHelper.addUploadFiles(model);
    }

    private void sendFile(FileTransfer take, File f, OutputStream os) {
        try (FileInputStream fis = new FileInputStream(f)) {
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] buf = new byte[2048];
            int len = 0;
            int pos = 0;

            String path = f.getAbsolutePath();
            long start = System.currentTimeMillis(), end;
            long prePos = 0;
            while ((len = bis.read(buf)) != -1) {
                os.write(buf, 0, len);
                pos += len;
                prePos += len;
                if ((end = System.currentTimeMillis()) - start >= 1000) {//每秒发送一次
                    take.setFlags(FileTransferType.UPLOADING);
                    take.setPerSecondLen((int) prePos);
                    take.setPos(pos);
                    broast(ActionType.UP_LOAD, FileTransferType.UPLOADING);
                    start = end;
                    prePos = 0;
                }
            }
            dbHelper.finishUpload(path);
            bis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broast(ActionType upLoad, FileTransferType flag) {
        if (upLoad != ActionType.UP_LOAD) {
            return;
        }
        Message msg = new Message();
        msg.obj = list;
        procHandler.sendMessage(msg);
    }

    private List<FileTransfer> queryUpload() {
        return dbHelper.queryUploadFiles(1000);
    }


    Handler procHandler;

    public class FileBinder extends Binder {
        public void registerCallback(Handler handler) {
            procHandler = handler;
        }

    }

}
