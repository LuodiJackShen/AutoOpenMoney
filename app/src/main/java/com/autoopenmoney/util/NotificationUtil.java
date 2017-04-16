package com.autoopenmoney.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.autoopenmoney.R;

/**
 * Created by Jack on 2017/1/29 20:24.
 * Copyright 2017 Jack
 */

public class NotificationUtil {
    private static final String TAG = "NotificationUtil";

    private static NotificationUtil sNotificationUtil;
    private static NotificationManager sManager;
    private Context mContext;

    public static NotificationUtil getInstance(Context context) {
        if (sNotificationUtil == null) {
            sNotificationUtil = new NotificationUtil(context);
        }
        return sNotificationUtil;
    }

    private NotificationUtil(Context context) {
        mContext = context;
        sManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void sendNotification(String contentTitle, String contentText,
                                 Intent intent, int notifyId, int notifyRequestCode) {
        PendingIntent pi = null;
        if (intent != null) {
            pi = PendingIntent
                    .getActivity(mContext,
                            notifyRequestCode,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            Log.d(TAG, "intent is not null.");
        }
        Notification notification = new Notification.Builder(mContext)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build();
        sManager.notify(notifyId, notification);
    }
}
