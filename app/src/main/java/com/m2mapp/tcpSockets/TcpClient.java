package com.m2mapp.tcpSockets;

import android.app.Activity;
import android.widget.Toast;

import com.koushikdutta.async.*;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.ConnectCallback;
import com.koushikdutta.async.callback.DataCallback;

import java.net.InetSocketAddress;

public class TcpClient {

    private String host;
    private int port;
    private Activity activity;

    public TcpClient(Activity activity, String host, int port) {
        this.host = host;
        this.port = port;
        this.activity = activity;
        setup();
    }

    private void setup() {
        AsyncServer.getDefault().connectSocket(new InetSocketAddress(host, port), new ConnectCallback() {
            @Override
            public void onConnectCompleted(Exception ex, final AsyncSocket socket) {
                try {
                    handleConnectCompleted(ex, socket);
                }catch (Exception ex1) {

                }
            }
        });
    }

    private void handleConnectCompleted(Exception ex, final AsyncSocket socket) {
        if(ex != null) throw new RuntimeException(ex);

        Util.writeAll(socket, "Hello Server".getBytes(), new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                System.out.println("[Client] Successfully wrote message");
                //Toast.makeText(activity, "[Client] Successfully wrote message", Toast.LENGTH_SHORT).show();
            }
        });

        socket.setDataCallback(new DataCallback() {
            @Override
            public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                System.out.println("[Client] Received Message " + new String(bb.getAllByteArray()));
                Toast.makeText(activity, "[Client] Received Message ", Toast.LENGTH_SHORT).show();
            }
        });

        socket.setClosedCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if(ex != null) throw new RuntimeException(ex);
                System.out.println("[Client] Successfully closed connection");
                //Toast.makeText(activity, "[Client] Successfully closed connection", Toast.LENGTH_SHORT).show();
            }
        });

        socket.setEndCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if(ex != null) throw new RuntimeException(ex);
                System.out.println("[Client] Successfully end connection");
                //Toast.makeText(activity, "[Client] Successfully end connection", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
