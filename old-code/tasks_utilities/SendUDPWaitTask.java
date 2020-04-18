package com.m2mapp.tasks_utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.m2mapp.socket_utilities.UDPSender;
import com.m2mapp.utilities.EndPoint;

public class SendUDPWaitTask extends AsyncTask<EndPoint, Void, Void> {
    final private boolean sendingBytes;
    final private byte[] bytes;
    final private String str;
    private Context context = null;
    private Handler handler;

    public SendUDPWaitTask(byte[] bytes, Handler handler) {
        sendingBytes = true;
        this.bytes = bytes;
        this.str = null;
        this.handler = handler;
    }

    public SendUDPWaitTask(String str, Handler handler) {
        sendingBytes = false;
        this.bytes = null;
        this.str = str;
        this.handler = handler;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(EndPoint... endPoints) {
        for (EndPoint endPoint : endPoints) {
            byte[] result;
            if (sendingBytes) {
                result = UDPSender.sendWait(endPoint, bytes);
            } else {
                result = UDPSender.sendWait(endPoint, str);
            }
            if (handler != null) {
                Message message = Message.obtain();
                message.obj = result;
                try {
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
