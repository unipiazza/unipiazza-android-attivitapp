package com.unipiazza.attivitapp;

public class UnipiazzaParams {

    public static final String BASE_URL = "http://unipiazza.herokuapp.com/";

    public static final String LOGIN_URL = BASE_URL + "oauth/token";

    public static final String ME_URL = BASE_URL + "api/shops/me";

    public static final String USER_SEARCH_URL = BASE_URL + "api/search";
    public static final String RECEIPTS_URL = BASE_URL + "api/receipts";
    public static final String PRIZE_URL = BASE_URL + "api/prizes";

    public static final String PING = BASE_URL + "api/shops/device_info";

    public static final String NFC_DEVICE_MIMETYPE = "application/unipiazza.utentiapp";
}
