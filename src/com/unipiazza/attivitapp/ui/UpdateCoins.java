package com.unipiazza.attivitapp.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.unipiazza.attivitapp.JSONParser;
import com.unipiazza.attivitapp.R;

public class UpdateCoins extends Activity {
	JSONParser jsonParser = new JSONParser();
	private ProgressDialog pDialog;
	private Button btnOk;
	private NfcAdapter mNfcAdapter;
	private PendingIntent pendingIntent;
	private String[][] techListsArray;
	// url per la ricerca dell'ID
	private static final String URL_BALANCE = "http://attivitapp.herokuapp.com/update_balance.php";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";
	private static final String TAG_SALDO = "saldo";
	private static final String TAG_COINS = "coins_inseriti";
	static final String errore = "Errore.";
	public static final String MIME_TEXT_PLAIN = "text/plain";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Get the message from the intent
		Intent i = getIntent();
		String gift_value = (String) i.getSerializableExtra("gift_value");
		Log.v("value ", "E' un regalo? " + gift_value);
		SharedPreferences sp4 = PreferenceManager.getDefaultSharedPreferences(UpdateCoins.this);
		String saldo = sp4.getString("saldo", "anon");
		Log.v("value ", "Saldo = " + saldo);
		super.onCreate(savedInstanceState);

		// Creo Layout in base al valore di Gift
		if (gift_value.equals("yes"))
			setContentView(R.layout.update_prize_layout);
		else
			setContentView(R.layout.update_layout);

		new UpdateBalance().execute();
		// Buttoni
		btnOk = (Button) findViewById(R.id.btnOk);

		// Listener del bottone Ok:
		btnOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Lancia l'activity per aggiungere prodotti
				Intent i = new Intent(getApplicationContext(), HomeTap.class);
				startActivity(i);
			}
		});

		prepareNFCIntercept();
	}

	public String updateJSONdata() {
		SharedPreferences sp2 = PreferenceManager.getDefaultSharedPreferences(UpdateCoins.this);
		String user = sp2.getString("user", "anon");
		SharedPreferences sppass = PreferenceManager.getDefaultSharedPreferences(UpdateCoins.this);
		String pass = sppass.getString("pass", "anon");
		SharedPreferences sp3 = PreferenceManager.getDefaultSharedPreferences(UpdateCoins.this);
		String saldo = sp3.getString("saldo", "anon");
		SharedPreferences sp11 = PreferenceManager.getDefaultSharedPreferences(UpdateCoins.this);
		String id = sp11.getString("id", "anon");
		SharedPreferences sp6 = PreferenceManager.getDefaultSharedPreferences(UpdateCoins.this);
		String id_attivita = sp6.getString("id_attivita", "anon");
		SharedPreferences sp4 = PreferenceManager.getDefaultSharedPreferences(UpdateCoins.this);
		String user_lastname = sp4.getString("user_lastname", "a2");
		user_lastname = (user_lastname.substring(0, 1));
		final TextView text_name = (TextView) findViewById(R.id.nicknameview);
		final TextView last_name_view = (TextView) findViewById(R.id.lastnameview);
		text_name.setText(user);
		last_name_view.setText(user_lastname);
		Intent i = getIntent();
		String gift_value = i.getStringExtra("gift_value");
		String gift_id = i.getStringExtra("gift_id");
		Log.v("value ", "E' un prodotto regalo? " + gift_value);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("id_utente", id));
		params.add(new BasicNameValuePair("id_attivita", id_attivita));
		params.add(new BasicNameValuePair("saldo", saldo));
		params.add(new BasicNameValuePair("pass", pass));
		params.add(new BasicNameValuePair("gift", gift_value));
		params.add(new BasicNameValuePair("gift_id", gift_id));
		Log.v("value ", "Gift Id da passare: " + gift_id);
		int success = 0;
		Log.v("value ", "Il saldo di " + user + " con id_utente = " + id + " nell'attivita con ID " + id_attivita + " aggiornato di " + saldo + " | Il gift � " + gift_value + " | Id prodotto = " + gift_id + "Tipo PASS = " + pass);
		try {
			JSONObject json = jsonParser.makeHttpRequest(URL_BALANCE, "POST", params);
			Log.v("value ", "Success First: " + success);
			Log.v("Messaggio: ", json.getString(TAG_MESSAGE));
			success = json.getInt(TAG_SUCCESS);
			Log.d("value ", "Success Later: " + success);
			if (success == 1) {
				String saldo_aggiornato = json.getString(TAG_SALDO);
				Log.d("Saldo Aggiornato!", saldo_aggiornato);
				SharedPreferences sp21 = PreferenceManager
						.getDefaultSharedPreferences(UpdateCoins.this);
				Editor edit = sp21.edit();
				edit.putString("saldo_aggiornato", saldo_aggiornato);
				edit.commit();
			}
			else if (success == 0) {
				setContentView(R.layout.update_fail_layout);
				btnOk = (Button) findViewById(R.id.btnOk);

				// Listener del bottone Ok:
				btnOk.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						// Lancia l'activity per aggiungere prodotti
						Intent i = new Intent(getApplicationContext(), HomeTap.class);
						startActivity(i);
					}
				});
			}
			else {
				Log.v("value ", "Errore ");
				Intent i_error = new Intent(UpdateCoins.this, HomeTap.class);
				finish();
				startActivity(i_error);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return errore;
	}

	public class UpdateBalance extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(UpdateCoins.this);
			pDialog.setMessage("Aggiornamento Saldo...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			updateJSONdata();
			return null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			SharedPreferences sp21 = PreferenceManager.getDefaultSharedPreferences(UpdateCoins.this);
			String saldo_aggiornato = sp21.getString("saldo_aggiornato", "anon");
			final TextView text_saldo = (TextView) findViewById(R.id.saldoview);
			text_saldo.setText(saldo_aggiornato);
			Log.v("value ", "Saldo_aggiornato  = " + saldo_aggiornato);
			super.onPostExecute(result);
			pDialog.dismiss();
		}
	}

	public void onBackPressed() {
		//Non uscire, cane!
	}

	public void onPause() {
		super.onPause();
		mNfcAdapter.disableForegroundDispatch(this);
	}

	public void onResume() {
		super.onResume();
		mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, techListsArray);
	}

	public void onNewIntent(Intent intent) {
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		//do nothing
		Log.v("UNIPIAZZA", "onNewIntent");
	}

	private void prepareNFCIntercept() {
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(
				this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		techListsArray = new String[][] {};
	}

}
