package com.autoopenmoney;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import com.autoopenmoney.util.NotificationUtil;

import java.util.List;

/**
 * Created by Jack on 2017/2/6 15:35.
 * Copyright 2017 Jack
 */

public class AutoRunReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, DoService.class);
        context.startService(i);
        if (!enabled(context, "com.autoopenmoney/.DoService")) {
            NotificationUtil.getInstance(context)
                    .sendNotification("抢红包", "辅助工能未开",
                            new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 1, 1);
        }
    }

    private boolean enabled(Context context, String name) {
        AccessibilityManager am
                = (AccessibilityManager)
                context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceInfos
                = am.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_GENERIC);
        List<AccessibilityServiceInfo> installedAccessibilityServiceList
                = am.getInstalledAccessibilityServiceList();
        for (AccessibilityServiceInfo info : serviceInfos) {
            if (name.equals(info.getId())) {
                return true;
            }
        }
        return false;
    }
}
