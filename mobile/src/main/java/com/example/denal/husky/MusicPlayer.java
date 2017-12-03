package com.example.denal.husky;

import android.media.MediaPlayer;

/**
 * Created by denal on 10/25/2017.
 */

public class MusicPlayer implements MediaPlayer.OnCompletionListener{
    //audio files
    static final String[] MUSICPATH = new String[]{
            "aeriths_theme",
            "gusty_gardens",
            "journey"
    };


    //titles to the files
    static final String[] MUSICNAME = new String[]{
            "Aerith's Theme",
            "Gusty Gardens",
            "Journey"
    };

    //reference of the service
    private MusicService musicService;

    //Android's media player
    MediaPlayer player;

    // seek possition
    int currentPosition = 0;

    //selected song
    int musicIndex = 0;


    //id of the file in the folder
    int resID=-1;

    //0: before starts 1: playing 2: paused
    private int musicStatus = 0;

    public int getMusicStatus() {
        return musicStatus;
    }

    public String getMusicName() {
        return MUSICNAME[musicIndex];
    }


    //starts playing
    public void playMusic() {

        //build the media player
        //play from res/raw directly
        try{
            player = new MediaPlayer();

            int resID=musicService.getResources().getIdentifier(MUSICPATH[musicIndex], "raw", musicService.getPackageName());

            player=MediaPlayer.create(musicService,resID);
            player.start();
            musicService.onUpdateMusicName(getMusicName());
            player.setOnCompletionListener(this);


        }
        catch(Exception ex) {
            ex.printStackTrace();
        }


//        //set the mode -- we are streaming from a URL
//        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        try {
//            player.setDataSource(MUSICPATH[musicIndex]);
//            player.prepare();
//            //setting up a listener so we can control and see what is going on with the player
//            player.setOnCompletionListener(this);
//            player.start();
//            //sending the current song title
//            musicService.onUpdateMusicName(getMusicName());
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
        musicStatus = 1;
    }

    public void pauseMusic() {
        if (player != null && player.isPlaying()) {
            // pause the player
            player.pause();
            //save current position
            currentPosition = player.getCurrentPosition();
            //update status
            musicStatus = 2;
        }
    }

    public void resumeMusic() {
        if (player != null) {
            //reusme to the saved position
            player.seekTo(currentPosition);
            //start player
            player.start();
            //udpate status
            musicStatus = 1;
        }
    }


    public void playNext(){
        player.release();
        player = null;
        musicIndex = (musicIndex + 1) % MUSICNAME.length;
        //start over -- infinite loop
        playMusic();
    }

    public void playPrevious(){
        player.release();
        player = null;
        musicIndex = Math.abs((musicIndex - 1) % MUSICNAME.length);
        //start over -- infinite loop
        playMusic();

    }
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //once the song is done, we switch to the next song
        musicIndex = (musicIndex + 1) % MUSICNAME.length;
        //clean out the music player
        player.release();
        player = null;
        //start over -- infinite loop
        playMusic();
    }

    public MusicPlayer(MusicService musicService) {
        this.musicService = musicService;
    }

}
