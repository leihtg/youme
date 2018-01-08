package com.youme.server;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * create at 2018年1月8日17:39:49
 */
public class TCPSingleton {
    public void sendBrocast() throws UnknownHostException {
        InetAddress.getLocalHost();
    }

    public static void main(String[] args) throws UnknownHostException {
        System.out.println(InetAddress.getLocalHost());
    }
}
