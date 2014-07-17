package com.example.unipiazza;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class AddProducts extends Activity {
	
    // url to create new product
    private static String url_create_product = "http://api.androidhive.info/android_connect/create_product.php";
 
    // JSON Node names
    private static final String TAG_SUCCESS = "success";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_products);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

}
