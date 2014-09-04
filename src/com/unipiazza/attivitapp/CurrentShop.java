package com.unipiazza.attivitapp;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class CurrentShop extends User {

	private static CurrentShop instance;

	public static CurrentShop getInstance() {
		if (instance == null) {
			instance = new CurrentShop();
			return instance;
		} else
			return instance;
	}

	private ArrayList<Prize> prizes;

	public CurrentShop(int id, String first_name, String last_name, String image_url) {
		super(id, first_name, last_name, image_url);
	}

	public CurrentShop() {
		super();
	}

	public void isAuthenticated(Context context, HttpCallback callback) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String access_token = pref.getString("access_token", "");
		String email = pref.getString("email", "");
		String refresh_token = pref.getString("refresh_token", "");

		if (!access_token.isEmpty() && !email.isEmpty() && !refresh_token.isEmpty()) {
			AttivitAppRESTClient.getInstance(context, true).refreshToken(context, refresh_token, callback);
		} else
			callback.onFail(null, null);
	}

	public void checkToken(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		int expires_in = pref.getInt("expires_in", 0);
		long token_date = pref.getLong("token_date", 0);
		if (System.currentTimeMillis() - token_date >= expires_in) {
			String refresh_token = pref.getString("refresh_token", "");
			AttivitAppRESTClient.getInstance(context, false).refreshToken(context, refresh_token, null);
		}
	}

	public void setAuthenticated(Context context, String email, String first_name, String access_token
			, String refresh_token, int expires_in, int id, String password, ArrayList<Prize> prizes) {
		this.id = id;
		this.first_name = first_name;
		this.last_name = last_name;
		this.image_url = image_url;
		this.prizes = prizes;

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = pref.edit();

		edit.putString("access_token", access_token);
		edit.putString("email", email);
		edit.putString("password", password);
		edit.putString("refresh_token", refresh_token);
		edit.putInt("expires_in", expires_in);
		edit.putInt("id_attivita", id);
		edit.commit();
	}

	public void setToken(Context context, String access_token, String refresh_token, int expires_in) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = pref.edit();
		edit.putString("access_token", access_token);
		edit.putString("refresh_token", refresh_token);
		edit.putInt("expires_in", expires_in);
		edit.putLong("token_date", System.currentTimeMillis());
		edit.commit();
	}

	public String getAccessToken(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getString("access_token", "");
	}

	public void logout(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = pref.edit();

		edit.clear();
		edit.commit();

		File dir = context.getExternalCacheDir();

		File file = new File(dir.getAbsolutePath() + "/me.jpg");
		if (file.exists())
			file.delete();
	}

	public ArrayList<Prize> getPrizes() {
		return prizes;
	}

	public void setPrizes(ArrayList<Prize> prizes) {
		this.prizes = prizes;
	}
}
