package com.monitoreomayorista.superapp;

import android.net.Uri;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

class NumGetter extends AsyncTask<Void, Void, String>{
    OnNumGot onNumGot;
    public interface OnNumGot{ void gotNumber(String num);}
    NumGetter(OnNumGot onNumGot){ this.onNumGot = onNumGot;}

    @Override protected String doInBackground(Void... params) {
        try {
            URL url = new URL("http://ayaxseg.000webhostapp.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.connect();
            BufferedWriter osw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            osw.write((new Uri.Builder()).appendQueryParameter("ask",null).build().getEncodedQuery());
            osw.flush();
            connection.getOutputStream().flush();
            osw.close();
            connection.getOutputStream().close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override protected void onPostExecute(String s) {
        super.onPostExecute(s);
        onNumGot.gotNumber(s);
    }
}
