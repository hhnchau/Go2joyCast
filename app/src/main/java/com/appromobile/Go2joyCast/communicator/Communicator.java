package com.appromobile.Go2joyCast.communicator;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by appro on 16/01/2018.
 */

public class Communicator {

    private static Communicator Instance = null;

    public static Communicator getInstance() {
        if (Instance == null) {
            Instance = new Communicator();
        }
        return Instance;
    }

    public boolean isConnect() {

        try {
            int timeout = 1500;
            Socket socket = new Socket();
            //SocketAddress socketAddress = new InetSocketAddress("8.8.8.8", 53);
            // socket.connect(socketAddress, timeout);
            socket.close();
            Log.d("Check_Connection: ", "OK");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Check_Connection: ", "FAIL");
            return false;
        }
    }

    public boolean isWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    Log.d("Check_Wifi: ", "OK");
                    return true;
                }
            }

        }
        Log.d("Check_Wifi: ", "FAIL");
        return false;
    }

    // AlertDisconnect
    public void Alert(final Context context, String title, String message, String btnYes, String btnNo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(btnYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent SettingWifi = new Intent();
                        SettingWifi.setAction(Settings.ACTION_WIFI_SETTINGS);
                        context.startActivity(SettingWifi);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(btnNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle(title);
        alert.show();
        Log.d("AlertDisconnect: ", "SHOW");
    }
}
