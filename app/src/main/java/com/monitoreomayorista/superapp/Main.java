package com.monitoreomayorista.superapp;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
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
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

enum Evento {
    MEDICA (100),
    FUEGO (110),
    PANICO (120),
    TVO (886),  // Añadido por //
    TEST (603); //  TVOENTER   //

    int code;

    Evento(int code){
        this.code = code;
    }
};

public class Main extends AppCompatActivity {
    SharedPreferences tinyDB;
    String numAbonado = "0031";
    String claveAbonado = "1234";
    int minutos;
    int segundos;
    Timer timer;

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
        (findViewById(R.id.btnTVO)).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { evento(Evento.TVO); }});
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
        editor.putString("num", user).putString("key", pass).commit();
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

    void timer(){
        if(segundos == 0 && minutos == 0){
            timer.cancel();
            timer.schedule(new TimerTask() {
                @Override public void run() { runOnUiThread(new Runnable() {
                    @Override public void run() {
                        (findViewById(R.id.btnTVO)).setClickable(false);
                        (findViewById(R.id.bwx4)).setVisibility(View.VISIBLE);
                        (findViewById(R.id.tvo1txt)).setVisibility(View.GONE);
                        (findViewById(R.id.tvo2txt)).setVisibility(View.GONE);
                    }
                });}}, 1000);
        } else if(segundos == 0){
            segundos = 59;
            minutos--;
        } else segundos--;
        ((TextView) findViewById(R.id.tvo2txt)).setText(String.valueOf(minutos) + ':' + String.format("%02d", segundos));    }

    void callback(Evento evt, boolean result){
        if(!result){
            Snackbar.make(findViewById(R.id.coord), "La señal no se pudo enviar", Snackbar.LENGTH_SHORT).show();
            return;
        } else if(evt == Evento.TEST){
            Snackbar.make(findViewById(R.id.coord), "La señal de prueba ha sido recibida", Snackbar.LENGTH_SHORT).show();
            return;
        } else if(evt == Evento.TVO){
            (findViewById(R.id.btnTVO)).setClickable(false);
            (findViewById(R.id.bwx4)).setVisibility(View.GONE);
            (findViewById(R.id.tvo1txt)).setVisibility(View.VISIBLE);
            (findViewById(R.id.tvo2txt)).setVisibility(View.VISIBLE);
            minutos = 5;
            segundos = 0;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override public void run() { runOnUiThread(new Runnable() {
                    @Override
                    public void run() {timer();
                    }
                });}}, 1000, 1000);
            ((TextView) findViewById(R.id.tvo2txt)).setText(String.valueOf(minutos) + ':' + String.format("%02d", segundos));
        }
        Snackbar.make(findViewById(R.id.coord), "Señal enviada", Snackbar.LENGTH_SHORT).show();
    }

    void evento(final Evento evt) {
        if(!numAbonado.equals("")){
            UDPTask udpTask = new UDPTask("ram.dyndns.ws", 6341);
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

            udpTask.setOnTaskCompletedListener(new OnTaskCompletedListener() {
                @Override public void onTaskCompleted(boolean result) {callback(evt, result);
                }});
            udpTask.sendUDP(msg);
        } else Snackbar.make(findViewById(R.id.coord), "No está conectado", Snackbar.LENGTH_SHORT).show();
    }
}
