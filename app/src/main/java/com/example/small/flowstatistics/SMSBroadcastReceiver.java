package com.example.small.flowstatistics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by small on 2016/9/30.
 */

public class SMSBroadcastReceiver extends BroadcastReceiver {
    private Interaction interaction;

    public interface Interaction {
        void setTexts(Context context, String[] content);
    }

    public void setInteractionListener(Interaction interaction) {
        this.interaction = interaction;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("qiang", "短信收到");
        Bundle bundle = intent.getExtras();

        if (bundle != null) {

            Object[] objArray = (Object[]) bundle.get("pdus");
            assert objArray != null;
            SmsMessage[] messages = new SmsMessage[objArray.length];
            for (int i = 0; i < objArray.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) objArray[i]);
            }
            String phoneNum = "";//电话号码
            StringBuilder sb = new StringBuilder();//短信内容

            for (SmsMessage currentMessage : messages) {
                phoneNum = currentMessage.getDisplayOriginatingAddress();
                sb.append(currentMessage.getDisplayMessageBody());
            }
            if (phoneNum.equals("10086")) {
                Toast.makeText(context, "接受成功!", Toast.LENGTH_SHORT)
                        .show();
                //Toast.makeText(context, sb, Toast.LENGTH_LONG).show();
                Log.d("qiang", sb.toString());
                //String duanxin = "尊敬的客户，您好！截至03月12日11时，您的\"流量汇0元包10M省内流量专属包\": 共含流量为30.00M，剩余流量为6.17M，有效期20160311-20160319。\"10元包1100M校内手机WLAN流量\": 共含WLAN为1100.00M，剩余WLAN为367.75M，有效期20160228-20160319。\"13元套餐(不区分G网和T网)\": 共含流量为60.00M，剩余流量为5.85M，有效期20160220-20160319。\"13元套餐(不区分G网和T网)（结转）\": 共含流量为10.82M，剩余流量为0.00M，有效期20160220-20160319。\"13元套餐(不区分G网和T网)\": 共含短信为50条，剩余短信为42条，有效期20160220-20160319。13元包80分钟小区内本地主叫 共含通话为80分钟，剩余通话为40分钟，有效期20160220-20160319。13元包40分钟小区内本地闲时主叫 共含通话为40分钟，剩余通话为40分钟，有效期20160220-20160319。\"智能网3元包(集团)\": 共含通话为1000分钟，剩余通话为896分钟，有效期20160220-20160319。 中国移动。";
                Re re_10086 = new Re();
                String[] fin = re_10086.calculate(context, sb.toString());

                context.unregisterReceiver(this);
                interaction.setTexts(context, fin);
            } else {
                //Toast.makeText(context, sb, Toast.LENGTH_LONG).show();
                //System.out.println("发送人：" + phoneNum + "  短信内容：" + sb.toString());
                context.unregisterReceiver(this);
            }
        }
    }
}
