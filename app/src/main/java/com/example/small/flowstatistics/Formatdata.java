package com.example.small.flowstatistics;

import android.util.Log;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by small on 2016/11/2.
 */

 class Formatdata {

    String longtostring(long data) {
        DecimalFormat df = new DecimalFormat("#.##");
        double bytes = data / 1024.0;
        String result = "";
        if (bytes > 1048576.0 && bytes / 1048576.0 > 0) {
            result = df.format(bytes / 1048576.0) + "G";
        } else if (bytes > 1024.0 && bytes / 1024.0 > 0) {
            result = df.format(bytes / 1024.0) + "M";
        } else {
            result = df.format(bytes) + "k";
        }
        Log.d("qiang", "longtostring:" + result);
        return result;
    }

    long GetNumFromString(String data) {
        String reg = "\\d+.\\d+";
        String result_match = "";
        Pattern reg_data = Pattern.compile(reg);
        Matcher matcher = reg_data.matcher(data);
        if (matcher.find()) {
            result_match = matcher.group(0);
        }
        long result;
        double num=Double.parseDouble(result_match);
        String last=GetLastFromString(data);
        switch (last){
            case "k":
                result=(long)(num*1024);
                break;
            case "M":
                result=(long)(num*1024*1024);
                break;
            default://G
                result=(long)(num*1024*1024*1024);
        }
        Log.d("qiang", "GetNumFromString(bytes):" + result);
        return result;
    }
    private String GetLastFromString(String data){
        String reg = "[MGk]";
        String result = "";
        Pattern reg_data = Pattern.compile(reg);
        Matcher matcher = reg_data.matcher(data);
        if (matcher.find()) {
            result = matcher.group(0);
        }
        Log.d("qiang", "GetLastFromString:" + result);
        return result;
    }

}
