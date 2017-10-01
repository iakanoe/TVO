package com.monitoreomayorista.superapp;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Main extends AppCompatActivity {
    SharedPreferences tinyDB;
    String numAbonado = "0031";
    String claveAbonado = "1234";
    enum Evento {
        MEDICA (100),
        FUEGO (110),
        PANICO (120);

        int code;

        Evento(int code){
            this.code = code;
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tinyDB = this.getPreferences(Context.MODE_PRIVATE);
        (findViewById(R.id.btnAmbulancia)).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { evento(Evento.MEDICA); }});
        (findViewById(R.id.btnBomberos)).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { evento(Evento.FUEGO); }});
        (findViewById(R.id.btnPanico)).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { evento(Evento.PANICO); }});
        (findViewById(R.id.loginBtn)).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { crearLoginDialog(); }});
        refrescar();
    }

    void crearLoginDialog(){
        LayoutInflater li = getLayoutInflater();
        final View v = li.inflate(R.layout.login_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(v)
                .setPositiveButton("Iniciar sesion", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        EditText a = (EditText) v.findViewById(R.id.userInput);
                        EditText b = (EditText) v.findViewById(R.id.passInput);
                        procesarUserPass(a.getText().toString(), b.getText().toString());
                        dialog.dismiss();
                    }})
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {dialog.cancel();}});
        builder.show();
    }

    void procesarUserPass(String user, String pass){
        SharedPreferences.Editor editor = tinyDB.edit();
        editor.putString("num", user);
        editor.putString("key", pass);
        editor.commit();
        editor.apply();
        refrescar();
    }

    void refrescar(){
        ((TextView) findViewById(R.id.statusTxt)).setText("No conectado");
        numAbonado = tinyDB.getString("num", "");
        claveAbonado = tinyDB.getString("key", "");
        if(numAbonado.equals("") || claveAbonado.equals("")) crearLoginDialog();
        else ((TextView) findViewById(R.id.statusTxt)).setText("Conectado con cuenta " + numAbonado);
    }

    void evento(Evento evt) {
        UDPTask udpTask = null;
        try {
            udpTask = new UDPTask("ram.dyndns.ws", 6341);
        } catch (UnknownHostException e) {
            Log.println(Log.ASSERT, "UnknownHostException", e.toString());
        } catch (SocketException e) {
            Log.println(Log.ASSERT, "SocketException", e.toString());
        }

        Date d = Calendar.getInstance().getTime();
        String msg =
            "$B," +
            numAbonado +
            "," +
            (new SimpleDateFormat("ss")).format(d) +
            "," +
            (new SimpleDateFormat("HH:mm")).format(d) +
            ",01," +
            numAbonado +
            "181" +
            String.format("%03d", evt.code) +
            "0000,8,0,0," +
            claveAbonado +
            ",10,4_4.3,$E";
        udpTask.execute(msg);
        Snackbar.make(findViewById(R.id.coord), "Señal enviada", Snackbar.LENGTH_SHORT).show();
    }
}
