package com.example.nearbyplaces;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.skyfishjy.library.RippleBackground;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient mPlacesClient;

    private Location mLastLocation;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    private List<AutocompletePrediction> mListPrediction;

    private View mapView;
    private MaterialSearchBar mSearchBar;
    private Button mFindSalon;
    private RippleBackground mRippleBackground;

    private final float DEFAULT_ZOOM = 18;

    private final int EXEPTION_REQUEST_CODE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mSearchBar = findViewById(R.id.searchBar);
        mFindSalon = findViewById(R.id.btn_find_salon);
        mRippleBackground = findViewById(R.id.ripple_background);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Places.initialize(this, "AIzaSyAr3bZWxa_tli23ZVuxMg7iHpphWTHnefA");
        mPlacesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        mSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    // opening or closing a navigation drawer
                }else if (buttonCode == MaterialSearchBar.BUTTON_BACK)
                     mSearchBar.disableSearch();

            }
        });

        mSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();

                mPlacesClient.findAutocompletePredictions(predictionsRequest)
                        .addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                                if (task.isSuccessful()){
                                    FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                                    if (predictionsResponse != null){
                                        mListPrediction = predictionsResponse.getAutocompletePredictions();
                                        List<String> mListSuggestions = new ArrayList<>();
                                        for (int i = 0; i < mListPrediction.size(); i++) {
                                            AutocompletePrediction prediction = mListPrediction.get(i);
                                            mListSuggestions.add(prediction.getFullText(null).toString());
                                        }
                                        mSearchBar.updateLastSuggestions(mListSuggestions);
                                        if (!mSearchBar.isSuggestionsVisible())
                                            mSearchBar.showSuggestionsList();;
                                    }
                                }else {
                                    Log.i("TAG_Predictions", "Predictions fetching task unsuccessful");
                                }
                            }
                        });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= mListPrediction.size())
                    return;

                AutocompletePrediction selectedPrediction = mListPrediction.get(position);
                String mSuggestion = mSearchBar.getLastSuggestions().get(position).toString();
                mSearchBar.setText(mSuggestion);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSearchBar.clearSuggestions();;
                    }
                }, 1000);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(mSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> mListPlaceField = Arrays.asList(Place.Field.LAT_LNG);

                final FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, mListPlaceField).build();
                mPlacesClient.fetchPlace(fetchPlaceRequest)
                                .addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                                    @Override
                                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                                        Place place = fetchPlaceResponse.getPlace();
                                        Log.i("TAG_PLACE", "Place found:" + place.getName());
                                        LatLng latLng = place.getLatLng();
                                        if (latLng != null){
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException){
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();;
                            int statusCode = apiException.getStatusCode();
                            Log.i("TAG_FetchPlaceRequest", "Place not found: "+ e.getMessage());
                            Log.i("TAG_STSTUS_CODE", "status Code " + statusCode);

                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });

        mFindSalon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng currentMarkerlocation = mMap.getCameraPosition().target;
                mRippleBackground.startRippleAnimation();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRippleBackground.stopRippleAnimation();
                        startActivity(new Intent(MapActivity.this, MainActivity.class));
                        finish();
                    }
                }, 3000);
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null){

            View locationBtn = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationBtn.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0,0,40,180);
        }

        // Check if GPS is enabled or not and then request user to enable it
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException){
                    ResolvableApiException exception = (ResolvableApiException) e;
                    try {
                        exception.startResolutionForResult(MapActivity.this, EXEPTION_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (mSearchBar.isSuggestionsVisible())
                    mSearchBar.clearSuggestions();
                if (mSearchBar.isSearchEnabled())
                    mSearchBar.disableSearch();
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EXEPTION_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                getDeviceLocation();
            }
        }
    }

    private void getDeviceLocation() {

        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        if (task.isSuccessful()){
                            mLastLocation = task.getResult();
                            if (mLastLocation != null){
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                        mLastLocation.getLatitude(),
                                        mLastLocation.getLongitude()
                                ), DEFAULT_ZOOM));
                            }else {

                               mLocationRequest = LocationRequest.create();
                               mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                               mLocationRequest.setInterval(10000);
                               mLocationRequest.setFastestInterval(5000);
                               mLocationCallback = new LocationCallback(){
                                   @Override
                                   public void onLocationResult(LocationResult locationResult) {
                                       super.onLocationResult(locationResult);

                                       if (locationResult == null)
                                           return;
                                       mLastLocation = locationResult.getLastLocation();
                                       mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                               mLastLocation.getLatitude(),
                                               mLastLocation.getLongitude()),
                                               DEFAULT_ZOOM));
                                       mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                                   }
                               };

                               mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                            }
                            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                        } else {
                            Toast.makeText(MapActivity.this, "Unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
