package com.m2mapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.m2mapp.models.Node;
import com.m2mapp.models.Verifier;
import com.m2mapp.receivers.RequestPacketsReceiver;
import com.m2mapp.receivers.SendMyIPReceiver;
import com.m2mapp.udpSockets.UdpServer;
import com.m2mapp.ui.main.SectionsPagerAdapter;
import com.m2mapp.ui.main.activities.UDPTCPActivity;
import com.m2mapp.ui.main.activities.WiFiDirect;
import com.m2mapp.receivers.StreamReceiver;
import com.m2mapp.utilities.BlockChainService;
import com.m2mapp.utilities.SharedFinals;
import com.m2mapp.utilities.Utilities;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private PendingIntent pendingIntent;
    private PendingIntent pendingIntent2;
    private PendingIntent pendingIntent3;
    private AlarmManager manager;

    private UdpServer udpServerPacket;
    private UdpServer udpServerPacketVerifier;
    private UdpServer udpServerRequestPacket;
    private UdpServer udpServerBroadCastIP;

    private FirebaseAuth mAuth;
    private boolean skip = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            Intent streamIntent = new Intent(this, StreamReceiver.class);
            Intent sendMyIpIntent = new Intent(this, SendMyIPReceiver.class);
            Intent requestPacketsIntent = new Intent(this, RequestPacketsReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(this, 0, streamIntent, 0);
            pendingIntent2 = PendingIntent.getBroadcast(this, 1, sendMyIpIntent, 0);
            pendingIntent3 = PendingIntent.getBroadcast(this, 2, requestPacketsIntent, 0);

            manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            int interval = 5000;

            final Calendar time = Calendar.getInstance();
            time.set(Calendar.MINUTE, 0);
            time.set(Calendar.SECOND, 0);
            time.set(Calendar.MILLISECOND, 0);

            if(!BlockChainService.IsVerifier) {
                manager.setRepeating(AlarmManager.RTC, time.getTime().getTime(), interval, pendingIntent);
                manager.setRepeating(AlarmManager.RTC, time.getTime().getTime(), interval, pendingIntent3);
            }
            manager.setRepeating(AlarmManager.RTC, time.getTime().getTime(), interval, pendingIntent2);
            manager.setRepeating(AlarmManager.RTC, time.getTime().getTime(), interval, pendingIntent);

            final String myIp = Utilities.GetMyIP(this.getApplicationContext());
            final Activity activity = this;

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    udpServerPacketVerifier = new UdpServer(activity, myIp, SharedFinals.UDP_PACKET_TO_VERIFIER_PORT);
                    return null;
                }
            }.execute();
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