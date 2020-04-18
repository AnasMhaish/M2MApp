package com.m2mapp.ui.main.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.m2mapp.R;
import com.m2mapp.adapters.StreamedAdapter;
import com.m2mapp.udpSockets.UdpClient;
import com.m2mapp.udpSockets.UdpServer;
import com.m2mapp.utilities.EndPoint;
import com.m2mapp.utilities.ParseMessage;
import com.m2mapp.utilities.SharedFinals;
import com.m2mapp.utilities.Validation;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class StreamedScreen extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private   ArrayList<String> myDataset;
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

        recyclerView = (RecyclerView) rootView.findViewById(R.id.rycStreamed);

        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(layoutManager);

        myDataset = new ArrayList<>();

        Button refreshBtn = (Button) rootView.findViewById(R.id.btnStreamedRefresh);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  SharedPreferences sharedPref = rootView.getContext().getSharedPreferences(SharedFinals.PREFERENCES, Context.MODE_WORLD_READABLE);
                  String streamedString = sharedPref.getString(SharedFinals.STREAMED_PACKETS, "");
                  if (streamedString != "") {
                      myDataset.clear();
                      String[] data = streamedString.split(";");
                      myDataset.addAll(Arrays.asList(data));
                  }
                  mAdapter.notifyDataSetChanged();
              }
        });

        SharedPreferences sharedPref = rootView.getContext().getSharedPreferences(SharedFinals.PREFERENCES, Context.MODE_WORLD_READABLE);
        String streamedString = sharedPref.getString(SharedFinals.STREAMED_PACKETS, "");
        if (streamedString != "") {
            String[] data = streamedString.split(";");
            myDataset.addAll(Arrays.asList(data));
        }

        mAdapter = new StreamedAdapter( rootView.getContext(), myDataset);
        recyclerView.setAdapter(mAdapter);
        return rootView;
    }

}