package com.m2mapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.widget.Toast;

import com.m2mapp.models.Packet;
import com.m2mapp.models.Packettt;
import com.m2mapp.udpSockets.UdpClient;
import com.m2mapp.utilities.BlockChainService;
import com.m2mapp.utilities.LoggerService;
import com.m2mapp.utilities.SharedFinals;
import com.m2mapp.utilities.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;

public class StreamReceiver extends BroadcastReceiver {
    private final String NodeFolder = "Node2";
    private final String logFilePath = "/stream.log";
    @Override
    public void onReceive(final Context context, Intent intent) {
        AssetManager assetManager = context.getAssets();
        try {
            String[] filelist = assetManager.list(NodeFolder);
            if (Utilities.readIndex < filelist.length) {
                final String fileName =  filelist[Utilities.readIndex];
                String filePath = NodeFolder + '/' + filelist[Utilities.readIndex];


                final InputStream inputStream = assetManager.open(filePath);
                final byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);

                //BlockChainService.Node.PacketsNameDataDict.put(fileName, buffer);
                /*
                File targetFile = new File(fileName);
                targetFile.createNewFile();
                OutputStream outStream = new FileOutputStream(targetFile);
                outStream.write(buffer);
                */
                /*
                SharedPreferences sharedPref = context.getSharedPreferences(SharedFinals.PREFERENCES, Context.MODE_PRIVATE);
                if (sharedPref.contains(SharedFinals.STREAMED_PACKETS)) {
                    String streamedString = sharedPref.getString(SharedFinals.STREAMED_PACKETS, null);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(SharedFinals.STREAMED_PACKETS, streamedString + ';' + fileName);
                    editor.commit();
                }else{
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(SharedFinals.STREAMED_PACKETS, fileName);
                    editor.commit();
                }
                 */

                if (BlockChainService.Verifier != null) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            Packet packet = new Packet();
                            packet.OwnerIP = Utilities.MyIP;
                            packet.Name = fileName;
                            packet.Data = buffer;
                            packet.Type = 1;
                            // Serialize to a byte array
                            try {
                                ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                                ObjectOutput oo = null;
                                oo = new ObjectOutputStream(bStream);
                                oo.writeObject(packet);
                                oo.close();

                                //new UdpClient(BlockChainService.Verifier.NodeIP, SharedFinals.UDP_PACKET_TO_VERIFIER_PORT).send(bStream.toByteArray());
                                UdpClient.SendPacket(bStream.toByteArray(), BlockChainService.Verifier.NodeIP, SharedFinals.UDP_PACKET_TO_VERIFIER_PORT, true);

                                System.out.println("[Stream] from " + Utilities.MyIP + " sending to " + BlockChainService.Verifier.NodeIP + " packet name" + fileName);
                                LoggerService.appendLog(LocalDateTime.now() + " ; [Stream] from " + Utilities.MyIP + " sending to " + BlockChainService.Verifier.NodeIP + " packet name" + fileName + " packet size " +  " ;NodeFolder " + NodeFolder, logFilePath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute();
                }
                Utilities.readIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
