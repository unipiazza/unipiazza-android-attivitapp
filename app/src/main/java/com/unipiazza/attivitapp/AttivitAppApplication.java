package com.unipiazza.attivitapp;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.google.gson.JsonObject;

/**
 * Created by monossido on 13/01/15.
 */
public class AttivitAppApplication extends Application {

    private Handler handler = new Handler();
    private final static int fivehours = 5 * 60 * 60 * 1000;
    private final static int oneHour = 1 * 60 * 60 * 1000;

    public void startPing() {
        handler.postDelayed(runnable, 2 * 1000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            CurrentShop.getInstance().isAuthenticated(AttivitAppApplication.this, new HttpCallback() {
                @Override
                public void onSuccess(JsonObject result) {
                    AttivitAppRESTClient.getInstance(AttivitAppApplication.this).postPing(AttivitAppApplication.this, true, null);
                }

                @Override
                public void onFail(JsonObject result, Throwable e) {

                }
            });
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected()) {
                handler.postDelayed(this, oneHour);
            } else
                handler.postDelayed(this, fivehours);
        }
    };

}
