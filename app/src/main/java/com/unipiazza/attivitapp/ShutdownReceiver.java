package com.unipiazza.attivitapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by monossido on 28/01/15.
 */
public class ShutdownReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        AttivitAppRESTClient.getInstance().saveDataUsageToSend(context);
    }

}

