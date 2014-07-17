package com.example.unipiazza;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HomeActivity extends Activity {
	JSONParser jParser = new JSONParser();
    private TextView mTextView;
	 // url per la ricerca dell'ID
    private static String url_search_id = "http://www.icmevolution.com/Unipiazza/search_id.php";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		 // Get the message from the intent
	    Intent intent = getIntent();
		Button btnAddProduct;
		Button btnGift;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		 // Buttoni
		btnAddProduct = (Button) findViewById(R.id.btnAddProduct);
		btnGift = (Button) findViewById(R.id.btnGift);
        
        // btnaddProduct click event
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View view) {
                // Lancia l'activity per aggiungere prodotti
                Intent i = new Intent(getApplicationContext(), AddProducts.class);
                startActivity(i);
 
            }
        });
        btnGift.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View view) {
        		// Lancia l'activity per regalare prodotti
        		Intent i = new Intent(getApplicationContext(), GiftActivity.class);
        		startActivity(i);
        	}
    	});
 	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	 /**
     * Background Async Task per cercare l'ID facendo una chiamata HTTP
     * */
	class LoadAllProducts extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            JSONObject json = jParser.makeHttpRequest(url_search_id, "GET", params);
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
