package com.m2mapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.m2mapp.models.IPPacket;
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
import java.util.Hashtable;

public class SendMyIPReceiver extends BroadcastReceiver {
    private final String logFilePath = "/sendmyip.log";
    @Override
    public void onReceive(final Context context, Intent intent) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                IPPacket IpPacket = new IPPacket();
                IpPacket.OwnerIP = Utilities.MyIP;
                IpPacket.Type = 0;

                if (BlockChainService.IsVerifier){
                    IpPacket.Type = 1;
                    IpPacket.PacketsDataHashDict = BlockChainService.Verifier.PacketsDataHashDict;
                    IpPacket.PacketsNameOwnersIPsDict = BlockChainService.Verifier.PacketsNameOwnersIPsDict;
                }


                // Serialize to a byte array
                try {

                    ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                    ObjectOutput oo = null;
                    oo = new ObjectOutputStream(bStream);
                    oo.writeObject(IpPacket);
                    oo.close();

                    //UdpClient.SendPacket(bStream.toByteArray(), SharedFinals.BROAD_CAST_IP, SharedFinals.UDP_IP_BROADCAST_PORT, true);

                    //UdpClient.SendPacket(bStream.toByteArray(), SharedFinals.A1_IP, SharedFinals.UDP_IP_BROADCAST_PORT, true);
                    //UdpClient.SendPacket(bStream.toByteArray(), SharedFinals.Redim_IP, SharedFinals.UDP_IP_BROADCAST_PORT, true);

                    if (SharedFinals.UDP_IP_BROADCAST_PORT_CLIENT == null) {

                    }

                    try {
                        Client cc = new Client(80000, 80000);
                        cc.start();
                        Kryo kryo = cc.getKryo();
                        kryo.register(IPPacket.class);
                        kryo.register(byte[].class);
                        kryo.register(Hashtable.class);
                        kryo.register(ArrayList.class);

                        cc.connect(5000, SharedFinals.Redim_IP, SharedFinals.UDP_IP_BROADCAST_PORT, SharedFinals.UDP_IP_BROADCAST_PORT);
                        cc.sendUDP(IpPacket);
                    } catch (Exception e) {

                    }

                    try {
                        Client cc = new Client(80000, 80000);
                        cc.start();
                        Kryo kryo = cc.getKryo();
                        kryo.register(IPPacket.class);
                        kryo.register(byte[].class);
                        kryo.register(Hashtable.class);
                        kryo.register(ArrayList.class);

                        cc.connect(5000, SharedFinals.A1_IP, SharedFinals.UDP_IP_BROADCAST_PORT, SharedFinals.UDP_IP_BROADCAST_PORT);
                        cc.sendUDP(IpPacket);
                    } catch (Exception e) {

                    }

                    try {
                        Client cc = new Client(80000, 80000);
                        cc.start();
                        Kryo kryo = cc.getKryo();
                        kryo.register(IPPacket.class);
                        kryo.register(byte[].class);
                        kryo.register(Hashtable.class);
                        kryo.register(ArrayList.class);

                        cc.connect(5000, SharedFinals.Redim_Bro_IP, SharedFinals.UDP_IP_BROADCAST_PORT, SharedFinals.UDP_IP_BROADCAST_PORT);
                        cc.sendUDP(IpPacket);
                    } catch (Exception e) {

                    }

                    System.out.println(" ; [Send MyIP] from " + Utilities.MyIP);

                    LoggerService.appendLog(LocalDateTime.now() + " ; [Send MyIP] from " + Utilities.MyIP, logFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

    }
}