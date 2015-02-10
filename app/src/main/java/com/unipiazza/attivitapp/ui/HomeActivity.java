package com.unipiazza.attivitapp.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.unipiazza.attivitapp.CurrentShop;
import com.unipiazza.attivitapp.CurrentUser;
import com.unipiazza.attivitapp.JSONParser;
import com.unipiazza.attivitapp.R;

public class HomeActivity extends Activity {
    JSONParser jParser = new JSONParser();
    // url per la ricerca dell'ID
    ImageButton btnAddProduct;
    ImageButton btnGift;
    private NfcAdapter mNfcAdapter;
    private String[][] techListsArray;
    private PendingIntent pendingIntent;
    public static final String MIME_TEXT_PLAIN = "text/plain";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        CurrentUser currentUser = CurrentUser.getInstance();

        String user = currentUser.getFirst_name();
        String user_lastname = currentUser.getLast_name().substring(0, 1);

        int coins = currentUser.getTotal_coins();

        // Buttoni
        ImageButton btnAddProduct = (ImageButton) findViewById(R.id.add_coin);
        ImageView avatar = (ImageView) findViewById(R.id.avatar);
        if (currentUser.isMale())
            avatar.setImageResource(R.drawable.user_icon_m);
        else
            avatar.setImageResource(R.drawable.user_icon_f);

        btnAddProduct.setBackground(null);
        ImageButton btnGift = (ImageButton) findViewById(R.id.gift_list);
        btnGift.setBackground(null);
        if (CurrentShop.getInstance().getFilteredPrizes(CurrentUser.getInstance().getTotal_coins()).size() == 0)
            btnGift.setEnabled(false);
        final TextView text_name = (TextView) findViewById(R.id.nicknameview);
        final TextView last_name_view = (TextView) findViewById(R.id.lastnameview);
        final TextView view_saldo = (TextView) findViewById(R.id.saldoview);
        text_name.setText(user);
        last_name_view.setText(user_lastname);
        view_saldo.setText(coins + "");
        Log.d("Utente Trovato, NOME --> ", user);
        Log.d("Utente Trovato, COGNOME --> ", user_lastname);

        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Controllo connessione ad Internet
                ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
                Boolean internet = cd.isConnectingToInternet();

                if (internet == false)
                    Toast.makeText(HomeActivity.this, "Non c'è connessione ad internet =(\r\nRiprova fra qualche minuto!", Toast.LENGTH_SHORT).show();
                else {
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

                if (internet == false)
                    Toast.makeText(HomeActivity.this, "Non c'è connessione ad internet =(\r\nRiprova fra qualche minuto!", Toast.LENGTH_SHORT).show();
                else {
                    Intent i = new Intent(getApplicationContext(), GiftActivity.class);
                    startActivity(i);
                }
            }
        });

        prepareNFCIntercept();
    }

    /**
     * Background Async Task per cercare l'ID facendo una chiamata HTTP
     */
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
