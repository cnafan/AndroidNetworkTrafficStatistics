package com.example.small.flowstatistics;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.MODE_APPEND;
import static android.content.Context.MODE_PRIVATE;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.example.small.flowstatistics.MainActivity.TAG;

/**
 * Created by small on 2016/11/10.
 */

class LogManager {

    public SharedPreferences pref;


    //写数据
    void writeLogFileAppend(Context context) throws IOException {
        SharedPreferences pref_default = getDefaultSharedPreferences(context);
        if (pref_default.getBoolean("log", false)) {
            Time time = new Time();
            time.setToNow();
            String logstr = time.monthDay + ":" + time.hour + ":" + time.minute + ":" + time.second;
            try {
                FileOutputStream fout = context.openFileOutput("log", MODE_APPEND);
                byte[] bytes = logstr.getBytes();
                fout.write(bytes);
                fout.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("qiang", "log写入成功");
        }
    }

    void writeLogFilePrivate(Context context) throws IOException {

        pref = context.getSharedPreferences("data", MODE_PRIVATE);

        long remain_liuliang = pref.getLong("remain_liuliang", 0);
        long all_liuliang = pref.getLong("all_liuliang", 0);
        long curdayflow = pref.getLong("curdayflow", 0);
        long lastmonthflow = pref.getLong("lastmonthflow", 0);
        long curfreetimeflow = pref.getLong("curfreetimeflow", 0);
        long curfreefront = pref.getLong("curfreefront", 0);
        long curfreebehind = pref.getLong("curfreebehind", 0);

        Time time = new Time();
        time.setToNow();
        String logstr = time.monthDay + ":" + time.hour + ":" + time.minute + ":" + time.second;
        logstr+="\nremain_liuliang:"+remain_liuliang;
        logstr+="\nall_liuliang:"+all_liuliang;
        logstr+="\ncurdayflow:"+curdayflow;
        logstr+="\nlastmonthflow:"+lastmonthflow;
        logstr+="\ncurfreetimeflow:"+curfreetimeflow;
        logstr+="\ncurfreefront:"+curfreefront;
        logstr+="\ncurfreebehind:"+curfreebehind;

        FileOutputStream fout = context.openFileOutput("log_refresh", MODE_PRIVATE);
        byte[] bytes = logstr.getBytes();
        fout.write(bytes);
        fout.close();
        Log.d(TAG,"log refresh");
    }

}
