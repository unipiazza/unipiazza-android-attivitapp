package com.unipiazza.attivitapp.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.unipiazza.attivitapp.JSONParser;
import com.unipiazza.attivitapp.R;
import com.unipiazza.attivitapp.UnipiazzaApp;

public class Login extends Activity implements OnClickListener {

	private EditText user, pass;
	private Button mSubmit;
	private ProgressDialog pDialog;
	JSONParser jsonParser = new JSONParser();
	private static final String LOGIN_URL = "http://attivitapp.herokuapp.com/login.php";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";
	private static final String TAG_ID_ATTIVITA = "id_attivita";
	public static final String MIME_TEXT_PLAIN = "text/plain";
	private NfcAdapter mNfcAdapter;
	private SharedPreferences sp;
	private String username;
	private String password;

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
						if (info[i].getState() == NetworkInfo.State.CONNECTED)
							return true;
			}
			return false;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		sp = PreferenceManager.getDefaultSharedPreferences(Login.this);
		username = sp.getString("username", "");
		password = sp.getString("password", "");
		String id_attivita = sp.getString("id_attivita", "");

		if (!username.isEmpty() && !id_attivita.isEmpty()) {
			new AttemptLogin().execute();
		}

		user = (EditText) findViewById(R.id.username);
		pass = (EditText) findViewById(R.id.password);
		pass.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					username = user.getText().toString();
					password = pass.getText().toString();
					new AttemptLogin().execute();
				}
				return false;
			}
		});
		mSubmit = (Button) findViewById(R.id.login);
		mSubmit.setOnClickListener(this);
	}

	public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
		final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
		IntentFilter[] filters = new IntentFilter[1];
		String[][] techList = new String[][] {};
		filters[0] = new IntentFilter();
		filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
		filters[0].addCategory(Intent.CATEGORY_DEFAULT);
		try {
			filters[0].addDataType(MIME_TEXT_PLAIN);
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("Check your mime type.");
		}
		adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
	}

	public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
		adapter.disableForegroundDispatch(activity);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setupForegroundDispatch(this, mNfcAdapter);
	}

	@Override
	protected void onPause() {
		stopForegroundDispatch(this, mNfcAdapter);
		super.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login:
			username = user.getText().toString();
			password = pass.getText().toString();
			new AttemptLogin().execute();
			break;
		default:
			break;
		}
	}

	class AttemptLogin extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(Login.this);
			pDialog.setMessage("Login in corso...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			// Check for success tag
			int success;

			//Controllo connessione ad Internet
			ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
			Boolean internet = cd.isConnectingToInternet();

			Log.d("value", "Internet:  " + internet);
			Log.d("request!", "starting");
			if (internet == true) {
				//Internet presente quindi:
				try {
					// Building Parameters
					Log.v("UNIPIAZZA", "username=" + username + " password=" + password);
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("username", username));
					params.add(new BasicNameValuePair("password", password));
					// getting product details by making HTTP request
					JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
					// check your log for json response
					Log.d("Login attempt", json.toString());
					// json success tag
					success = json.getInt(TAG_SUCCESS);
					Log.d("value", "Valore success:  " + success);
					if (success == 1) {
						Log.d("Login avvenuto con successo!", json.toString());
						// save user data
						Editor edit = sp.edit();
						edit.putString("username", username);
						edit.putString("password", password);
						String id_attivita = json.getString(TAG_ID_ATTIVITA);
						edit.putString("id_attivita", id_attivita);
						edit.commit();
						Intent i = new Intent(Login.this, HomeTap.class);
						((UnipiazzaApp) getApplication()).setLoggato(true);
						Log.d("value", "Login attivit� " + username + " con id_attivita = " + id_attivita + " avvenuto con successo!!");
						finish();
						startActivity(i);
						return json.getString(TAG_MESSAGE);
					} else {
						Log.d("Login Failure!", json.getString(TAG_MESSAGE));
						return json.getString(TAG_MESSAGE);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			//Se arrivito qua, vuol dire che internet non � attivo
			Log.d("value", "Internet non attivo. Valore internet : " + internet);
			return null;

		}

		protected void onPostExecute(String file_url) {
			// dismiss the dialog once product deleted
			pDialog.dismiss();
			if (file_url != null) {
				Toast.makeText(Login.this, file_url, Toast.LENGTH_LONG).show();
			}

			//Controllo connessione ad Internet
			ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
			Boolean internet = cd.isConnectingToInternet();

			if (internet == false)
				Toast.makeText(Login.this, "Non c'� connessione ad internet =(\r\nRiprova fra qualche minuto!", Toast.LENGTH_SHORT).show();
		}

	}

	//Classe per aggiornamento App
	private class DownloadTask extends AsyncTask<String, Integer, String> {
		private Context context;
		private PowerManager.WakeLock mWakeLock;
		private ProgressDialog mProgressDialog;

		public DownloadTask(Context context) {
			this.context = context;
		}

		@Override
		protected String doInBackground(String... sUrl) {
			InputStream input = null;
			OutputStream output = null;
			HttpURLConnection connection = null;
			try {
				URL url = new URL(sUrl[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();
				// expect HTTP 200 OK, so we don't mistakenly save error report
				// instead of the file
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
				}
				// this will be useful to display download percentage
				// might be -1: server did not report the length
				int fileLength = connection.getContentLength();
				// download the file
				input = connection.getInputStream();
				output = new FileOutputStream(getExternalCacheDir() + "/Unipiazza.apk");
				byte data[] = new byte[4096];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					// allow canceling with back button
					if (isCancelled()) {
						input.close();
						return null;
					}
					total += count;
					// publishing the progress....
					if (fileLength > 0) // only if total length is known
						publishProgress((int) (total * 100 / fileLength));
					output.write(data, 0, count);
				}
			} catch (Exception e) {
				return e.toString();
			} finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();
				} catch (IOException ignored) {
				}

				if (connection != null)
					connection.disconnect();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// take CPU lock to prevent CPU from going off if the user 
			// presses the power button during download
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
			mWakeLock.acquire();

			// instantiate it within the onCreate method
			mProgressDialog = new ProgressDialog(Login.this);
			mProgressDialog.setMessage("A message");
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(true);
			mProgressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			// if we get here, length is known, now set indeterminate to false
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setMax(100);
			mProgressDialog.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			mWakeLock.release();
			mProgressDialog.dismiss();
			if (result != null)
				Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
			else {
				Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
				updateApp();
			}
		}
	}

	private void updateApp() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(getExternalCacheDir() + "/Unipiazza.apk")), "application/vnd.android.package-archive");
		startActivity(intent);
	}

}
