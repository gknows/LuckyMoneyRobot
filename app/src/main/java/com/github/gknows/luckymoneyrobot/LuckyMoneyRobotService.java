package com.github.gknows.luckymoneyrobot;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import android.util.Log;

import java.util.List;

/**
 * Created by gknows on 2015/7/26.
 */
public class LuckyMoneyRobotService extends AccessibilityService {

    static final String LUCKY_MONEY_KEY_TEXT = "[微信红包]";
    static final String LUCKY_MONEY_OPEN_TEXT = "领取红包";
    static final String LUCKY_MONEY_UNPACK_TEXT = "拆红包";
    static final String LUCKY_MONEY_GONE_TEXT = "手慢了";
    static final String LUCKY_MONEY_BACK = "返回";
    static final String LUCKY_MONEY_WECHAT = "微信";

    static final String TAG = "LUCKYMONEYTEST";

    //private boolean m_isLuckyNotification = false;
    private int m_luckyNotificationFlag = 0;
    private long m_notificationTime;
    private int m_delayTimems = 0;

    Prefs prefs;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int event_type = event.getEventType();
        if(prefs != null){
            m_delayTimems = prefs.getDelayTimems();
        }
        if(event_type == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED){
            List<CharSequence> texts = event.getText();
            if(!texts.isEmpty()){
                for(CharSequence t : texts){
                    String text = String.valueOf(t);
                    if(text.contains(LUCKY_MONEY_KEY_TEXT)){
                        //m_isLuckyNotification = true;
                        m_luckyNotificationFlag = 2;
                        m_notificationTime = System.currentTimeMillis();
                        wakeupScreen();
                        openNotify(event);
                        loopToOpen();
                    }
                }
            }
        }
        else if(event_type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            grabLuckyMoney(event);
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        prefs = new Prefs(this);
        Toast.makeText(this,R.string.service_connect_success, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, R.string.service_connect_interrupt, Toast.LENGTH_SHORT).show();
    }

    private void delaySomeTime() {
        while(true){
            if (System.currentTimeMillis() - m_notificationTime > m_delayTimems) {
                break;
            }
        }
    }

    private void wakeupScreen()
    {
        if(!prefs.isRegisterd()) {
            return;
        }
        if( !((PowerManager)getSystemService(POWER_SERVICE)).isScreenOn() ) {
            try{
                startActivity(new Intent(this, LightUp.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception e){
                //e.printStackTrace();
            }
        }
    }

    private void openNotify(AccessibilityEvent event) {
        if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }

        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try{
            pendingIntent.send();
        }catch (PendingIntent.CanceledException e){
          //  e.printStackTrace();
        }
    }

    private void grabLuckyMoney(AccessibilityEvent event) {
        CharSequence class_name = event.getClassName();
        //Log.d(TAG, "class_name---->" + class_name);
        if(class_name == null) {
            return;
        }

        if(class_name.equals("com.tencent.mm.ui.LauncherUI")) {
            //if(m_isLuckyNotification){
            if(m_luckyNotificationFlag ==2) {
                pressKeyToOpen();
            }
        }else if(class_name.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")){
            if(pressKeyToUnpack() == false){
                LoopToGoBack();
            }
        }
        else if(class_name.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")){
            LoopToGoBack();
        }
    }

    private void loopToOpen(){
        long curTime = System.currentTimeMillis();
        while( ! pressKeyToOpen()) {
            if (System.currentTimeMillis() - curTime > 4999) {
                //Log.d(TAG, "ui timeout---->" + curTime);
                LoopToGoBack();
                break;
            }
        }
    }

    private boolean pressKeyToOpen(){
        AccessibilityNodeInfo nodeinfo = getRootInActiveWindow();
        //Log.d(TAG, "nodeinfo---->" + nodeinfo);
        if(nodeinfo == null){
            return false;
        }
        List<AccessibilityNodeInfo> list = nodeinfo.findAccessibilityNodeInfosByText(LUCKY_MONEY_OPEN_TEXT);
        //Log.d(TAG, "list---->" + list);
        if(list.isEmpty())
        {
            return false;
        }

        String t;
        for(int i=list.size()-1; i>=0; i--) {
            AccessibilityNodeInfo info = list.get(i);
            if(info == null){
                continue;
            }
            if(info.getText() == null) {
                continue;
            }
            t = info.getText().toString();
            if(t.contains(LUCKY_MONEY_OPEN_TEXT))
            {
                if(!prefs.isRegisterd()) {
                    Toast.makeText(this,"抢钱未注册", Toast.LENGTH_LONG).show();
                }
                AccessibilityNodeInfo parent = info.getParent();
                if ((parent != null) && (parent.isClickable())) {

                    if(parent.getChild(0).getText() == null){
                        return false;
                    }
                    String humanCheckString = parent.getChild(0).getText().toString();
                    if(IsHumanCheckPackage(humanCheckString)) {
                        Toast.makeText(this, "发现过滤关键字:\n"+humanCheckString, Toast.LENGTH_SHORT).show();
                        LoopToGoBack();
                        return true;
                    }

                    delaySomeTime();
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                    //Log.d(TAG, "open---->" + list.get(i).getParent().getChildCount());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean IsHumanCheckPackage(String s) {
        if(s == null) {
            return false;
        }
        if(s.contains("测") || s.contains("机器") || s.contains("专") || s.contains("翻")) {
            return true;
        }
        return false;
    }

    private boolean pressKeyToUnpack() {
        AccessibilityNodeInfo nodeinfo = getRootInActiveWindow();
        if(nodeinfo == null) {
            return false;
        }

        /*
        List<AccessibilityNodeInfo> list = nodeinfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b2c");
        //List<AccessibilityNodeInfo> list = nodeinfo.findAccessibilityNodeInfosByText(LUCKY_MONEY_UNPACK_TEXT);
        if(list.isEmpty()) {
            if(nodeinfo.findAccessibilityNodeInfosByText(LUCKY_MONEY_GONE_TEXT).isEmpty() == false){
                BackToHome();
            }
            return false;
        }
        for(AccessibilityNodeInfo n : list){
            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }*/
        AccessibilityNodeInfo findInfo = LoopToFindButton(nodeinfo);
        if((findInfo != null) && (findInfo.isClickable())) {
            findInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            return true;
        }
        return false;
    }

    private AccessibilityNodeInfo LoopToFindButton(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
            //Log.d(TAG, "test" + info.getClassName());
            if(info.getClassName().equals("android.widget.Button")) {
                return info;
            } else {
                return null;
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if(info.getChild(i) != null){
                    AccessibilityNodeInfo findInfo = LoopToFindButton(info.getChild(i));
                    if(findInfo != null) {
                        return findInfo;
                    }
                }
            }
        }
        return null;
    }

    private void LoopToGoBack() {
        if(m_luckyNotificationFlag <= 0) {
            return;
        }
        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        m_luckyNotificationFlag --;
        int times = 20;
        while((times--)>=0) {
            SystemClock.sleep(100);
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            SystemClock.sleep(100);
            AccessibilityNodeInfo nodeinfo = getRootInActiveWindow();
            //Log.d(TAG, "nodeinfo:" + nodeinfo.toString());
            if ((nodeinfo != null) && (nodeinfo.getPackageName() != null)
                    && (nodeinfo.getPackageName().equals("com.tencent.mm") == false)) {
                m_luckyNotificationFlag = 0;
                return;
            }
        }
    }

    /*
    private void BackToHome(){
        if(m_isLuckyNotification){
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        }
    }*/
}
