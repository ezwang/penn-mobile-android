package com.pennapps.labs.pennmobile;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pennapps.labs.pennmobile.api.Labs;
import com.pennapps.labs.pennmobile.classes.Building;
import com.pennapps.labs.pennmobile.classes.MapCallbacks;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MapFragment extends Fragment {

    private Labs mLabs;
    private MapView mapView;
    private GoogleMap googleMap;

    private SearchView searchView;
    private String query;
    private MainActivity activity;
    private LinearLayout card;
    private static Marker currentMarker;
    private static Set<Marker> loadedMarkers;
    private static MapCallbacks mapCallbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLabs = MainActivity.getLabsInstance();
        activity = (MainActivity) getActivity();
        loadedMarkers = new HashSet<>();
        mapCallbacks = new MapCallbacks();
        Context context = activity.getApplicationContext();
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(mapCallbacks)
                .addOnConnectionFailedListener(mapCallbacks)
                .addApi(LocationServices.API)
                .build();
        mapCallbacks.setGoogleApiClient(mGoogleApiClient);
        activity.closeKeyboard();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = (MapView) v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        card = (LinearLayout) v.findViewById(R.id.map_card);

        googleMap = mapView.getMap();
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setMyLocationEnabled(true);

        googleMap.setInfoWindowAdapter(new CustomWindowAdapter(inflater));

        try {
            MapsInitializer.initialize(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCallbacks.getLatLng(), 14));


        return v;
    }

    private class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private View view;

        public CustomWindowAdapter(LayoutInflater inflater) {
            view = inflater.inflate(R.layout.info_window, null);
        }

        @Override
        public View getInfoWindow(Marker arg0) {
            return null;
        }

        @Override
        public View getInfoContents(final Marker marker) {
            currentMarker = marker;

            ImageView imageView = (ImageView) view.findViewById(R.id.building_image);
            final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.mapProgress);
            TextView name = (TextView) view.findViewById(R.id.building_name);
            name.setText(marker.getTitle());

            if (marker.getSnippet().isEmpty()) {
                imageView.setVisibility(View.GONE);
            } else if (loadedMarkers.contains(currentMarker)) {
                Picasso.with(activity).load(marker.getSnippet()).fit().centerInside().into(imageView);
            } else {
                loadedMarkers.add(currentMarker);
                progressBar.setVisibility(View.VISIBLE);
                Picasso.with(activity).load(marker.getSnippet()).fit().centerInside().into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        currentMarker.hideInfoWindow();
                        currentMarker.showInfoWindow();
                    }

                    @Override
                    public void onError() {}
                });
            }
            return view;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapCallbacks.getGoogleApiClient().connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        mapCallbacks.requestLocationUpdates();
        activity.setTitle(R.string.map);
        activity.setNav(R.id.nav_map);
    }

    @Override
    public void onStop(){
        super.onStop();
        mapCallbacks.stopLocationUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mapCallbacks.stopLocationUpdates();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.building_search:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        searchView = (SearchView) menu.findItem(R.id.building_search).getActionView();
        searchView.setIconifiedByDefault(true);
        searchView.setIconified(true);
    }

    public static MapCallbacks getMapCallbacks(){
        return mapCallbacks;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.building, menu);

        searchView = (SearchView) menu.findItem(R.id.building_search).getActionView();
        final SearchView.OnQueryTextListener queryListener = new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String arg0) {
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String arg0) {
                searchView.clearFocus();
                query = arg0;
                searchBuildings(query);
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryListener);
    }

    private void searchBuildings(String query) {
        mLabs.buildings(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Building>>() {
                    @Override
                    public void call(List<Building> buildings) {
                        drawResults(buildings);
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        activity.showErrorToast(R.string.location_not_found);
                    }
                });
    }

    private void drawResults(List<Building> buildings) {
        googleMap.clear();
        if (buildings.isEmpty()) {
            activity.showErrorToast(R.string.location_not_found);
            return;
        }
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        Building first = buildings.remove(0);
        googleMap.addMarker(new MarkerOptions()
                .position(first.getLatLng())
                .title(first.title)
                .snippet(first.getImageURL()));
        for (Building building : buildings) {
            boundsBuilder.include(building.getLatLng());
            googleMap.addCircle(new CircleOptions()
                    .center(building.getLatLng())
                    .visible(true)
                    .radius(5)
                    .fillColor(Color.RED)
                    .strokeWidth(0));
        }
        changeZoomLevel(googleMap, boundsBuilder.build());
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.cardscaleloc);
        card.startAnimation(animation);
    }

    public static void changeZoomLevel(GoogleMap googleMap, LatLngBounds bounds){
        Location NECorner = new Location("");
        Location SWCorner = new Location("");
        LatLng northeast = bounds.northeast;
        LatLng southwest = bounds.southwest;
        NECorner.setLatitude(northeast.latitude);
        NECorner.setLatitude(northeast.longitude);
        SWCorner.setLatitude(southwest.latitude);
        SWCorner.setLatitude(southwest.longitude);
        int padding = 100;
        if (SWCorner.distanceTo(NECorner) < 40) {
            padding = 500;
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }
}
