package com.unipiazza.attivitapp.ui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.unipiazza.attivitapp.JSONParser;
import com.unipiazza.attivitapp.R;
import com.unipiazza.attivitapp.UnipiazzaApp;

public class HomeTap extends Activity {
	// url
	private static String url_search_id = "http://attivitapp.herokuapp.com/search_id.php";
	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";
	private static final String TAG_USER = "first_name";
	private static final String TAG_USER_LASTNAME = "last_name";
	private static final String TAG_ID_USER = "id";
	private static final String TAG_COINS = "coins";
	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String TAG = "NfcDemo";
	private TextView mTextView;
	private NfcAdapter mNfcAdapter;
	// Progress Dialog
	private ProgressDialog pDialog;
	JSONParser jsonParser = new JSONParser();
	
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
	
	//Controllo Smartphone NFC
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hometap_layout);		
		if(((UnipiazzaApp)getApplication()).isLoggato()) {		
		mTextView = (TextView) findViewById(R.id.textView_explanation);
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			// Stop here, we definitely need NFC
			Toast.makeText(this, "Questo dispositivo non supporta l'NFC", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		if (!mNfcAdapter.isEnabled()) {
			mTextView.setText("L'NFC ï¿½ disabilitato, abilitalo e riavvia.");
		} else {
			mTextView.setText(R.string.explanation);
		}
		handleIntent(getIntent());
		} else {
			Intent login = new Intent(this, Login.class);
			finish();
			startActivity(login);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		setupForegroundDispatch(this, mNfcAdapter);
		setContentView(R.layout.hometap_layout);
	}
	@Override
	protected void onPause() {
		/**
		 * Call this before onPause, otherwise an IllegalArgumentException is
		 * thrown as well.
		 */
		stopForegroundDispatch(this, mNfcAdapter);
		super.onPause();
	}
	@Override
	protected void onNewIntent(Intent intent) {
		/**
		 * This method gets called, when a new Intent gets associated with the
		 * current activity instance. Instead of creating a new activity,
		 * onNewIntent will be called. For more information have a look at the
		 * documentation.
		 * 
		 * In our case this method gets called, when the user attaches a Tag to
		 * the device.
		 */
		handleIntent(intent);
	}
	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			String type = intent.getType();
			if (MIME_TEXT_PLAIN.equals(type)) {
				Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);	
				Log.v("value ", "TAG Preso : " + tag);
				byte[] tag_id = tag.getId();
				String tag_id_string = bytesToHex(tag_id);
				SharedPreferences sp19 = PreferenceManager
						.getDefaultSharedPreferences(HomeTap.this);
				Editor edit19 = sp19.edit();
				edit19.putString("tag_id_string", tag_id_string);
				edit19.commit();
				Log.i("tag ID", tag_id_string);
				new NdefReaderTask().execute(tag);
				
			} else {
				Log.d(TAG, "Wrong mime type: " + type);
			}
		} 
		
	}
    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    public static String bytesToHex(byte[] bytes) {
    	char[] hexChars = new char[bytes.length * 2];
    	int v;
    	for ( int j = 0; j < bytes.length; j++ ) {
    		v = bytes[j] & 0xFF;
    		hexChars[j * 2] = hexArray[v >>> 4];
    		hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    	}
    	return new String(hexChars);
    }
	/**
	 * @param activity
	 *            The corresponding {@link Activity} requesting the foreground
	 *            dispatch.
	 * @param adapter
	 *            The {@link NfcAdapter} used for the foreground dispatch.
	 */
	public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
		final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
		IntentFilter[] filters = new IntentFilter[1];
		String[][] techList = new String[][] {};
		// Notice that this is the same filter as in our manifest.
		filters[0] = new IntentFilter();
		filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
		filters[0].addCategory(Intent.CATEGORY_DEFAULT);
		try {
			filters[0].addDataType(MIME_TEXT_PLAIN);
		} 
		catch (MalformedMimeTypeException e) {
			throw new RuntimeException("Check your mime type.");
		}
		adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
	}

	/**
	 * @param activity
	 *            The corresponding {@link BaseActivity} requesting to stop the
	 *            foreground dispatch.
	 * @param adapter
	 *            The {@link NfcAdapter} used for the foreground dispatch.
	 */
	public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
		adapter.disableForegroundDispatch(activity);
	}

	private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
		@Override
		protected String doInBackground(Tag... params) {
			Tag tag = params[0];
			Ndef ndef = Ndef.get(tag);
			if (ndef == null) {
				// NDEF is not supported by this Tag.
				return null;
			}
			NdefMessage ndefMessage = ndef.getCachedNdefMessage();
			NdefRecord[] records = ndefMessage.getRecords();
			for (NdefRecord ndefRecord : records) {
				if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
					try {
						return readText(ndefRecord);
					} 
					catch (UnsupportedEncodingException e) {
						Log.e(TAG, "Unsupported Encoding", e);
					}
				}
			}
			return null;
		}

		private String readText(NdefRecord record) throws UnsupportedEncodingException {
			/*
			 * See NFC forum specification for "Text Record Type Definition" at
			 * 3.2.1
			 * 
			 * http://www.nfc-forum.org/specs/
			 * 
			 * bit_7 defines encoding bit_6 reserved for future use, must be 0
			 * bit_5..0 length of IANA language code
			 */
			byte[] payload = record.getPayload();
			// Get the Text Encoding
			String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
			// Get the Language Code
			int languageCodeLength = payload[0] & 0063;
			// String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
			// e.g. "en"
			// Get the Text
			return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
		}

		//Evento dopo il TAP corretto
		@Override
		protected void onPostExecute(final String tag) {
			if (tag != null) {
				String[] res_array = new String[] { tag };
				Log.v("value ", "res_array : " + res_array);
				Log.v("value ", "tag_id_string : " + tag);
				SharedPreferences sp9 = PreferenceManager.getDefaultSharedPreferences(HomeTap.this);
				Editor edit9 = sp9.edit();
				edit9.putString("hash_tessera", tag);
				edit9.commit();
				SharedPreferences sp19 = PreferenceManager.getDefaultSharedPreferences(HomeTap.this);
				String tag_id_string = sp19.getString("tag_id_string", "anon");
				Log.v("value ", "tag_id_string : " + tag_id_string);
				new SearchId().execute(tag_id_string);
				
			}
		}
		//JSON Ricerca ID
		class SearchId extends AsyncTask<String, String, String> {
			/**
			 * Before starting background thread Show Progress Dialog
			 * */
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				pDialog = new ProgressDialog(HomeTap.this);
				pDialog.setMessage("Sto cercando l'utente...");
				pDialog.setIndeterminate(false);
				pDialog.setCancelable(true);
				pDialog.show();
			}

			/**
			 * Ricerca ID
			 * */
			protected String doInBackground(String... res_array) {
				
				//Controllo connessione ad Internet
				ConnectionDetector cd = new ConnectionDetector(getApplicationContext()); 
				Boolean internet = cd.isConnectingToInternet(); 
				
				if (internet==true){
					try {
						SharedPreferences sp19 = PreferenceManager.getDefaultSharedPreferences(HomeTap.this);
						String tag_id_string = sp19.getString("tag_id_string", "anon");
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						SharedPreferences sp_id_attivita = PreferenceManager.getDefaultSharedPreferences(HomeTap.this);
						String id_attivita = sp_id_attivita.getString("id_attivita", "a2");
						Log.v("value ", "Tag_Id_String da inviare : " + tag_id_string);
						Log.v("value ", "Id_attivita da inviare : " + id_attivita);
						params.add(new BasicNameValuePair("hash_tessera", tag_id_string));
						params.add(new BasicNameValuePair("shop_id", id_attivita));
						JSONObject json = jsonParser.makeHttpRequest(url_search_id,"POST", params);
						Log.d("Login attempt", json.toString());

						int success = json.getInt(TAG_SUCCESS);
						if (success == 1) {
							Log.d("Sync avvenuta", json.toString());
							Log.d("Login Avvenuto!", json.getString(TAG_MESSAGE));
							String user = json.getString(TAG_USER);
							String user_lastname = json.getString(TAG_USER_LASTNAME);							
							String id = json.getString(TAG_ID_USER);
							String coins = json.getString(TAG_COINS);
							Log.d("Utente Trovato, NOME --> ",user);
							Log.d("Utente Trovato, COGNOME --> ",user_lastname);
							Log.d("Id Trovato --> ",id);
							Log.d("Gettoni utente --> ",coins);
							SharedPreferences sp2 = PreferenceManager.getDefaultSharedPreferences(HomeTap.this);
							Editor edit = sp2.edit();
							edit.putString("user", user);
							edit.commit(); 
							SharedPreferences sp27 = PreferenceManager.getDefaultSharedPreferences(HomeTap.this);
							Editor edit_lastname = sp27.edit();
							edit_lastname.putString("user_lastname", user_lastname);
							edit_lastname.commit(); 
							SharedPreferences sp11 = PreferenceManager.getDefaultSharedPreferences(HomeTap.this);
							Editor edit_id = sp11.edit();
							edit_id.putString("id", id);
							edit_id.commit(); 
							SharedPreferences sp20 = PreferenceManager.getDefaultSharedPreferences(HomeTap.this);
							Editor edit_coins = sp20.edit();
							edit_coins.putString("coins", coins);
							edit_coins.commit(); 						
							Log.d("Utente Trovato --> ", user);
							Intent i = new Intent(HomeTap.this, HomeActivity.class);
							startActivity(i);
							return json.getString(TAG_MESSAGE);
						} 
						else {
							Log.d("Login Failure!", json.getString(TAG_MESSAGE));
							return json.getString(TAG_MESSAGE);
						}
					}
					catch (JSONException e) {
						e.printStackTrace();
					}
				}
				Log.d("value", "Internet non attivo. Valore internet : " + internet);
				SharedPreferences sp_internet = PreferenceManager.getDefaultSharedPreferences(HomeTap.this);
				Editor edit19 = sp_internet.edit();
				edit19.putBoolean("internet", internet);
				edit19.commit();
				return null;
			}
			/**
			 * After completing background task Dismiss the progress dialog
			 * **/
			protected void onPostExecute(String file_url) {
				// dismiss the dialog once done
				pDialog.dismiss();
				if (file_url != null) {
					//Messaggio che compare sempre
					Toast.makeText(HomeTap.this, file_url, Toast.LENGTH_LONG).show();
				}
				
				//Controllo connessione ad Internet
				ConnectionDetector cd = new ConnectionDetector(getApplicationContext()); 
				Boolean internet = cd.isConnectingToInternet(); 
				
				if (internet==false)
					Toast.makeText(HomeTap.this, "Non c'è connessione ad internet =(\r\nRiprova fra qualche minuto!", Toast.LENGTH_SHORT).show();
			}

		}
	}
	@Override
	public void onBackPressed() {
		//Non uscire, cane!
	}
}
