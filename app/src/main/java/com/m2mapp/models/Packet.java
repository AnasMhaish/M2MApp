package com.m2mapp.models;

import java.io.Serializable;

public class Packet implements Serializable {
    public  String OwnerIP;
    public String Name;
    public byte[] Data;

    //0 for normal node
    //1 for verifier node
    public int Type;

}
