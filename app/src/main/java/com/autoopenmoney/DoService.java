package com.autoopenmoney;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.autoopenmoney.util.FileUtil;

import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.accessibility.AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;

/**
 * Created by Jack on 2017/2/5 19:08.
 * Copyright 2017 Jack
 */

public class DoService extends AccessibilityService {
    private static final String TAG = "DoService";

    private static final String CHAT_UI = "com.tencent.mm.ui.LauncherUI";
    private static final String HONG_BAO_UI =
            "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    private static final String DETAIL_UI =
            "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";

    private boolean isFromNotification = false;
    private boolean hasOpened = false;
    private boolean hasGoneHome = false;
    private PowerManager.WakeLock mWakeLock;
    private KeyguardManager.KeyguardLock mKeyLock;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        switch (event.getEventType()) {
            case TYPE_NOTIFICATION_STATE_CHANGED:
                notificationStateChanged(event);
                break;
            case TYPE_WINDOW_STATE_CHANGED:
                /*Log.d(TAG, "state changed");*/
                windowStateChanged(event);
                break;
            default:
                break;
        }

    }

    private void windowStateChanged(AccessibilityEvent event) {
        String className = event.getClassName().toString();

        if (!isFromNotification) {
            return;
        }
        /*Log.d(TAG, "className is " + className);*/
        if (className.equals(CHAT_UI)) {
          /*Log.d(TAG, "ready to find");*/
            findPacket(getRootInActiveWindow());
        } else if (className.equals(HONG_BAO_UI)) {
            /*Log.d(TAG, "ready to open");*/
            openPacket(getRootInActiveWindow());
        } else if (className.equals(DETAIL_UI)) {
            FileUtil.writeToLog(String.valueOf(hasGoneHome), "red_log.java");
            if (!hasGoneHome) {
                //                goDesktop();
            }
        }
    }

    /***
     * 递归的方式找到最新的那个红包。
     * 找到红包所在的控件，然后找他的父控件，
     * 直到找到一个可以点击的控件，进行点击。
     * 也试过用 findAccessibilityNodeInfosByViewId(...)方法直接找到那个控件，
     * 但是总会找到的总不是最新发的红包，原因未知。
     * <p>
     * 核心代码其实是那段遍历代码。
     *
     * @param info 节点信息
     */
    private void findPacket(AccessibilityNodeInfo info) {
        /*Log.d(TAG, "be in findPacket");*/
        if (info == null) {
            return;
        }
        int childCount = info.getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            AccessibilityNodeInfo node = info.getChild(i);
            String text = null;
            if (node != null && node.getText() != null) {
                text = node.getText().toString();
            }
            if (!TextUtils.isEmpty(text)
                    && (text.equals("领取红包") || text.equals("抢红包"))) {
                if (node.isClickable()) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else {
                    while (true) {
                        AccessibilityNodeInfo parent = node.getParent();
                        if (parent != null && parent.isClickable()) {
                            hasOpened = true;
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            break;
                        }
                    }
                }
            }
            if (!hasOpened) {
                findPacket(node);
            }
        }
    }

    /***
     * 打开红包。
     *
     * @param info
     */
    private void openPacket(AccessibilityNodeInfo info) {
        if (info == null) {
            return;
        }
        List<AccessibilityNodeInfo> infoes =
                info.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bi3");
        if (infoes.isEmpty() || infoes == null) {
            return;
        }
        for (AccessibilityNodeInfo nodeInfo : infoes) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        hasGoneHome = false;
    }

    /***
     * 当来通知的时候进行判断：是否有人发了微信红包。
     *
     * @param event
     */
    private void notificationStateChanged(AccessibilityEvent event) {
        List<CharSequence> texts = event.getText();
        if (!texts.isEmpty()) {
            for (CharSequence text : texts) {
                String content = text.toString();
                if (content.contains("[微信红包]")) {
                    Parcelable data = event.getParcelableData();
                    if (data != null && data instanceof Notification) {
                        openScreen();
                        isFromNotification = true;
                        hasOpened = false;
                        Notification notification = (Notification) data;
                        PendingIntent pi = notification.contentIntent;
                        try {
                            pi.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /***
     * open screen if screen is close.
     * open lock if it has screen lock.
     * <p>
     * （虽是蹩脚的英文，但仍能看懂^_^）
     * 主要是考虑到手机黑屏的情况。
     * Note:解锁方式必须是滑动解锁（其他解锁方式没试过）。
     */
    private void openScreen() {
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.SCREEN_DIM_WAKE_LOCK,
                    "flag");
        }

        if (mKeyLock == null) {
            mKeyLock = ((KeyguardManager) getSystemService(KEYGUARD_SERVICE))
                    .newKeyguardLock("unlock");
        }

        mWakeLock.acquire();
        mKeyLock.disableKeyguard();
    }

    /***
     * release the resource.
     */
    public void closeScreen() {
        if (mKeyLock != null && mWakeLock != null) {
            mKeyLock.reenableKeyguard();
            mWakeLock.release();
        }
    }

    private void goDesktop() {
        FileUtil.writeToLog("goDesktop()", "red_log.java");
        hasGoneHome = true;
        isFromNotification = false;
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.addFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(home);
        closeScreen();
    }

    @Override
    public void onInterrupt() {
        // Nothing Here
    }
}
