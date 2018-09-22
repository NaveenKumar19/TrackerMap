package com.example.naveenkumar_v.tracker;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnCameraMoveListener,GoogleMap.OnCameraMoveStartedListener,GoogleMap.OnCameraIdleListener,
        LocationListener, GoogleMap.OnMarkerClickListener{

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker,mCurrentMarker1;
    LocationRequest mLocationRequest;
    TextView startLocation;
    TextView dropLocation;
    static Button startDate,endDate;
    LocationAddress locationAddress;
    ImageView imageMarker;
    Button rideNow;
    private static LatLng start_location,drop_location;
    private static Double range;
    private RequestQueue queue;
    MarkerOptions markerOptions,markerOptions1;
    List<Marker> mMarkers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        startLocation = (TextView) findViewById(R.id.start_location);
        dropLocation = (TextView) findViewById(R.id.drop_location);
        startDate = (Button) findViewById(R.id.startdate);
        endDate = (Button) findViewById(R.id.enddate);
        rideNow=(Button) findViewById(R.id.ride_now);
        locationAddress = new LocationAddress();
        queue = Volley.newRequestQueue(this);
        imageMarker = (ImageView) findViewById(R.id.imageMarker);
        mMarkers = new ArrayList<Marker>();


         startLocation.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {

                                                  if(mMarkers.contains(mCurrentMarker1)){
                                                      mMap.clear();
                                                      imageMarker.setVisibility(View.VISIBLE);
                                                      range=null;
                                                      mMarkers.clear();
                                                      mMarkers.add(mCurrLocationMarker);

                                                  }
                                                  startLocation.setClickable(false);
                                                  dropLocation.setClickable(true);
                                                  imageMarker.setImageResource(R.drawable.red_pin);
                                                  if(start_location!=null){
                                                      CameraPosition cameraPosition = new CameraPosition.Builder()
                                                              .target(start_location).zoom(19f).tilt(70).build();
                                                      mMap.moveCamera(CameraUpdateFactory.newLatLng(start_location));
                                                      mMap.animateCamera(CameraUpdateFactory
                                                              .newCameraPosition(cameraPosition));
                                                  }
                                              }
                                          });


                 dropLocation.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         if(mMarkers.contains(mCurrentMarker1)){
                             mMap.clear();
                             imageMarker.setVisibility(View.VISIBLE);
                             range=null;
                             mMarkers.clear();
                             mMarkers.add(mCurrLocationMarker);

                         }
                         dropLocation.setClickable(false);
                         startLocation.setClickable(true);
                         imageMarker.setImageResource(R.drawable.green_pin);
                         if(drop_location!=null){
                             CameraPosition cameraPosition = new CameraPosition.Builder()
                                     .target(drop_location).zoom(19f).tilt(70).build();
                             mMap.moveCamera(CameraUpdateFactory.newLatLng(drop_location));
                         }

                     }
                 });

                 startDate.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         showFromDatePickerDialog(v);

                     }
                 });
                 startDate.setOnLongClickListener(new View.OnLongClickListener() {
                     @Override
                     public boolean onLongClick(View v) {
                         startDate.setText("");
                         return true;
                     }
                 });

                 endDate.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         if(startDate.getText()!=""){
                             showToDatePickerDialog(v);
                         }

                     }
                 });
                 endDate.setOnLongClickListener(new View.OnLongClickListener() {
                     @Override
                    public boolean onLongClick(View v) {
                        endDate.setText("");
                        return true;
                    }
                });

                 rideNow.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         Log.e("test","--"+startDate.getText().toString());
                         if(start_location!=null&&drop_location!=null&&startDate.getText()!=""&&endDate.getText()!=""){
                             rideNow.setText("RIDE ON");
                             Toast.makeText(getApplicationContext(), "Your Ride Starts", Toast.LENGTH_LONG).show();

                         }else{
                             Toast.makeText(getApplicationContext(), "Please select all the fields", Toast.LENGTH_LONG).show();

                         }
                     }
                 });

    }

/**
 *  Dialog popup for Date selection
 **/

    public void showFromDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showToDatePickerDialog(View v) {
        DialogFragment newFragment = new ToDatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }




    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog;
            datePickerDialog = new DatePickerDialog(getActivity(),this, year,
                    month,day);
            datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user

            startDate.setText(day + "/" + month  + "/" + year);
        }

    }


public static class ToDatePickerFragment extends DialogFragment implements
        DatePickerDialog.OnDateSetListener {
    // Calendar startDateCalendar=Calendar.getInstance();
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        String getfromdate = startDate.getText().toString().trim();
        String getfrom[] = getfromdate.split("/");
        int year,month,day;
        year= Integer.parseInt(getfrom[2]);
        month = Integer.parseInt(getfrom[1]);
        day = Integer.parseInt(getfrom[0]);
        final Calendar c = Calendar.getInstance();
        c.set(year,month,day);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),this, year,month,day);
        datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
        return datePickerDialog;
    }
    public void onDateSet(DatePicker view, int year, int month, int day) {

        endDate.setText(day + "/" + month  + "/" + year);
    }
}



