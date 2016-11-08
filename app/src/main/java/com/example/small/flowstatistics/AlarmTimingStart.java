package com.example.small.flowstatistics;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by small on 2016/9/30.
 */

public class AlarmTimingStart extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("qiang", "AlarmTimingStart已启动");

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent1 = new Intent(this, AlarmReceiverTiming.class); //触发广播，广播回调此方法，实现循环
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        long triggerTime = SystemClock.elapsedRealtime() +3*60 * 1000; //每隔--秒触发一次
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
        Log.d("qiang", "快速广播已发");

        stopSelf();
        Log.d("qiang", "AlarmTimingStart已关闭");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //在Service结束后关闭AlarmManager
        // AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // Intent i = new Intent(this, AlarmReceiver.class);
        // PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        // manager.cancel(pi);

    }
}
