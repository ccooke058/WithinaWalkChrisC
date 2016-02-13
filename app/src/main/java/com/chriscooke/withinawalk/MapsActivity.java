package com.chriscooke.withinawalk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Chris_Home on 26/11/15.
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private MainActivity mapMark;
    private GoogleMap mMap;
    private LatLng latlng;
    private String MODE = "mode=driving";
    private SupportMapFragment mapFragment;
    private LatLng mylatlng = null;
    private Document doc;
    private Boolean isDirectionDrawn = false;
    private Polyline polylin;
    LocationManager mLocationManager;
    Location currentLoc;
    Location markerLoc;
    Float distance;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.chriscooke.withinawalk.R.layout.activity_maps);

        // Obtain the SupportMapFragment
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(com.chriscooke.withinawalk.R.id.map);
        mapFragment.getMapAsync(this);
        mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    2000, 1, this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    2000, 1, this);
            Location location = getLastKnownLocation();
            myLocationChangeListener.onMyLocationChange(location);
        }catch (SecurityException e){
            e.printStackTrace();
        }

        try {
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            loco loco = (loco) bundle.getSerializable("value");

            assert loco != null;
            double lat = loco.getLat();
            double lng = loco.getLng();
            latlng = new LatLng(lat, lng);
        } catch (Exception e) {
            latlng = MainActivity.addMark();
        }

        Toolbar toolBar = (Toolbar) findViewById(com.chriscooke.withinawalk.R.id.toolbar);
        setSupportActionBar(toolBar);
    try {
        String url = getDirectionsUrl(mylatlng, latlng);

        DownloadTask downloadTask = new DownloadTask();

// Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }catch (Exception e){
        e.printStackTrace();
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MapsActivity.this);
        builder1.setMessage("Please turn on the application permissions to continue." +
                "\n Thank you!");
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Intent intent = new Intent (MapsActivity.this, MainActivity.class);
                        startActivity(intent);




                    }
                });
        builder1.show();

    }



        zoomToPoints();
    }


    private Location getLastKnownLocation() {
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            try {
                Location l = mLocationManager.getLastKnownLocation(provider);


                if (l == null) {
                    continue;
                }
                if (bestLocation == null
                        || l.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = l;
                }

                if (bestLocation == null) {
                    return null;
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }

        }return bestLocation;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        CharSequence title = item.getTitle();
        if (id == com.chriscooke.withinawalk.R.id.settings) {
            if (item.getTitle().equals("Satellite Map")) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        }
        if (id == com.chriscooke.withinawalk.R.id.Normal) {
            if (item.getTitle().equals("Normal Map")) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        }
        if (id == com.chriscooke.withinawalk.R.id.map) {
            if (item.getTitle().equals("Terrain Map")) {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }
        }
        if (id == com.chriscooke.withinawalk.R.id.Hybrid) {
            if (item.getTitle().equals("Hybrid Map")) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        }

        if (id == com.chriscooke.withinawalk.R.id.Drive) {
            if (item.getTitle().equals("Travel by Car")) {
                MODE = "mode=driving";
                zoomToPoints();
            }
        }
        if (id == com.chriscooke.withinawalk.R.id.Walk)
            if (item.getTitle().equals("Travel on Foot")) {
                MODE = "mode=walking";
                zoomToPoints();
            }



        if (id == com.chriscooke.withinawalk.R.id.Distance){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MapsActivity.this);
            builder1.setMessage("It is " + distance.intValue() + "m to your destination");
            builder1.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();




                        }
                    });
            builder1.show();

        }

        if (id == com.chriscooke.withinawalk.R.id.resetButton) {
            String url = getDirectionsUrl(mylatlng, latlng);
            DownloadTask downloadTask = new DownloadTask();

// Start downloading json data from Google Directions API
            downloadTask.execute(url);
            zoomToPoints();
            // new LongOperation().execute("");
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.chriscooke.withinawalk.R.menu.maps, menu);
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions()
                        .position(latlng)
                        .title("Marker")
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));

        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getFocusedBuilding();
        mMap.getMapType();


        moveToCurrentLocation(latlng);

        //   new LongOperation().execute();
    }


    private void moveToCurrentLocation(LatLng currentLocation) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);


    }

    private void zoomToPoints() {
        try {


            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            //        for (Marker marker : markers) {
            builder.include(mylatlng);
            builder.include(latlng);
            //        }
            LatLngBounds bounds = builder.build();

            int padding = 400; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            mMap.animateCamera(cu);
        } catch (Exception e) {
            ///possible error:
            /// java.lang.NullPointerException: Attempt to invoke interface method 'org.w3c.dom.NodeList org.w3c.dom.Document.getElementsByTagName(java.lang.String)' on a null object reference
        }
    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Travelling Mode
        String mode = "mode=driving";


        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + MODE;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }



    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            if (location != null)
                mylatlng = new LatLng(location.getLatitude(), location.getLongitude());




        }
    };

        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            currentLoc = new Location("Current");
            currentLoc.setLatitude(mylatlng.latitude);
            currentLoc.setLongitude(mylatlng.longitude);
            markerLoc = new Location("Marker");
            markerLoc.setLatitude(latlng.latitude);
            markerLoc.setLongitude(latlng.longitude);
            distance = currentLoc.distanceTo(markerLoc);






            String url = getDirectionsUrl(mylatlng,latlng);

            DownloadTask downloadTask = new DownloadTask();

            downloadTask.execute(url);

        }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {


        String url = getDirectionsUrl(mylatlng, latlng);
        DownloadTask downloadTask = new DownloadTask();

// Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }


    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                JSONDirection parser = new JSONDirection();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(14);
                lineOptions.color(Color.BLUE);

                // Changing the color polyline according to the mode
                if (MODE == "mode=driving") {
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions()
                            .position(latlng)
                            .title("Marker"));

                    lineOptions.color(Color.RED);
                } else if (MODE == "mode=walking") {
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions()
                            .position(latlng)
                            .title("Marker"));
                    lineOptions.color(Color.GREEN);
                }

            }


            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);

        }


}}


    //@Override
   /* public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        mylatlng = new LatLng(latitude, longitude);

        zoomToPoints();

        if (!isDirectionDrawn) {

            new LongOperation().execute("");
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (!isDirectionDrawn) {
            //            zoomToPoints();
            new LongOperation().execute("");
        }
    }



    private class LongOperation extends AsyncTask<String, Void, PolylineOptions> {
        private PolylineOptions rectLine = new PolylineOptions();

        private PolylineOptions getDirection() {
            try {
                DirectionMaps md = new DirectionMaps();

                Document doc = md.getDocument(mylatlng, latlng,
                        DirectionMaps.MODE);

                ArrayList<LatLng> directionPoint = md.getDirection(doc);
                rectLine.width(15).color(Color.BLUE);

                for (int i = 0; i < directionPoint.size(); i++) {
                    rectLine.add(directionPoint.get(i));
                }
                isDirectionDrawn = true;
            } catch (Exception e) {
                e.printStackTrace();
                ///possible error:

                ///java.lang.IllegalStateException: Error using newLatLngBounds(LatLngBounds, int): Map size can't be 0. Most likely, layout has not yet occured for the map view.  Either wait until layout has occurred or use newLatLngBounds(LatLngBounds, int, int, int) which allows you to specify the map's dimensions.
            }
            return rectLine;
        }

        @Override
        protected PolylineOptions doInBackground(String... params) {
            PolylineOptions polylineOptions = null;
            try {
                polylineOptions = getDirection();
            } catch (Exception e) {
                e.printStackTrace();
                Thread.interrupted();
            }
            return polylineOptions;
        }

        @Override
        protected void onPostExecute(PolylineOptions result) {
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you

            mMap.clear();///TODO: clean the path only.

            mMap.addMarker(new MarkerOptions()
                            .position(latlng)
                            .title("Destination")
            );

            mMap.addPolyline(result);
            zoomToPoints();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }


}*/









