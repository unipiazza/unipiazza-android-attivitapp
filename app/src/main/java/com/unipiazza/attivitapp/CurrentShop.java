package com.unipiazza.attivitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import net.minidev.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;

public class CurrentShop extends User {

    private static CurrentShop instance;
    private ArrayList<Event> events;

    public static CurrentShop getInstance() {
        if (instance == null) {
            instance = new CurrentShop();
            return instance;
        } else
            return instance;
    }

    private ArrayList<Prize> prizes;

    public CurrentShop(int id, String first_name, String last_name, String image_url) {
        super(id, first_name, last_name, image_url);
    }

    public CurrentShop() {
        super();
    }

    public void isAuthenticated(Context context, HttpCallback callback) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String access_token = pref.getString("access_token", "");
        String email = pref.getString("email", "");

        if (!access_token.isEmpty() && !email.isEmpty() && CurrentShop.getInstance().getFirst_name() != null) {
            callback.onSuccess(null);
        } else
            callback.onFail(null, null);
    }

    public void checkToken(Context context, final HttpCallback callback) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(CurrentShop.getInstance().getAccessToken(context));
            if (!verifyClaims(signedJWT.getJWTClaimsSet())) {
                Log.v("UNIPIAZZA", "checkToken");
                AttivitAppRESTClient.getInstance().postPing(context, false, callback);
            } else
                callback.onSuccess(null);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private boolean verifyClaims(ReadOnlyJWTClaimsSet claimSet) {
        JSONObject claims = claimSet.toJSONObject();
        Long expiration = (Long) claims.get("exp");

        if (expiration == null || expiration < System.currentTimeMillis() / 1000) {
            Log.w("UNIPIAZZA", "Expired");
            return false;
        }
        return true;
    }

    public void setAuthenticated(Context context, String email, String first_name, String token
            , int id, String password, ArrayList<Prize> prizes, ArrayList<Event> events) {
        this.id = id;
        this.first_name = first_name;
        this.prizes = prizes;
        this.events = events;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = pref.edit();

        edit.putString("access_token", token);
        edit.putString("email", email);
        edit.putString("password", password);
        edit.putLong("token_date", System.currentTimeMillis());
        edit.putInt("id_attivita", id);
        edit.commit();
    }

    public void setToken(Context context, String access_token) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = pref.edit();
        edit.putString("access_token", access_token);
        edit.putLong("token_date", System.currentTimeMillis());
        edit.commit();
    }

    public String getAccessToken(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString("access_token", "");
    }

    public void logout(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = pref.edit();

        edit.clear();
        edit.commit();

        File dir = context.getExternalCacheDir();

        File file = new File(dir.getAbsolutePath() + "/me.jpg");
        if (file.exists())
            file.delete();
    }

    public ArrayList<Prize> getPrizes() {
        return prizes;
    }

    public ArrayList<Prize> getFilteredPrizes(int coins) {
        ArrayList<Prize> filteredPrize = new ArrayList<Prize>();
        for (Prize prize : prizes) {
            if (prize.getCoins() <= coins)
                filteredPrize.add(prize);
        }
        return filteredPrize;
    }

    public void setPrizes(ArrayList<Prize> prizes) {
        this.prizes = prizes;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }
}
