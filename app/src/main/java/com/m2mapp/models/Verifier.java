package com.m2mapp.models;

import java.util.ArrayList;
import java.util.Hashtable;

public class Verifier extends Node {
    public Hashtable<String, ArrayList<String>> PacketsNameOwnersIPsDict;
    public Hashtable<String, byte[]> PacketsDataHashDict;

    public Verifier(String NodeIP) {
        super(NodeIP);
        PacketsNameOwnersIPsDict = new Hashtable<>();
        PacketsDataHashDict = new Hashtable<>();
    }

    public Verifier(String NodeIP,  Hashtable<String, ArrayList<String>> packetsNameOwnersIPsDict, Hashtable<String, byte[]> packetsDataHashDict) {
        super(NodeIP);
        PacketsNameOwnersIPsDict = packetsNameOwnersIPsDict;
        PacketsDataHashDict = packetsDataHashDict;
    }
}
