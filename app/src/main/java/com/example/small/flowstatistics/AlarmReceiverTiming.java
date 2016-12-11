package com.example.small.flowstatistics;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.example.small.flowstatistics.MainActivity.TAG;

/**
 * Created by small on 2016/9/30.
 */

public class AlarmReceiverTiming extends BroadcastReceiver {
    public NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "快速更新广播收到");
        String logstr = "AlarmReceiverTiming";
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        String notification_string;

        SharedPreferences.Editor editor = context.getSharedPreferences("data", MODE_PRIVATE).edit();
        SharedPreferences pref = context.getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences pref_default = getDefaultSharedPreferences(context);

        if (activeInfo == null) {
            Log.d(TAG, "网络没有连接");
            return;
        }
        if (activeInfo.isConnected()) {
            if (Objects.equals(activeInfo.getTypeName(), "MOBILE")) {
                long curmonthflow = pref.getLong("curmonthflow", 0);
                long remain_liuliang = pref.getLong("remain_liuliang", 0);
                CalculateTodayFlow calculateTodayFlow = new CalculateTodayFlow();
                long curdayflow = ((pref.getBoolean("isreboot", false)) ? calculateTodayFlow.calculate(context) : calculateTodayFlow.calculate(context) - pref.getLong("firststartflow", 0));
                long allfreetimeflow = new Formatdata().GetNumFromString(pref_default.getString("freeflow", "0") + "M");
                long curmonthremainflow = pref_default.getBoolean("free", false) ? (remain_liuliang - curmonthflow - curdayflow - allfreetimeflow) : (remain_liuliang - curmonthflow - curdayflow);
               //alert_notification
                if (curmonthremainflow < (new Formatdata().GetNumFromString(pref_default.getString("alertsflow", "0")))) {
                    new NotificationManagers().showNotificationRough(context);
                }

                long cur_boot_mobiletx = TrafficStats.getMobileTxBytes();
                long cur_boot_mobilerx = TrafficStats.getMobileRxBytes();
                long thisbootflow = cur_boot_mobilerx + cur_boot_mobiletx;//4
                //long curdayflow = pref.getLong("curdayflow", 0);
                //long onedaylastbootflow = pref.getLong("onedaylastbootflow", 0);//5
                //long onebootlastdayflow = pref.getLong("onebootlastdayflow", 0);//6

                long freetimeflowstart = pref.getLong("freetimeflowstart", 0);

                Calendar calendar = Calendar.getInstance();
                int systemTime = calendar.get(Calendar.HOUR_OF_DAY);
                //Toast.makeText(context,systemTime,Toast.LENGTH_SHORT);
                if (systemTime > 22) {//从23点开始截止到次日7点
                    Log.d(TAG, "23free");
                    editor.putLong("curfreebehind", curdayflow - freetimeflowstart);
                    logstr += "\ncurfreebehind:" + (curdayflow - freetimeflowstart);
                } else if (systemTime < 6) {
                    Log.d(TAG, "07free");
                    editor.putLong("curfreefront", curdayflow);
                    logstr += "\ncurfreefront:" + (curdayflow);
                }
                editor.putLong("thisbootflow", thisbootflow);
                logstr += "\nthisbootflow:" + (thisbootflow);
                editor.putLong("curdayflow", curdayflow);
                logstr += "\ncurdayflow:" + (curdayflow);
                editor.putLong(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH) + "day"), pref.getLong("curdayflow", 0));
                logstr += "\n" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH) + "day") + ":" + (pref.getLong("curdayflow", 0));
                editor.commit();

                //log
                try {
                    new LogManager().writeLogFileAppend(context, logstr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //log_refresh
                try {
                    new LogManager().writeLogFilePrivate(context);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new NotificationManagers().showNotificationPrecise(context, curdayflow);
                context.startService(new Intent(context, AlarmTimingStart.class));
            }
//            Toast.makeText(context, "mobile:" + mobileInfo.isConnected() + "\n" + "wifi:" + wifiInfo.isConnected()      + "\n" + "active:" + activeInfo.getTypeName(), Toast.LENGTH_SHORT).show();
            //Log.d("qiang", "mobile:" + mobileInfo.isConnected() + ",wifi:" + wifiInfo.isConnected() + ",active:" + activeInfo.getTypeName());
        } else {
            Log.d(TAG, "网络没有连接,所以终止定时广播");
        }
    }


}
