package com.example.small.flowstatistics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by small on 2016/12/4.
 */

public class AlarmReceiverUserPresent extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, AlarmTimingStart.class));
    }
}
