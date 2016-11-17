package com.example.small.flowstatistics;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.MODE_APPEND;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by small on 2016/11/10.
 */

class LogManager {
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
}
