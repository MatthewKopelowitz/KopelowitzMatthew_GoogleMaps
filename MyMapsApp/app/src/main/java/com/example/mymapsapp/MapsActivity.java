package com.example.mymapsapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LogPrinter;
import android.view.View;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean tracking = false;

    private static final long MIN_TIME_BW_UPDATES = 1000 * 5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;

    private Location myLocation;
    private double latitude, longitude;
    private static final int MY_LOC_ZOOM_FACTOR = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
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

        // Add a marker at Matthew's Birth Place and move the camera
        LatLng birthPlace = new LatLng(32.799687, -117.155044); //32.799687, -117.155044
        mMap.addMarker(new MarkerOptions().position(birthPlace).title("Marker at Matthew's Place of Birth"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(birthPlace));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);

    }


    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) Log.d("MyMaps", "getLocation: GPS is enabled");

            //get network status (cell tower + wifi) - look for network provider in LocationManager
            //add code here to update isNetworkEnabled and output Log.d

            if (!isGPSEnabled && !isNetworkEnabled) //no provider is enabled
                Log.d("MyMaps", "getLocation: no provider is enabled");
            else {
                if (isNetworkEnabled) {
                    Log.d("MyMaps", "getLocation: Network is enabled");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                }
                if (isGPSEnabled) {
                    Log.d("MyMaps", "getLocation: GPS is enabled");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                }
            }
        } catch (Exception e) {
            //put Log.d in here
            e.printStackTrace();
        }


    }//end of getLocation()

    LocationListener locationListenerNetwork = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "locationListenerNetwork: location changed");
            dropAmarker(LocationManager.NETWORK_PROVIDER);
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    LocationListener locationListenerGPS = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "locationListenerGPS: location changed");
            dropAmarker(LocationManager.GPS_PROVIDER);
            locationManager.removeUpdates(locationListenerNetwork);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("MyMaps", "locationListenerGPS: location provider available");
                    Toast.makeText(MapsActivity.this, "locationListenerGPS: AVAIl", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMaps", "locationListenerGPS: location provider out of service");
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMaps", "locationListenerGPS: location provider temporarily unavailable");
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;
                default:
                    Log.d("MyMaps", "locationListenerGPS: location provider default");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void dropAmarker(String provider) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myLocation = locationManager.getLastKnownLocation(provider);
        if (myLocation != null) {
            latitude = myLocation.getLatitude();
            longitude = myLocation.getLongitude();
        }

        LatLng userLocation = null;
        if (myLocation == null) {
            Toast.makeText(this, "dropAmarker: myLocation is null - cannot drop a marker", Toast.LENGTH_SHORT).show();
        } else {
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            if (provider == locationManager.GPS_PROVIDER) {
                //add circles for the marker with 2 outer rings
                Circle circle = mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.RED).strokeWidth(2).fillColor(Color.RED));
                //add outer ring 1
                Circle ringOne = mMap.addCircle(new CircleOptions().center(userLocation).radius(1.5).strokeColor(Color.RED).strokeWidth(2));
                //add outer ring 2
                Circle ringTwo = mMap.addCircle(new CircleOptions().center(userLocation).radius(2).strokeColor(Color.RED).strokeWidth(2));

            } else {
                Circle circle = mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.BLUE));
            }

            mMap.animateCamera(update);
        }

    }//end of dropAmarker()


    //Write method trackMyLocation(View v) ---
    //call getLocation if currently not tracking
    //or turn off tracking if currently enabled (removeUpdates for both listeners)
    public void trackMyLocation(View v) {
        tracking = !tracking;
        if (tracking) {
           getLocation();
           Toast.makeText(this, "tracking", Toast.LENGTH_SHORT).show();
           dropAmarker(LocationManager.NETWORK_PROVIDER);
           dropAmarker(LocationManager.GPS_PROVIDER);

       } else if (!tracking){
           locationManager.removeUpdates(locationListenerGPS);
           locationManager.removeUpdates(locationListenerNetwork);
           Toast.makeText(this, "not tracking", Toast.LENGTH_SHORT).show();
       }
    }


    //Write metho clearMarkers(View v) ---
    //clear all markers from map
    public void clearMarkers(View v) {
        mMap.clear();
    }

    int counter = 0;
    public void changeView(View view) {
        if (counter % 2 == 0) {
            mMap.setMapType(2);
            counter++;
        } else {
            mMap.setMapType(1);
            counter++;
        }
    }

}//end of class

