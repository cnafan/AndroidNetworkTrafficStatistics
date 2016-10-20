package com.example.small.flowstatistics;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Objects;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.media.AudioManager.RINGER_MODE_SILENT;
import static android.media.AudioManager.STREAM_RING;

/**
 * Created by small on 2016/9/30.
 */

public class AlarmReceiverManual extends BroadcastReceiver implements Notifications.Interaction_notification, SMSBroadcastReceiver.Interaction {
    public int mode;
    public AudioManager audio;
    public int volumn = 0;
    public NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {

        Sendmessage sendmessage = new Sendmessage();//发送短信
        sendmessage.Sendmessages(context);
        //静音
        sendmessage.silent(context);
        Log.d("qiang", "定时短信发送成功");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        SMSBroadcastReceiver dianLiangBR = new SMSBroadcastReceiver();
        context.getApplicationContext().registerReceiver(dianLiangBR, intentFilter);
        dianLiangBR.setInteractionListener(this);

        //启动longRunningService
        Intent i = new Intent(context, AlarmManualStart.class);
        context.startService(i);

        SharedPreferences.Editor editor = context.getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        SharedPreferences pref = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        long thisbootflow=pref.getLong("thisbootflow", 0);;
        if (activeInfo.isConnected()) {
            if (Objects.equals(activeInfo.getTypeName(), "MOBILE")) {
                long cur_boot_mobiletx = TrafficStats.getMobileTxBytes();
                long cur_boot_mobilerx = TrafficStats.getMobileRxBytes();
                thisbootflow = cur_boot_mobilerx + cur_boot_mobiletx;//4
            }
        }
        long result;//1 当日使用流量
        boolean isreboot = pref.getBoolean("isreboot", false); //2 重启过
        boolean iszero = pref.getBoolean("iszero", false);//3 过0点

        Log.d("qiang", "thisbootflow:" + thisbootflow);
        long onedaylastbootflow = pref.getLong("onedaylastbootflow", 0);//5
        long onebootlastdayflow = pref.getLong("onebootlastdayflow", 0);//6
        long curdayflow=pref.getLong("curdayflow",0);

        editor.putLong("onedaylastbootflow",0);
        editor.putLong("onebootlastdayflow", thisbootflow);
        editor.putBoolean("iszero", true);
        editor.putBoolean("isreboot", false);
        editor.commit();

        Log.d("qiang", "定时更新广播处理完毕manual");
    }

    @Override
    public void show_notifiction(Context context, long curdayflow) {
        SharedPreferences pref = context.getSharedPreferences("data", MODE_PRIVATE);
        String remain_liuliang = pref.getString("remain_liuliang", "");
        String all_liuliang = pref.getString("all_liuliang", "");
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        String notification_string;

        //long today = calculate_today(this);

        if (Objects.equals(remain_liuliang, "") | Objects.equals(all_liuliang, "")) {
            notification_string = "无流量数据，请启动应用查询";
        } else {
            notification_string = "本月流量还剩 " + remain_liuliang + " 今日已用" + show_change(curdayflow);
        }
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_album_black_24dp)
                .setContentTitle("流量计")
                .setAutoCancel(true)
                .setOngoing(true)
                .setContentText(notification_string);
        if (Build.VERSION.SDK_INT < 16) {
            notificationManager.notify(0, builder.getNotification());
        } else {
            notificationManager.notify(0, builder.build());
        }
    }

    @Override
    public void setTexts(Context context, String content, String content1) {
//delay();

        try {
            //Log.d("qiang", "delay");
            Thread.currentThread();
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        recovery(context);

        if (content != null && content1 != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences("data", MODE_PRIVATE).edit();

            editor.putString("remain_liuliang", content);
            editor.putString("all_liuliang", content1);
            editor.commit();


            CalculateTodayFlow calculateTodayFlow = new CalculateTodayFlow();
            long todayflow = calculateTodayFlow.calculate(context);
            show_notifiction(context, todayflow);

        } else {
            Toast.makeText(context, "查询失败-.-", Toast.LENGTH_LONG).show();
        }
    }
    String show_change(long data) {

        DecimalFormat df = new DecimalFormat("#.##");
        double bytes = data / 1024.0;
        if (bytes > 1048576.0 && bytes / 1048576.0 > 0) {
            return df.format(bytes / 1048576.0) + "G";
        } else if (bytes > 1024.0 && bytes / 1024.0 > 0) {
            return df.format(bytes / 1024.0) + "M";
        } else {
            return df.format(bytes) + "k";
        }
    }

    public void recovery(Context context) {
        //Log.d("qiang", "volumn:" + volumn);
        audio = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        audio.setRingerMode(mode);
        audio.setStreamVolume(mode, volumn, 0);
        /*
        if (mode == RINGER_MODE_SILENT) {
            audio.setRingerMode(RINGER_MODE_SILENT);
            //audio.setStreamVolume(RINGER_MODE_SILENT, volumn, 0);
            audio.setRingerMode(RINGER_MODE_VIBRATE);
            audio.setStreamVolume(RINGER_MODE_VIBRATE, volumn, 0);
        } else if (volumn == RINGER_MODE_VIBRATE) {
            audio.setRingerMode(RINGER_MODE_VIBRATE);
            audio.setStreamVolume(RINGER_MODE_VIBRATE, volumn, 0);
        } else {
            audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audio.setStreamVolume(AudioManager.RINGER_MODE_NORMAL, volumn, 0);
        }
        */
    }
    public class Sendmessage {
        void Sendmessages(Context context) {
            SmsManager manager = SmsManager.getDefault();
            manager.sendTextMessage(context.getString(R.string.phone), null, context.getString(R.string.message_search), null, null);  //发送短信
        }

        void silent(Context context) {
            //静音--------
            audio = (AudioManager) context.getSystemService(AUDIO_SERVICE);
            mode = audio.getRingerMode();
            int volumn = audio.getStreamVolume(STREAM_RING);
            audio.setStreamVolume(STREAM_RING, 0, 0);
            audio.setRingerMode(RINGER_MODE_SILENT);
            //audio.setRingerMode(RINGER_MODE_SILENT);

            Log.d("qiang", "old_mode:" + volumn);
            Log.d("qiang", "old_volumn:" + volumn);
        }
    }

}
