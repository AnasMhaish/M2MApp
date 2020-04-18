package com.m2mapp.tasks_utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.m2mapp.R;
import com.m2mapp.socket_utilities.UDPSender;
import com.m2mapp.utilities.EndPoint;

public class SendUDPTask extends AsyncTask<EndPoint, Void, Void> {
    final private boolean sendingBytes;
    final private byte[] bytes;
    final private String str;
    private Context context = null;

    public SendUDPTask(byte[] bytes) {
        sendingBytes = true;
        this.bytes = bytes;
        this.str = null;
    }

    public SendUDPTask(String str) {
        sendingBytes = false;
        this.bytes = null;
        this.str = str;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(EndPoint... endPoints) {
        for (EndPoint endPoint : endPoints) {
            if (sendingBytes) {
                UDPSender.send(endPoint, bytes);
            } else {
                UDPSender.send(endPoint, str);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (context != null) {
            Toast.makeText(context, R.string.sent, Toast.LENGTH_SHORT).show();
        }
    }
}
