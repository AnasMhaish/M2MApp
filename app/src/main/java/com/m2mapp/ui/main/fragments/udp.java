package com.m2mapp.ui.main.fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.m2mapp.R;
import com.m2mapp.udpSockets.UdpClient;
import com.m2mapp.udpSockets.UdpServer;
import com.m2mapp.utilities.EndPoint;
import com.m2mapp.utilities.ParseMessage;
import com.m2mapp.utilities.Utilities;
import com.m2mapp.utilities.Validation;

import java.util.Arrays;

import androidx.fragment.app.Fragment;

public class udp extends Fragment {

    UdpServer udpServer;
    UdpClient udpClient;

    boolean keepRecieving = false;
    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_udp, container, false);

        /*
        ImageButton udpRecieve = (ImageButton) rootView.findViewById(R.id.imageButton_udp_recieve);
        udpRecieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keepRecieving = !keepRecieving;

                String portStr = ((EditText) rootView.findViewById(R.id.editText_udp_port)).getText().toString().trim();
                int port = Integer.parseInt(portStr);
                EndPoint endPoint = new EndPoint("", port);

                while(keepRecieving) {
                    receiveUDPTask = new ReceiveUDPTask();
                    receiveUDPTask.execute(endPoint);
                }
            }
        });
        */

        EditText portTxt = ((EditText) rootView.findViewById(R.id.editText_udp_port));
        portTxt.setText("7001");

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                udpServer = new UdpServer(getActivity(), Utilities.MyIP, 7001);
                return null;
            }
        }.execute();

        ImageButton udpSend = (ImageButton) rootView.findViewById(R.id.imageButton_udp_send);
        udpSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ipAddress = ((EditText) rootView.findViewById(R.id.editText_udp_ip_address)).getText().toString().trim();
                String ipValidation = Validation.validateIP(getActivity(), ipAddress);
                if (!(ipValidation == null)) {
                    Toast.makeText(getActivity(), ipValidation, Toast.LENGTH_SHORT).show();
                    return;
                }
                String portStr = ((EditText) rootView.findViewById(R.id.editText_udp_port)).getText().toString().trim();
                String portValidation = Validation.validatePort(getActivity(), portStr);
                if (!(portValidation == null)) {
                    Toast.makeText(getActivity(), portValidation, Toast.LENGTH_SHORT).show();
                    return;
                }
                int port = Integer.parseInt(portStr);
                String message = ((EditText) rootView.findViewById(R.id.editText_udp_message)).getText().toString();
                RadioButton udpBytes = (RadioButton) rootView.findViewById(R.id.radioButton_udp_bytes);
                if (udpBytes.isChecked()) {
                    //CheckBox hexFormat = (CheckBox) rootView.findViewById(R.id.checkBox_udp_hex);
                    byte[] bytes;
                    //if (!hexFormat.isChecked()) {
                        bytes = ParseMessage.parseBytes(message);
                    //} else {
                    //    bytes = ParseMessage.parseBytesHex(message);
                    //}
                    if (!(bytes == null)) {
                        //CheckBox expectReply = (CheckBox)rootView.findViewById(R.id.checkBox_udp_expect_reply);
                        EndPoint endPoint = new EndPoint(ipAddress, port);
                        /*
                        if(!expectReply.isChecked()){
                            SendUDPTask sendUDPTask = new SendUDPTask(bytes);
                            sendUDPTask.setContext(getActivity());
                            sendUDPTask.execute(endPoint);
                        }else{
                            Handler replyHandler = new Handler(new Handler.Callback() {
                                @Override
                                public boolean handleMessage(Message message) {
                                    byte[] reply = (byte[]) message.obj;
                                    String replyStr = Arrays.toString(reply);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage(replyStr)
                                            .setTitle(R.string.recd_reply);
                                    builder.show();
                                    return true;
                                }
                            });
                            SendUDPWaitTask sendUDPWaitTask = new SendUDPWaitTask(bytes, replyHandler);
                            sendUDPWaitTask.setContext(getActivity());
                            sendUDPWaitTask.execute(endPoint);
                        }
                         */
                        new UdpClient(ipAddress, port).send(message);
                    } else {
                        Toast.makeText(getActivity(), R.string.message_invalid, Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    if (message.length() > 0) {
                        //CheckBox expectReply = (CheckBox)rootView.findViewById(R.id.checkBox_udp_expect_reply);
                        EndPoint endPoint = new EndPoint(ipAddress, port);
                        /*
                        if(!expectReply.isChecked()){
                            //SendUDPTask sendUDPTask = new SendUDPTask(message);
                            //sendUDPTask.setContext(getActivity());
                            //sendUDPTask.execute(endPoint);
                        }else{
                            Handler replyHandler = new Handler(new Handler.Callback() {
                                @Override
                                public boolean handleMessage(Message message) {
                                    byte[] reply = (byte[]) message.obj;
                                    String replyStr = Arrays.toString(reply);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage(replyStr)
                                            .setTitle(R.string.recd_reply);
                                    builder.show();
                                    return true;
                                }
                            });
                            //SendUDPWaitTask sendUDPWaitTask = new SendUDPWaitTask(message, replyHandler);
                            //sendUDPWaitTask.setContext(getActivity());
                            //sendUDPWaitTask.execute(endPoint);
                        }
                         */
                        new UdpClient(ipAddress, port).send(message);
                    } else {
                        Toast.makeText(getActivity(), R.string.message_invalid, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

        return rootView;
    }

}
