package com.m2mapp.utilities;

public class EndPoint {
    public String ipAddress;
    public int port;

    public EndPoint(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public EndPoint(int port) {
        this.ipAddress = null;
        this.port = port;
    }
}
