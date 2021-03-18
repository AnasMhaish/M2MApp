package com.m2mapp.udpSockets;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.location.SettingInjectorService;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import com.koushikdutta.async.AsyncDatagramSocket;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.m2mapp.MainActivity;
import com.m2mapp.models.IPPacket;
import com.m2mapp.models.Packet;
import com.m2mapp.models.Packettt;
import com.m2mapp.models.Verifier;
import com.m2mapp.utilities.BlockChainService;
import com.m2mapp.utilities.LoggerService;
import com.m2mapp.utilities.SharedFinals;
import com.m2mapp.utilities.Utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class UdpServer {
    private InetSocketAddress host;
    private AsyncDatagramSocket asyncDatagramSocket;
    private Activity activity;
    private  WifiManager wifi;
    public UdpServer(Activity activity, String host, int port) {
        this.host = new InetSocketAddress(host, port);
        this.activity = activity;

        this.wifi = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock lock = wifi.createMulticastLock("lock");
        lock.acquire();

        setup();
    }

    private void setup() {
        try {
            asyncDatagramSocket = AsyncServer.getDefault().openDatagram(host, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        asyncDatagramSocket.setDataCallback(new DataCallback() {
            @Override
            public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                ObjectInputStream iStream = null;
                try {
                    iStream = new ObjectInputStream(new ByteArrayInputStream(bb.getAllByteArray()));

                   switch (host.getPort()) {
                       case SharedFinals.UDP_PACKET_TO_VERIFIER_PORT:
                       {
                           final String logFile = "/verifierReceivedPacket.log";
                           final Packet receivedPacket = (Packet) iStream.readObject();
                           iStream.close();
                           //This is initialized at MainActivity
                           //if ( BlockChainService.Verifier  == null)
                           //    BlockChainService.Verifier = new Verifier();
                           if (receivedPacket != null) {
                               if (BlockChainService.Verifier.PacketsNameOwnersIPsDict.containsKey(receivedPacket.Name)) {
                                   ArrayList<String> ownerIPs = BlockChainService.Verifier.PacketsNameOwnersIPsDict.get(receivedPacket.Name);
                                   if (!ownerIPs.contains(receivedPacket.OwnerIP)) {
                                       ownerIPs.add(receivedPacket.OwnerIP);
                                       BlockChainService.Verifier.PacketsNameOwnersIPsDict.put(receivedPacket.Name, ownerIPs);
                                   }
                               } else {
                                   ArrayList<String> ownerIPs = new ArrayList<>();
                                   ownerIPs.add(receivedPacket.OwnerIP);
                                   BlockChainService.Verifier.PacketsNameOwnersIPsDict.put(receivedPacket.Name, ownerIPs);
                               }

                               try {
                                   MessageDigest md = MessageDigest.getInstance("MD5");

                                   AssetManager assetManager = activity.getApplicationContext().getAssets();
                                   final String fileName =  receivedPacket.Name;
                                   String filePath = Utilities.SelectedNode + '/' + fileName;
                                   final InputStream inputStream = assetManager.open(filePath);
                                   final byte[] buffer = new byte[inputStream.available()];

                                   byte[] hashed = md.digest(buffer);
                                   if (BlockChainService.Verifier.PacketsDataHashDict.containsKey(receivedPacket.Name)) {
                                       BlockChainService.Verifier.PacketsDataHashDict.put(receivedPacket.Name, hashed);
                                   } else {
                                       BlockChainService.Verifier.PacketsDataHashDict.put(receivedPacket.Name, hashed);
                                   }

                                   System.out.println("[Receiving Packet to verifier] from port" + SharedFinals.UDP_PACKET_TO_VERIFIER_PORT + " sender IP "  + receivedPacket.OwnerIP + " receiver IP " + Utilities.MyIP  + " packet name" + receivedPacket.Name);
                                   LoggerService.appendLog(LocalDateTime.now() + " ; [Receiving Packet to verifier] from port" + SharedFinals.UDP_PACKET_TO_VERIFIER_PORT + " sender IP "  + receivedPacket.OwnerIP + " receiver IP " + Utilities.MyIP  + " packet name " + receivedPacket.Name + " packet size " , logFile);
                               } catch (NoSuchAlgorithmException e) {
                                   e.printStackTrace();
                               }
                           }
                       }
                       break;
                       case SharedFinals.UDP_PACKET_TO_NODES_PORT:
                       {
                           final String logFile = "/nodeReceivedPacket.log";
                           final Packet receivedPacket = (Packet) iStream.readObject();
                           iStream.close();
                           if (receivedPacket != null) {
                               if (BlockChainService.Verifier != null) {
                                   //Checking node validity
                                   try {
                                       if (BlockChainService.Verifier.PacketsDataHashDict.containsKey(receivedPacket.Name)) {
                                           System.out.println("[Receiving Validated Packet] from port" + SharedFinals.UDP_PACKET_TO_NODES_PORT + " sender IP "  + receivedPacket.OwnerIP + " receiver IP " + Utilities.MyIP  + " packet name" + receivedPacket.Name);
                                           LoggerService.appendLog(LocalDateTime.now() + " ; [Receiving Validated Packet] from port" + SharedFinals.UDP_PACKET_TO_NODES_PORT + " sender IP "  + receivedPacket.OwnerIP + " receiver IP " + Utilities.MyIP  + " packet name " + receivedPacket.Name + " packet size " +  receivedPacket.Data.length, logFile);
                                           MessageDigest md = MessageDigest.getInstance("MD5");
                                           byte[] hashed = md.digest(receivedPacket.Data);
                                           byte[] hashHistory = BlockChainService.Verifier.PacketsDataHashDict.get(receivedPacket.Name);
                                           if (hashHistory != null) {
                                               System.out.println("[Receiving Validated Packet] received hash " + new String(hashed) + " with verifier hashed " + new String(hashHistory));
                                               LoggerService.appendLog(LocalDateTime.now() + " ; [Receiving Validated Packet] received hash " + new String(hashed) + " with verifier hashed " + new String(hashHistory), logFile);
                                           }
                                           if (hashHistory.equals(hashed)) {
                                               //receive packet
                                               BlockChainService.Node.PacketsNameDataDict.put(receivedPacket.Name, receivedPacket.Data);
                                           }
                                       }
                                   } catch (NoSuchAlgorithmException e) {
                                       e.printStackTrace();
                                   }
                               }
                           }
                       }
                       break;
                       case SharedFinals.UDP_IP_BROADCAST_PORT:
                       {
                           final String logFile = "/nodeReceivedIP.log";
                           final IPPacket receivedPacket = (IPPacket) iStream.readObject();
                           //Broadcast received my packet, ignore it
                           if (receivedPacket.OwnerIP.equals(Utilities.MyIP))
                               break;
                           iStream.close();
                           if (receivedPacket != null) {
                               System.out.println("[Receiving IP] from port" + SharedFinals.UDP_IP_BROADCAST_PORT + " sender IP "  + receivedPacket.OwnerIP + " receiver IP " + Utilities.MyIP );
                               LoggerService.appendLog(LocalDateTime.now() + " ; [Receiving IP] from port" + SharedFinals.UDP_IP_BROADCAST_PORT + " sender IP "  + receivedPacket.OwnerIP + " receiver IP " + Utilities.MyIP,  logFile);
                               if (!BlockChainService.IsVerifier) {
                                   if (receivedPacket.Type == 0) {
                                       if (!BlockChainService.Node.ConnectedIPs.contains(receivedPacket.OwnerIP)) {
                                           BlockChainService.Node.ConnectedIPs.add(receivedPacket.OwnerIP);
                                       }
                                   } else {
                                       if (!BlockChainService.Node.ConnectedIPs.contains(receivedPacket.OwnerIP)) {
                                           BlockChainService.Node.ConnectedIPs.add(receivedPacket.OwnerIP);
                                           BlockChainService.Verifier = new Verifier(receivedPacket.OwnerIP, receivedPacket.PacketsNameOwnersIPsDict, receivedPacket.PacketsDataHashDict);
                                       } else {
                                           BlockChainService.Verifier.PacketsNameOwnersIPsDict = receivedPacket.PacketsNameOwnersIPsDict;
                                           BlockChainService.Verifier.PacketsDataHashDict = receivedPacket.PacketsDataHashDict;
                                       }
                                       System.out.println("[Receiving IP] from port" + SharedFinals.UDP_IP_BROADCAST_PORT + " updating Verifier data tables" );
                                       LoggerService.appendLog(LocalDateTime.now() + " ; [Receiving IP] from port" + SharedFinals.UDP_IP_BROADCAST_PORT + " updating Verifier data tables" , logFile);

                                       Set<String> keys1 = BlockChainService.Verifier.PacketsNameOwnersIPsDict.keySet();
                                       System.out.println("[Verifier data]  Verifier PacketsNameOwnersIPsDict:");
                                       LoggerService.appendLog(LocalDateTime.now() + " ; [Verifier data]  Verifier PacketsNameOwnersIPsDict:", logFile);
                                       for(String key: keys1){
                                           System.out.println("[Verifier data]  PacketName " + key);
                                           System.out.println("[Verifier data]  PacketOwnerIP" + BlockChainService.Verifier.PacketsNameOwnersIPsDict.get(key));
                                           LoggerService.appendLog(LocalDateTime.now() + " ; [Verifier data]  PacketName " + key , logFile);
                                           LoggerService.appendLog(LocalDateTime.now() + " ; [Verifier data]  PacketOwnerIP " + BlockChainService.Verifier.PacketsNameOwnersIPsDict.get(key), logFile);
                                       }

                                       Set<String> keys2 = BlockChainService.Verifier.PacketsDataHashDict.keySet();
                                       System.out.println("[Verifier data]  Verifier PacketsDataHashDict:");
                                       LoggerService.appendLog(LocalDateTime.now() + " ; [Verifier data]  Verifier PacketsDataHashDict:", logFile);
                                       for(String key: keys2){
                                           System.out.println("[Verifier data]  PacketName " + key);
                                           LoggerService.appendLog(LocalDateTime.now() + " ; [Verifier data]  PacketName " + key, logFile);
                                           byte[] dataBytes = BlockChainService.Verifier.PacketsDataHashDict.get(key);
                                           if (dataBytes != null) {
                                               System.out.println("[Verifier data]  PacketHashed " + new String(dataBytes));
                                               LoggerService.appendLog(LocalDateTime.now() + " ; [Verifier data]  PacketHashed " + new String(dataBytes), logFile);
                                           }
                                       }
                                   }

                               } else {
                                   if (!BlockChainService.Verifier.ConnectedIPs.contains(receivedPacket.OwnerIP)) {
                                       BlockChainService.Verifier.ConnectedIPs.add(receivedPacket.OwnerIP);
                                   }
                               }
                           }
                       }
                       break;
                       case SharedFinals.UDP_REQUEST_PACKET_FROM_NODE_PORT:
                       {
                           final String logFile = "/receivedRequestFromNode.log";
                           final Packet receivedPacket = (Packet) iStream.readObject();
                           iStream.close();
                           if (receivedPacket != null) {
                               if (BlockChainService.Node.PacketsNameDataDict.contains(receivedPacket.Name)) {
                                   new AsyncTask<Void, Void, Void>() {
                                       @Override
                                       protected Void doInBackground(Void... params) {
                                           Packet packet = new Packet();
                                           packet.OwnerIP = Utilities.MyIP;
                                           packet.Data = BlockChainService.Node.PacketsNameDataDict.get(receivedPacket.Name);
                                           packet.Type = 0;
                                           packet.Name = receivedPacket.Name;
                                           // Serialize to a byte array
                                           try {
                                               ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                                               ObjectOutput oo = null;
                                               oo = new ObjectOutputStream(bStream);
                                               oo.writeObject(packet);
                                               oo.close();

                                               UdpClient.SendPacket(bStream.toByteArray(), receivedPacket.OwnerIP, SharedFinals.UDP_PACKET_TO_NODES_PORT, false);

                                               System.out.println("[Receiving Request Packet] from port" + SharedFinals.UDP_REQUEST_PACKET_FROM_NODE_PORT + " sender ip" + receivedPacket.OwnerIP + " receiver ip " +  Utilities.MyIP + " send packet name " + receivedPacket.Name);
                                               LoggerService.appendLog(LocalDateTime.now() + " ; [Receiving Request Packet] from port" + SharedFinals.UDP_REQUEST_PACKET_FROM_NODE_PORT + " sender ip" + receivedPacket.OwnerIP + " receiver ip " +  Utilities.MyIP + " send packet name " + receivedPacket.Name, logFile);
                                           } catch (IOException e) {
                                               e.printStackTrace();
                                           }
                                           return null;
                                       }
                                   }.execute();
                               }
                           }
                       }
                       break;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        asyncDatagramSocket.setClosedCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                System.out.println("[Server] Successfully closed connection");
                //LoggerService.appendLog(LocalDateTime.now() + " ; [Server] Successfully closed connection");
                //Toast.makeText(activity, "[Server] Successfully closed connection", Toast.LENGTH_SHORT).show();
            }
        });

        asyncDatagramSocket.setEndCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                System.out.println("[Server] Successfully end connection");
                //LoggerService.appendLog(LocalDateTime.now() + " ; [Server] Successfully end connection");

                //Toast.makeText(activity, "[Server] Successfully end connection", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
