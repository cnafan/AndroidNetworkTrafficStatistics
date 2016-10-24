package com.example.small.flowstatistics;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by small on 2016/9/30.
 */

class CalculateTodayFlow {
    long calculate(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("data", MODE_PRIVATE).edit();
        SharedPreferences pref = context.getSharedPreferences("data", MODE_PRIVATE);

        long result;//1 当日使用流量
        //boolean isreboot = pref.getBoolean("isreboot", false); //2 重启过
        //boolean iszero = pref.getBoolean("iszero", false);//3 过0点
        long cur_boot_mobiletx = TrafficStats.getMobileTxBytes();
        long cur_boot_mobilerx = TrafficStats.getMobileRxBytes();
        long thisbootflow = cur_boot_mobilerx + cur_boot_mobiletx;//4

        editor.putLong("thisbootflow", thisbootflow);
        //Log.d("qiang", "thisbootflow:" + thisbootflow);
        long onedaylastbootflow = pref.getLong("onedaylastbootflow", 0);//5
        long onebootlastdayflow = pref.getLong("onebootlastdayflow", 0);//6

        result = thisbootflow + onedaylastbootflow - onebootlastdayflow;

        editor.putLong("curdayflow", result);
        Log.d("qiang", "curdayflow:" + result);
        editor.commit();
        return result;
    }
}
