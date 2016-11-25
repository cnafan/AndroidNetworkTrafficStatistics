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
import static com.example.small.flowstatistics.MainActivity.TAG;

/**
 * Created by small on 2016/9/30.
 */

public class AlarmReceiverTiming extends BroadcastReceiver {
    public NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "快速更新广播收到");

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        if (activeInfo == null) {
            Log.d(TAG, "网络没有连接");
            return;
        }
        if (activeInfo.isConnected()) {
            if (Objects.equals(activeInfo.getTypeName(), "MOBILE")) {
                //log
                try {
                    new LogManager().writeLogFileAppend(context);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SharedPreferences.Editor editor = context.getSharedPreferences("data", MODE_PRIVATE).edit();
                SharedPreferences pref = context.getSharedPreferences("data", MODE_PRIVATE);

                long cur_boot_mobiletx = TrafficStats.getMobileTxBytes();
                long cur_boot_mobilerx = TrafficStats.getMobileRxBytes();
                long thisbootflow = cur_boot_mobilerx + cur_boot_mobiletx;//4
                //long curdayflow = pref.getLong("curdayflow", 0);
                //long onedaylastbootflow = pref.getLong("onedaylastbootflow", 0);//5
                //long onebootlastdayflow = pref.getLong("onebootlastdayflow", 0);//6

                CalculateTodayFlow calculateTodayFlow = new CalculateTodayFlow();
                long curdayflow = calculateTodayFlow.calculate(context);


                long freetimeflowstart = pref.getLong("freetimeflowstart", 0);

                Calendar calendar= Calendar.getInstance();
                int systemTime = calendar.get(Calendar.HOUR_OF_DAY);
                //Toast.makeText(context,systemTime,Toast.LENGTH_SHORT);
                if (systemTime > 22) {//从23点开始截止到次日7点
                    Log.d(TAG,"23free");
                    editor.putLong("curfreetimeflow", curdayflow - freetimeflowstart);

                } else if (systemTime < 7) {
                    Log.d(TAG,"07free");
                    editor.putLong("curfreetimeflow", pref.getLong("freetimeflow", 0) + curdayflow);
                }
                editor.putLong("thisbootflow", thisbootflow);
                editor.putLong("curdayflow", curdayflow);
                editor.commit();

                new NotificationManagers().showNotificationPrecise(context, curdayflow);
                context.startService(new Intent(context, AlarmTimingStart.class));
            }
//            Toast.makeText(context, "mobile:" + mobileInfo.isConnected() + "\n" + "wifi:" + wifiInfo.isConnected()      + "\n" + "active:" + activeInfo.getTypeName(), Toast.LENGTH_SHORT).show();
            //Log.d("qiang", "mobile:" + mobileInfo.isConnected() + ",wifi:" + wifiInfo.isConnected() + ",active:" + activeInfo.getTypeName());
        } else

        {
            Log.d(TAG, "网络没有连接,所以终止定时广播");
        }
    }


}
