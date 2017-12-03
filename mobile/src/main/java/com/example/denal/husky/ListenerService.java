package com.example.denal.husky;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by denal on 10/24/2017.
 */

public class ListenerService extends WearableListenerService
{
    private GoogleApiClient mGoogleApiClient;
    private String path;
    private int steps, heart;
    private String emotion, address, time;


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);


        Intent startIntent = new Intent(this, MainActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(startIntent);
        Log.d("received", "onMessageReceived: " + messageEvent);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API).build();
        }
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());
        path = messageEvent.getPath();

        if (path.equals("/Husky")) {
            if (dataMap.containsKey("steps")) {
                steps = dataMap.getInt("steps");
            }
            if (dataMap.containsKey("emotion")) {
                emotion = dataMap.getString("emotion");
            }
            if (dataMap.containsKey("heart")) {
                heart = dataMap.getInt("heart");
            }
            if (dataMap.containsKey("address")) {
                address = dataMap.getString("address");
            }
            if (dataMap.containsKey("time")) {
                time = dataMap.getString("time");
            }

        }

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);

        DataMap config = putDataMapRequest.getDataMap();

        config.putInt("steps", steps);
        config.putString("emotion", emotion);
        config.putInt("heart", heart);
        config.putString("address", address);
        config.putString("time", time);

        Wearable.DataApi.putDataItem(mGoogleApiClient,
                putDataMapRequest.setUrgent().asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        Log.d("saveConfig", "SaveConfig: " + dataItemResult.getStatus() + ", " +
                                dataItemResult.getDataItem().getUri());
                        mGoogleApiClient.disconnect();
                    }
                });

    }
}
