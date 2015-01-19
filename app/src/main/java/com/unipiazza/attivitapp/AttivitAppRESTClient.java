package com.unipiazza.attivitapp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.util.ArrayList;

public class AttivitAppRESTClient {
    private static AttivitAppRESTClient instance;

    public static AttivitAppRESTClient getInstance(Context context) {
        if (instance == null) {
            return new AttivitAppRESTClient();
        } else {
            synchronized (instance) {
                return instance;
            }
        }
    }

    public void postAuthenticate(final Context context, final String email, final String password, final HttpCallback callback) {
        Log.v("UNIPIAZZA", "postAuthenticate");
        JsonObject json = new JsonObject();
        json.addProperty("grant_type", "password");
        json.addProperty("email", email);
        json.addProperty("password", password);
        json.addProperty("scope", "shop");

        Ion.with(context)
                .load(UnipiazzaParams.LOGIN_URL)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        Log.v("UNIPIAZZA", "postAuthenticate result=" + result);
                        Log.v("UNIPIAZZA", "postAuthenticate e=" + e);
                        if (e == null) {
                            try {
                                String access_token = result.get("access_token").getAsString();
                                String refresh_token = result.get("refresh_token").getAsString();
                                int expires_in = result.get("expires_in").getAsInt();
                                getUser(context, access_token, refresh_token, expires_in, password, callback);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                callback.onFail(result, ex);
                            }
                        } else
                            callback.onFail(result, e);
                    }

                });

    }

    public void getUser(final Context context, final String access_token
            , final String refresh_token, final int expires_in, final String password
            , final HttpCallback callback) {
        String url = UnipiazzaParams.ME_URL + "?access_token=" + access_token;

        Ion.with(context)
                .load(url)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        Log.v("UNIPIAZZA", "result=" + result);
                        if (e == null) {
                            try {
                                String email = result.get("email").getAsString();
                                String first_name = result.get("name").getAsString();
                                int id = result.get("id").getAsInt();
                                JsonArray prizesJson = result.get("prizes").getAsJsonArray();

                                ArrayList<Prize> prizes = new ArrayList<Prize>();
                                for (int i = 0; i < prizesJson.size(); i++) {
                                    JsonObject prizeJ = prizesJson.get(i).getAsJsonObject();
                                    if (prizeJ.get("visible").getAsBoolean()) {
                                        Prize prize = new Prize(prizeJ.get("id").getAsInt()
                                                , prizeJ.get("title").getAsString()
                                                , prizeJ.get("description").getAsString()
                                                , prizeJ.get("coins").getAsInt());
                                        prizes.add(prize);
                                    }
                                }

                                CurrentShop.getInstance().setAuthenticated(context, email, first_name
                                        , access_token, refresh_token, expires_in
                                        , id
                                        , password
                                        , prizes);
                                callback.onSuccess(result);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                callback.onFail(result, ex);
                            }
                        }
                    }
                });
    }

    public void refreshToken(final Context context, final String refresh_token, final HttpCallback callback) {
        Log.v("UNIPIAZZA", "refreshToken");

        JsonObject json = new JsonObject();
        json.addProperty("grant_type", "refresh_token");
        json.addProperty("refresh_token", refresh_token);

        Ion.with(context)
                .load(UnipiazzaParams.LOGIN_URL)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        Log.v("UNIPIAZZA", "refreshToken result=" + result);
                        if (e == null) {
                            try {
                                String access_token = result.get("access_token").getAsString();
                                String refresh_token = result.get("refresh_token").getAsString();
                                int expires_in = result.get("expires_in").getAsInt();
                                CurrentShop.getInstance().setToken(context, access_token, refresh_token, expires_in);
                                if (callback != null)
                                    callback.onSuccess(result);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                if (callback != null)
                                    callback.onFail(result, ex);
                            }
                        } else {
                            if (callback != null)
                                callback.onFail(result, e);
                        }
                    }

                });

    }

    public void getSearchUser(final Context context, final String hash_pass, final String user_id, final boolean checkToken, final HttpCallback callback) {
        if (checkToken) {
            CurrentShop.getInstance().checkToken(context, new HttpCallback() {

                @Override
                public void onSuccess(JsonObject result) {
                    getSearchUserHttp(context, hash_pass, user_id, checkToken, callback);
                }

                @Override
                public void onFail(JsonObject result, Throwable e) {
                }
            });
        } else
            getSearchUserHttp(context, hash_pass, user_id, checkToken, callback);

    }

    private void getSearchUserHttp(final Context context, final String hash_pass, final String user_id, boolean checkToken, final HttpCallback callback) {
        Log.v("UNIPIAZZA", "getSearchUser");

        String url;
        String access_token = CurrentShop.getInstance().getAccessToken(context);
        url = UnipiazzaParams.USER_SEARCH_URL + "?access_token=" + access_token;
        if (hash_pass != null)
            url += "&hash_pass=" + hash_pass;
        else if (user_id != null)
            url += "&user_id=" + user_id;
        Log.v("UNIPIAZZA", "url=" + url);

        Ion.with(context)
                .load(url)
                .asJsonObject()
                .setCallback(
                        new FutureCallback<JsonObject>() {

                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                try {
                                    Log.v("UNIPIAZZA", "getUserSearch result=" + result);
                                    Log.v("UNIPIAZZA", "getUserSearch e=" + e);

                                    if (e == null) {
                                        String hash_type = "";
                                        if (user_id != null && user_id.equals(result.get("id").getAsString()))
                                            hash_type = "smartphone";
                                        else if (!result.get("hash_keychain").isJsonNull()
                                                && hash_pass.equals(result.get("hash_keychain").getAsString()))
                                            hash_type = "keychain";
                                        else if (!result.get("hash_card").isJsonNull()
                                                && hash_pass.equals(result.get("hash_card").getAsString()))
                                            hash_type = "card";
                                        CurrentUser.getInstance().setUser(context,
                                                result.get("first_name").getAsString(),
                                                result.get("last_name").getAsString(),
                                                result.get("shop_coins").getAsInt(),
                                                result.get("id").getAsInt(),
                                                hash_type,
                                                result.get("gender").getAsBoolean());
                                        callback.onSuccess(result);
                                    } else {
                                        callback.onFail(result,
                                                e);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    callback.onFail(result, ex);
                                }
                            }
                        }
                );
    }

    public void postReceipts(final Context context, final int user_id, final String coins, final String hash_type, final boolean checkToken, final HttpCallback callback) {
        if (checkToken) {
            CurrentShop.getInstance().checkToken(context, new HttpCallback() {

                @Override
                public void onSuccess(JsonObject result) {
                    postReceiptsHttp(context, user_id, coins, hash_type, checkToken, callback);
                }

                @Override
                public void onFail(JsonObject result, Throwable e) {
                }

            });
        } else
            postReceiptsHttp(context, user_id, coins, hash_type, checkToken, callback);

    }

    private void postReceiptsHttp(final Context context, final int user_id, String coinsString, final String hash_type, boolean checkToken, final HttpCallback callback) {
        float coins = 0;
        try {
            coins = Float.valueOf(coinsString);
        } catch (NumberFormatException e) {
            callback.onFail(null, e);
        }
        JsonObject json = new JsonObject();
        JsonObject jsonReceipt = new JsonObject();
        jsonReceipt.addProperty("user_id", user_id);
        jsonReceipt.addProperty("total", coins);
        jsonReceipt.addProperty("hash_type", hash_type);
        json.add("receipt", jsonReceipt);

        String url;
        String access_token = CurrentShop.getInstance().getAccessToken(context);
        url = UnipiazzaParams.RECEIPTS_URL + "?access_token=" + access_token;

        Ion.with(context)
                .load(url)
                .setJsonObjectBody(json)
                .asJsonObject().setCallback(new FutureCallback<JsonObject>() {

            @Override
            public void onCompleted(Exception e, JsonObject result) {
                Log.v("UNIPIAZZA", "postReceiptsHttp result=" + result);
                Log.v("UNIPIAZZA", "postReceiptsHttp e=" + e);
                if (e == null) {
                    try {
                        if (result.get("error").getAsBoolean())
                            callback.onFail(result, null);
                        else
                            callback.onSuccess(result);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        callback.onFail(result, ex);
                    }
                } else
                    callback.onFail(result, e);
            }
        });
    }

    public void postGift(final Context context, final int user_id, final int product_id, final String hash_type, final boolean checkToken, final HttpCallback callback) {
        if (checkToken) {
            CurrentShop.getInstance().checkToken(context, new HttpCallback() {

                @Override
                public void onSuccess(JsonObject result) {
                    postGiftHttp(context, user_id, product_id, hash_type, checkToken, callback);
                }

                @Override
                public void onFail(JsonObject result, Throwable e) {
                }
            });
        } else
            postGiftHttp(context, user_id, product_id, hash_type, checkToken, callback);

    }

    private void postGiftHttp(final Context context, final int user_id, final int product_id, final String hash_type, boolean checkToken, final HttpCallback callback) {
        JsonObject json = new JsonObject();
        JsonObject jsonReceipt = new JsonObject();
        jsonReceipt.addProperty("user_id", user_id);
        jsonReceipt.addProperty("product_id", product_id);
        jsonReceipt.addProperty("hash_type", hash_type);
        json.add("prize", jsonReceipt);

        String url;
        String access_token = CurrentShop.getInstance().getAccessToken(context);
        url = UnipiazzaParams.PRIZE_URL + "?access_token=" + access_token;

        Ion.with(context)
                .load(url)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        Log.v("UNIPIAZZA", "postGiftHttp result=" + result);
                        Log.v("UNIPIAZZA", "postGiftHttp e=" + e);
                        if (e == null) {
                            try {
                                if (result.get("error").getAsBoolean())
                                    callback.onFail(result, null);
                                else
                                    callback.onSuccess(result);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                callback.onFail(result, ex);
                            }
                        } else
                            callback.onFail(result, e);
                    }

                });
    }

    public void postPing(final Context context, final boolean checkToken) {
        if (checkToken) {
            CurrentShop.getInstance().checkToken(context, new HttpCallback() {

                @Override
                public void onSuccess(JsonObject result) {
                    postPingHttp(context);
                }

                @Override
                public void onFail(JsonObject result, Throwable e) {
                }
            });
        } else
            postPingHttp(context);

    }

    public void postPingHttp(final Context context) {
        String url = UnipiazzaParams.PING + "?access_token=" + CurrentShop.getInstance().getAccessToken(context);

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        int extra_level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float level = extra_level / (float) scale * 100.0f;

        JsonObject bodyShop = new JsonObject();
        JsonObject body = new JsonObject();
        bodyShop.addProperty("battery_level", level);
        bodyShop.addProperty("battery_isCharging", isCharging);
        body.add("shop", bodyShop);

        Ion.with(context)
                .load("PUT", url)
                .setJsonObjectBody(body)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {
                        Log.v("UNIPIAZZA", "result header=" + result.getHeaders().message());
                        Log.v("UNIPIAZZA", "result code=" + result.getHeaders().code());
                        Log.v("UNIPIAZZA", "result result=" + result.getResult());
                        Log.v("UNIPIAZZA", "e=" + e);
                    }
                });
    }


}
