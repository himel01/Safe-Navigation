package com.example.safenavigation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GetDirectionActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap map;
    private DatabaseReference databaseReference;
    private Button locationName;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimary};

    private LatLng currentLatLng;
    private LatLng targetLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_direction);
        init();
        getLocationPermission();
        SupportMapFragment supportMapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

    }

    private void init() {
        databaseReference= FirebaseDatabase.getInstance().getReference();
        locationName=findViewById(R.id.locationName);
        polylines = new ArrayList<>();
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
        targetLocation(googleMap);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);


    }

    private void targetLocation(GoogleMap googleMap) {
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                targetLatLng = map.getCameraPosition().target;

                locationName.setText(getAddress(targetLatLng.latitude,targetLatLng.longitude));


            }
        });
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
                    currentLatLng=new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
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

    public void backToMain(View view) {
        onBackPressed();

    }















    private void showDirection(LatLng start, LatLng end) {

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .key("AIzaSyBg8i0hgKB-G08vlQtuGyjlns1RfwzDzoE")
                .waypoints(start, end)
                .build();
        routing.execute();

    }


    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        float distance = routes.get(0).getDistanceValue()/1000;
        int position = 0;

        for (int i = 0; i <routes.size(); i++) {

            if (distance>=routes.get(i).getDistanceValue()/1000){
                distance = routes.get(i).getDistanceValue()/1000;
                position = i;
            }
        }

        int colorIndex = position % COLORS.length;

        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(getResources().getColor(COLORS[colorIndex]));
        polyOptions.width(10);
        polyOptions.addAll(routes.get(position).getPoints());
        Polyline polyline = map.addPolyline(polyOptions);
        polylines.add(polyline);

    }

    @Override
    public void onRoutingCancelled() {

    }

    public void ShowRoute(View view) {
        if (currentLatLng!=null && targetLatLng!=null){
            showDirection(currentLatLng,targetLatLng);
        }
    }
}
