package com.youme.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.anser.contant.Contant;
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

import java.io.*;
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

        broast(ActionType.DOWN_LOAD, pos, 0, FileTransferType.DOWNLOADING);

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
                            broast(ActionType.DOWN_LOAD, pos, 0, FileTransferType.OVER);
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
                Socket socket = new Socket(TCPSingleton.getHostAddr(), Contant.FILE_SOCKET_PORT);
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                os.write(1);//1上传
                while (true) {
                    try {
                        FileTransfer take = getTask();
                        sendFile(take, os);
                        broast(ActionType.UP_LOAD, take.getLength(), 0, FileTransferType.OVER);
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

    int taskIndex = 0;

    private FileTransfer getTask() throws InterruptedException {
        synchronized (list) {
            if (list.isEmpty()) {
                list.addAll(queryUpload());
            }
            if (list.isEmpty() || taskIndex - 1 > list.size()) {
                list.wait();
            }
            return list.get(taskIndex++);
        }
    }

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

        String path = f.getAbsolutePath();
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

    private void sendFile(FileTransfer take, OutputStream os) throws IOException {
        String path = take.getPath();
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        take.setPath(path.substring(dirLen));
        String fileInfo = gson.toJson(take);
        byte[] bs = fileInfo.getBytes("utf8");
        byte[] head = BitConvert.convertToBytes(bs.length, 4);
        os.write(head);
        if (file.isDirectory()) {
            os.write(BitConvert.convertToBytes(0));
            os.write(bs);
            dbHelper.finishUpload(path);
            return;
        }
        long length = take.getLength();
        os.write(BitConvert.convertToBytes(length));
        os.write(bs);
        try (FileInputStream fis = new FileInputStream(path)) {
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] buf = new byte[2048];
            int len = 0;
            int pos = 0;

            start = System.currentTimeMillis();
            while ((len = bis.read(buf)) != -1) {
                os.write(buf, 0, len);
                pos += len;
                prePos += len;
                if ((end = System.currentTimeMillis()) - start >= 1000) {//每秒发送一次
                    take.setFlags(FileTransferType.UPLOADING);
                    take.setPerSecondLen((int) prePos);
                    take.setPos(pos);
                    broast(ActionType.UP_LOAD, pos, prePos, FileTransferType.UPLOADING);
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

    long start = 0, end;
    long prePos = 0;

    private void broast(ActionType upLoad, long pos, long prePos, FileTransferType flag) {
        if (upLoad != ActionType.UP_LOAD) {
            return;
        }
        boolean isOver = flag == FileTransferType.OVER;
        if (procHandler != null) {
            Message msg = new Message();
            msg.obj = isOver ? 1 : 0;
            msg.arg1 = (int) pos;
            msg.arg2 = (int) prePos;
            procHandler.sendMessage(msg);
        } else if (isOver) {
            synchronized (list) {
                while (taskIndex > 0) {
                    list.remove(0);
                    taskIndex--;
                }
            }
        }
    }

    private List<FileTransfer> queryUpload() {
        return dbHelper.queryUploadFiles(1000);
    }


    Handler procHandler;

    public class FileBinder extends Binder {
        public void registerCallback(Handler handler) {
            procHandler = handler;
        }

        public void refresh(int pos, int prePos, boolean isOver) {
            synchronized (list) {
                if (list.isEmpty()) {
                    return;
                }
                int location = taskIndex - 1;
                if (location < 0) {
                    location = 0;
                }
                FileTransfer ft = list.get(location);
                ft.setPos(pos);
                ft.setPerSecondLen(prePos);
                if (isOver) {
                    ft.setFlags(FileTransferType.OVER);
                }
            }
        }

        public List<FileTransfer> getList() {
            return list;
        }
    }
}
