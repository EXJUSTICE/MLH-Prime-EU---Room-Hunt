package com.xu.roomhunter;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import android.support.v4.app.Fragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static com.xu.roomhunter.RoomDetailActivity.MY_PERMISSIONS_REQUEST_INTERNET;
//TODO get the room IDS, place into arrayList, set that as a tag for the marker, then call onclicklistener, and fetch JSON
//using the ID

//
public class MainActivity extends AppCompatActivity implements  OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,SettingsFragment.updateMapListener {
    GoogleMap googleMap;
    LocationManager locationManager;
    PendingIntent proximityIntent;
    SharedPreferences sharedPreferences;
    int locationCount = 0;
    private static final String PROX_ALERT_INTENT = "com.xu.bombventure";
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1972;

    //Code for GoogleApiClient, and LocationListener
    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    double lat, lon;
    double userlat, userlon;
    Location userloc;
    Marker mCurrLocationMarker;
    String JSON;

    int uniqueId;

    //JSON fetched coordinates
    ArrayList<LatLng>CoordinatesFetched;
    ArrayList<String>IDs;
    ArrayList<String>Prices;
    ArrayList<String>Bedrooms;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check if Google Play is available
        boolean status = isGooglePlayServicesAvailable(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);


        buildGoogleApiClient();
        //http://stackoverflow.com/questions/21831224/googleapiclient-is-not-connected-yet-exception-in-cast-application
        //only need to build one instance otherwise error
        IDs = new ArrayList<String>();
        Prices = new ArrayList<String>();
        Bedrooms = new ArrayList<String>();
        checkLocationPermission();
        JSONFetch fetch = new JSONFetch();
        fetch.execute("http://b116.ml/roomhunt/api/flat-list");
        if (status) {
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            // Getting GoogleMap object from Map Fragment
            //googleMap = fm.getMapAsync(this); - YOU CANNOT ASSIGN A MAPFRAGMENT TO A GOOGLEMAP
            fm.getMapAsync(this);


            //mapAsync calls onMapReady(), along with its subordinate build googleServices
            //ALL GOOGLEMAP RELATED CODE HAS TO BE IN ONMAPREADY

            //Set current view to current location


            //Get locationManager object from System Service
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //fetch sharedpreferences object
            sharedPreferences = getSharedPreferences("location", 0);
            //In future versions, should use DB server

            //getting number of proximity alert locations
            locationCount = sharedPreferences.getInt("locationCount", 0);




            //Register the BroadcastReceiver DefuseReceiver for any PROX Alerts
            //THIS IS DUPLICATED IN ONMAPREADY, HENCE TEMPORARILY REMOVED
            //IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
            //registerReceiver(new DefuseReceiver(), filter);


           /* //Iterating through locationCounts
            if (locationCount != 0) {
                String lat = "";
                String lng = "";

                //Iterating through locations stored
                //Draw marker and circles for each location stored
                for (int i = 0; i < locationCount; i++) {
                    //getting latitutude of i-th location
                    lat = sharedPreferences.getString("lat" + i, "0");
                    //getting longitude of the i-th location
                    lng = sharedPreferences.getString("lng" + i, "0");

                    //DRAW MARKER ON MAP, if latitude has nonzero value
                    if (lat.equals("0")){
                        //
                        drawMarker(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));

                        //DRAW CIRCLE ON MAP
                        drawCircle(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
                    }


                }
 */
            //Move CamerPosition to last proxed position, not necessary?
            //googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

            //set Correct zoom Level, 20- buildings


        }

    }

    public void drawMarkersFromLatLng(){
        for (int i=0 ;i<CoordinatesFetched.size();i++){
            drawMarker(CoordinatesFetched.get(i),IDs.get(i));

        }
    }


    //Required code for LocationListener, onConnected to Google Play Services, we will find
    @Override
    public void onConnected(Bundle bundle) {

        checkLocationPermission();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }
    }
    //if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()){

    //    buildGoogleApiClient();
    //  mGoogleApiClient.connect();

    // }


    //NOT NECESSARY/CONFLICTING CODE?  connection, allow map to display users latest location

    // mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    //if (mLastLocation != null) {
    //  lat = mLastLocation.getLatitude();
    //  lon = mLastLocation.getLongitude();
    //  LatLng loc = new LatLng(lat, lon);
    //  googleMap.addMarker(new MarkerOptions().position(loc).title("Current Loction"));
    //  googleMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
    //}

    //}
    @Override
    public void onConnectionSuspended(int i) {

    }

    //locationlistener code, update camera movement on Location
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        lat = mLastLocation.getLatitude();
        lon = mLastLocation.getLongitude();


        //check if marker exists in old place, if so then remove it
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        LatLng loc = new LatLng(lat, lon);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(loc);

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = googleMap.addMarker(markerOptions);

        // without zoom : googleMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        //TODO is this necessary
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 17));


        //stop location updates
        //if(googleMap !=null){
        // LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        // }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        buildGoogleApiClient();
    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    ;

    //Connect and disconnect to ApiClient during start/destroy
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {

            buildGoogleApiClient();


        }

        mGoogleApiClient.connect();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }


    //Code required for getMapAsync, part of onMapReadyCallback
    //GetMapAsync needs hence this callback method, where you can immediately set stuff
    @Override
    public void onMapReady(GoogleMap map) {

        this.googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMyLocationEnabled(true);
        //all of this code should be moved to setUpMap();
        map.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        //Custom Map UI set up
        //disable zoom Controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        CameraUpdateFactory.zoomTo(17.0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location granted

                googleMap.setMyLocationEnabled(true);
            } else {
                //Request location permission
                checkLocationPermission();
            }
        } else {

            googleMap.setMyLocationEnabled(true);
        }

        //TODO at the moment we are using setOnMapClickListener, but in truth we should be using on MarkerclickListeners
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {



            }
        });

        //Long click currntly removes the last placed pendingIntent
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {


                checkLocationPermission();
                //remove proximityAlert, must use same pendingIntent as addProximityAlert
                locationManager.removeProximityAlert(proximityIntent);

                googleMap.clear();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.commit();

                Toast.makeText(getBaseContext(), "Prox alert removed", Toast.LENGTH_SHORT);
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String id = (String)marker.getTag();
                checkLocationPermission();

                Intent launchRecycler = new Intent(MainActivity.this,RoomDetailActivity.class);
                launchRecycler.putExtra("id",id);

                startActivity(launchRecycler);

                return false;
            }
        });


    }


    private void drawCircle(LatLng point) {
        //CircleOptions needed
        CircleOptions circleOptions = new CircleOptions();
        //find center of circle
        circleOptions.center(point);
        //radius of the circle
        circleOptions.radius(20);
        //Border color of circle
        circleOptions.strokeColor(Color.BLACK);
        //fillcolor of circle
        circleOptions.fillColor(0x30ff0000);
        //Border width of circle
        circleOptions.strokeWidth(2);

        googleMap.addCircle(circleOptions);

        //add circle to googleMap
    }

    private void drawMarker(LatLng point, String id) {
        //create MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);

        markerOptions.snippet(point.latitude + "," + point.longitude);
        //ADD INFOWINDOW CONTENTS
        markerOptions.snippet(Double.toString(point.latitude) + ',' + Double.toString(point.longitude));
        //adding marker on googlemap
        Marker mark = googleMap.addMarker(markerOptions);
        mark.setTag(id);

    }


    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int status = api.isGooglePlayServicesAvailable(this);
        //if we have a problem, return false
        if (status != ConnectionResult.SUCCESS) {
            if (api.isUserResolvableError(status)) {
                api.getErrorDialog(activity, status, 2404).show();
            }
            return false;

        }
        return true;
    }

    //required to check for when the user doesnt allow permission
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //No permission allowed, force user to give one
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},  MY_PERMISSIONS_REQUEST_LOCATION);


        }
        return true;

    }

    //callback from RequestPermissions() method
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                //if request is cancelled result arrays are empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted, so do everything related to locations
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        googleMap.setMyLocationEnabled(true);
                    }
                } else {

                    //permission denied
                    Toast.makeText(this, "permission denied, app functionality disabled", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            SettingsFragment settingsFrag = new SettingsFragment();

            settingsFrag.show(getSupportFragmentManager(), "Settings");


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Required CONNECT CALL TO ACTUALLY START FUSED LOCATION API

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {


            mGoogleApiClient.connect();

        }

        if (googleMap == null) {
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            fm.getMapAsync(this);
        }


        IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
        //registerReceiver(new DefuseReceiver(), filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        try {
            //unregisterReceiver(new DefuseReceiver());
        } catch (final Exception exception) {
            //was already unregistered, so do nothing
        }

    }
    public void parseIDsBedroomsPrices(String json){
        try{

            JSONObject response = new JSONObject(json);

            JSONArray flats = response.getJSONArray("results");

            for(int i =0;i<flats.length();i++){
                JSONObject object = flats.getJSONObject(i);
                String id = object.getString("id");
                String price = object.getString("price");
                String bedroomNo = object.getString("bedroomNo");

                IDs.add(id);
                Prices.add(price);
                Bedrooms.add(bedroomNo);
                //Maybe create a flat object?



                /*Toast toast =Toast.makeText(this,name,Toast.LENGTH_SHORT);
                toast.show();
                */

            }

        }catch (org.json.JSONException e){
            e.printStackTrace();
        }
    }

    public ArrayList<LatLng> parseCoordinatesFromJSON(String json){
        ArrayList<LatLng>coordinateslist = new ArrayList<LatLng>();
        try{

            JSONObject response = new JSONObject(json);

            JSONArray flats = response.getJSONArray("results");

            for(int i =0;i<flats.length();i++){
                JSONObject object = flats.getJSONObject(i);
                double latitude =Double.parseDouble(object.getString("latitude"));
                double longitude = Double.parseDouble(object.getString("longitude"));

                LatLng newloc = new LatLng(latitude,longitude);
                coordinateslist.add(newloc);



                /*Toast toast =Toast.makeText(this,name,Toast.LENGTH_SHORT);
                toast.show();
                */

            }

        }catch (org.json.JSONException e){
            e.printStackTrace();
        }

        return coordinateslist;

    }
    public boolean checkInternetPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST_INTERNET);

        }
        return true;
    }

    //Inner class for fetching json in the mapView, mainly for coordinates
    public class JSONFetch extends AsyncTask<String, String, String> {
        private ProgressDialog pdialog;

        protected void onPreExecute() {
            super.onPreExecute();
            checkInternetPermission();

            pdialog= new ProgressDialog(MainActivity.this);
            pdialog.setMessage("Fetching Properties...");
            pdialog.show();



        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pdialog.cancel();

            JSON = result;

            CoordinatesFetched = parseCoordinatesFromJSON(JSON);
            parseIDsBedroomsPrices(JSON);

            drawMarkersFromLatLng();









        }
    }

    @Override
    public void updateMap(String maxp,String maxb, String maxd){

        // Debug- WORKS Toast.makeText(this,maxp,Toast.LENGTH_LONG).show();
        googleMap.clear();
        JSONFetch fetch = new JSONFetch();
        fetch.execute("http://b116.ml/roomhunt/api/flat-list?max_price="+maxp+"&min_bedroom_no="+maxb);

    }

}
