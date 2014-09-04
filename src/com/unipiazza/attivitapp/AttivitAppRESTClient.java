package com.unipiazza.attivitapp;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class AttivitAppRESTClient {
	private static AttivitAppRESTClient instance;

	public static AttivitAppRESTClient getInstance(Context context, boolean checkToken) {
		if (instance == null) {
			return new AttivitAppRESTClient();
		} else {
			synchronized (instance) {
				if (checkToken)
					CurrentShop.getInstance().checkToken(context);
				return instance;
			}
		}
	}

	public void postAuthenticate(final Context context, final String email, final String password, final HttpCallback callback) {
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

	private void getUser(final Context context, final String access_token, final String refresh_token, final int expires_in, final String password, final HttpCallback callback) {
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
									Prize prize = new Prize(prizeJ.get("id").getAsInt()
											, prizeJ.get("description").getAsString()
											, prizeJ.get("coins").getAsInt());
									prizes.add(prize);
								}

								CurrentShop.getInstance().setAuthenticated(context, email, first_name
										, access_token, refresh_token, expires_in
										, id
										, password
										, prizes);
								callback.onSuccess(result);
								//getUserImg(context, image_url, callback);
							} catch (Exception ex) {
								ex.printStackTrace();
								callback.onFail(result, ex);
							}
						}
					}
				});

	}

	public void refreshToken(final Context context, final String refresh_token, final HttpCallback callback) {
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
						Log.v("UNIPIAZZA", "result=" + result);
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

	public void getSearchUser(final Context context, final String hash_pass, final HttpCallback callback) {
		String url;
		String access_token = CurrentShop.getInstance().getAccessToken(context);
		url = UnipiazzaParams.USER_SEARCH_URL + "?access_token=" + access_token + "&hash_pass=" + hash_pass;
		Log.v("UNIPIAZZA", "url=" + url);

		Ion.with(context)
				.load(url)
				.asJsonObject()
				.setCallback(
						new FutureCallback<JsonObject>() {

							@Override
							public void onCompleted(Exception e, JsonObject
									result) {
								try {

									Log.v("UNIPIAZZA", "getUserSearch result=" +
											result);
									Log.v("UNIPIAZZA", "getUserSearch e=" + e);

									if (e == null) {
										String hash_type = "";
										if
										(hash_pass.equals(result.get("hash_keychain").getAsString()))
											hash_type = "keychain";
										else
											hash_type = "card";
										CurrentUser.getInstance().setUser(context,
												result.get("email").getAsString(),
												result.get("first_name").getAsString(),
												result.get("last_name").getAsString(),
												result.get("image_url").getAsString(),
												result.get("unipoints").getAsInt(),
												result.get("id").getAsInt(), hash_type,
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

	public void postReceipts(final Context context, final int user_id, double coins, String hash_type, final HttpCallback callback) {
		JsonObject json = new JsonObject();
		JsonObject jsonReceipt = new JsonObject();
		jsonReceipt.addProperty("user_id", user_id);
		jsonReceipt.addProperty("coins", coins);
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
						Log.v("UNIPIAZZA", "postAuthenticate result=" + result);
						Log.v("UNIPIAZZA", "postAuthenticate e=" + e);
						if (e == null) {
							try {
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

	public void postGift(final Context context, final int user_id, int product_id, String hash_type, final HttpCallback callback) {
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
						Log.v("UNIPIAZZA", "postAuthenticate result=" + result);
						Log.v("UNIPIAZZA", "postAuthenticate e=" + e);
						if (e == null) {
							try {
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

}
