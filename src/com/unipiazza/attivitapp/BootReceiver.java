package com.unipiazza.attivitapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.unipiazza.attivitapp.ui.Login;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		Intent intent = new Intent(context, Login.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

}
