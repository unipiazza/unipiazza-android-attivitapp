package com.unipiazza.attivitapp;

public class UnipiazzaParams {

    //public static final String BASE_URL = "http://unipiazza.herokuapp.com/";
    public static final String BASE_URL = "http://192.168.1.115:5000/";

    public static final String LOGIN_URL = BASE_URL + "auth/sign_in";

    public static final String USER_SEARCH_URL = BASE_URL + "api/attivitapp/v1/search";
    public static final String RECEIPTS_URL = BASE_URL + "api/attivitapp/v1/receipts";
    public static final String PRIZE_URL = BASE_URL + "api/attivitapp/v1/prizes";

    public static final String PING = BASE_URL + "api/attivitapp/v1/shops/device_info";

    public static final String NFC_DEVICE_MIMETYPE = "application/unipiazza.utentiapp";
}
