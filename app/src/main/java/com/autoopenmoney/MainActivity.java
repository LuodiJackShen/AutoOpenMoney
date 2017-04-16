package com.autoopenmoney;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.autoopenmoney.util.NotificationUtil;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * 调用统计SDK
         *
         * @param appKey
         *            Bmob平台的Application ID
         * @param channel
         *            当前包所在渠道，可以为空
         * @return 是否成功，如果失败请看logcat，可能是混淆或so文件未正确配置
         */
        cn.bmob.v3.statistics.AppStat.i("8199f34ee94a88ccc45d9e50a7310a55", null);

        findViewById(R.id.content1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(i);
            }
        });
        if (!enabled("com.autoopenmoney/.DoService")) {
            NotificationUtil.getInstance(this)
                    .sendNotification("抢红包", "辅助工能未开",
                            new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 1, 1);
        } else {
            Toast.makeText(this, "辅助功能已开！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!enabled("com.autoopenmoney/.DoService")) {
            NotificationUtil.getInstance(this)
                    .sendNotification("抢红包", "辅助工能未开",
                            new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 1, 1);
        } else {
            Toast.makeText(this, "辅助功能已开！", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean enabled(String name) {
        AccessibilityManager am
                = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
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
