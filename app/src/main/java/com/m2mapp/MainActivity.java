package com.m2mapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.m2mapp.models.IPPacket;
import com.m2mapp.models.Node;
import com.m2mapp.models.Packet;
import com.m2mapp.models.Verifier;
import com.m2mapp.receivers.RequestPacketsReceiver;
import com.m2mapp.receivers.SendMyIPReceiver;
import com.m2mapp.udpSockets.UdpServer;
import com.m2mapp.ui.main.SectionsPagerAdapter;
import com.m2mapp.ui.main.activities.UDPTCPActivity;
import com.m2mapp.ui.main.activities.WiFiDirect;
import com.m2mapp.receivers.StreamReceiver;
import com.m2mapp.utilities.BlockChainService;
import com.m2mapp.utilities.LoggerService;
import com.m2mapp.utilities.SharedFinals;
import com.m2mapp.utilities.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private PendingIntent pendingIntent;
    private PendingIntent pendingIntent2;
    private PendingIntent pendingIntent3;
    private AlarmManager manager;

    private UdpServer udpServerPacket;
    private UdpServer udpServerPacketVerifier;
    private UdpServer udpServerRequestPacket;
    private UdpServer udpServerBroadCastIP;
    private Server server;

    private FirebaseAuth mAuth;
    private boolean skip = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utilities.ApplicationContext = getApplicationContext();
        Intent intent = getIntent();
        skip = intent.getBooleanExtra("SKIP",false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null && !skip) {
            Intent myIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(myIntent);
        } else {
            Utilities.GetMyIP(getApplicationContext());
            if (currentUser != null) {
                BlockChainService.Verifier = new Verifier(Utilities.MyIP);
                BlockChainService.IsVerifier = true;
            } else {
                BlockChainService.Node = new Node(Utilities.MyIP);
            }

            WifiManager wifi = (WifiManager)getSystemService( Context.WIFI_SERVICE );
            if(wifi != null){
                WifiManager.MulticastLock lock = wifi.createMulticastLock("Log_Tag");
                lock.acquire();
            }

            setContentView(R.layout.activity_main);
            SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
            ViewPager viewPager = findViewById(R.id.view_pager);
            viewPager.setAdapter(sectionsPagerAdapter);
            TabLayout tabs = findViewById(R.id.tabs);
            tabs.setupWithViewPager(viewPager);

            FloatingActionButton fab = findViewById(R.id.fab);
            FloatingActionButton fab2 = findViewById(R.id.fab2);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, WiFiDirect.class));
                }
            });

            fab2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, UDPTCPActivity.class));
                }
            });

            // Retrieve a PendingIntent that will perform a broadcast
            //Intent streamIntent = new Intent(this, StreamReceiver.class);
            //pendingIntent = PendingIntent.getBroadcast(this, 0, streamIntent, 0);

            manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            int interval = 100;

            final Calendar time = Calendar.getInstance();
            time.set(Calendar.MINUTE, 0);
            time.set(Calendar.SECOND, 5);
            time.set(Calendar.MILLISECOND, 0);

            //Keeping Sending my IP in a loop
            Intent sendMyIpIntent = new Intent(this, SendMyIPReceiver.class);
            pendingIntent2 = PendingIntent.getBroadcast(this, 1, sendMyIpIntent, 0);
            manager.setRepeating(AlarmManager.RTC, time.getTime().getTime(), interval, pendingIntent2);

            //Recieving send IP from others
            /*
            final Activity activity = this;
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    udpServerBroadCastIP = new UdpServer(activity, SharedFinals.BROAD_CAST_IP, SharedFinals.UDP_IP_BROADCAST_PORT);
                    return null;
                }
            }.execute();
*/


            SharedFinals.UDP_IP_BROADCAST_PORT_SERVER = new Server(80000, 80000);
            SharedFinals.UDP_IP_BROADCAST_PORT_SERVER.start();
            try {
                Kryo kryo = SharedFinals.UDP_IP_BROADCAST_PORT_SERVER.getKryo();
                kryo.register(IPPacket.class);
                kryo.register(byte[].class);
                kryo.register(Hashtable.class);
                kryo.register(ArrayList.class);
                SharedFinals.UDP_IP_BROADCAST_PORT_SERVER.bind(SharedFinals.UDP_IP_BROADCAST_PORT,  SharedFinals.UDP_IP_BROADCAST_PORT);
                SharedFinals.UDP_IP_BROADCAST_PORT_SERVER.addListener(new Listener() {
                    public void received (Connection connection, Object object) {
                        if (object instanceof IPPacket) {
                            IPPacket request = (IPPacket)object;
                            BlockChainService.ReceiveIPPacketToNode(request);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }


            final AssetManager assetManager = this.getAssets();
            final String NodeFolder = Utilities.SelectedNode;
            final String PacketSizeFolder = Utilities.SelectedPacketSize;

            final String logFilePath = "/stream.log";
            if(!BlockChainService.IsVerifier) {
                //manager.setRepeating(AlarmManager.RTC, time.getTime().getTime(), interval, pendingIntent);
                Intent requestPacketsIntent = new Intent(this, RequestPacketsReceiver.class);
                pendingIntent3 = PendingIntent.getBroadcast(this, 2, requestPacketsIntent, 0);
                manager.setRepeating(AlarmManager.RTC, time.getTime().getTime(), interval, pendingIntent3);

                Timer timer = new Timer();
                //Set the schedule function
                timer.scheduleAtFixedRate(new TimerTask() {
                      @Override
                      public void run() {
                          try {
                              if (BlockChainService.Verifier != null) {
                                    String[] filelist = assetManager.list(NodeFolder + '/' + PacketSizeFolder);
                                    if (Utilities.readIndex < filelist.length) {
                                        final String fileName =  filelist[Utilities.readIndex];
                                        String filePath = NodeFolder + '/' + PacketSizeFolder + '/' + filelist[Utilities.readIndex];


                                        final InputStream inputStream = assetManager.open(filePath);
                                        final byte[] buffer = new byte[inputStream.available()];
                                        inputStream.read(buffer);


                                        Packet packet = new Packet();
                                        packet.OwnerIP = Utilities.MyIP;
                                        packet.Name = fileName;
                                        packet.Data = buffer;

                                        packet.Type = 1;
                                        // Serialize to a byte array
                                        try {
                                            //if (SharedFinals.UDP_PACKET_TO_VERIFIER_PORT_CLIENT == null) {
                                            Client cc = new Client(80000, 80000);
                                            cc.start();
                                            Kryo kryo = cc.getKryo();
                                            kryo.register(Packet.class);
                                            kryo.register(byte[].class);
                                            //}
                                            cc.connect(2000, BlockChainService.Verifier.NodeIP, SharedFinals.UDP_PACKET_TO_VERIFIER_PORT, SharedFinals.UDP_PACKET_TO_VERIFIER_PORT);
                                            cc.sendUDP(packet);

                                            System.out.println("[Stream] from " + Utilities.MyIP + " sending to " + BlockChainService.Verifier.NodeIP + " packet name" + fileName);
                                            LoggerService.appendLog(LocalDateTime.now() + " ; [Stream] from " + Utilities.MyIP + " sending to " + BlockChainService.Verifier.NodeIP + " packet name" + fileName + " packet size " +  " ;NodeFolder " + filePath, logFilePath);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    Utilities.readIndex++;
                                }
                          } catch (IOException e) {
                              e.printStackTrace();
                          }
                      }
                  },
                0, interval);

                SharedFinals.UDP_REQUEST_PACKET_FROM_NODE_PORT_SERVER = new Server(80000, 80000);
                SharedFinals.UDP_REQUEST_PACKET_FROM_NODE_PORT_SERVER.start();
                try {
                    Kryo kryo = SharedFinals.UDP_REQUEST_PACKET_FROM_NODE_PORT_SERVER.getKryo();
                    kryo.register(Packet.class);
                    kryo.register(byte[].class);
                    SharedFinals.UDP_REQUEST_PACKET_FROM_NODE_PORT_SERVER.bind(SharedFinals.UDP_REQUEST_PACKET_FROM_NODE_PORT,  SharedFinals.UDP_REQUEST_PACKET_FROM_NODE_PORT);
                    SharedFinals.UDP_REQUEST_PACKET_FROM_NODE_PORT_SERVER.addListener(new Listener() {
                        public void received (Connection connection, Object object) {
                            if (object instanceof Packet) {
                                Packet request = (Packet)object;
                                BlockChainService.ReceiveRequestPacketFromNode(request);
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                SharedFinals.UDP_PACKET_TO_NODES_PORT_SERVER = new Server(80000, 80000);
                SharedFinals.UDP_PACKET_TO_NODES_PORT_SERVER.start();
                try {
                    Kryo kryo = SharedFinals.UDP_PACKET_TO_NODES_PORT_SERVER.getKryo();
                    kryo.register(Packet.class);
                    kryo.register(byte[].class);
                    SharedFinals.UDP_PACKET_TO_NODES_PORT_SERVER.bind(SharedFinals.UDP_PACKET_TO_NODES_PORT,  SharedFinals.UDP_PACKET_TO_NODES_PORT);
                    SharedFinals.UDP_PACKET_TO_NODES_PORT_SERVER.addListener(new Listener() {
                        public void received (Connection connection, Object object) {
                            if (object instanceof Packet) {
                                Packet request = (Packet)object;
                                BlockChainService.ReceivePacketToNode(request);
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (BlockChainService.IsVerifier) {
                SharedFinals.UDP_PACKET_TO_VERIFIER_PORT_SERVER = new Server(80000, 80000);
                SharedFinals.UDP_PACKET_TO_VERIFIER_PORT_SERVER.start();
                try {
                    Kryo kryo = SharedFinals.UDP_PACKET_TO_VERIFIER_PORT_SERVER.getKryo();
                    kryo.register(Packet.class);
                    kryo.register(byte[].class);
                    SharedFinals.UDP_PACKET_TO_VERIFIER_PORT_SERVER.bind(SharedFinals.UDP_PACKET_TO_VERIFIER_PORT,  SharedFinals.UDP_PACKET_TO_VERIFIER_PORT);
                    SharedFinals.UDP_PACKET_TO_VERIFIER_PORT_SERVER.addListener(new Listener() {
                        public void received (Connection connection, Object object) {
                            if (object instanceof Packet) {
                                Packet request = (Packet)object;
                                BlockChainService.ReceivePacketToVerifier(request, getApplicationContext());
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

/*
            if (BlockChainService.IsVerifier) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        udpServerPacketVerifier = new UdpServer(activity, myIp, SharedFinals.UDP_PACKET_TO_VERIFIER_PORT);
                        return null;
                    }
                }.execute();
            }else {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        udpServerRequestPacket = new UdpServer(activity, myIp, SharedFinals.UDP_REQUEST_PACKET_FROM_NODE_PORT);
                        return null;
                    }
                }.execute();

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        udpServerPacket = new UdpServer(activity, myIp, SharedFinals.UDP_PACKET_TO_NODES_PORT);
                        return null;
                    }
                }.execute();
            }

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    udpServerBroadCastIP = new UdpServer(activity, SharedFinals.BROAD_CAST_IP, SharedFinals.UDP_IP_BROADCAST_PORT);
                    return null;
                }
            }.execute();

            */
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {
//            Toast.makeText(MainActivity.this, "Action : Search clicked", Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, WiFiDirect.class);
            this.startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}