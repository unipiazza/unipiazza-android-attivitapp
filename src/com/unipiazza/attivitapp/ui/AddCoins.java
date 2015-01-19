package com.unipiazza.attivitapp.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.unipiazza.attivitapp.JSONParser;
import com.unipiazza.attivitapp.R;

public class AddCoins extends Activity implements OnClickListener {

	// Progress Dialog
	JSONParser jsonParser = new JSONParser();
	TextView totResult;
	Button canc_products;
	Button send_products;
	ImageButton btnBack;
	// JSON IDS:
	public static final String TAG_NAME = "nome";
	public static final String TAG_PRICE = "gettoni";
	public static final String TAG_CATEGORY = "categoria";
	public ExpandableListView gv;
	protected HashMap<String, Integer> categoryBadged;
	int prodottiTotali = 0;
	private TextView txtResult; // Reference to EditText of result
	private int result = 0; // Result of computation
	private String inStr = "0"; // Current input string
	private NfcAdapter mNfcAdapter;
	private PendingIntent pendingIntent;
	private String[][] techListsArray;
	private Button comma;
	public static final String MIME_TEXT_PLAIN = "text/plain";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_coins);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		send_products = (Button) findViewById(R.id.send_products);
		send_products.setOnClickListener(this);
		// Retrieve a reference to the EditText field for displaying the result.
		txtResult = (TextView) findViewById(R.id.txtResultId);
		txtResult.setText("0");

		// Register listener (this class) for all the buttons
		BtnListener listener = new BtnListener();
		((Button) findViewById(R.id.btnNum00Id)).setOnClickListener(listener);
		((Button) findViewById(R.id.btnNum0Id)).setOnClickListener(listener);
		((Button) findViewById(R.id.btnNum1Id)).setOnClickListener(listener);
		((Button) findViewById(R.id.btnNum2Id)).setOnClickListener(listener);
		((Button) findViewById(R.id.btnNum3Id)).setOnClickListener(listener);
		((Button) findViewById(R.id.btnNum4Id)).setOnClickListener(listener);
		((Button) findViewById(R.id.btnNum5Id)).setOnClickListener(listener);
		((Button) findViewById(R.id.btnNum6Id)).setOnClickListener(listener);
		((Button) findViewById(R.id.btnNum7Id)).setOnClickListener(listener);
		((Button) findViewById(R.id.btnNum8Id)).setOnClickListener(listener);
		((Button) findViewById(R.id.btnNum9Id)).setOnClickListener(listener);
		((Button) findViewById(R.id.btnBack)).setOnClickListener(listener);
		comma = (Button) findViewById(R.id.btnComma);
		comma.setOnClickListener(listener);

		prepareNFCIntercept();
	}

	private class BtnListener implements OnClickListener {
		// On-click event handler for all the buttons

		@Override
		public void onClick(View view) {
			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			switch (view.getId()) {
			// Number buttons: '0' to '9'
			case R.id.btnNum00Id:
				break;
			case R.id.btnNum0Id:
				break;
			case R.id.btnNum1Id:
				break;
			case R.id.btnNum2Id:
				break;
			case R.id.btnNum3Id:
				break;
			case R.id.btnNum4Id:
				break;
			case R.id.btnNum5Id:
				break;
			case R.id.btnNum6Id:
				break;
			case R.id.btnNum7Id:
				break;
			case R.id.btnNum8Id:
				break;
			case R.id.btnNum9Id:
				break;
			case R.id.btnComma:
				comma.setEnabled(false);
				break;
			// Clear button
			case R.id.btnBack:
				String str = txtResult.getText().toString().trim();
				if (str.length() != 0) {
					String charToRemove = str.substring(str.length() - 1);
					if (charToRemove.equals(","))
						comma.setEnabled(true);
					str = str.substring(0, str.length() - 1);
					txtResult.setText(str);
					inStr = str;
					if (str.length() < 1) {
						Log.v("UNipiazza", "setText 0");
						txtResult.setText("0");
						inStr = "0";
					}
				}
				break;
			}
			String inDigit = ((Button) view).getText().toString();
			if (inStr.equals("0") || inStr.equals("0,")) {
				if (inDigit.equals("00")) {
					inStr = "0";
					v.vibrate(100);
				}
				else if (inDigit.equals(",")) {
					inStr = "0,";
					v.vibrate(100);
				}
				else if (inStr.equals("0,") && (!inDigit.equals("0") || !inDigit.equals("00"))) {
					inStr = "0," + inDigit;
					v.vibrate(100);
				}
				else if (inDigit.length() > 0) {
					inStr = inDigit; // no leading zero
					v.vibrate(100);
				}
			}
			else if (inStr.length() > 5) // Dimensione max cifra = 5
				return;
			else {
				inStr += inDigit; // accumulate input digit
				v.vibrate(100);
			}
			txtResult.setText(inStr);
		}

		// Perform computation on the previous result and the current input number,
		// based on the previous operator.
	}

	/**
	 * Retrieves recent post data from the server.
	 */
	public void updateJSONdata() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AddCoins.this);
		String username = sp.getString("username", "anon");
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("username", username));

	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.send_products:
			if (inStr.equals("0") || inStr.equals("00")) // Valore inserito = 0
			{
				Log.v("value ", "inStr = " + inStr);
				Toast.makeText(AddCoins.this, "Inserisci l'importo corretto!", Toast.LENGTH_SHORT).show();
				break;
			}
			else {
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							//Yes button clicked
							Intent i = new Intent(AddCoins.this, UpdateCoins.class);
							String gift_value = "no";
							Log.v("value ", "Saldo da passare con intent = " + result);
							SharedPreferences sp4 = PreferenceManager.getDefaultSharedPreferences(AddCoins.this);
							Editor edit = sp4.edit();
							String result = txtResult.getText().toString().replace(",", ".");
							i.putExtra("gift_value", gift_value);
							edit.putString("saldo", result);
							edit.commit();
							startActivity(i);
							break;

						case DialogInterface.BUTTON_NEGATIVE:
							//No button clicked
							break;
						}
					}
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Confermi l'invio di " + txtResult.getText().toString() + "€?").setPositiveButton("Si", dialogClickListener)
						.setNegativeButton("No", dialogClickListener);
				AlertDialog dialog = builder.create();
				dialog.setOnShowListener(new OnShowListener() {

					@Override
					public void onShow(DialogInterface dialog) {
						Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
						if (b != null) {
							b.setBackgroundColor(Color.RED);
							b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
							b.setTypeface(null, Typeface.BOLD);
						}
						Button bp = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
						if (bp != null) {
							bp.setBackgroundColor(Color.GREEN);
							bp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
							bp.setTypeface(null, Typeface.BOLD);
						}
					}
				});

				dialog.show();
			}
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		finish();
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
