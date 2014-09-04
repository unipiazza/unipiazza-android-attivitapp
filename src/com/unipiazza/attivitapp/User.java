package com.unipiazza.attivitapp;

import android.util.Log;

public class User {

	protected String first_name;
	protected String last_name;
	protected String image_url;
	protected int id;

	public User(int id, String first_name, String last_name, String image_url) {
		this.id = id;
		this.first_name = first_name;
		this.last_name = last_name;
		this.image_url = image_url;
	}

	public User() {
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getLast_name() {
		Log.v("UNIPIAZZA", "getLast_name last_name=" + last_name);
		return last_name;
	}

	public void setLast_name(String second_name) {
		this.last_name = second_name;
	}

	public String getImage_url() {
		return image_url;
	}

	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
