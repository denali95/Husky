package com.example.denal.husky;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by denal on 10/25/2017.
 */

public class MusicCompletionReceiver extends BroadcastReceiver {
    MainActivity mainActivity;
    // constructor takes a reference to main activity so we can communicate with it
    public MusicCompletionReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public MusicCompletionReceiver() {}
    // when we receive the udpate...
    @Override
    public void onReceive(Context context, Intent intent) {

        //...extract the song name
        String musicName = intent.getStringExtra(MusicService.MUSICNAME);
        //...pass it to main activity
        mainActivity.updateName(musicName);
    }
}
