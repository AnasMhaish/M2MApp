package com.m2mapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

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

                    UdpClient.SendPacket(bStream.toByteArray(), SharedFinals.BROAD_CAST_IP, SharedFinals.UDP_IP_BROADCAST_PORT, true);

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