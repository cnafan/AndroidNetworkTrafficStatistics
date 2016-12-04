package com.example.small.flowstatistics;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by small on 2016/12/4.
 */

public class Running extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(receiver, filter);
        */
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_TICK)) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent intent1 = new Intent(Running.this, AlarmReceiverTiming.class); //触发广播，广播回调此方法，实现循环
                PendingIntent pendingIntent = PendingIntent.getBroadcast(Running.this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                long triggerTime = SystemClock.elapsedRealtime() + 3 * 60 * 1000; //每隔--秒触发一次
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
                Log.d("qiang", "快速广播已发");

                //do what you want to do ...13
            }
            */
        }
    };
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(Integer.MAX_VALUE);
        if (serviceList == null || serviceList.size() == 0)
            return false;
        for (ActivityManager.RunningServiceInfo info : serviceList) {
            if (info.service.getClassName().equals(serviceClass.getName()))
                return true;
        }
        return false;
    }
}
