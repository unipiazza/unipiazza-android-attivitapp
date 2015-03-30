package com.unipiazza.attivitapp.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.unipiazza.attivitapp.AttivitAppRESTClient;
import com.unipiazza.attivitapp.CurrentUser;
import com.unipiazza.attivitapp.HttpCallback;
import com.unipiazza.attivitapp.JSONParser;
import com.unipiazza.attivitapp.R;

import java.util.concurrent.TimeoutException;

public class UpdateCoins extends Activity {
    JSONParser jsonParser = new JSONParser();
    private ProgressDialog pDialog;
    private Button btnOk;
    private NfcAdapter mNfcAdapter;
    private PendingIntent pendingIntent;
    private String[][] techListsArray;
    private LinearLayout updateLayout;
    private ProgressBar progresssCoins;
    private String saldo;
    private boolean gift;
    private String gift_id;
    static final String errore = "Errore.";
    public static final String MIME_TEXT_PLAIN = "text/plain";
    private String event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the message from the intent
        Intent i = getIntent();
        String gift_value = (String) i.getSerializableExtra("gift_value");
        Log.v("value ", "E' un regalo? " + gift_value);

        // Creo Layout in base al valore di Gift
        if (gift_value.equals("yes")) {
            gift = true;
            setContentView(R.layout.update_prize_layout);
            updateLayout = (LinearLayout) findViewById(R.id.update_gift_layout);
            progresssCoins = (ProgressBar) findViewById(R.id.progressCoinsGift);
            gift_id = (String) i.getSerializableExtra("gift_id");
        } else {
            gift = false;
            setContentView(R.layout.update_layout);
            updateLayout = (LinearLayout) findViewById(R.id.update_layout);
            progresssCoins = (ProgressBar) findViewById(R.id.progressCoins);
            event = (String) i.getStringExtra("event");
        }

        updateJSONdata();
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
        SharedPreferences sp3 = PreferenceManager.getDefaultSharedPreferences(UpdateCoins.this);
        saldo = sp3.getString("saldo", "anon");

        String user = CurrentUser.getInstance().getFirst_name();
        String user_lastname = CurrentUser.getInstance().getLast_name();
        user_lastname = (user_lastname.substring(0, 1));

        final TextView text_name = (TextView) findViewById(R.id.nicknameview);
        final TextView last_name_view = (TextView) findViewById(R.id.lastnameview);
        text_name.setText(user);
        last_name_view.setText(user_lastname);

        ImageView avatar = (ImageView) findViewById(R.id.avatar);
        if (!CurrentUser.getInstance().isMale())
            avatar.setImageResource(R.drawable.user_icon_m);
        else
            avatar.setImageResource(R.drawable.user_icon_f);

        if (!gift)
            AttivitAppRESTClient.getInstance().postReceipts(UpdateCoins.this
                    , CurrentUser.getInstance().getId(), saldo, CurrentUser.getInstance().getPass(), true, event, new HttpCallback() {
                @Override
                public void onSuccess(JsonObject result) {
                    updateLayout.setVisibility(View.VISIBLE);
                    progresssCoins.setVisibility(View.GONE);
                    final TextView text_saldo = (TextView) findViewById(R.id.saldoview);
                    try {
                        text_saldo.setText(result.get("user_coins").getAsString());
                    } catch (NullPointerException e) {
                    }
                }

                @Override
                public void onFail(JsonObject result, Throwable e) {
                    if (e instanceof TimeoutException)
                        Toast.makeText(UpdateCoins.this, "Problema con la connessione internet", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(UpdateCoins.this, "Errore nell'invio dei dati", Toast.LENGTH_LONG).show();
                    Log.v("value ", "Errore ");
                    //Intent i_error = new Intent(UpdateCoins.this, Home.class);
                    finish();
                    //startActivity(i_error);
                }
            });
        else
            AttivitAppRESTClient.getInstance().postGift(UpdateCoins.this
                    , CurrentUser.getInstance().getId(), Integer.parseInt(gift_id), CurrentUser.getInstance().getPass(), true, new HttpCallback() {

                @Override
                public void onSuccess(JsonObject result) {
                    updateLayout.setVisibility(View.VISIBLE);
                    progresssCoins.setVisibility(View.GONE);
                    final TextView text_saldo = (TextView) findViewById(R.id.saldoview);
                    try {
                        text_saldo.setText(result.get("user_coins").getAsString());
                    } catch (NullPointerException e) {
                    }
                }

                @Override
                public void onFail(JsonObject result, Throwable e) {
                    if (e instanceof TimeoutException)
                        Toast.makeText(UpdateCoins.this, "Problema con la connessione internet", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(UpdateCoins.this, "Errore nell'invio dei dati", Toast.LENGTH_LONG).show();
                    Log.v("value ", "Errore ");
                    //Intent i_error = new Intent(UpdateCoins.this, HomeTap.class);
                    finish();
                    //startActivity(i_error);
                }
            });

        return errore;
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

        techListsArray = new String[][]{};
    }

}
