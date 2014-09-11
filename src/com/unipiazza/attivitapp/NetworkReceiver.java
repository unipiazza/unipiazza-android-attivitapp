package com.unipiazza.attivitapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.unipiazza.attivitapp.ui.Login;

public class NetworkReceiver extends BroadcastReceiver {

	public static boolean first = true;

	@Override
	public void onReceive(Context context, Intent arg1) {
		if (!arg1.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false) && first) {
			first = false;
			Intent intent = new Intent(context, Login.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}
}
