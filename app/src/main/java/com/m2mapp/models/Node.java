package com.m2mapp.models;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class Node {
    public Hashtable<String, byte[]>  PacketsNameDataDict;
    public ArrayList<String> ConnectedIPs;
    public String NodeIP;

    public Node(String nodeIP) {
        PacketsNameDataDict = new Hashtable<>();
        ConnectedIPs = new ArrayList<>();
        this.NodeIP = nodeIP;
    }
}
