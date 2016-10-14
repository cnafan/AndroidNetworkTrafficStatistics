package com.example.small.flowstatistics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by small on 2016/9/30.
 */

public class ShutdownReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences.Editor editor = context.getSharedPreferences("data", MODE_PRIVATE).edit();
        SharedPreferences pref = context.getSharedPreferences("data", MODE_PRIVATE);

        long result;//1 当日使用流量
        boolean isreboot = pref.getBoolean("isreboot", false); //2 重启过
        boolean iszero = pref.getBoolean("iszero", false);//3 过0点
        long cur_boot_mobiletx = TrafficStats.getMobileTxBytes();
        long cur_boot_mobilerx = TrafficStats.getMobileRxBytes();
        long thisbootflow = cur_boot_mobilerx + cur_boot_mobiletx;//4
        Log.d("qiang", "thisbootflow:" + thisbootflow);
        long onedaylastbootflow = pref.getLong("onedaylastbootflow", 0);//5
        long onebootlastdayflow = pref.getLong("onebootlastdayflow", 0);//6

        long curdayflow = pref.getLong("curdayflow", 0);

        editor.putBoolean("isreboot", true);
        if (iszero) {
            onedaylastbootflow = thisbootflow - onebootlastdayflow;
        } else {
            onedaylastbootflow = onedaylastbootflow+ thisbootflow;
        }
        editor.putLong("onedaylastbootflow", onedaylastbootflow);
        editor.putBoolean("iszero", false);

        editor.commit();
        Log.d("qiang", "shutdown");
    }
}
