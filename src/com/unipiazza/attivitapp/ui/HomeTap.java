package com.unipiazza.attivitapp.ui;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.unipiazza.attivitapp.AttivitAppRESTClient;
import com.unipiazza.attivitapp.CurrentShop;
import com.unipiazza.attivitapp.HttpCallback;
import com.unipiazza.attivitapp.JSONParser;
import com.unipiazza.attivitapp.R;

public class HomeTap extends Activity {
	// url
	private static String url_search_id = "http://attivitapp.herokuapp.com/search_id.php";
	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";
	private static final String TAG_USER = "first_name";
	private static final String TAG_USER_LASTNAME = "last_name";
	private static final String TAG_ID_USER = "id";
	private static final String TAG_PASS_TYPE = "pass";
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

		public ConnectionDetector(Context context) {
			this._context = context;
		}

		public boolean isConnectingToInternet() {
			ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo[] info = connectivity.getAllNetworkInfo();
				if (info != null)
					for (int i = 0; i < info.length; i++)
						if (info[i].getState() == NetworkInfo.State.CONNECTED) {
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
		ImageView gyroView = (ImageView) findViewById(R.id.taploop);
		gyroView.setBackgroundResource(R.drawable.loop_animation);
		AnimationDrawable gyroAnimation = (AnimationDrawable) gyroView.getBackground();
		gyroAnimation.start();
		mNfcAdapter = NfcAdapter.getDefaultAdapter(HomeTap.this);

		CurrentShop.getInstance().isAuthenticated(HomeTap.this, new HttpCallback() {

			@Override
			public void onSuccess(JsonObject result) {
				mTextView = (TextView) findViewById(R.id.textView_explanation);
				PendingIntent pendingIntent = PendingIntent.getActivity(
						HomeTap.this, 0, new Intent(HomeTap.this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
				if (mNfcAdapter == null) {
					// Stop here, we definitely need NFC
					Toast.makeText(HomeTap.this, "Questo dispositivo non supporta l'NFC", Toast.LENGTH_LONG).show();
					finish();
					return;
				}
				if (!mNfcAdapter.isEnabled()) {
					mTextView.setText("L'NFC � disabilitato, abilitalo e riavvia.");
				} else {
					mTextView.setText(R.string.explanation);
				}
				handleIntent(getIntent());
			}

			@Override
			public void onFail(JsonObject result, Throwable e) {
				Intent login = new Intent(HomeTap.this, Login.class);
				finish();
				startActivity(login);
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();

		setupForegroundDispatch(this, mNfcAdapter);
	}

	@Override
	protected void onPause() {
		/**
		 * Call this before onPause, otherwise an IllegalArgumentException is
		 * thrown as well.
		 */
		mNfcAdapter.disableForegroundDispatch(this);
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
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) ||
				NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			String type = intent.getType();
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			Log.v("value ", "TAG Preso : " + tag);
			v.vibrate(500);
			byte[] tag_id = tag.getId();
			String tag_id_string = bytesToHex(tag_id);
			Log.i("tag ID Appena preso", tag_id_string);
			SharedPreferences sp19 = PreferenceManager
					.getDefaultSharedPreferences(HomeTap.this);
			Editor edit19 = sp19.edit();
			edit19.putString("tag_id_string", tag_id_string);
			edit19.commit();
			Log.i("tag ID", tag_id_string);
			new NdefReaderTask().execute(tag);
		}

	}

	final protected static char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
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
		String[][] techList = new String[][] {};
		adapter.enableForegroundDispatch(activity, pendingIntent, null, techList);
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
				Log.v("value ", "NDEF is not supported by this Tag");
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(HomeTap.this, "Pass utente non valido", Toast.LENGTH_LONG).show();
					}
				});
				return null;
			}
			NdefMessage ndefMessage = ndef.getCachedNdefMessage();
			NdefRecord[] records = ndefMessage.getRecords();
			for (NdefRecord ndefRecord : records) {
				if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN) {
					try {
						return readText(ndefRecord);
					} catch (UnsupportedEncodingException e) {
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
				edit9.putString("hash_pass", tag);
				edit9.commit();
				SharedPreferences sp19 = PreferenceManager.getDefaultSharedPreferences(HomeTap.this);
				String tag_id_string = sp19.getString("tag_id_string", "anon");
				Log.v("value ", "tag_id_string : " + tag_id_string);
				searchId(tag_id_string);
			}
		}

		private void searchId(String hash_pass) {
			pDialog = new ProgressDialog(HomeTap.this);
			pDialog.setMessage("Sto cercando l'utente...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
			ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
			Boolean internet = cd.isConnectingToInternet();

			if (internet == true) {
				AttivitAppRESTClient.getInstance(HomeTap.this, true).getSearchUser(HomeTap.this, hash_pass, new HttpCallback() {

					@Override
					public void onSuccess(JsonObject result) {
						Intent i = new Intent(HomeTap.this, HomeActivity.class);
						startActivity(i);
						pDialog.dismiss();
						//Toast.makeText(HomeTap.this, result.toString(), Toast.LENGTH_LONG).show();
					}

					@Override
					public void onFail(JsonObject result, Throwable e) {
						pDialog.dismiss();
						if (result != null)
							Toast.makeText(HomeTap.this, result.get("msg").getAsString(), Toast.LENGTH_LONG).show();
						else if (e != null)
							Toast.makeText(HomeTap.this, e.toString(), Toast.LENGTH_LONG).show();

					}
				});
			}
		}

	}

	@Override
	public void onBackPressed() {
		//Non uscire, cane!
	}
}
