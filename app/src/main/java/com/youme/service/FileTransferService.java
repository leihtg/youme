package com.youme.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.anser.enums.ActionType;
import com.anser.model.FileModel;
import com.anser.model.FileTransfer_in;
import com.anser.model.FileTransfer_out;
import com.core.server.FunCall;
import com.youme.db.DbHelper;
import com.youme.entity.FileTransferType;
import com.youme.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.youme.constant.APPFinal.storageDir;

/**
 * Created by leihtg on 2018/11/30 22:25.
 */
public class FileTransferService extends Service {
    private static ExecutorService executor = new ThreadPoolExecutor(2, 3, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));
    private static ExecutorService handle = Executors.newSingleThreadExecutor();
    private boolean isStart = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new FileBinder();
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isStart) {
            handle.execute(new Runnable() {
                @Override
                public void run() {
                    beginBack();
                }
            });
        }
        return super.onStartCommand(intent, flags, startId);
    }

    FunCall<FileTransfer_in, FileTransfer_out> fc = new FunCall<>();
    final DbHelper dbHelper = new DbHelper(this);

    private void beginBack() {
        isStart = true;
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

    private void addToExec(final File f, final int length) {
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

            byte[] buf = new byte[2048];
            int len = 0;
            int pos = 0;

            String path = f.getAbsolutePath();
            boolean b = dbHelper.hasUploaded(path, path);
            if (b) {
                broast(in.getModel(), pos, ActionType.UP_LOAD, FileTransferType.OVER);
                return;
            }
            while ((len = fis.read(buf)) != -1) {
                if (len < 2048) {
                    buf = Arrays.copyOf(buf, len);
                }
                in.setBuf(buf);
                in.setPos(pos);
                pos += len;
                fc.call(in, FileTransfer_out.class);
                broast(in.getModel(), pos, ActionType.UP_LOAD, FileTransferType.UPLOADING);
            }
            dbHelper.finishUpload(path, path);
            broast(in.getModel(), pos, ActionType.UP_LOAD, FileTransferType.OVER);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broast(FileModel model, long pos, ActionType type, FileTransferType flag) {
        for (FileBinderCallback back : callbacks) {
            back.status(model, pos, type, flag);
        }
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
