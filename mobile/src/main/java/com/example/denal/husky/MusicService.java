package com.example.denal.husky;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by denal on 10/25/2017.
 */

public class MusicService extends Service{


    private static final String TAG = "tutorial_4";


    // we delegate all of the playback related aspects in a separate class
    MusicPlayer musicPlayer;

    public static final String COMPLETE_INTENT = "complete intent";
    public static final String MUSICNAME = "music name";


    // We send song title using broadcast
    public void onUpdateMusicName(String musicName) {
        Intent intent = new Intent(COMPLETE_INTENT);
        intent.putExtra(MUSICNAME, musicName);

        // this broadcast will eventually deliver the updated label to MainActivity
        sendBroadcast(intent);
    }


    // once the service is built...
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate() inside service");
        //we initialize the music player
        musicPlayer = new MusicPlayer(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy() inside service");
        super.onDestroy();
    }


    //service learns that it is unbound.
    // We pause it with a 3 second delay
    @Override
    public boolean onUnbind(Intent intent) {


        // we use Handler as a way to run a delayed operation
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //we pause playback
                pauseMusic();
                //stop the service...
                stopSelf();

            }

            //after 3000ms (3 seconds)
        }, 10000);
        return super.onUnbind(intent);
    }

    public void startMusic() {
        musicPlayer.playMusic();
    }

    public void pauseMusic() {
        musicPlayer.pauseMusic();
    }

    public void resumeMusic() {
        musicPlayer.resumeMusic();
    }

    public void playNext() {
        musicPlayer.playNext();
    }
    public void playPrev() {
        musicPlayer.playPrevious();
    }

    public int getMusicStatus() {
        return musicPlayer.getMusicStatus();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.d(TAG,"onBind() inside service");
        return iBinder;
    }

    // private reference to the private class that return reference to the service
    private final IBinder iBinder = new MyBinder();


    // need to create a small private class that can return reference to the service
    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
}
