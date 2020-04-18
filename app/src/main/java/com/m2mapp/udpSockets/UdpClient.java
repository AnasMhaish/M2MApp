package com.m2mapp.udpSockets;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.koushikdutta.async.AsyncDatagramSocket;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.callback.CompletedCallback;
import com.m2mapp.R;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class UdpClient {

    private final InetSocketAddress host;
    private AsyncDatagramSocket asyncDatagramSocket;
    private Activity activity;
    public UdpClient(String host, int port) {
        this.host = new InetSocketAddress(host, port);
        //this.activity = activity;
        setup();
    }

    private void setup() {
        try {
            asyncDatagramSocket = AsyncServer.getDefault().connectDatagram(host);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        asyncDatagramSocket.setClosedCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if(ex != null) throw new RuntimeException(ex);
                System.out.println("[Client] Successfully closed connection");
                //Toast.makeText(activity, "[Client] Successfully closed connection", Toast.LENGTH_SHORT).show();
            }
        });

        asyncDatagramSocket.setEndCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if(ex != null) throw new RuntimeException(ex);
                System.out.println("[Client] Successfully end connection");
                //Toast.makeText(activity, "[Client] Successfully end connection", Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void send(String msg) {
        asyncDatagramSocket.send(host, ByteBuffer.wrap(msg.getBytes()));
    }

    public void send(byte[] msg) {
        asyncDatagramSocket.send(host, ByteBuffer.wrap(msg));
    }


    public static void SendPacket(byte [] data, String ipAddress, int port, boolean isBroadCast) {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(ipAddress);
            DatagramPacket sendPacket = new DatagramPacket(data , data.length, IPAddress, port);
            if (isBroadCast)
                clientSocket.setBroadcast(true);
            clientSocket.send(sendPacket);
            clientSocket.close();
        } catch (Exception e) {
            Exception ee = e;
        }
    }
}
