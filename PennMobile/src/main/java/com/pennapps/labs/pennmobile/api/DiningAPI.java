package com.pennapps.labs.pennmobile.api;


import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;

public class DiningAPI extends API {

    public DiningAPI() {
        super();
        BASE_URL = "http://api.pennlabs.org:5000/dining/";
    }

    public JSONObject getDiningInfo(String courseId) {
        HttpGet httpGet = new HttpGet(BASE_URL + courseId);
        httpGet.addHeader(new BasicHeader("Content-Type", "application/json; charset=utf-8"));

        try {
            HttpResponse response = httpClient.execute(httpGet);
            JSONTokener tokener = new JSONTokener(inputStreamToString
                    (response.getEntity().getContent()).toString());
            Log.v("vivlabs", tokener.toString());
            return new JSONObject(tokener);
        } catch (IOException e) {
            Log.v("vivlabs", "yo1" + e);
            return null;
        } catch (JSONException e) {
            Log.v("vivlabs", "yo2" + e);
            return null;
        }
    }

    public JSONObject getVenues() {
        HttpGet httpGet = new HttpGet(BASE_URL + "venues");
        try {
            HttpResponse response = httpClient.execute(httpGet);
            JSONTokener tokener = new JSONTokener(inputStreamToString
                    (response.getEntity().getContent()).toString());
            return new JSONObject(tokener);
        } catch (IOException e) {
            Log.v("vivlabs", "" + e);
            return null;
        } catch (JSONException e) {
            Log.v("vivlabs", "" + e);
            return null;
        }
    }

    public JSONObject getDailyMenu(String hallID) {
        HttpGet httpGet = new HttpGet(BASE_URL + "daily_menu/" + hallID);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            JSONTokener tokener = new JSONTokener(inputStreamToString
                    (response.getEntity().getContent()).toString());
            return new JSONObject(tokener);
        } catch (IOException e) {
            Log.v("vivlabs", "" + e);
            return null;
        } catch (JSONException e) {
            Log.v("vivlabs", "" + e);
            return null;
        }
    }

    public JSONObject getWeeklyMenu(String hallID) {
        HttpGet httpGet = new HttpGet(BASE_URL + "weekly_menu/" + hallID);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            JSONTokener tokener = new JSONTokener(inputStreamToString
                    (response.getEntity().getContent()).toString());
            return new JSONObject(tokener);
        } catch (IOException e) {
            Log.v("vivlabs", "" + e);
            return null;
        } catch (JSONException e) {
            Log.v("vivlabs", "" + e);
            return null;
        }
    }
}
