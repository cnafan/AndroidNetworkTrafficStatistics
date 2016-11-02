package com.example.small.flowstatistics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.util.Log;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by small on 2016/9/30.
 */

public class ShutdownReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences.Editor editor = context.getSharedPreferences("data", MODE_PRIVATE).edit();
        SharedPreferences pref = context.getSharedPreferences("data", MODE_PRIVATE);

        //long result;//1 当日使用流量
        long thisbootflow = pref.getLong("thisbootflow", 0);  //4
        long onedaylastbootflow = pref.getLong("onedaylastbootflow", 0);//5
        long onebootlastdayflow = pref.getLong("onebootlastdayflow", 0);//6
        //long curdayflow = pref.getLong("curdayflow", 0);

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        if (activeInfo == null) {
            Log.d("qiang", "网络没有连接");
            return;
        }
        if (activeInfo.isConnected()) {
            if (Objects.equals(activeInfo.getTypeName(), "MOBILE")) {
                long cur_boot_mobiletx = TrafficStats.getMobileTxBytes();
                long cur_boot_mobilerx = TrafficStats.getMobileRxBytes();
                thisbootflow = cur_boot_mobilerx + cur_boot_mobiletx;
            }
        }
        onedaylastbootflow = onedaylastbootflow + thisbootflow - onebootlastdayflow;
        editor.putLong("onedaylastbootflow", onedaylastbootflow);
        editor.putLong("onebootlastdayflow", 0);
        editor.commit();
        Log.d("qiang", "shutdown");
    }
}
