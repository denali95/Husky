package com.example.denal.husky;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by denal on 10/12/2017.
 */

public class HelpActivity extends Activity implements LocationListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private TextView mTextView;
    private static final String TAG = "MainActivity";
    private TextView mTextViewStepCount;
    private TextView mTextViewStepDetect;
    private TextView mTextViewHeart;
    //private SensorManager mSensorManager;
    //private Sensor mLight, mHeart, mSteps;
    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;
    public Button send;
    public Location location;
    private LocationManager locationManager;
    String LOCATIONPROVIDER;
    private LocationRequest mLocationRequest;
    String emotion;
    int heart_rate;
    List<Address> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.round_activity_main);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            emotion = b.getString("emotion");
            heart_rate = b.getInt("heart");
           // Toast.makeText(this, emotion, Toast.LENGTH_SHORT).show();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        // createLocationRequest();
        findViewById(R.id.push).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClicked(v);
            }
        });
        //Connect the GoogleApiClient
        new Thread(new Runnable() {
            @Override
            public void run() {
                mGoogleApiClient.blockingConnect(5000, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                List<Node> nodes = result.getNodes();
                if (nodes.size() > 0) {
                    mPeerId = nodes.get(0).getId();
                    Log.d("watch_connected", "device id:" + mPeerId);
                }
            }
        }).start();

        mTextViewHeart = findViewById(R.id.heart);
        mTextViewStepCount = findViewById(R.id.steps);

    }


    public void onButtonClicked(View view) {
        DataMap dataMap = new DataMap();
        dataMap.putString("emotion", emotion);
        dataMap.putString("address", addresses.get(0).toString());
        dataMap.putInt("heart", heart_rate);
        dataMap.putInt("steps", new Random().nextInt(10000));
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        dataMap.putString("time", currentDateTimeString);
        Log.d("button", "but");
        Toast.makeText(this, dataMap.getString("emotion"), Toast.LENGTH_SHORT).show();
        if (mPeerId != null) {
            Log.d("config", "sending config: " + dataMap);

            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, "/Husky", dataMap.toByteArray())
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.d("watch_message_status", "send config result: " + sendMessageResult.getStatus());
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] vibPattern = {0, 500, 50, 300};
        final int indexInPatternToRepeat = -1;
        vibrator.vibrate(vibPattern, indexInPatternToRepeat);
        //mTextViewHeart.setText("");
        Log.d("RESUME", "resuming");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("pause", "pausing");
        //mSensorManager.unregisterListener(this);
        //mSensorManager.unregisterListener(this);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(250);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    /*public void register() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "not working", Toast.LENGTH_SHORT);
            locationManager.requestLocationUpdates(LOCATIONPROVIDER, 5000, 0, this);
        } else {
            Toast.makeText(this, "not working", Toast.LENGTH_SHORT);

        }
    }

    public void unregister() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
    }*/

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("connect", "connected");
        // Create the LocationRequest object
        LocationRequest locationRequest = LocationRequest.create();
        // Use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 2 seconds
        locationRequest.setInterval(TimeUnit.SECONDS.toMillis(2));
        // Set the fastest update interval to 2 seconds
        locationRequest.setFastestInterval(TimeUnit.SECONDS.toMillis(2));
        // Set the minimum displacement
        locationRequest.setSmallestDisplacement(2);

        // Register listener using the LocationRequest object
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
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("changed", "" + location.getAltitude());
        Geocoder gc = new Geocoder(getApplicationContext());
        try {
            addresses = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "" + addresses.get(0), Toast.LENGTH_SHORT).show();
        mTextViewHeart.setText("" + location.getLatitude());

    }

    /*@Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }*/
}
