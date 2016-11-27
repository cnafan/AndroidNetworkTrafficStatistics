package com.example.small.flowstatistics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.example.small.flowstatistics.MainActivity.TAG;

/**
 * Created by small on 2016/11/25.
 */

public class AlarmReceiverFree extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "快速更新广播收到");
        SharedPreferences pref_default = getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = context.getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        SharedPreferences pref = context.getSharedPreferences("data", MODE_PRIVATE);
        if (pref_default.getBoolean("free", true)) {

            CalculateTodayFlow calculateTodayFlow = new CalculateTodayFlow();
            long curdayflow = calculateTodayFlow.calculate(context);
            long curfreetimeflow = pref.getLong("curfreetimeflow", 0);
            editor.putLong("freetimeflowstart",curdayflow);
            editor.putLong("curmonthfreeflow",pref.getLong("curmonthfreeflow",0)+curfreetimeflow);
            editor.commit();
        }
    }
}
