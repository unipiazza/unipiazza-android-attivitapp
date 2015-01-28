package com.unipiazza.attivitapp;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import com.google.gson.JsonObject;

/**
 * Created by monossido on 13/01/15.
 */
public class AttivitAppApplication extends Application {

    private Handler handler = new Handler();
    private final static int fivehours = 5 * 60 * 60 * 1000;

    public void startPing() {
        handler.postDelayed(runnable, 2 * 1000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            CurrentShop.getInstance().isAuthenticated(AttivitAppApplication.this, new HttpCallback() {
                @Override
                public void onSuccess(JsonObject result) {
                    Log.v("UNIPIAZZA", "runnable");
                    AttivitAppRESTClient.getInstance(AttivitAppApplication.this).postPing(AttivitAppApplication.this, true, null);
                }

                @Override
                public void onFail(JsonObject result, Throwable e) {

                }
            });
            handler.postDelayed(this, fivehours);
        }
    };

}
