package com.monitoreomayorista.superapp;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

class UDPTask extends AsyncTask<Void, Void, Boolean> {
    private String ip;
    private int port;
    private String msg;
    public interface OnTaskCompletedListener {
        void onTaskCompleted(boolean result);
    }
    private OnTaskCompletedListener onTaskCompletedListener;

    public void setOnTaskCompletedListener(OnTaskCompletedListener onTaskCompletedListener){
        this.onTaskCompletedListener = onTaskCompletedListener;
    }

    public void sendUDP(String packet){
        this.msg = packet;
        execute();
    }

    @Override protected Boolean doInBackground(Void... params) {
        byte[] message = msg.getBytes();
        int msg_length = msg.length();
        try {
            (new DatagramSocket()).send(new DatagramPacket(message, msg_length, InetAddress.getByName(ip), port));
        } catch (IOException e) {
            Log.println(Log.ASSERT, "IOException", e.toString());
            return false;
        }
        return true;
    }

    @Override protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        onTaskCompletedListener.onTaskCompleted(result);
    }

    public UDPTask(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}