package com.example.small.flowstatistics;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.util.EncodingUtils;

import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.Objects;

import static android.media.AudioManager.RINGER_MODE_SILENT;
import static android.media.AudioManager.STREAM_RING;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SMSBroadcastReceiver.Interaction, Notifications.Interaction_notification {

    private int volumn = 0;
    public NotificationManager notificationManager;
    public AudioManager audio;
    public int mode;

    public TextView textView;
    private ProgressDialog progressDialog;
    public Button button;
    public FloatingActionButton fab;


    private static final int REQUEST_CODE = 1;

    private static final int RECEIVE_SMS_REQUEST_CODE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        boolean isfirstrun = pref.getBoolean("isfirstrun", true);
        if (isfirstrun) {
            //editor.putBoolean("isreboot", false);//1
            //editor.putBoolean("iszero", false);//2
            editor.putLong("thisbootflow", 0);//3
            editor.putLong("curdayflow", 0);//4
            editor.putLong("onedaylastbootflow", 0);//一日内上次开机使用的流量 5
            editor.putLong("onebootlastdayflow", 0);//一次开机前一天使用的流量 6
            editor.putBoolean("isfirstrun", false);
            editor.commit();

            if (isMIUI()) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage("检测到您的手机为MIUI系统，软件正常运行需要申请权限，现跳转至权限管理界面");
                alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gotoPermissionSettings(MainActivity.this);
                    }
                });
            }
        }

        if (!isMyServiceRunning()) {
            Intent intent = new Intent(this, AlarmTimingStart.class);
            Intent intent2 = new Intent(this, AlarmManualStart.class);
            //开启Service
            startService(intent);
            startService(intent2);
        }
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d("qiang", "requestCode:" + requestCode);
        switch (requestCode) {
            case 1:
                Log.d("qiang", "grantResults:" + grantResults[0]);
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("查询中...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    Sendmessage sendmessage = new Sendmessage();
                    sendmessage.Sendmessages();
                    //静音
                    sendmessage.silent();

                    //Snackbar.make(MainActivity.class, "已发送", Snackbar.LENGTH_LONG).setAction("", null).show();
                    Log.d("qiang", "发送成功");
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "接受短信权限已关闭", Toast.LENGTH_SHORT)
                            .show();
                    Log.d("qiang", "接受短信权限已关闭");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent intent_setting = new Intent(this, SettingsActivity.class);
                startActivity(intent_setting);
                break;
            case R.id.help:
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("帮助");
                String helpmessage = "1.点击查询按钮是请务必在开启数据且关闭wifi后。" +
                        "\n2.使用校正功能后请点击查询按钮导入设置。" + "\n3.每日自动校正将于次日早上6点开始。";
                alertDialog.setMessage(helpmessage);
                alertDialog.setPositiveButton("OK", null);
                alertDialog.show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo activeInfo = manager.getActiveNetworkInfo();
                if (activeInfo == null) {
                    Log.d("qiang", "网络没有连接");

                    Toast.makeText(this, "请关闭wifi并打开数据开关后查询", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (activeInfo.isConnected()) {
                    if (Objects.equals(activeInfo.getTypeName(), "MOBILE")) {

                        Sendmessage sendmessage = new Sendmessage();//发送短信
                        if (Build.VERSION.SDK_INT >= 23) {
                            int checkCallPhonePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS);
                            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECEIVE_SMS}, REQUEST_CODE);

                            } else {
                                progressDialog = new ProgressDialog(MainActivity.this);
                                progressDialog.setMessage("查询中...");
                                progressDialog.setCancelable(false);
                                progressDialog.show();

                                sendmessage.Sendmessages();
                                //静音
                                sendmessage.silent();

                                Snackbar.make(v, "已发送", Snackbar.LENGTH_LONG).setAction("", null).show();
                                Log.d("qiang", "发送成功");
                            }
                        } else {
                            progressDialog = new ProgressDialog(MainActivity.this);
                            progressDialog.setMessage("查询中...");
                            progressDialog.setCancelable(false);
                            progressDialog.show();

                            sendmessage.Sendmessages();
                            //静音
                            sendmessage.silent();

                            Snackbar.make(v, "已发送", Snackbar.LENGTH_LONG).setAction("", null).show();
                            Log.d("qiang", "发送成功");
                        }

                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
                        SMSBroadcastReceiver dianLiangBR = new SMSBroadcastReceiver();
                        registerReceiver(dianLiangBR, intentFilter);
                        dianLiangBR.setInteractionListener(MainActivity.this);
                    } else {

                        Toast.makeText(this, "请关闭wifi并打开数据开关后查询", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(this, "请关闭wifi并打开数据开关后查询", Toast.LENGTH_SHORT).show();
                }
        }
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
            Log.d("qiang", "delay");
            Thread.currentThread();
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        recovery();

        if (content != null && content1 != null) {
            SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
            progressDialog.dismiss();
            editor.putString("remain_liuliang", content);
            editor.putString("all_liuliang", content1);
            editor.commit();

            CalculateTodayFlow calculateTodayFlow = new CalculateTodayFlow();
            long todayflow = calculateTodayFlow.calculate(context);
            show_notifiction(this, todayflow);

        } else {
            Toast.makeText(this, "查询失败-.-", Toast.LENGTH_LONG).show();
        }
    }

    /* 检查手机是否是miui
    *
            * @ref http://dev.xiaomi.com/doc/p=254/index.html
            * @return
            */

    /**
     * 检查手机是否是miui
     *
     * @return
     * @ref http://dev.xiaomi.com/doc/p=254/index.html
     */
    public static boolean isMIUI() {
        String device = Build.MANUFACTURER;
        System.out.println("Build.MANUFACTURER = " + device);
        if (device.equals("Xiaomi")) {
            System.out.println("this is a xiaomi device");
            return true;
        } else {
            return false;
        }
    }

    /**
     * 跳转到应用权限设置页面 http://www.tuicool.com/articles/jUby6rA
     *
     * @param context 传入app 或者 activity
     *                context，通过context获取应用packegename，之后通过packegename跳转制定应用
     * @return 是否是miui
     */
    public static boolean gotoPermissionSettings(Context context) {
        boolean mark = isMIUI();

        if (mark) {

            // 只兼容miui v5/v6 的应用权限设置页面，否则的话跳转应用设置页面（权限设置上一级页面）
            try {
                Intent localIntent = new Intent(
                        "miui.intent.action.APP_PERM_EDITOR");
                localIntent
                        .setClassName("com.miui.securitycenter",
                                "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                localIntent.putExtra("extra_pkgname", context.getPackageName());
                context.startActivity(localIntent);

            } catch (ActivityNotFoundException e) {
                Intent intent = new Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(),
                        null);
                intent.setData(uri);
                context.startActivity(intent);
            }
        }

        return mark;
    }

    public boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.AlarmReceiver".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void recovery() {
        //Log.d("qiang", "volumn:" + volumn);
        if (Build.VERSION.SDK_INT >= 24) {

            audio = (AudioManager) getSystemService(AUDIO_SERVICE);
        } else {
            audio = (AudioManager) getSystemService(AUDIO_SERVICE);
            audio.setRingerMode(mode);
            audio.setStreamVolume(mode, volumn, 0);
        }

    }

    public class Sendmessage {
        void Sendmessages() {
            SmsManager manager = SmsManager.getDefault();
            manager.sendTextMessage(getString(R.string.phone), null, getString(R.string.message_search), null, null);  //发送短信
            Log.d("qiang", "发送短信中");
        }

        void silent() {
            //静音--------
            if (Build.VERSION.SDK_INT >= 24) {
                audio = (AudioManager) getSystemService(AUDIO_SERVICE);
                audio.adjustVolume(AudioManager.ADJUST_LOWER, 0);
            } else {
                audio = (AudioManager) getSystemService(AUDIO_SERVICE);
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

    public String readLogFile(Context context, String fileName) {
        String res = "";
        try {
            FileInputStream fin = context.openFileInput(fileName);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            res = EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }//读取到/data/data/目录

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

}
