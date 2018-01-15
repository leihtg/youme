package com.core.server;

import com.core.contant.Contant;

/**
 * 报文包
 * Created by leihuating on 2018/1/15.
 */

public class BagPacket {
    //报文里面各类型字节数
    private static final byte ver_len = 2;//2字节
    private static final byte type_len = 4;
    private static final byte total_len = 4;

    public int version;
    public int type;
    public int length;

    public static int getHeadLen() {
        return ver_len + type_len + total_len;
    }

    /**
     * 拆包
     *
     * @return
     */
    public static BagPacket splitBag(byte[] buf) {
        if (buf.length < getHeadLen()) {
            throw new RuntimeException("length buf is:" + buf.length);
        }
        BagPacket bp = new BagPacket();
        bp.version = BitConvert.convertToInt(buf, 0, ver_len);
        bp.type = BitConvert.convertToInt(buf, ver_len, type_len);
        bp.length = BitConvert.convertToInt(buf, ver_len + type_len, total_len);
        if (bp.version == Contant.VERSION) {
            //版本控制,暂时没用
        }
        return bp;
    }

    /**
     * 装包
     *
     * @param dataLen
     * @param type
     * @return
     */
    public static byte[] AssembleBag(int dataLen, int type) {
        byte[] ret = new byte[getHeadLen()];

        int version = Contant.VERSION;
        byte[] vBytes = BitConvert.convertToBytes(version, ver_len);
        byte[] tBytes = BitConvert.convertToBytes(type, type_len);
        byte[] sBytes = BitConvert.convertToBytes(dataLen, total_len);

        System.arraycopy(vBytes, 0, ret, 0, ver_len);
        System.arraycopy(tBytes, 0, ret, ver_len, type_len);
        System.arraycopy(sBytes, 0, ret, ver_len + type_len, total_len);

        return ret;
    }

}
