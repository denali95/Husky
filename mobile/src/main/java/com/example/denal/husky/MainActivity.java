package com.example.denal.husky;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Geocoder;
import android.media.Image;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.ConnectionCallbacks,DataApi.DataListener,ServiceConnection{

    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;
    public Button link;
    TextView music;
    MusicService musicService;
    MusicCompletionReceiver musicCompletionReceiver;
    Intent startMusicServiceIntent;
    boolean isInitialized = false;
    boolean isBound = false;
    private ImageView imageView;
    int imageIndex;
    public FragmentTransaction trans;

    public SelectionFrag selectionFrag;


    static final int[] PICS = new int[]{
            R.mipmap.beach,
            R.mipmap.forest,
            R.mipmap.husky_wink
    };

    // play/pause button
    Button play,next,prev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addConnectionCallbacks(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        FragmentManager man = getSupportFragmentManager();
        trans = man.beginTransaction();
        selectionFrag = (SelectionFrag) man.findFragmentByTag("select");
        if (selectionFrag == null) {
            Log.d("frag", "fragment");
            selectionFrag = new SelectionFrag();
            trans.add(R.id.fragment_container, selectionFrag, "select");
            trans.show(selectionFrag);
            trans.commit();
        }

        link = findViewById(R.id.link);
        link.setOnClickListener(this);
        link.setVisibility(View.GONE);

        imageView = (ImageView)findViewById(R.id.beach);


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


        //setting up the views
        play = (Button) findViewById(R.id.play);
        next = (Button) findViewById(R.id.next);
        prev = (Button) findViewById(R.id.prev);
        music = (TextView) findViewById(R.id.music);

        //setting up the buttons with the callback
        play.setOnClickListener(this);
        prev.setOnClickListener(this);
        next.setOnClickListener(this);
        play.setVisibility(View.GONE);
        prev.setVisibility(View.GONE);
        next.setVisibility(View.GONE);

        //restoring info for the boolean and the 'song' label
        if (savedInstanceState != null) {
            isInitialized = savedInstanceState.getBoolean(INITIALIZE_STATUS);
            music.setText(savedInstanceState.getString(MUSIC_PLAYING));
        }


        // preparing the intent object that will launch the service
        startMusicServiceIntent = new Intent(this, MusicService.class);

        // if not started we go ahead and start it
        if (!isInitialized) {
            startService(startMusicServiceIntent);
            isInitialized = true;
        }

        //also registering the broadcast receiver
        musicCompletionReceiver = new MusicCompletionReceiver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("pausing", "onPause()");
        // unbinding from service
        // the service will have onUnbind() called after this
        // inside that method we will handle the logic of unbinding
        if (isBound) {
            unbindService(this);
            isBound = false;
        }
        //remove the broadcast receiver
        unregisterReceiver(musicCompletionReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //is service is initialized and not boud we bind to it
        if (isInitialized && !isBound) {
            bindService(startMusicServiceIntent, this, Context.BIND_AUTO_CREATE);
        }

        // registering the broadcast receiver
        registerReceiver(musicCompletionReceiver, new IntentFilter(MusicService.COMPLETE_INTENT));
    }

    //keys for retrieving info on restore
    public static final String INITIALIZE_STATUS = "is_initialized";
    public static final String MUSIC_PLAYING = "is_music_playing";

    // saving state of the mplayer and service
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(INITIALIZE_STATUS, isInitialized);
        outState.putString(MUSIC_PLAYING, music.getText().toString());
    }

    @Override
    public void onClick(View view) {
        DataMap dataMap = new DataMap();
        dataMap.putInt("steps", new Random().nextInt(10000));

        if (view.getId() == R.id.link) {
            link.setVisibility(View.GONE);
            play.setVisibility(View.GONE);
            next.setVisibility(View.GONE);
            prev.setVisibility(View.GONE);
            FragmentManager man = getSupportFragmentManager();
            FragmentTransaction transaction = man.beginTransaction();
            transaction.show(man.findFragmentByTag("select")).commit();
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
        if (isBound) {
            if(view.equals(play)){
                Log.d("tst", "" + view.getId());
                switch (musicService.getMusicStatus()) {
                    // 0 - means not playing, so we start it then label the button 'pause'
                    case 0:
                        musicService.startMusic();
                        play.setText("Pause");
                        break;
                    // 1 - means playing, we pause it and then label the button 'resume'
                    case 1:
                        musicService.pauseMusic();
                        play.setText("Resume");
                        break;
                    case 2:
                        // 2 - means paused, we resume it and then label the button 'pause'
                        musicService.resumeMusic();
                        play.setText("Pause");
                        break;
                }
        }
        if(view.equals(next)) {
            musicService.playNext();
            Log.d("next", "nexting");
            imageIndex = (imageIndex + 1) % 3;
            imageView.setImageResource(PICS[imageIndex]);
        }
        if(view.equals(prev)) {
            musicService.playPrev();
            Log.d("prev", "preving");
            imageIndex = Math.abs((imageIndex - 1) % 3);
            imageView.setImageResource(PICS[imageIndex]);
        }
        Log.d("yo", "" + imageIndex);
    }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (int i = 0; i < dataEvents.getCount(); i++) {
            DataEvent event = dataEvents.get(i);
            DataMap dataMap = DataMap.fromByteArray(event.getDataItem().getData());
            Log.d("test_watchface", "onDataChanged():" + dataMap);
           // play.performClick();
            String time, emotion, address;
            int heart;
            /*if (dataMap.containsKey("steps")){
                link.setText("" +dataMap.getInt("steps"));
            }*/
            if (dataMap.containsKey("emotion")) {
                emotion = dataMap.getString("emotion");
                //link.setText(dataMap.getString("emotion"));
            }
            else {
                emotion = "No Emotion";
            }
            if (dataMap.containsKey("heart")) {
                heart = dataMap.getInt("heart");
            }
            else {
                heart = -1;
            }
            if (dataMap.containsKey("address")) {
                address = dataMap.getString("address");
                Toast.makeText(this, dataMap.getString("address"), Toast.LENGTH_LONG).show();
            }
            else {
                address = "No Address";
            }
            if (dataMap.containsKey("time")) {
                time = dataMap.getString("time");
            }
            else {
                time = "No Time";
            }
            DocumentReference mDocRef = FirebaseFirestore.getInstance().document("UserA/" + time);
            Map<String, Object> dataEntry = new HashMap<String, Object>();
            dataEntry.put("Time", time);
            dataEntry.put("Address", address);
            dataEntry.put("Emotion", emotion);
            dataEntry.put("Heart Rate", heart);

            mDocRef.set(dataEntry).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("saving", "Document was saved");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("saving", "Document wasn't saved");
                }
            });

        }
    }

    public void updateName(String musicName) {
        music.setText("You are listening: " + musicName);
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder_to_service) {
// the biner object gets us an object that we use to extract a reference to service
        MusicService.MyBinder binder = (MusicService.MyBinder) binder_to_service;

        // extracting the service object
        musicService = binder.getService();

        // it is bound so we set the boolean
        isBound = true;

        //depending on what it is doing (start, pause, resume) we set the label on the button
        switch (musicService.getMusicStatus()) {
            case 0:
                play.setText("Start");
                break;
            case 1:
                play.setText("Pause");
                break;
            case 2:
                play.setText("Resume");
                break;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
        isBound = false;
    }
}
