package com.youme.util;

import com.youme.R;

import java.text.SimpleDateFormat;

/**
 * Created by Thinkpad on 2018/1/16 22:20.
 */
public class FileUtil {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final int KB = 1024;
    private static final int MB = KB * 1024;
    private static final int GB = MB * 1024;
    private static final long TB = (long)GB * 1024;

    public static String formatTime(Object date){
        return sdf.format(date);
    }

    /**
     * 获得文件大小的字符形式
     * @param size
     * @return
     */
    public static String getSize(long size) {
        String ret;
        if (size >= TB) {
            ret = String.format("%.2fTB", (float) size / TB);
        } else if (size >= GB) {
            ret = String.format("%.2fGB", (float) size / GB);
        } else if (size >= MB) {
            ret = String.format("%.2fMB", (float) size / MB);
        } else {//最小单位KB
            ret = String.format("%.2fKB", (float) size / KB);
        }
        return ret;
    }

    /**
     * 根据文件后缀判断类型
     *
     * @param file
     * @return
     */
    public static int getImg(String file) {
        if (null != file) {
            if (file.endsWith(".txt")) {
                return R.mipmap.txt;
            }
        }
        return R.mipmap.unknow;
    }
}
