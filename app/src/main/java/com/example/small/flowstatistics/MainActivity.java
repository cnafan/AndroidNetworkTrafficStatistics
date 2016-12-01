package com.example.small.flowstatistics;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import lecho.lib.hellocharts.animation.ChartAnimationListener;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

import static android.media.AudioManager.RINGER_MODE_SILENT;
import static android.media.AudioManager.STREAM_RING;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SMSBroadcastReceiver.Interaction {

    private int volumn = 0;
    public AudioManager audio;
    public int mode;

    public TextView textView;
    private ProgressDialog progressDialog;
    public FloatingActionButton fab;
    private static final int REQUEST_CODE = 1;
    private static final int RECEIVE_SMS_REQUEST_CODE = 2;

    public SharedPreferences pref_default;
    public SharedPreferences.Editor editor;
    public SharedPreferences pref;

    static String TAG = "qiang";
    private final static String[] mLabels = {"ANT", "GNU", "OWL", "APE", "COD", "YAK", "RAM", "JAY"};

    @Override
    protected void onResume() {
        super.onResume();

        long remain_liuliang = pref.getLong("remain_liuliang", 0);
        long all_liuliang = pref.getLong("all_liuliang", 0);
        long curdayflow = pref.getLong("curdayflow", 0);
        long lastmonthflow = pref.getLong("lastmonthflow", 0);
        long curfreetimeflow = pref.getLong("curfreetimeflow", 0);
        long curfreefront = pref.getLong("curfreefront", 0);
        long curfreebehind = pref.getLong("curfreebehind", 0);

        String textstr;
        if (pref_default.getBoolean("free", false)) {
            long allfreetimeflow = new Formatdata().GetNumFromString(pref_default.getString("freeflow", "0") + "M");
            textstr = "本月可用流量（含闲时）：" + new Formatdata().longtostring(all_liuliang) + "\n本月可用闲时流量：" + new Formatdata().longtostring(allfreetimeflow)
                    + "\n本月已用流量：" + new Formatdata().longtostring(all_liuliang - remain_liuliang)
                    + "\n本月还剩流量：" + new Formatdata().longtostring(remain_liuliang) + "\n上个月使用流量：" + new Formatdata().longtostring(lastmonthflow)
                    + "\n今日使用流量(不含闲时)：" + new Formatdata().longtostring(curdayflow - curfreebehind - curfreefront) + "\n今日闲时使用流量：" + new Formatdata().longtostring(curfreetimeflow);
        } else {
            textstr = "本月可用流量：" + new Formatdata().longtostring(all_liuliang) + "\n本月已用流量：" + new Formatdata().longtostring(all_liuliang - remain_liuliang)
                    + "\n本月还剩流量：" + new Formatdata().longtostring(remain_liuliang) + "\n上个月使用流量：" + new Formatdata().longtostring(lastmonthflow)
                    + "\n今日使用流量：" + new Formatdata().longtostring(curdayflow);
        }
        textView = (TextView) findViewById(R.id.main);
        textView.setText(textstr);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pref_default = getDefaultSharedPreferences(this);
        editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        pref = getSharedPreferences("data", MODE_PRIVATE);

        boolean isfirstrun = pref.getBoolean("isfirstrun", true);
        if (isfirstrun) {
            Calendar calendar = Calendar.getInstance();
            int curmonth = calendar.get(Calendar.MONTH);
            editor.putInt("savemonth", curmonth);
            editor.putLong("thisbootflow", 0);//3
            editor.putLong("curdayflow", 0);//4
            editor.putLong("onedaylastbootflow", 0);//一日内上次开机使用的流量 5
            editor.putLong("onebootlastdayflow", 0);//一次开机前一天使用的流量 6
            editor.putBoolean("isfirstrun", false);
            editor.putLong("curmonthflow", 0);//7
            editor.putLong("lastmonthflow", 0);
            editor.putLong("curfreetimeflow", 0);//当日闲时流量
            editor.putLong("allfreetimeflow", 0);//闲时流量总量
            editor.putLong("curmonthfreeflow", 0);//当月使用闲时流量
            editor.commit();

            /*
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
            */
        }
        if (!isMyServiceRunning()) {
            startService(new Intent(this, AlarmTimingStart.class));
            startService(new Intent(this, AlarmFreeStart.class));
            startService(new Intent(this, AlarmManualStart.class));
        }
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        lineChart = (LineChartView) findViewById(R.id.linechart);
        lineChart.setInteractive(true);//是否可以缩放
        lineChart.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
                Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                //addLineToData();
                //resetViewport();
            }

            @Override
            public void onValueDeselected() {

            }
        });
        //lineChart.setValueSelectionEnabled(false);//设置节点点击后动画
        //toggleCubic();
        AddLineChartDate();
        //float[]  = {33.0f, 33.4f, 33.2f, 33.4f, 33.0f, 33.4f, 33.2f, 33.4f, 33.0f, 33.4f, 33.2f, 33.4f, 33.0f, 33.4f, 33.2f, 33.4f, 33.0f, 33.4f, 33.2f, 33.4f, 33.0f, 33.4f, 33.2f, 33.4f, 33.0f, 33.4f, 33.2f, 33.4f, 33.0f, 33.4f, 33.2f};
    }

    /**
     * 添加数据
     */

    static int LineChartNums = 31;
    int numberOfLines = 1;
    int maxNumberOfLines = 4;
    boolean isCubic = false;

    LineChartView lineChart;

    private LineChartData chartData;

    public void AddLineChartDate() {

        List<Line> lines = new ArrayList<Line>();
        for (int i = 0; i < numberOfLines; i++) {
            List<PointValue> pointValues = new ArrayList<PointValue>();//节点数据结合
            Axis axisY = new Axis();//Y轴属性
            Axis axisX = new Axis();//X轴属性
            //axisY.setName("Y轴");
            //axisX.setName("X轴");
            ArrayList<AxisValue> axisValuesY = new ArrayList<AxisValue>();
            ArrayList<AxisValue> axisValuesX = new ArrayList<AxisValue>();
            for (int j = 0; j < LineChartNums; j++) {
                pointValues.add(new PointValue(j,new Formatdata().longtofloat(pref.getLong(j+1+"",0))));//添加节点数据
                //axisValuesY.add(new AxisValue(j * 10 * (i + 1)).setLabel(j + ""));//添加Y轴显示的刻度值
                axisValuesX.add(new AxisValue(j).setLabel(j+1 + ""));//添加X轴显示的刻度值
            }
            axisY.setValues(axisValuesY);
            axisX.setValues(axisValuesX);
            axisX.setLineColor(Color.BLACK);//无效果
            axisY.setLineColor(Color.BLACK);//无效果
            axisX.setTextColor(Color.BLACK);//设置X轴文字颜色
            axisY.setTextColor(Color.BLACK);//设置Y轴文字颜色

            axisX.setTextSize(10);//设置X轴文字大小
            axisX.setTypeface(Typeface.SERIF);//设置文字样式

            axisX.setHasTiltedLabels(false);//设置X轴文字向左旋转45度
            axisX.setHasLines(false);//是否显示X轴网格线
            axisY.setHasLines(false);
            axisX.setInside(false);//设置X轴文字在X轴内部
            //axisX.setMaxLabelChars(9); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length


            Line line = new Line(pointValues);
            line.setColor(Color.parseColor("#b6ddfb"));//设置折线颜色
            line.setStrokeWidth(5);//设置折线宽度
            line.setFilled(false);//设置折线覆盖区域颜色
            //line.setCubic(isCubic);//节点之间的过渡
            line.setCubic(true);//曲线是否平滑，即是曲线还是折线
            line.setPointColor(Color.parseColor("#2196F3"));//设置节点颜色
            line.setPointRadius(5);//设置节点半径
            line.setHasLabels(true);//是否显示节点数据
            line.setHasLines(true);//是否显示折线
            line.setHasPoints(true);//是否显示节点
            line.setShape(ValueShape.CIRCLE);//节点图形样式 DIAMOND菱形、SQUARE方形、CIRCLE圆形
            line.setHasLabelsOnlyForSelected(false);//隐藏数据，触摸可以显示


            lines.add(line);//将数据集合添加到线

            chartData = new LineChartData(lines);
            chartData.setAxisYLeft(axisY);//将Y轴属性设置到左边
            chartData.setAxisXBottom(axisX);//将X轴属性设置到底部
            chartData.setBaseValue(20);//设置反向覆盖区域颜色
            chartData.setValueLabelBackgroundAuto(false);//设置数据背景是否跟随节点颜色
            chartData.setValueLabelBackgroundColor(Color.BLUE);//设置数据背景颜色
            chartData.setValueLabelBackgroundEnabled(false);//设置是否有数据背景
            chartData.setValueLabelsTextColor(Color.parseColor("#000000"));//设置数据文字颜色
            chartData.setValueLabelTextSize(10);//设置数据文字大小
            chartData.setValueLabelTypeface(Typeface.MONOSPACE);//设置数据文字样式
        }
        //lineChart.setInteractive(true);
        //lineChart.setZoomType(ZoomType.HORIZONTAL);
        //lineChart.setVisibility(View.VISIBLE);
        lineChart.setLineChartData(chartData);//将数据添加到控件中
        //lineChart.setMaxZoom((float) 2);//最大方法比例
        Viewport tempViewport = new Viewport(0, lineChart.getMaximumViewport().height()*1.4f, 9, 0) ;//调整y轴,使图标上部有留白:
        lineChart.setCurrentViewport(tempViewport);//left：0//X轴为0   top:chart.getMaximumViewport()//Y轴的最大值right: 9//X轴显示9列 bottom：0//Y轴为0
    }

    private void resetViewport() {
        // Reset viewport height range to (0,100)
        final Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.bottom = 0;
        v.top = 100;
        v.left = 0;
        v.right = LineChartNums - 1;
        lineChart.setMaximumViewport(v);
        lineChart.setCurrentViewport(v);
    }

    private void toggleCubic() {
        isCubic = !isCubic;

        if (isCubic) {
            final Viewport v = new Viewport(lineChart.getMaximumViewport());
            v.bottom = -5;
            v.top = 105;
            lineChart.setMaximumViewport(v);
            lineChart.setCurrentViewportWithAnimation(v);
        } else {
            // If not cubic restore viewport to (0,100) range.
            final Viewport v = new Viewport(lineChart.getMaximumViewport());
            v.bottom = 0;
            v.top = 100;
            lineChart.setViewportAnimationListener(new ChartAnimationListener() {
                @Override
                public void onAnimationStarted() {
                    // TODO Auto-generated method stub
                }
                @Override
                public void onAnimationFinished() {
                    lineChart.setMaximumViewport(v);
                    lineChart.setViewportAnimationListener(null);
                }
            });
            lineChart.setCurrentViewportWithAnimation(v);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "requestCode:" + requestCode);
        switch (requestCode) {
            case 1:
                Log.d("qiang", "grantResults:" + grantResults[0]);
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("查询中...");
                    progressDialog.setCancelable(true);
                    progressDialog.show();
                    Sendmessage sendmessage = new Sendmessage();
                    sendmessage.Sendmessages();
                    //静音
                    sendmessage.silent();
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
                Intent settingintent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(settingintent);
                break;
            case R.id.log:
                final AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(this);
                alertDialog2.setTitle("log");
                String logstr = new FileManager().readLogFile(this);
                alertDialog2.setMessage(logstr);
                alertDialog2.setPositiveButton("OK", null);
                alertDialog2.show();
                break;
            case R.id.help:
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("帮助");
                String helpmessage = "1.点击查询按钮是请务必在开启数据且关闭wifi后。" +
                        "\n2.使用校正功能后请点击查询按钮导入设置。";
                alertDialog.setMessage(helpmessage);
                alertDialog.setPositiveButton("OK", null);
                alertDialog.show();
                break;
            case R.id.about_activity:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
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
                                progressDialog.setCancelable(true);
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
                            progressDialog.setCancelable(true);
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
    public void setTexts(Context context, String[] content) {
        //delay();
        try {
            Log.d("qiang", "delay");
            Thread.currentThread();
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        recovery();
        if (content[1] != null && content[0] != null) {
            progressDialog.dismiss();
            editor.putLong("curmonthflow", 0);
            editor.putLong("remain_liuliang", new Formatdata().GetNumFromString(content[0]));
            editor.putLong("all_liuliang", new Formatdata().GetNumFromString(content[1]));
            editor.commit();

            CalculateTodayFlow calculateTodayFlow = new CalculateTodayFlow();
            long todayflow = calculateTodayFlow.calculate(context);
            new NotificationManagers().showNotificationPrecise(context, todayflow);
        } else {
            Toast.makeText(this, "查询失败-.-", Toast.LENGTH_LONG).show();
        }
    }

    /* 检查手机是否是miui
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
     * 跳转到应用权限设置页面
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
}
