package com.unipiazza.attivitapp;

import android.content.Context;
import android.util.Log;

public class CurrentUser extends User {

	private static CurrentUser instance;

	public static CurrentUser getInstance() {
		Log.v("UNIPIAZZA", "instance=" + instance);
		if (instance == null) {
			instance = new CurrentUser();
			return instance;
		} else
			return instance;
	}

	private String pass;
	private int unipoints;
	private boolean gender;

	public CurrentUser(int id, String first_name, String last_name, String image_url) {
		super(id, first_name, last_name, image_url);
	}

	public CurrentUser() {
		super();
	}

	public void setUser(Context context, String email, String first_name, String last_name, String image_url
			, int unipoints, int id, String pass, boolean gender) {
		this.id = id;
		this.first_name = first_name;
		Log.v("UNIPIAZZA", "last_name=" + last_name);
		this.last_name = last_name;
		this.image_url = image_url;
		this.pass = pass;
		this.unipoints = unipoints;
		this.gender = gender;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public int getTotal_coins() {
		return unipoints;
	}

	public void setTotal_coins(int total_coins) {
		this.unipoints = total_coins;
	}

	public boolean isGender() {
		return gender;
	}

	public void setGender(boolean gender) {
		this.gender = gender;
	}
}
