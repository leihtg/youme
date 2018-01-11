package com.youme.util;

import android.content.Context;

/**
 * 获得屏幕相关的辅助类
 * Created by leihuating on 2018/1/11.
 */

public class ScreenUtils {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, int dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
