package com.example.small.flowstatistics;

import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.MODE_APPEND;

/**
 * Created by small on 2016/11/10.
 */

public class LogManager {
    //写数据
    public void writeFileAppend(Context context,String fileName, String writestr) throws IOException {
        try {
            FileOutputStream fout = context.openFileOutput(fileName, MODE_APPEND);
            byte[] bytes = writestr.getBytes();
            fout.write(bytes);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("qiang", "写入成功");
    }
}
