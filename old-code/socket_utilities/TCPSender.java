package com.m2mapp.socket_utilities;

import android.os.Handler;

import com.m2mapp.utilities.EndPoint;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TCPSender {
    public static void send(EndPoint endPoint, String str){
        try {
            Socket s = new Socket(endPoint.ipAddress, endPoint.port);
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            pw.println(str);
            pw.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void send(EndPoint endPoint, byte[] bytes){
        try {
            Socket s = new Socket(endPoint.ipAddress, endPoint.port);
            OutputStream outputStream = s.getOutputStream();
            outputStream.write(bytes);
            outputStream.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
