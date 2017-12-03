package com.example.denal.husky;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * Created by denal on 10/20/2017.
 */

public class MessageReceiverService extends WearableListenerService {


    private GoogleApiClient mGoogleApiClient;
    private String path;
    private int steps;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
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
            Log.d("testing", "husky");
            if (dataMap.containsKey("steps")) {
                steps = dataMap.getInt("steps");
            }
        }

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);

        DataMap config = putDataMapRequest.getDataMap();

        config.putInt("steps", steps);

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
