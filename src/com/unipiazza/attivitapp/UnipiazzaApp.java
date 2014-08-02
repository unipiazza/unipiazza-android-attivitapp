package com.unipiazza.attivitapp;

import android.app.Application;

public class UnipiazzaApp extends Application {
	
	private boolean loggato = false;
	
	public void setLoggato(boolean log) {
		loggato=log;
	}
	
	public boolean isLoggato() {
		return loggato;
	}

}
