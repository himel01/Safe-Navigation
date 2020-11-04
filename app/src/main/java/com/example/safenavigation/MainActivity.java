package com.example.safenavigation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private static int VIDEO_REQUEST=1;
    private Uri videoUri=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        getLocationPermission();
        SupportMapFragment supportMapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

    }

    private void init() {
        firebaseAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();


    }

    private void getLocationFromDatabase() {
        DatabaseReference locations=databaseReference.child("Location").child("General Location");
        locations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot data:dataSnapshot.getChildren())
                    {
                        LocationInfo locationInfo=data.getValue(LocationInfo.class);
                        LatLng l=new LatLng(locationInfo.getLat(),locationInfo.getLng());
                        map.addMarker(new MarkerOptions().position(l).title(locationInfo.getAddress()).snippet(locationInfo.getLocationInfo()));

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(bd,12));
        getLocationFromDatabase();
        //googleMap.addMarker(new MarkerOptions().position(bd).title("hi").snippet("hello"));
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {return;}
        else {}
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void currentLocation(View view) {
        getCurrentLocation();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getCurrentLocation() {
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
                    map.addMarker(new MarkerOptions().position(currentLatLng).title(address));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,12));

                }

            }
        });
    }
    public String getAddress(double lat,double lng) {
        String address = "";
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


    public void addLocation(View view) {
        startActivity(new Intent(MainActivity.this,AddLocationActivity.class));
    }


    public void getDirection(View view) {
        startActivity(new Intent(MainActivity.this,GetDirectionActivity.class));

    }

    public void recordVideo(View view) {
        Intent videoIntent=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if(videoIntent.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(videoIntent,VIDEO_REQUEST);
        }


    }

    public void logOut(View view) {
        firebaseAuth.signOut();
        startActivity(new Intent(MainActivity.this,LoginActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==VIDEO_REQUEST&&resultCode==RESULT_OK)
        {
            videoUri=data.getData();
        }
    }
}
