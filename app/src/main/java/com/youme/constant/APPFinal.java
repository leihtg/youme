package com.youme.constant;

import android.os.Environment;

/**
 * app的常量类
 *
 * @author DR
 *         Created by Administrator on 2016/5/16 0016.
 */
public class APPFinal {

    public static final String appDir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/yunPan/";

    public static final String dbName=appDir+"/db/file.db3";
    /**
     * bug发送地址
     */
    public static final String URL_BUGSEND = "http://bugs.dianshi-tech.com/home/receive";


    public static final String URL_EXCHANGE_INFO = "http://code.lymidas.com/TradeCenter/Get?TradeCenterAccount=";


    /**
     * bug文件存储地址
     */
    public static final String FILE_BUG = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/DianshiTrade/Bug/";
    /**
     * 更新文件存储地址
     */
    public static final String FILE_UPDATE = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/DianshiTrade/Update/";

    /**
     * SQLite的数据库名
     */
    public static final String DB_NAME = "dianshiChoose";
    /**
     * SQLite的数据版本号
     */
    public static final int DB_VERSION = 1;

    /**
     * SQLite的数据表名前缀
     */
    public static final String TABLE_NAME = "choose_list";
    /**
     * SQLite的数据表的各列的列名
     */
    public static final String C_ID = "c_id";


    /**
     * SharedPreference的文件名称--储存刷新状态
     */
    public static final String ShAERD_REFRESH = "refresh_state";
    /**
     * SharedPreference的文件名称--储存用户信息
     */
    public static final String ShAERD_FILE_USER = "userInfo";

    /**
     * SharedPreference的文件名称---存储商品信息
     */
    public static final String ShAERD_FILE_PRODUCT = "productInfo";

    /**
     * 未签名的debugAPK签名所申请的微信开发平台key
     */
//    public static final String WEXIN_APP_ID = "wx4635ae3619b0254e";
//    public static final String WEXIN_APP_SERCET = "a978a51b5a52e52a879c8437eaa1524c";

    /**
     * 签名版的app-release版APK签名所申请的微信开发平台key
     * 上线的时候使用的是签名版，要记得调换过来
     */
    public static final String WEXIN_APP_ID = "wx008611efbff064ff";
    public static final String WEXIN_APP_SERCET = "07add1b6dd0e8b1a8467f92c85518aee";


}
