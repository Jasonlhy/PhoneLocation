package com.example.nthucs.phonelocation;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.telephony.TelephonyManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import android.util.Log;

/**
 * Schedule outgoing and incoming call. It is assumed that the activity already have the permission
 *
 * Created by Jason on 6/21/16.
 */
public class ScheduleCallManager {
    private Object endCallManager;
    private Method endCallMethod;
    private String phoneNumber;
    private Activity activity;

    /**
     * Get the underlying endCallManager by Java reflection because it is an internal class inside telephonyManager....
     *
     * @return True if success, False if failed
     */
    public boolean initialize(TelephonyManager telephonyManager, Activity context) {
        try {
            Method getTelephonyManager = telephonyManager.getClass().getDeclaredMethod("getITelephony");
            getTelephonyManager.setAccessible(true);
            Object endCallManager = getTelephonyManager.invoke(telephonyManager);
            Method endCallMethod = endCallManager.getClass().getDeclaredMethod("endCall");

            this.endCallManager = endCallManager;
            this.endCallMethod = endCallMethod;
            this.activity = context;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex ){
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * End the phone call
     * @return True on Success, False on Failed
     */
    public boolean endCall(){
        try {
            endCallMethod.invoke(endCallManager);
            Log.d("EndCall", "invoked end call manager");
        } catch (InvocationTargetException | IllegalAccessException ex ){
            Log.d("EndCall", "failed to end call");
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * End call after delay second
     *
     * @param delay Second to be delayed
     * @return
     */
    public void endCall(long delay){
        Timer timer = new Timer(true);
        timer.schedule(new EndCallTask(), delay);
    }

    class EndCallTask extends TimerTask {
        public void run() {
            ScheduleCallManager.this.endCall();
        }
    }

    public void makeCall(){
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        try {
            activity.startActivity(intent);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }

    public void makeCall(long delay){
        Timer timer = new Timer(true);
        timer.schedule(new MakeCallTask(), delay);
    }

    class MakeCallTask extends TimerTask {
        public void run() {
            ScheduleCallManager.this.makeCall();
        }
    }

}
