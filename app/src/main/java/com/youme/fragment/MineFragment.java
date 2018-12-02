package com.youme.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.anser.enums.ActionType;
import com.youme.R;
import com.youme.activity.FileSelectActivity;
import com.youme.constant.APPFinal;
import com.youme.service.FileTransferService;
import com.youme.util.ImageUtil;
import com.youme.view.CircleImageViewCustom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * Created by leihtg on 2018/11/24 23:06.
 */
public class MineFragment extends Fragment implements View.OnClickListener {
    // 拍照回传码
    public final static int CAMERA_REQUEST_CODE = 0;
    // 相册选择回传吗
    public final static int GALLERY_REQUEST_CODE = 1;

    private String iconPath = APPFinal.iconPath;

    // 照片所在的Uri地址
    private Uri imageUri;
    CircleImageViewCustom headIcon;

    Button backUp;
    Button begin_back;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.activity_mine, null);
        headIcon = (CircleImageViewCustom) inflate.findViewById(R.id.headIcon);
        backUp = (Button) inflate.findViewById(R.id.backUp);
        begin_back = (Button) inflate.findViewById(R.id.begin_back);

        headIcon.setOnClickListener(this);
        backUp.setOnClickListener(this);
        begin_back.setOnClickListener(this);

        if (new File(iconPath).exists()) {
            headIcon.setImageBitmap(BitmapFactory.decodeFile(iconPath));
        }
        return inflate;
    }

    @Override
    /*弹出选择图片方式，相册中获取和照相*/
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.headIcon:
                selectIcon();
                break;
            case R.id.backUp:
                selectBackup();
                break;
            case R.id.begin_back:
                Intent service = new Intent(getContext(), FileTransferService.class);
                service.putExtra("type", ActionType.UP_LOAD);
                getActivity().startService(service);
                break;
        }
    }


    private final static int REQ_FILE_DIR = 3;

    /*选择备份文件夹*/
    private void selectBackup() {
        Intent intent = new Intent(this.getContext(), FileSelectActivity.class);
        startActivityForResult(intent, REQ_FILE_DIR);
    }

    /*选择头像*/
    private void selectIcon() {
        final String[] item = {"相册", "拍照"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("请选择获取相片方式：");
        builder.setItems(item, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        goXiangChe();
                        break;
                    case 1:
                        goXiangJi();
                        break;
                }
            }


        });
        builder.create().show();
    }

    /*调用相册*/
    protected void goXiangChe() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private void goXiangJi() {
        Context context = this.getContext();
        FragmentActivity activity = this.getActivity();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //还没有权限
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GALLERY_REQUEST_CODE);
            return;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            return;
        }
        takePhoto();
    }

    /*获取权限后的回调*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {//调用系统相机申请拍照权限回调
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    if (Build.VERSION.SDK_INT > 23)
//                        imageUri = FileProvider.getUriForFile(getContext(), "com.zz.fileprovider", fileUri);//通过FileProvider创建一个content类型的Uri
                    takePhoto();
                }
                break;
            }
            case GALLERY_REQUEST_CODE://调用系统相册申请Sdcard权限回调
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goXiangChe();
                }
                break;
        }
    }

    private static SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);

    private void takePhoto() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(APPFinal.appDir, "IMG_" + fmt.format(new Date()) + ".jpg");
        imageUri = Uri.fromFile(file);
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(openCameraIntent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        CircleImageViewCustom head = (CircleImageViewCustom) headIcon;
        Bitmap bit = null;
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                bit = ImageUtil.decodeSampledBitmapFromFilePath(imageUri.getPath(), head.getWidth(), head.getHeight());
                head.setImageBitmap(bit);
                break;
            case GALLERY_REQUEST_CODE:
                try {
                    Uri uri = data.getData();
                    ContentResolver cr = getActivity().getContentResolver();
                    bit = BitmapFactory.decodeStream(cr.openInputStream(uri));
                    head.setImageBitmap(bit);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case REQ_FILE_DIR:
                Toast.makeText(this.getContext(), "ok", Toast.LENGTH_LONG).show();
                break;
        }
        if (null != bit) {
            try {
                File file = new File(iconPath);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                bit.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
