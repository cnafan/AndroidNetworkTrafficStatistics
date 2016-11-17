package com.example.small.flowstatistics;

import android.content.Context;

import org.apache.http.util.EncodingUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.MODE_APPEND;

/**
 * Created by small on 2016/11/7.
 */

class FileManager {

    //写数据
    void writeFileAppend(Context context, String fileName, String writestr) throws IOException {
        try {

            FileOutputStream fout = context.openFileOutput(fileName, MODE_APPEND);
            byte[] bytes = writestr.getBytes();
            fout.write(bytes);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String readLogFile(Context context) {
        String res = "";
        try {
            FileInputStream fin = context.openFileInput("log");
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

}
