package com.unipiazza.attivitapp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.unipiazza.attivitapp.JSONParser;
import com.unipiazza.attivitapp.R;

public class HomeActivity extends Activity {
	JSONParser jParser = new JSONParser();
	// url per la ricerca dell'ID
	ImageButton btnAddProduct;
	ImageButton btnGift;
	public static final String MIME_TEXT_PLAIN = "text/plain";
	
	// Classe di controllo connessione
	public class ConnectionDetector {
		 private Context _context;
		 public ConnectionDetector(Context context){
		     this._context = context;
		 }
		 public boolean isConnectingToInternet(){
		 	ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
		       if (connectivity != null){
		           NetworkInfo[] info = connectivity.getAllNetworkInfo();
		           if (info != null) 
		               for (int i = 0; i < info.length; i++) 
		                   if (info[i].getState() == NetworkInfo.State.CONNECTED){
		                       return true;
		                   }
		       }
		       return false;
		 }
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
		String user = sp.getString("user", "a2");
		SharedPreferences sp2 = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
		String user_lastname = sp2.getString("user_lastname", "a2");
		user_lastname=(user_lastname.substring(0,1));
		SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
		String coins = sp1.getString("coins", "a2");		

		// Buttoni
		ImageButton btnAddProduct = (ImageButton) findViewById(R.id.add_coin);
		btnAddProduct.setBackground(null);
		ImageButton btnGift = (ImageButton) findViewById(R.id.gift_list);
		btnGift.setBackground(null);
		final TextView text_name = (TextView) findViewById(R.id.nicknameview);
		final TextView last_name_view = (TextView) findViewById(R.id.lastnameview);
		final TextView view_saldo = (TextView) findViewById(R.id.saldoview);
		text_name.setText(user);
		last_name_view.setText(user_lastname);
		view_saldo.setText(coins);
		Log.d("Utente Trovato, NOME --> ",user);
		Log.d("Utente Trovato, COGNOME --> ",user_lastname);

		btnAddProduct.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				
				//Controllo connessione ad Internet
				ConnectionDetector cd = new ConnectionDetector(getApplicationContext()); 
				Boolean internet = cd.isConnectingToInternet(); 
				
				if (internet==false)
					Toast.makeText(HomeActivity.this, "Non c'è connessione ad internet =(\r\nRiprova fra qualche minuto!", Toast.LENGTH_SHORT).show();
				else{
					Intent i = new Intent(getApplicationContext(), AddCoins.class);
					startActivity(i);
				}
		    }
		});
		btnGift.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				
				//Controllo connessione ad Internet
				ConnectionDetector cd = new ConnectionDetector(getApplicationContext()); 
				Boolean internet = cd.isConnectingToInternet(); 
				
				if (internet==false)
					Toast.makeText(HomeActivity.this, "Non c'è connessione ad internet =(\r\nRiprova fra qualche minuto!", Toast.LENGTH_SHORT).show();
				else{
					Intent i = new Intent(getApplicationContext(), GiftActivity.class);
					startActivity(i);
				}
			}
		});
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
			return null;
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
