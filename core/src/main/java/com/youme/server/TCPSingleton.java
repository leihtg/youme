package com.youme.server;

import com.youme.contant.Contant;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * create at 2018年1月8日17:39:49
 */
public class TCPSingleton {
    public void sendBrocast() throws IOException {
        InetAddress.getLocalHost();
        InetAddress address = InetAddress.getByName("255.255.255.255");
        byte[] msg = "".getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length, address, Contant.BROCAST_PORT);

        DatagramSocket ds = new DatagramSocket();
        ds.setBroadcast(true);
        ds.send(packet);
    }

    public static void main(String[] args) throws UnknownHostException {
        System.out.println(InetAddress.getLocalHost());
    }
}
