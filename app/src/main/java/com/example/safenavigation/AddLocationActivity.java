package com.example.safenavigation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private String address,locationinfo,userId;
    private FirebaseAuth firebaseAuth;
    private EditText locationInfo;
    private DatabaseReference databaseReference;
    private long maxId=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        firebaseAuth=FirebaseAuth.getInstance();
        locationInfo=findViewById(R.id.locationInfoET);

        databaseReference=FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if(dataSnapshot.exists()){maxId=(dataSnapshot.getChildrenCount());}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        getLocationPermission();
        SupportMapFragment supportMapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
    }
    private void getLocationPermission() {
        String [] permissions={Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(permissions,0);
            }
            else { }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
        LatLng bd=new LatLng(23.7916897,90.386919);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(bd,14));
        //googleMap.addMarker(new MarkerOptions().position(bd).title("hi").snippet("hello"));
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {return;}
        else {}
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);

    }

    public String getAddress(double lat,double lng) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0);

            }
        } catch (IOException e) {
            e.printStackTrace();

        }
        return address;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void addLocation(View view) {
        FusedLocationProviderClient fusedLocationProviderClient=new FusedLocationProviderClient(this);

        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){return;}
        Task location=fusedLocationProviderClient.getLastLocation();
        location.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if(task.isSuccessful())
                {
                    Location currentLocation=(Location) task.getResult();
                    LatLng currentLatLng=new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
                    String address=getAddress(currentLocation.getLatitude(),currentLocation.getLongitude());
                    //map.addMarker(new MarkerOptions().position(currentLatLng).title(address));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,12));
                    locationinfo=locationInfo.getText().toString();
                    userId=firebaseAuth.getCurrentUser().getUid();

                   /* Map<String,Object> locationMap=new HashMap<>();
                    locationMap.put("locationInfo",locationinfo);
                    locationMap.put("address",address);
                    locationMap.put("latitude",currentLocation.getLatitude());
                    locationMap.put("longitude",currentLocation.getLongitude());
                    DatabaseReference locationReference=databaseReference.child("Location").child("User Location Request").child(userId);
                    locationReference.setValue(locationMap);*/
                    //DatabaseReference generalLocation=databaseReference.child("Location").child("")\
                    LocationInfo locationInfo=new LocationInfo(locationinfo,address,currentLocation.getLatitude(),currentLocation.getLongitude());
                    DatabaseReference locationReference=databaseReference.child("Location").child("User Location Request").child(userId);
                    locationReference.setValue(locationInfo);

                    locationReference=databaseReference.child("Location").child("General Location").child(String.valueOf(maxId+1));
                    locationReference.setValue(locationInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(AddLocationActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AddLocationActivity.this,MainActivity.class));
                            finish();
                        }
                    });






                }

            }
        });


    }
}
