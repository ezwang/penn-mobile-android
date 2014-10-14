package com.pennapps.labs.pennmobile;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.pennapps.labs.pennmobile.adapters.DiningAdapter;
import com.pennapps.labs.pennmobile.api.DiningAPI;
import com.pennapps.labs.pennmobile.classes.DiningHall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DiningFragment extends ListFragment {

    private DiningAPI mAPI;
    private ListView mListView;
    private ArrayList<DiningHall> mDiningHalls;
    private DiningAdapter mAdapter;
    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAPI = new DiningAPI();
        mActivity = getActivity();
        mDiningHalls = new ArrayList<DiningHall>();
        new GetOpenTask().execute();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView = getListView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dining, container, false);
        return v;
    }

    private class GetOpenTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                JSONObject resultObj = mAPI.getVenues();
                JSONArray venues = resultObj.getJSONObject("document").getJSONArray("venue");
                for (int i = 0; i < venues.length(); i++) {
                    JSONObject venue = venues.getJSONObject(i);
                    int id = venue.getInt("id");
                    String name = venue.getString("name");
                    boolean isResidential = venue.getString("venueType").equals("residential");
                    boolean hasMenu = !venue.getString("dailyMenuURL").isEmpty();
                    mDiningHalls.add(new DiningHall(id, name, isResidential, hasMenu));
                }
            } catch (JSONException e) {

            } catch (NullPointerException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            new GetMenusTask().execute();
        }
    }

    private class GetMenusTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (DiningHall mDiningHall : mDiningHalls) {
                    if (mDiningHall.isResidential() && mDiningHall.hasMenu()) {
                        JSONObject resultObj = mAPI.getDailyMenu(mDiningHall.getId());

                        JSONArray meals = resultObj.getJSONObject("Document")
                                .getJSONObject("tblMenu")
                                .getJSONArray("tblDayPart");

                        for (int i = 0; i < meals.length(); i++) {
                            JSONObject meal = meals.getJSONObject(i);

                            String mealName = meal.getString("txtDayPartDescription");

                            JSONArray stations = new JSONArray();
                            try {
                                stations = meal.getJSONArray("tblStation");
                            } catch (JSONException e) {
                                JSONObject stationsObject = meal.getJSONObject("tblStation");
                                stations.put(stationsObject);
                            }
                            HashMap<String, String> currentMenu = new HashMap<String, String>();
                            for (int j = 0; j < stations.length(); j++) {
                                JSONObject station = stations.getJSONObject(j);

                                String stationName = station.getString("txtStationDescription");
                                JSONArray stationItems = new JSONArray();
                                try {
                                    stationItems = station.getJSONArray("tblItem");
                                } catch (JSONException e) {
                                    JSONObject stationItem = station.getJSONObject("tblItem");
                                    stationItems.put(stationItem);
                                }
                                for (int k = 0; k < stationItems.length(); k++) {
                                    JSONObject foodItem = stationItems.getJSONObject(k);
                                    String foodName = foodItem.getString("txtTitle");
                                    currentMenu.put(stationName, foodName);
                                }
                            }

                            if (mealName.equals("Lunch")) {
                                mDiningHall.setLunchMenu(currentMenu);
                            } else if (mealName.equals("Dinner")) {
                                mDiningHall.setDinnerMenu(currentMenu);
                            }
                        }
                    }
                }
            } catch (JSONException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            mAdapter = new DiningAdapter(mActivity, mDiningHalls);
            mListView.setAdapter(mAdapter);
        }
    }
}
