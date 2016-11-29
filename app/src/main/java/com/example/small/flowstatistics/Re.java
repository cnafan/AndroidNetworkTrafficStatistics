package com.example.small.flowstatistics;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by small on 2016/9/30.
 */

class Re {
    private DecimalFormat df = new DecimalFormat("#.##");
    //尊敬的客户，您好！截至09月23日16时，您本帐单月使用的"赠送1.5G省内通用流量，48个月"套餐移动数据流量共有1536.00M，剩余流量还有1358.83M; "0元50M省内流量包"套餐移动数据流量共有50.00M，剩余流量还有50.00M; "赠送10M省内流量"套餐移动数据流量共有10.00M，剩余流量还有10.00M; "18元包来电显示+100M国内流量，超出0.29元/M"套餐移动数据流量共有100.00M，剩余流量还有100.00M; 普通GPRS"10元包1G省内通用流量"套餐移动数据流量共有1024.00M，剩余流量还有1024.00M; "0元包6GB咪咕省内定向流量，有效期12个月"套餐移动数据流量共有6144.00M，剩余流量还有6144.00M; 仅供参考，具体以月结账单为准。

    //尊敬的客户，您好！截至11月28日19时，您本帐单月使用的普通GPRS"10元包10G省内4G单模流量(闲时)"套餐移动数据流量共有10240.00M，剩余流量还有9318.19M;
    // "赠送1.5G省内通用流量，48个月"套餐移动数据流量共有1536.00M，剩余流量还有291.79M;
    // "赠送5M省内流量"套餐移动数据流量共有5.00M，剩余流量还有5.00M; "18元包来电显示+100M国内流量，超出0.29元/M"套餐移动数据流量共有100.00M，剩余流量还有100.00M;
    // "18元包来电显示+100M国内流量，超出0.29元/M（结转）"套餐移动数据流量共有100.00M，剩余流量还有100.00M;
    // 普通GPRS"10元包1G省内通用流量"套餐移动数据流量共有1024.00M，剩余流量还有1024.00M; 普通GPRS"10元包1G省内通用流量（结转）"套餐移动数据流量共有1024.00M，剩余流量还有1024.00M;
    // "0元包6GB咪咕省内定向流量，有效期12个月"套餐移动数据流量共有6144.00M，剩余流量还有6144.00M; 仅供参考，具体以月结账单为准。

    private String[] reg = {
            "(?<=剩余流量还有)\\d+\\.\\d\\d",
            "(?<=流量共有)\\d+\\.\\d\\d"
    };
    private String deal(Context context, String data, String reg) {

        SharedPreferences pref = getDefaultSharedPreferences(context);
        Double check = Double.valueOf(pref.getString("check", "0"));

        Double return_data = 0.0;
        Pattern reg_data = Pattern.compile(reg);
        Matcher matcher = reg_data.matcher(data);
        while (matcher.find()) {
            return_data += Double.valueOf(matcher.group());
        }

        Log.d("qiang", "remain_liuliang_before =" + return_data);
        return_data -= check;

        Log.d("qiang", "remain_liuliang_after=" + return_data);
        DecimalFormat df = new DecimalFormat("#.##");

        if (return_data > 1024.0 && return_data / 1024.0 > 0) {
            return df.format(return_data / 1024.0) + "G";
        } else if (return_data < 1024.0 && return_data > 1) {
            return df.format(return_data) + "M";
        } else {
            return df.format(return_data * 1024.0) + "k";
        }
    }

    String[] calculate(Context context, String data) {
        String[] remain = new String[2];
        for (int t = 0; t < remain.length; t++) {
            remain[t] = deal(context, data, reg[t]);
        }
        Log.d("qiang", "remain_liuliang =" + remain[0]);
        Log.d("qiang", "all_liuliang =" + remain[1]);
        return remain;
    }
}
