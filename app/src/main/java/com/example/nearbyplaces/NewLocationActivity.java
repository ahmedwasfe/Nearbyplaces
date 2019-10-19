package com.example.nearbyplaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nearbyplaces.Adapter.NewLocationAdapter;
import com.example.nearbyplaces.Model.StoreLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class NewLocationActivity extends AppCompatActivity {


    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private EditText mInputSalonName;
    private RecyclerView mRecyclerNewLocation;
    private Button mAddNewLocation;

    private AlertDialog mDialog;

    private List<StoreLocation> mListNewLocation;

    private DatabaseReference mDatabaseReference;

    private double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_location);

        mInputSalonName = findViewById(R.id.input_salon_name);
        mRecyclerNewLocation = findViewById(R.id.recycler_new_location);
        mAddNewLocation = findViewById(R.id.btn_add_new_location);

        init();

        getAllLocations();


        buildLocationRequest();
        buildLocationCallBack();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        mAddNewLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDialog.show();

                String salonName = mInputSalonName.getText().toString();
                addNewLocation(salonName, lat, lng);


            }
        });
    }

    private void getAllLocations() {

        mDialog.show();

        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                mDialog.dismiss();

                // for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                StoreLocation storeLocation = dataSnapshot.getValue(StoreLocation.class);
                mListNewLocation.add(storeLocation);
                // }

                NewLocationAdapter newLocationAdapter = new NewLocationAdapter(NewLocationActivity.this, mListNewLocation);
                newLocationAdapter.notifyDataSetChanged();
                mRecyclerNewLocation.setAdapter(newLocationAdapter);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void addNewLocation(String salonName, double lat, double lng) {

        mDialog.show();

        Map<String, Object> mMapNewLocation = new HashMap<>();
        mMapNewLocation.put("name", salonName);
        mMapNewLocation.put("lat", lat);
        mMapNewLocation.put("lng", lng);

        mDatabaseReference.push().setValue(mMapNewLocation)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mDialog.dismiss();
                            Toast.makeText(NewLocationActivity.this, "Added the new Location", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mDialog.dismiss();
                Toast.makeText(NewLocationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void init() {

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Locations");

        mRecyclerNewLocation.setHasFixedSize(true);
        mRecyclerNewLocation.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayout.VERTICAL));
        mListNewLocation = new ArrayList<>();

        mDialog = new SpotsDialog
                .Builder()
                .setContext(this)
                .setCancelable(false)
                .build();
    }

    private void buildLocationCallBack() {


        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);


                //String salonName = mInputSalonName.getText().toString();
                lat = locationResult.getLastLocation().getLatitude();
                lng = locationResult.getLastLocation().getLongitude();


            }
        };


    }

    private void buildLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);

    }
}
