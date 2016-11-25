package com.example.small.flowstatistics;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.example.small.flowstatistics.MainActivity.TAG;

/**
 * Created by small on 2016/11/25.
 */

public class AlarmFreeStart extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "AlarmFreeStart已开启");
        SharedPreferences pref_default = getDefaultSharedPreferences(this);
        if (pref_default.getBoolean("ShowNotification", true)) {

            Intent intent1 = new Intent(this, AlarmReceiverManual.class); //触发广播，广播回调此方法，实现循环
            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 1, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            long firstTime = SystemClock.elapsedRealtime();    // 开机之后到现在的运行时间(包括睡眠时间)
            long systemTime = System.currentTimeMillis();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            // 这里时区需要设置一下，不然会有8个小时的时间差
            calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            // 选择的定时时间
            long selectTime = calendar.getTimeInMillis();
            // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
            if (systemTime > selectTime) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                selectTime = calendar.getTimeInMillis();
            }
            // 计算现在时间到设定时间的时间差
            long time = selectTime - systemTime;
            firstTime += time;
            Log.d(TAG, "时间差：" + time);
            // 进行闹铃注册
            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    firstTime, time, pendingIntent2);
            Log.d(TAG, "23点广播已发");

            stopSelf();
            Log.d(TAG, "AlarmFreeStart已关闭");
        }

        return super.onStartCommand(intent, flags, startId);
    }
}
