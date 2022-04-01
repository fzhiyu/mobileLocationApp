package com.example.mobilelocationapp.utils;

import static android.content.Context.WIFI_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class CommendFun {

    @SuppressLint("DefaultLocale")
    public static String getLocalIP(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo info = wifiManager.getConnectionInfo();
        int ipAddress = info.getIpAddress();
        return String.format("%d.%d.%d.%d"
                , ipAddress & 0xff
                , ipAddress >> 8 & 0xff
                , ipAddress >> 16 & 0xff
                , ipAddress >> 24 & 0xff);
    }
}
