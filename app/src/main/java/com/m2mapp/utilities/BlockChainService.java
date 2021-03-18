package com.m2mapp.utilities;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;

import com.m2mapp.models.IPPacket;
import com.m2mapp.models.Node;
import com.m2mapp.models.Packet;
import com.m2mapp.models.Verifier;
import com.m2mapp.udpSockets.UdpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

public class BlockChainService {
    public static Node Node;
    public static Verifier Verifier;

    public static boolean IsVerifier = false;

    public static void ReceivePacketToVerifier(Packet receivedPacket, Context context) {
        final String logFile = "/verifierReceivedPacket.log";
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
                AssetManager assetManager =  context.getAssets();
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
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void ReceivePacketToNode(Packet receivedPacket) {
        final String logFile = "/nodeReceivedPacket.log";
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

    public static void ReceiveIPPacketToNode(IPPacket receivedPacket) {
        final String logFile = "/nodeReceivedIP.log";
        //Broadcast received my packet, ignore it
        if (receivedPacket.OwnerIP.equals(Utilities.MyIP))
            return;
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

    public static void ReceiveRequestPacketFromNode(Packet receivedPacket) {
        final String logFile = "/receivedRequestFromNode.log";

        if (receivedPacket != null) {
            if (BlockChainService.Node.PacketsNameDataDict.contains(receivedPacket.Name)) {
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
            }
        }
    }
}
