package com.example.unipiazza;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class testID extends ListActivity {
    private TextView mText;
	  // Progress Dialog
    private ProgressDialog pDialog;
 
    // Creating JSON Parser object
    JSONParser jsonParser = new JSONParser();
    ArrayList<HashMap<String, String>> productsList;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "products";
    private static final String TAG_DESC = "description";
    private static final String TAG_NAME = "name";

    // url to get user ID
    private static String url_search_id = "http://www.icmevolution.com/Unipiazza/unipiazza_create.php";
	 // url per la ricerca dell'ID
    JSONArray products = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.layout_id);       
        // Hashmap for ListView
        productsList = new ArrayList<HashMap<String, String>>();
        productsList = new ArrayList<HashMap<String, String>>();
        new SearchId().execute();
        ListView lv = getListView();
        mText = (TextView) findViewById(R.id.textView_welcome);
        // Get the message from the intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String test = extras.getString("new_variable_name");
            mText.setText("Utente di Nome : " + test);
        }
    }
     
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	 //JSON Ricerca ID
    class SearchId extends AsyncTask<String, String, String> {

    	 /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(testID.this);
            pDialog.setMessage("Ricerca id..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        /**
         * Ricerca ID
         * */
        protected String doInBackground(String... args) {
            //String name = inputName.getText().toString();
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            String id="zizza";
            String id2="bella";
            params.add(new BasicNameValuePair("name", id));
            params.add(new BasicNameValuePair("description", id2));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_search_id,
                    "POST", params);
    
 
            return null;
        }
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
          
 
        }
    }
    }
