package com.unipiazza.attivitapp;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;

import com.google.gson.JsonObject;

/**
 * Created by monossido on 13/01/15.
 */
public class AttivitAppApplication extends Application {

    private final static int fivehours = 5 * 60 * 60 * 1000;
    private final static int oneHour = 1 * 60 * 60 * 1000;

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public void startPing() {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        int time;
        if (mWifi.isConnected())
            time = oneHour;
        else
            time = fivehours;

        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        2 * 1000, time, alarmIntent);
    }

    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {
            CurrentShop.getInstance().isAuthenticated(context, new HttpCallback() {
                @Override
                public void onSuccess(JsonObject result) {
                    AttivitAppRESTClient.getInstance().postPing(mContext, true, null);
                }

                @Override
                public void onFail(JsonObject result, Throwable e) {

                }
            });
        }
    }

}
