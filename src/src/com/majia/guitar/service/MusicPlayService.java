/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.service;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

/**
 * 
 * @author panxu
 * @since 2013-12-17
 */
public class MusicPlayService extends Service 
                              implements MediaPlayer.OnPreparedListener, 
                                         MediaPlayer.OnErrorListener,
                                         MediaPlayer.OnCompletionListener, 
                                         MediaPlayer.OnInfoListener,
                                         MediaPlayer.OnSeekCompleteListener {
    
    public static final String CMD_PLAY = "com.majia.guitar.service.play";
    public static final String CMD_STOP = "com.majia.guitar.service.stop";
    
    private MediaPlayer mMediaPlayer;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);

    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Uri uri = intent.getData();
        
        if (action.equals(CMD_PLAY)) {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                
                String path = uri.toString();
                AssetFileDescriptor file = this.getAssets().openFd(path);

                mMediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return START_STICKY_COMPATIBILITY;
        } else if (action.equals(CMD_STOP)) {
            mMediaPlayer.stop();
            return START_STICKY_COMPATIBILITY;
        } else {
            return super.onStartCommand(intent, flags, startId);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        
        super.onDestroy();
    }
    
    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer.start();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    

    
}
