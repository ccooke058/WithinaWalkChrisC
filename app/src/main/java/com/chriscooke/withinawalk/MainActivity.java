package com.chriscooke.withinawalk;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.flurry.android.FlurryAgent;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private int PLACE_PICKER_REQUEST = 1;
    private int shake = 1;
    DBhandler db = new DBhandler(this);
    private AutoCompleteAdapter mAdapter;
    private TextView mTextView;
    private TextView textView;
    private ListView locolistview;
    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;
    private String content = "";
    private static LatLng latlng = new LatLng(0.0,0.0);
    private static double lat = 0.0;
    private static double lng = 0.0;
    private CharSequence DirName;
    private static loco loco = new loco();
   // private GoogleMap mMap;
    private MapsActivity myMap;
    private Collection<Integer> place = new ArrayList<Integer>();
    private PlaceFilter placeFilter = new PlaceFilter(place,true,null,null);

   // private SupportMapFragment mapFragment;

    //private Sensor accelerometer;
   // private long lastUpdate = 0;
   // private float last_x, last_y, last_z;
   // private static final int SHAKE_THRESHOLD = 600;


    private GoogleApiClient client;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set view to main page
        setContentView(com.chriscooke.withinawalk.R.layout.activity_main);
        latlng = null;
        FlurryAgent.setLogEnabled(false);
        // init Flurry
        FlurryAgent.init(this, "TPHNQ2KHCP55XF752X3M");

        mTextView = (TextView) findViewById(com.chriscooke.withinawalk.R.id.mTextView);
        textView = (TextView) findViewById(com.chriscooke.withinawalk.R.id.textView);
        textView.setVisibility(View.INVISIBLE);

        locolistview = (ListView) findViewById(com.chriscooke.withinawalk.R.id.locolistView);
        locolistview.setVisibility(View.INVISIBLE);

        mAdapter = new AutoCompleteAdapter(this);
        Toolbar toolBar = (Toolbar) findViewById(com.chriscooke.withinawalk.R.id.toolbar);
        setSupportActionBar(toolBar);




        final List<loco> lstLoco = db.getAlllocos();
        ListView myListView = (ListView) findViewById(com.chriscooke.withinawalk.R.id.locolistView);
        // get data from the table by the ListAdapter
        ListAdapter customAdapter = new ListAdapter(this, com.chriscooke.withinawalk.R.layout.locolist, lstLoco);
        myListView.setAdapter(customAdapter);
        locolistview = (ListView) findViewById(com.chriscooke.withinawalk.R.id.locolistView);


        locolistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                Bundle bundle = new Bundle();
                bundle.putSerializable("value", (loco) locolistview.getItemAtPosition(position));

                Intent myMap = new Intent(MainActivity.this, MapsActivity.class);
                myMap.putExtras(bundle);
                startActivity(myMap);

            }

        });

        locolistview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {


            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setMessage("Are you sure you want to delete this item?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                db.deleteloco((loco) locolistview.getItemAtPosition(position));
                                locolistview = (ListView) findViewById(com.chriscooke.withinawalk.R.id.locolistView);
                                List<loco> lstLoco = db.getAlllocos();
                                ListView myListView = (ListView) findViewById(com.chriscooke.withinawalk.R.id.locolistView);
                                ListAdapter customAdapter = new ListAdapter(MainActivity.this, com.chriscooke.withinawalk.R.layout.locolist, lstLoco);
                                myListView.setAdapter(customAdapter);

                                locolistview.setVisibility(View.VISIBLE);
                                textView = (TextView) findViewById(com.chriscooke.withinawalk.R.id.textView);
                                textView.setVisibility(View.VISIBLE);
                                shake = 0;


                            }
                        });
                builder1.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();



                return true;
            }
        });


        client = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, 0, this)
                .addApi(AppIndex.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


            // Obtain the SupportMapFragment and get notified when the map is ready to be used.


            //Creating Sensor manager and the accelerometer
            mSensorManager=(SensorManager)

            getSystemService(Context.SENSOR_SERVICE);

            mSensorListener=new

            ShakeEventListener();

            mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener()

            {


                public void onShake () {
                shake = shake + 1;

                if (shake == 1) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                    builder1.setMessage("Are you sure you want to clear the screen?");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    finish();
                                }
                            });
                    builder1.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }

            }
            }

            );

        }

    public static LatLng addMark() {


        return latlng;

    }





    public void onTextViewClick(View v) {

        if (latlng != null) {
            Intent myMap = new Intent(this, MapsActivity.class);
            startActivity(myMap);
        }
    }

    public void onPickButtonClick(View v) throws GooglePlayServicesRepairableException {

        try {


            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();


            Intent intent;

            intent = intentBuilder.build(this);



            // Start the intent by requesting a result,
            // identified by a request code.
            startActivityForResult(intent, PLACE_PICKER_REQUEST);

        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

    }



    private void displayPlacePicker() {
        if( client == null || !client.isConnected() )
            return;


        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();




        try {
            startActivityForResult( builder.build(getApplicationContext()), PLACE_PICKER_REQUEST);
        } catch ( GooglePlayServicesRepairableException e ) {
            Log.d("WithinAWalk", "GooglePlayServicesRepairableException thrown");
        } catch ( GooglePlayServicesNotAvailableException e ) {
            Log.d("WithinAWalk", "GooglePlayServicesNotAvailableException thrown");
        }
    }

    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if( requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK ) {
            displayPlace( PlacePicker.getPlace( data, this ) );

        }
        DBhandler db = new DBhandler(this);
        List<loco> lstLoco = db.getAlllocos();
        ListView myListView = (ListView) findViewById(com.chriscooke.withinawalk.R.id.locolistView);

        // get data from the table by the ListAdapter
        ListAdapter customAdapter = new ListAdapter(this, com.chriscooke.withinawalk.R.layout.locolist,lstLoco );
        myListView .setAdapter(customAdapter);
    }




    private void displayPlace( Place place ) {

        DBhandler db = new DBhandler(this);
        lat = place.getLatLng().latitude;
        lng = place.getLatLng().longitude;
        latlng = place.getLatLng();
        loco.setName((String) place.getName());
        loco.setAddress((String) place.getAddress());
        loco.setPhoneNum((String) place.getPhoneNumber());
        loco.setLat(lat);
        loco.setLng(lng);
        db.addloco(loco);

        DirName = place.getAddress();


        if (place == null)
            return;

        content = "";
        if (!TextUtils.isEmpty(place.getName())) {

            content += "Click here for Directions:" + "\n"+
                    "\n" + "Name: " + place.getName() + "\n";



        }
        if (!TextUtils.isEmpty(place.getAddress())) {
            content += "Address: " + place.getAddress() + "\n";
        }
        if (!TextUtils.isEmpty(place.getPhoneNumber())) {
            content += "Phone: " + place.getPhoneNumber();
        }


        mTextView.setText(content);

    }

    private void findPlaceById( String id ) {
        if( TextUtils.isEmpty( id ) || client == null || !client.isConnected() )
            return;

        Places.GeoDataApi.getPlaceById( client, id ) .setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(PlaceBuffer places) {
                if (places.getStatus().isSuccess()) {
                    Place place = places.get(0);
                    displayPlace(place);
                    mAdapter.clear();
                }

                //Release the PlaceBuffer to prevent a memory leak
                places.release();
            }
        });
    }


    public boolean onOptionsItemSelected( MenuItem item ) {
        int id = item.getItemId();
        CharSequence title = item.getTitle();


        if (id == com.chriscooke.withinawalk.R.id.action_place_picker) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?f=d&daddr=" + DirName));
            intent.setComponent(new ComponentName("com.google.android.apps.maps",
                    "com.google.android.maps.MapsActivity"));
            startActivity(intent);


            return true;
        } else if (id == com.chriscooke.withinawalk.R.id.action_guess_current_place) {
            guessCurrentPlace();
            return true;
        }else if (id == com.chriscooke.withinawalk.R.id.action_settings) {
            if (item.getTitle().equals("Delete History")){
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setMessage("Are you sure you want to Delete ALL History?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                db.deleteAll();

                                locolistview = (ListView) findViewById(com.chriscooke.withinawalk.R.id.locolistView);
                                List<loco> lstLoco = db.getAlllocos();
                                ListView myListView = (ListView) findViewById(com.chriscooke.withinawalk.R.id.locolistView);
                                ListAdapter customAdapter = new ListAdapter(MainActivity.this, com.chriscooke.withinawalk.R.layout.locolist, lstLoco);
                                myListView.setAdapter(customAdapter);
                                locolistview.setVisibility(View.INVISIBLE);
                                textView = (TextView) findViewById(com.chriscooke.withinawalk.R.id.textView);
                                textView.setVisibility(View.INVISIBLE);
                                shake = 1;


                            }
                        });
                builder1.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();



                return true;
            }



            Context context = getApplicationContext();
            CharSequence text = "Hint: Shake to Clear the Screen";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            DBhandler db = new DBhandler(this);
            List<loco> lstLoco = db.getAlllocos();
            ListView myListView = (ListView) findViewById(com.chriscooke.withinawalk.R.id.locolistView);

            // get data from the table by the ListAdapter
            ListAdapter customAdapter = new ListAdapter(this, com.chriscooke.withinawalk.R.layout.locolist,lstLoco );
            myListView .setAdapter(customAdapter);
            locolistview = (ListView) findViewById(com.chriscooke.withinawalk.R.id.locolistView);
            locolistview.setVisibility(View.VISIBLE);
            textView = (TextView) findViewById(com.chriscooke.withinawalk.R.id.textView);
            textView.setVisibility(View.VISIBLE);
            shake = 0;



        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {

        super.onStart();
        if (client != null)
            client.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.chriscooke.withinawalk/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }


    private void guessCurrentPlace() {
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(client, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {

                PlaceLikelihood placeLikelihood = likelyPlaces.get(0);
                String content = "";
                if (placeLikelihood != null && placeLikelihood.getPlace() != null && !TextUtils.isEmpty(placeLikelihood.getPlace().getName()))
                    content = "Location: " + placeLikelihood.getPlace().getName() + "\n";
                if (placeLikelihood != null)
                    content += "This is: " + (int) (placeLikelihood.getLikelihood() * 100) + "% accurate";
                mTextView.setText(content);

                likelyPlaces.release();

            }
        });
    }
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate(com.chriscooke.withinawalk.R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onStop() {

        if (client != null && client.isConnected()) {
            client.disconnect();
        }
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.chriscooke.withinawalk/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);


    }


    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
        super.onPause();
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not in use
    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void finish() {

        Intent intent = new Intent (MainActivity.this, MainActivity.class);
        startActivity(intent);

    }
}
