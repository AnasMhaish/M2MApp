package com.m2mapp.tasks_utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.m2mapp.R;
import com.m2mapp.socket_utilities.UDPReceiver;
import com.m2mapp.socket_utilities.UDPSender;
import com.m2mapp.utilities.EndPoint;

public class ReceiveUDPTask extends AsyncTask<EndPoint, byte[], byte[]> {
    private byte[] bytes;
    private String str;
    private Context context = null;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    protected byte[] doInBackground(EndPoint... endPoints) {
        for (EndPoint endPoint : endPoints) {
            byte[] data = UDPReceiver.receive(endPoint);
            if (data != null)
                return data;
        }
        return null;
    }

    @Override
    protected void onPostExecute(byte[] data) {
        super.onPostExecute(data);
        if (context != null) {
            Toast.makeText(context, R.string.received + data.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
