package com.example.small.flowstatistics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.Objects;

/**
 * Created by small on 2016/9/30.
 */

public class NetworkStats extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("qiang", "网络连接改变");
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        if (activeInfo == null) {
            return;
        }
        if (activeInfo.isConnected()) {
            if (Objects.equals(activeInfo.getTypeName(), "MOBILE")) {
                Log.d("qiang", "网络连接改变，改用数据，启动定时服务");

                context.startService(new Intent(context, AlarmTimingStart.class));
                context.startService(new Intent(context, AlarmFreeStart.class));
                context.startService(new Intent(context, AlarmManualStart.class));

            }
        }
    }
}
