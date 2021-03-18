package com.m2mapp.utilities;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

public class SharedFinals {

    public static final String PREFERENCES = "PREFERENCES" ;
    public static final String STREAMED_PACKETS = "STREAMED_PACKETS" ;

    public static final int UDP_REQUEST_PACKET_FROM_NODE_PORT = 5555;
    public static final int UDP_PACKET_TO_VERIFIER_PORT = 5999;
    public static final int UDP_PACKET_TO_NODES_PORT = 7777;
    public static final int UDP_IP_BROADCAST_PORT = 9988;

    public static final String BROAD_CAST_IP = "192.168.1.255";

    public static final String A1_IP = "192.168.1.32";
    public static final String Redim_IP = "192.168.1.33";
    public static final String Redim_Bro_IP = "192.168.1.42";

    public static Server UDP_REQUEST_PACKET_FROM_NODE_PORT_SERVER;
    public static Server UDP_PACKET_TO_VERIFIER_PORT_SERVER;
    public static Server UDP_PACKET_TO_NODES_PORT_SERVER;
    public static Server UDP_IP_BROADCAST_PORT_SERVER;

    public static Client UDP_REQUEST_PACKET_FROM_NODE_PORT_CLIENT;
    public static Client UDP_IP_BROADCAST_PORT_CLIENT;
    public static Client UDP_PACKET_TO_VERIFIER_PORT_CLIENT;
}
