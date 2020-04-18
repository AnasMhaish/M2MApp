package com.m2mapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

public class IPPacket implements Serializable {
    public  String OwnerIP;
    //0 for normal node
    //1 for verifier node
    public int Type;
    //If Type1 then Verifier Data should be filled
    public Hashtable<String, ArrayList<String>> PacketsNameOwnersIPsDict;
    public Hashtable<String, byte[]> PacketsDataHashDict;
}
