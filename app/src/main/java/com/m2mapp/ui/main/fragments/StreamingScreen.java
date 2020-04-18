package com.m2mapp.ui.main.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.m2mapp.R;

import org.json.JSONObject;

import java.io.File;

import androidx.fragment.app.Fragment;

public class StreamingScreen extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        System.out.println(Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator +"D2D");
        View view = inflater.inflate(R.layout.fragment_streaming_screen, container, false);
        //end API call
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
