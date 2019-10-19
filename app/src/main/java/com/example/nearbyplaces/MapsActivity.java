package com.example.nearbyplaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nearbyplaces.Adapter.NewLocationAdapter;
import com.example.nearbyplaces.Model.StoreLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private ImageView mCurrentLocation;
    private TextView mTxtShowLatLng;
    private GoogleMap mMap;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;

    private DatabaseReference mDatabaseReference;;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Locations");

        mCurrentLocation = findViewById(R.id.current_location);
        mTxtShowLatLng = findViewById(R.id.txt_show_lat_lng);
        mTxtShowLatLng.setVisibility(View.GONE);




        buildLocationRequest();
        buildLocationCallBack();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
        // Start Update location
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        mCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Toast.makeText(MapsActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                locationCallback = new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);

                        // Add a marker in User Current Location and move the camera
                        LatLng userCurrentLocation = new LatLng(locationResult.getLastLocation().getLatitude(),
                                locationResult.getLastLocation().getLongitude());
                        double latitude = locationResult.getLastLocation().getLatitude();
                        double longitude = locationResult.getLastLocation().getLongitude();
                        // mTxtShowLatLng.setText(String.valueOf(latitude + "\n" + longitude));
                        mMap.addMarker(new MarkerOptions().position(userCurrentLocation).title("Your Current Location"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userCurrentLocation, 17.0f));

                    }
                };

            }
        });

    }

    private void buildLocationCallBack() {

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // Add a marker in User Current Location and move the camera
                LatLng userCurrentLocation = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
               // mTxtShowLatLng.setText(String.valueOf(latitude + "\n" + longitude));
                mMap.addMarker(new MarkerOptions().position(userCurrentLocation).title("Your Current Location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userCurrentLocation, 17.0f));

                getNearbyplaces();

            }
        };
    }

    private void getNearbyplaces() {


       mDatabaseReference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

               for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                   StoreLocation storeLocation = snapshot.getValue(StoreLocation.class);

                   LatLng storeLatLng = new LatLng(storeLocation.getLat(), storeLocation.getLng());
                   mMap.addMarker(new MarkerOptions()
                           .position(storeLatLng)
                           .title(storeLocation.getName())
                           .snippet(new StringBuilder("Distance: ").append(storeLocation.getDistance()).append(" km").toString())
                           .icon(BitmapDescriptorFactory.fromResource(R.drawable.barbershop)));
               }

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

    }

    private void buildLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    @Override
    protected void onPause() {
        if (locationCallback != null )
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationCallback != null)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    protected void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }
}
