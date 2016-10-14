package com.example.small.flowstatistics;

import android.content.Context;

/**
 * Created by small on 2016/9/30.
 */

class Notifications {
    interface Interaction_notification {
        void show_notifiction(Context context, long curdayflow);
    }

    private Interaction_notification interaction_notification;
}
