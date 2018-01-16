package com.core.server;

/**
 * Created by leihuating on 2018/1/15.
 */

public class BitConvert {
    /**
     * 字节顺序为 小头 little_dian
     *
     * @param buf
     * @param offset
     * @return
     */
    public static int convertToInt(byte[] buf, int offset, int count) {
        int ret = 0, len = buf.length;
        for (int i = offset, px = 0; i < offset + count && i < len; i++, px++) {
            ret |= (buf[i] & 0xff) << (px * 8);
        }
        return ret;
    }

    /**
     * 转为bytes 小头
     *
     * @param data
     * @return
     */
    public static byte[] convertToBytes(int data, int size) {
        byte[] buf = new byte[size];
        for (int i = 0; i < size; i++) {
            if (i < 4) {
                buf[i] = (byte) ((data >>> (i * 8)) & 0xff);
            } else {
                buf[i] = 0;
            }
        }
        return buf;
    }

}
