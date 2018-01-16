package com.youme.util;

import com.youme.R;

/**
 * Created by Thinkpad on 2018/1/16 22:20.
 */
public class FileUtil {
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
