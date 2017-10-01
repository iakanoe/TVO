package com.monitoreomayorista.superapp;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

class UDPTask extends AsyncTask<String, Void, Void> {
    private InetAddress addr;
    private String ip;
    private int port;
    private DatagramSocket s;

    @Override protected Void doInBackground(String... params) {
        String msg = params[0];
        byte[] message = msg.getBytes();
        int msg_length = msg.length();
        try {
            addr = InetAddress.getByName(ip);
            s.send(new DatagramPacket(message, msg_length, addr, port));
        } catch (IOException e) {
            Log.println(Log.ASSERT, "IOException", e.toString());
        }
        return null;
    }

    UDPTask(String ip, int port) throws UnknownHostException, SocketException {
        this.ip = ip;
        this.port = port;
        s = new DatagramSocket();
    }
}