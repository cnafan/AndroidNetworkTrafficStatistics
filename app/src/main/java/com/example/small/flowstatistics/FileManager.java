package com.example.small.flowstatistics;

import android.content.Context;

import org.apache.http.util.EncodingUtils;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by small on 2016/11/7.
 */

class FileManager {

    //写数据
    void writeFile(Context context, String fileName, String writestr) throws IOException {
        try {
            BufferedWriter fout = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName, true)));
            /*** 追加文件：使用FileOutputStream，在构造FileOutputStream时，把第二个参数设为true
             */
            fout.write(writestr);
            /*
            FileOutputStream fout = context.openFileOutput(fileName, MODE_PRIVATE);
            byte[] bytes = writestr.getBytes();
            fout.write(bytes);
            */
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String readLogFile(Context context, String fileName) {
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

}
