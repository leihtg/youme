package com.youme.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.anser.contant.MsgType;
import com.anser.enums.ActionType;
import com.anser.model.FileModel;
import com.anser.model.FileTransfer_in;
import com.anser.model.FileTransfer_out;
import com.core.server.FunCall;
import com.youme.constant.APPFinal;
import com.youme.db.DbHelper;
import com.youme.entity.FileTransferType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.youme.constant.APPFinal.storageDir;

/**
 * Created by leihtg on 2018/11/30 22:25.
 */
public class FileTransferService extends Service {
    private static final int QUE_SIZE = 1000;
    private static ExecutorService executor = new ThreadPoolExecutor(2, 3, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(QUE_SIZE));
    private static ExecutorService handle = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(200));
    private static ExecutorService viewHandle = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(200));

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new FileBinder();
    }

    FunCall<FileTransfer_in, FileTransfer_out> downFun = new FunCall<>();

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
                handle.execute(new Runnable() {
                    @Override
                    public void run() {
                        beginBack();
                    }
                });
                break;
            case DOWN_LOAD:
                FileModel model = (FileModel) intent.getSerializableExtra("model");
                broast(model, 0, ActionType.DOWN_LOAD, FileTransferType.WAITDOWNLOAD);
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

        broast(model, pos, ActionType.DOWN_LOAD, FileTransferType.DOWNLOADING);

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
                            broast(model, pos, ActionType.DOWN_LOAD, FileTransferType.OVER);
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

    FunCall<FileTransfer_in, FileTransfer_out> fc = new FunCall<>();
    final DbHelper dbHelper = new DbHelper(this);

    private void beginBack() {
        List<String> list = dbHelper.queryAutoBakPath();
        for (String dir : list) {
            scanDir(dir);
        }
    }

    private void scanDir(String dir) {
        File file = new File(dir);
        final int length = storageDir.length();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                scanDir(f.getAbsolutePath());
            }
            addToExec(file, length);
        } else {
            addToExec(file, length);
        }
    }

    private synchronized void addToExec(final File f, final int length) {
        while (true) {
            BlockingQueue<Runnable> queue = ((ThreadPoolExecutor) executor).getQueue();
            if (null != queue && queue.size() == QUE_SIZE) {
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            break;
        }
        final FileModel model = new FileModel();
        String file = f.getAbsolutePath();
        String path = file.substring(length);
        model.setPath(path);
        model.setName(f.getName());
        model.setLastModified(f.lastModified());
        model.setLength(f.length());
        model.setDir(f.isDirectory());
        if (!model.isDir()) {
            broast(model, 0, ActionType.UP_LOAD, FileTransferType.WAITUPLOAD);
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                FileTransfer_in in = new FileTransfer_in();
                in.setModel(model);

                in.setBusType(ActionType.UP_LOAD);
                if (model.isDir()) {
                    fc.call(in, FileTransfer_out.class);
                } else {
                    sendFile(in, f);
                }
            }
        });
    }

    private void sendFile(FileTransfer_in in, File f) {
        try (FileInputStream fis = new FileInputStream(f)) {
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] buf = new byte[2048];
            int len = 0;
            int pos = 0;

            String path = f.getAbsolutePath();
            boolean b = dbHelper.hasUploaded(path);
            if (b) {
                broast(in.getModel(), pos, ActionType.UP_LOAD, FileTransferType.OVER);
                return;
            }
            long start = System.currentTimeMillis(), end;
            while ((len = bis.read(buf)) != -1) {
                if (len < 2048) {
                    buf = Arrays.copyOf(buf, len);
                }
                in.setBuf(buf);
                in.setPos(pos);
                pos += len;
                fc.call(in, FileTransfer_out.class);
                if ((end = System.currentTimeMillis()) - start >= 1000) {//每秒发送一次
                    broast(in.getModel(), pos, ActionType.UP_LOAD, FileTransferType.UPLOADING);
                    start = end;
                }
            }
            dbHelper.finishUpload(path);
            broast(in.getModel(), pos, ActionType.UP_LOAD, FileTransferType.OVER);
            bis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broast(final FileModel model, final long pos, final ActionType type, final FileTransferType flag) {
        viewHandle.execute(new Runnable() {
            @Override
            public void run() {
                for (FileBinderCallback back : callbacks) {
                    back.status(model, pos, type, flag);
                }
            }
        });
    }

    public class FileBinder extends Binder {
        public void registerCallback(FileBinderCallback callback) {
            callbacks.add(callback);
        }

        public void unRegisterCallback(FileBinderCallback callback) {
            callbacks.remove(callback);
        }
    }

    private List<FileBinderCallback> callbacks = new ArrayList<>();

    public interface FileBinderCallback {
        /**
         * @param model
         * @param type
         * @param flag  0 未开始 1进行中 2已完成
         */
        void status(FileModel model, long pos, ActionType type, FileTransferType flag);
    }
}
