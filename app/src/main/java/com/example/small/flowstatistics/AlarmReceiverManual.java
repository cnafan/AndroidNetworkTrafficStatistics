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

import java.util.Calendar;
import java.util.Objects;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.media.AudioManager.RINGER_MODE_SILENT;
import static android.media.AudioManager.STREAM_RING;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;

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

        SharedPreferences pref_default = getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = context.getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        SharedPreferences pref = context.getSharedPreferences("data", MODE_PRIVATE);
        if (pref_default.getBoolean("AutomaticCheck", false)) {
            Sendmessage sendmessage = new Sendmessage();//发送短信
            sendmessage.Sendmessages(context);
            //静音
            sendmessage.silent(context);
            Log.d("qiang", "每日短信发送成功");

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
            SMSBroadcastReceiver dianLiangBR = new SMSBroadcastReceiver();
            context.getApplicationContext().registerReceiver(dianLiangBR, intentFilter);
            dianLiangBR.setInteractionListener(this);
        }
        Calendar calendar = Calendar.getInstance();
        int cuday = calendar.get(Calendar.DAY_OF_MONTH);
        int curmonth = calendar.get(Calendar.MONTH);
        if (Objects.equals(Integer.valueOf(pref_default.getString("remonth", "")), cuday) && curmonth != pref.getInt("savemonth", 0)) {
            editor.putLong("curmonthflow", 0);
        }
        CalculateTodayFlow calculateTodayFlow = new CalculateTodayFlow();
        long todayflow = calculateTodayFlow.calculate(context);
        long curmonthflow = pref.getLong("curmonthflow", 0);
        curmonthflow = curmonthflow + todayflow;
        editor.putLong("curmonthflow", curmonthflow);

        //启动longRunningService
        Intent i = new Intent(context, AlarmManualStart.class);
        context.startService(i);

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();

        //long result;//1 当日使用流量
        long thisbootflow = pref.getLong("thisbootflow", 0);//4
        //long curdayflow=pref.getLong("curdayflow",0);
        long onedaylastbootflow = pref.getLong("onedaylastbootflow", 0);//5
        long onebootlastdayflow = pref.getLong("onebootlastdayflow", 0);//6

        if (activeInfo.isConnected()) {
            if (Objects.equals(activeInfo.getTypeName(), "MOBILE")) {
                long cur_boot_mobiletx = TrafficStats.getMobileTxBytes();
                long cur_boot_mobilerx = TrafficStats.getMobileRxBytes();
                thisbootflow = cur_boot_mobilerx + cur_boot_mobiletx;//4
            }
        }
        onebootlastdayflow = thisbootflow + onedaylastbootflow;
        editor.putLong("onebootlastdayflow", onebootlastdayflow);
        //editor.putLong("onedaylastbootflow", 0);
        editor.commit();
        Log.d("qiang", "每日更新广播处理完毕manual");
    }

    @Override
    public void show_notifiction(Context context, long curdayflow) {

        SharedPreferences pref_default = getDefaultSharedPreferences(context);
        if (!pref_default.getBoolean("ShowNotification", true)) {
            return;
        }

        SharedPreferences pref = context.getSharedPreferences("data", MODE_PRIVATE);
        long remain_liuliang = pref.getLong("remain_liuliang", 0);
        long all_liuliang = pref.getLong("all_liuliang", 0);
        long curmonthflow = pref.getLong("curmonthflow", 0);
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        String notification_string;

        if (Objects.equals(remain_liuliang, "") | Objects.equals(all_liuliang, "")) {
            notification_string = "无流量数据，请启动应用查询";
        } else {
            notification_string = "本月流量还剩 " + new Formatdata().longtostring(remain_liuliang - curmonthflow - curdayflow) + " 今日已用" + new Formatdata().longtostring(curdayflow);
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
            editor.putLong("remain_liuliang", new Formatdata().GetNumFromString(content));
            editor.putLong("all_liuliang", new Formatdata().GetNumFromString(content1));
            editor.putLong("curmonthflow", 0);
            editor.commit();

            CalculateTodayFlow calculateTodayFlow = new CalculateTodayFlow();
            long todayflow = calculateTodayFlow.calculate(context);
            show_notifiction(context, todayflow);

        } else {
            Toast.makeText(context, "查询失败-.-", Toast.LENGTH_LONG).show();
        }
    }

    public void recovery(Context context) {
        //Log.d("qiang", "volumn:" + volumn);
        if (Build.VERSION.SDK_INT >= 24) {
            audio = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        } else {
            audio = (AudioManager) context.getSystemService(AUDIO_SERVICE);
            audio.setRingerMode(mode);
            audio.setStreamVolume(mode, volumn, 0);
        }
    }

    public class Sendmessage {
        void Sendmessages(Context context) {
            SmsManager manager = SmsManager.getDefault();
            manager.sendTextMessage(context.getString(R.string.phone), null, context.getString(R.string.message_search), null, null);  //发送短信
        }

        void silent(Context context) {
            //静音--------
            if (Build.VERSION.SDK_INT >= 24) {
                audio = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                audio.adjustVolume(AudioManager.ADJUST_LOWER, 0);
            } else {
                audio = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                mode = audio.getRingerMode();
                volumn = audio.getStreamVolume(STREAM_RING);
                audio.setStreamVolume(STREAM_RING, 0, 0);
                audio.setRingerMode(RINGER_MODE_SILENT);
                //audio.setRingerMode(RINGER_MODE_SILENT);

                Log.d("qiang", "old_mode:" + volumn);
                Log.d("qiang", "old_volumn:" + volumn);
            }
        }
    }

}
