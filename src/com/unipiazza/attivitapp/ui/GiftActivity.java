package com.unipiazza.attivitapp.ui;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.unipiazza.attivitapp.CurrentShop;
import com.unipiazza.attivitapp.JSONParser;
import com.unipiazza.attivitapp.Prize;
import com.unipiazza.attivitapp.R;

public class GiftActivity extends ListActivity implements OnClickListener {
	private ProgressDialog pDialog;
	JSONParser jsonParser = new JSONParser();
	TextView totResult;
	float result = 0;
	Button back_button;
	private static final String READ_GIFT_PRODUCTS_URL = "http://attivitapp.herokuapp.com/giftproduct.php";
	private static final String TAG_POSTS = "products";
	private static final String TAG_NAME = "name";
	private static final String TAG_PRICE = "coins";
	private static final String TAG_ID_PRODUCT = "id";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";
	private JSONArray mProducts = null;
	private ArrayList<HashMap<String, String>> mProductList;
	private NfcAdapter mNfcAdapter;
	private PendingIntent pendingIntent;
	private String[][] techListsArray;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gift_activity);
		back_button = (Button) findViewById(R.id.gift_back);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		back_button.setOnClickListener(this);
		new LoadProducts().execute();

		prepareNFCIntercept();
	}

	/**
	 * Retrieves recent post data from the server.
	 */
	public void updateJSONdata() {
		ArrayList<Prize> prizes = CurrentShop.getInstance().getPrizes();
		mProductList = new ArrayList<HashMap<String, String>>();

		for (Prize prize : prizes) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(TAG_NAME, prize.getName());
			map.put(TAG_ID_PRODUCT, prize.getId() + "");
			map.put(TAG_PRICE, prize.getCoins() + "");
			// adding HashList to ArrayList
			mProductList.add(map);
		}
	}

	private void updateList() {
		ListAdapter adapter = new SimpleAdapter(this, mProductList,
				R.layout.single_gift, new String[] { TAG_NAME, TAG_PRICE }, new int[] { R.id.name, R.id.coins });
		setListAdapter(adapter);
		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final int InternalPosition = position;
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							String gift_value = "yes";
							String gift_coins = mProductList.get(+InternalPosition).get("coins");
							String gift_name = mProductList.get(+InternalPosition).get("name");
							String gift_id = mProductList.get(+InternalPosition).get("id");
							Intent i = new Intent(GiftActivity.this, UpdateCoins.class);
							i.putExtra("gift_coins", gift_coins);
							i.putExtra("gift_value", gift_value);
							i.putExtra("gift_id", gift_id);
							Log.v("value ", "Invio di : Premio = " + gift_name + " | Id premio = " + gift_id + " | Gettoni = " + gift_coins);
							SharedPreferences sp4 = PreferenceManager.getDefaultSharedPreferences(GiftActivity.this);
							Editor edit = sp4.edit();
							edit.putString("saldo", gift_coins);
							edit.commit();
							startActivity(i);
							break;
						case DialogInterface.BUTTON_NEGATIVE:
							//No button clicked
							break;
						}
					}
				};
				AlertDialog.Builder builder = new AlertDialog.Builder(GiftActivity.this);
				builder.setMessage("Confermi " + mProductList.get(+InternalPosition).get("name") + "?").setPositiveButton("Si", dialogClickListener)
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
		});
	}

	public class LoadProducts extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(GiftActivity.this);
			pDialog.setMessage("Carico i Premi disponibili...");
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
			super.onPostExecute(result);
			pDialog.dismiss();
			updateList();
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.gift_back:
			Intent i = new Intent(GiftActivity.this, HomeActivity.class);
			Log.v("action ", "Tasto indietro premuto");
			startActivity(i);
			finish();
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
