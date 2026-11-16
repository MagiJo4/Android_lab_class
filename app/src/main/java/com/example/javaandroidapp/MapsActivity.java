package com.example.javaandroidapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, LocationListener, GoogleMap.OnPoiClickListener {

    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        text = (TextView) findViewById(R.id.textv);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.map);
        if (fragment instanceof SupportMapFragment) {
            ((SupportMapFragment) fragment).getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mUiSettings = mMap.getUiSettings();

        // Marker in Jimma and move the camera there
        LatLng jimma = new LatLng(7.6736450324877294, 36.83693743308046);
        mMap.addMarker(new MarkerOptions().position(jimma).title("Marker in jimma"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jimma, 15));

        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setTrafficEnabled(true);          // Display traffic
        mMap.setIndoorEnabled(true);
        mUiSettings.setIndoorLevelPickerEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setZoomControlsEnabled(true);
        mMap.setOnPoiClickListener(this);

        enableMyLocation();
    }

    // "My location" crashes the app if the permission is missing, so ask for it first.
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        if (mMap == null) return;
        boolean fine = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (fine || coarse) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    return;
                }
            }
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        text.setText("lat: " + location.getLatitude() + " " + "long: " + location.getLongitude());
    }

    public void searchLocation(View view) {
        String location = "jimma";
        if (mMap == null || location.isEmpty()) return;

        List<Address> addressList = null;
        Geocoder geocoder = new Geocoder(this);
        try {
            addressList = geocoder.getFromLocationName(location, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addressList == null || addressList.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Location not found", Toast.LENGTH_SHORT).show();
            return;
        }
        Address address = addressList.get(0);
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title(location));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        Toast.makeText(getApplicationContext(),
                
                address.getLatitude() + " " + address.getLongitude(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPoiClick(@NonNull PointOfInterest poi) {
        Toast.makeText(getApplicationContext(), "Clicked: " +
                        poi.name + "\nPlace ID:" + poi.placeId +
                        "\nLatitude:" + poi.latLng.latitude +
                        " Longitude:" + poi.latLng.longitude,
                Toast.LENGTH_SHORT).show();
        text.setText(poi.latLng.latitude + "");
    }
}
