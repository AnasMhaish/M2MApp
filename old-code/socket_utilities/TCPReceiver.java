package com.m2mapp.socket_utilities;

import android.util.Log;

import com.m2mapp.utilities.EndPoint;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class TCPReceiver {
    public static byte[] receive(EndPoint endPoint) {
        try {
            byte[] receiveData = new byte[1024];
            DatagramSocket serverSocket = new DatagramSocket(endPoint.port);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            return receivePacket.getData();
        } catch (Exception e) {
            Log.d("UDPReceiver", e.getMessage());
        }
        return null;
    }
}
