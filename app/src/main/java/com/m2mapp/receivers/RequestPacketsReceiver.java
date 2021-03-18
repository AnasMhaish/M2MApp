package com.m2mapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.m2mapp.models.Packet;
import com.m2mapp.udpSockets.UdpClient;
import com.m2mapp.utilities.BlockChainService;
import com.m2mapp.utilities.LoggerService;
import com.m2mapp.utilities.SharedFinals;
import com.m2mapp.utilities.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class RequestPacketsReceiver extends BroadcastReceiver {
    private final String logFilePath = "/requestpacket.log";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BlockChainService.Verifier != null) {
            List<String> verifierPackets = Collections.list(BlockChainService.Verifier.PacketsDataHashDict.keys());
            List<String> myPackets = Collections.list(BlockChainService.Node.PacketsNameDataDict.keys());

            verifierPackets.removeAll(myPackets);
            if (verifierPackets.size() > 0){
                final String requestPacketName = verifierPackets.get(0);
                ArrayList<String> ownersIPs = BlockChainService.Verifier.PacketsNameOwnersIPsDict.get(requestPacketName);
                if (ownersIPs.size() > 0) {
                    final String ownerIP = ownersIPs.get(0);

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            Packet packet = new Packet();
                            packet.OwnerIP = Utilities.MyIP;
                            packet.Name = requestPacketName;
                            // Serialize to a byte array
                            try {
                                /*
                                ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                                ObjectOutput oo = null;
                                oo = new ObjectOutputStream(bStream);
                                oo.writeObject(packet);
                                oo.close();
                                */
                                //UdpClient.SendPacket(bStream.toByteArray(), ownerIP, SharedFinals.UDP_REQUEST_PACKET_FROM_NODE_PORT, true);
                                //if (SharedFinals.UDP_REQUEST_PACKET_FROM_NODE_PORT_CLIENT == null) {
                                Client cc = new Client(80000, 80000);
                                cc.start();
                                Kryo kryo = cc.getKryo();
                                kryo.register(Packet.class);
                                kryo.register(byte[].class);
                                //}
                                cc.connect(5000, BlockChainService.Verifier.NodeIP, SharedFinals.UDP_REQUEST_PACKET_FROM_NODE_PORT, SharedFinals.UDP_REQUEST_PACKET_FROM_NODE_PORT);
                                cc.sendUDP(packet);

                                System.out.println("[Request Packet] from " + Utilities.MyIP + " requesting from " + ownerIP + " packet name" + requestPacketName);

                                LoggerService.appendLog(LocalDateTime.now() + " ; [Request Packet] from " + Utilities.MyIP + " requesting from " + ownerIP + " packet name " + requestPacketName, logFilePath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute();
                }
            }
        }
    }
}