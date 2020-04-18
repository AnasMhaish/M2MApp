package com.m2mapp.utilities;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.m2mapp.tcpSockets.TcpServer;

public class Utilities {

    public static int readIndex = 0;

    public static String MyIP = "";

    public static String GetMyIP(Context context) {
        if (MyIP != "")
            return MyIP;

        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        myWifiManager.startScan();
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
        int myIp = myWifiInfo.getIpAddress();

        int intMyIp3 = myIp/0x1000000;
        int intMyIp3mod = myIp%0x1000000;

        int intMyIp2 = intMyIp3mod/0x10000;
        int intMyIp2mod = intMyIp3mod%0x10000;

        int intMyIp1 = intMyIp2mod/0x100;
        int intMyIp0 = intMyIp2mod%0x100;

        MyIP = intMyIp0 + "." + intMyIp1 + "." + intMyIp2 + "." + intMyIp3;
        return MyIP;
    }
}