/**
 * Marker click event handle
 */




    @Override
    public boolean onMarkerClick(Marker marker) {

        if(mMarkers.contains(mCurrLocationMarker)){
            mMarkers.remove(mCurrLocationMarker);
        }
        if(!mMarkers.contains(mCurrentMarker1)){
            marker.setPosition(start_location);
            range = Double.valueOf(new DecimalFormat("##.##").format(range));
            marker.setTitle("Distance to Selected Location is : "+range);
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(start_location.latitude,start_location.longitude), new LatLng(drop_location.latitude, drop_location.longitude))
                    .width(10)
                    .color(Color.RED));
            imageMarker.setVisibility(View.GONE);
            dropLocation.setClickable(true);
            markerOptions1 = new MarkerOptions();
            markerOptions1.position(drop_location);
            markerOptions1.icon(BitmapDescriptorFactory.fromResource(R.drawable.green_pin));
            mCurrentMarker1 = mMap.addMarker(markerOptions1);
            mCurrentMarker1.setTitle("Distance from Selected Location is : "+range);
            mMarkers.add(mCurrentMarker1);
        }

        return false;
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnMarkerClickListener(this);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

        }
    }




/**
 * When movement of camera idles at the time hub location will be fetched following conditons
 */
    @Override
    public void onCameraIdle() {
        double lat = mMap.getCameraPosition().target.latitude;
        double lng = mMap.getCameraPosition().target.longitude;
        locationAddress.getAddressFromLocation(lat, lng,
                getApplicationContext(), new GeocoderHandler());
        if(mMarkers.contains(mCurrLocationMarker)) {
            if (start_location != null && drop_location != null && range != null) {
                String url = "https://apidev.quantumrides.com/api/v1/user/nearest/hub?latitude=" + start_location.latitude + "&longitude=" + drop_location.longitude + "&range=" + range + "";
                try {
                    volleyStringRequst(url);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

    }
    @Override
    public void onCameraMove() {

    }
    @Override
    public void onCameraMoveStarted(int i) {

    }


    /**
     * apiclient Connection...access fine location checking
     */



    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(19f).tilt(70).build();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
        mMarkers.add(mCurrLocationMarker);
        startLocation.setClickable(false);
        locationAddress.getAddressFromLocation(location.getLatitude(), location.getLongitude(),
                getApplicationContext(), new GeocoderHandler());
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted. Do the
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }






    /**
     * It handles the address to display in textview field
     */

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress,latitude,longitude;

            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    latitude=bundle.getString("latitude");
                    longitude=bundle.getString("longitude");
                    break;
                default:
                    locationAddress = null;
                    latitude = null;
                    longitude=null;
            }
            if(!startLocation.isClickable()){
                startLocation.setText(locationAddress);
                LatLng latLng = new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude));
                start_location=latLng;

            }
            if(!dropLocation.isClickable()){
                dropLocation.setText(locationAddress);
                LatLng latLng = new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude));
                drop_location=latLng;
                 range = CalculationByDistance(start_location,drop_location);
            }
        }
    }

    /**
     * Range Calculation
     */
    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));

        return Radius * c;
    }




    /**
     *
     * @param url
     * @throws JSONException
     *
     *Here we handling the hub location api fetch.
     */

    private void volleyStringRequst(String url) throws JSONException {

        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject response1 = null;
                Double latitude,longitude,distance;
                String hub_name;
                try {
                    response1 = new JSONObject(response);
                   JSONArray json  = response1.getJSONArray("hubs");
                    for(int i =0; i<response1.getJSONArray("hubs").length();i++){

                        latitude = response1.getJSONArray("hubs").getJSONObject(i).getDouble("latitude");
                        longitude =response1.getJSONArray("hubs").getJSONObject(i).getDouble("longitude");
                        hub_name = response1.getJSONArray("hubs").getJSONObject(i).getString("hub_name");
                        distance =response1.getJSONArray("hubs").getJSONObject(i).getDouble("distance");

                        if(hub_name!=null) {

                            LatLng latLng = new LatLng(latitude, longitude);
                            mMap.clear();
                             markerOptions = new MarkerOptions();
                             mMarkers = new ArrayList<Marker>();
                            markerOptions.position(latLng);
                            markerOptions.title("Center HUB");
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.vehical_hub));
                            mCurrLocationMarker = mMap.addMarker(markerOptions);
                            mMarkers.add(mCurrLocationMarker);


                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq);
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            return;

        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}

