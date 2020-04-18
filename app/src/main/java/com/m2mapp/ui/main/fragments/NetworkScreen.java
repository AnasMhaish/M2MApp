package com.m2mapp.ui.main.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.m2mapp.R;
import com.m2mapp.models.DataHolder;
import com.m2mapp.utilities.SendPostRequest;

import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class NetworkScreen extends Fragment {

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batteryTxt.setText("Battery Level : " + level + "%");
        }
    };


    private TextView SSIDTxt;

    //rssi
    private TextView textConnected;
    private TextView textIp;
    private TextView textSsid;
    private TextView textBssid;
    private TextView textMac;
    private TextView textSpeed;
    private TextView textRssi;

    //battery broadcast receiver
    private TextView batteryTxt;
    //rssi receivers
    private BroadcastReceiver myRssiChangeReceiver
            = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            WifiManager wifiMan = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiMan.startScan();
            int newRssi = wifiMan.getConnectionInfo().getRssi();
            textRssi.setText("RSSI Level : " + newRssi);
        }
    };
    private BroadcastReceiver myWifiReceiver
            = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            NetworkInfo networkInfo = arg1.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                DisplayWifiState();

            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        //Note: Not using RSSI_CHANGED_ACTION because it never calls me back.
        IntentFilter rssiFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getActivity().registerReceiver(myRssiChangeReceiver, rssiFilter);

        WifiManager wifiMan = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiMan.startScan();
    }


    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(myRssiChangeReceiver);
        //getActivity().unregisterReceiver(myWifiReceiver);
        //getActivity().unregisterReceiver(mBatInfoReceiver);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_network_screen, container, false);
/*
        //battery level indication
        batteryTxt = rootView.findViewById(R.id.batteryLevel);
        getActivity().registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        //rssi indication
        textConnected = rootView.findViewById(R.id.connected);
        textIp = rootView.findViewById(R.id.ip);

        textSsid = rootView.findViewById(R.id.ssid);
        textBssid = rootView.findViewById(R.id.bssid);
        textMac = rootView.findViewById(R.id.mac);
        textSpeed = rootView.findViewById(R.id.speed);
        textRssi = rootView.findViewById(R.id.rssi);

        DisplayWifiState();

        getActivity().registerReceiver(this.myWifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        getActivity().registerReceiver(this.myRssiChangeReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
*/
        return rootView;
    }

    private void DisplayWifiState(){

        ConnectivityManager myConnManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo myNetworkInfo = myConnManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiManager myWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        myWifiManager.startScan();
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();

        textMac.setText("MAC:"+myWifiInfo.getMacAddress());

        if (myNetworkInfo.isConnected()){
            int myIp = myWifiInfo.getIpAddress();

            textConnected.setText("Connecton status : Connected");

            int intMyIp3 = myIp/0x1000000;
            int intMyIp3mod = myIp%0x1000000;

            int intMyIp2 = intMyIp3mod/0x10000;
            int intMyIp2mod = intMyIp3mod%0x10000;

            int intMyIp1 = intMyIp2mod/0x100;
            int intMyIp0 = intMyIp2mod%0x100;

            textIp.setText("IP:" + intMyIp0
                    + "." + intMyIp1
                    + "." + intMyIp2
                    + "." + intMyIp3
            );

            textSsid.setText("SSID :"+myWifiInfo.getSSID());
            textBssid.setText("BSSID :"+myWifiInfo.getBSSID());

            textSpeed.setText("Link speed : " + myWifiInfo.getLinkSpeed() + " " + WifiInfo.LINK_SPEED_UNITS);
            textRssi.setText("RSSI Level:" + myWifiInfo.getRssi());
        }
        else{
            textConnected.setText("Connecton status : Disconnected");
            textIp.setText("---");
            textSsid.setText("---");
            textBssid.setText("---");
            textSpeed.setText("---");
            textRssi.setText("---");
        }
    }
}
