/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.service;

import java.io.IOException;

import com.majia.guitar.data.MusicEntity;


import android.R.integer;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

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
    
    public static final String EXTRA_PLAY_ID = "play_id";
    
    private MediaPlayer mMediaPlayer;
    private long mPlayingId;
    private long mPrePlayingId;
    
    private IPlayingListener mPlayingListener;
    
    private static final String TAG = "MusicPlayService";
    
    
    
    
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
            long playingId = intent.getLongExtra(EXTRA_PLAY_ID, MusicEntity.INVALIDATE_ID);
            
            setPlayingId(playingId);
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                
                String path = uri.toString();
                

                mMediaPlayer.setDataSource(path);
                
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return START_STICKY_COMPATIBILITY;
        } else if (action.equals(CMD_STOP)) {
            mMediaPlayer.stop();
            setPlayingId(MusicEntity.INVALIDATE_ID);
            return START_STICKY_COMPATIBILITY;
        } else {
            return super.onStartCommand(intent, flags, startId);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }
    
    @Override
    public void onDestroy() {
        
        setPlayingId(MusicEntity.INVALIDATE_ID);
        
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
        Log.d(TAG, "what: " + what + ", extra: " + extra);
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        setPlayingId(MusicEntity.INVALIDATE_ID);
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        setPlayingId(MusicEntity.INVALIDATE_ID);
        return false;
    }
    
    
    /**
     * binder
     * 
     * @author panxu
     * @since 2014-2-18
     */
    public final class MusicBinder extends Binder {

        public MusicPlayService getService() {
            return MusicPlayService.this;
        }
    }

    

    public static interface IPlayingListener {
        void onChanged(long prePlayingId, long currentPlayingId);
    }
    
    public void setPlayingListener(IPlayingListener playingListener) {
        mPlayingListener = playingListener;
    }
    
    public IPlayingListener getPlayingListener() {
        return mPlayingListener;
    }
    
    private void setPlayingId(long playingId) {
        mPrePlayingId = mPlayingId;
        mPlayingId = playingId;
        
        if (mPlayingListener != null) {
            mPlayingListener.onChanged(mPrePlayingId, mPlayingId);
        }
    }
    
    public long getPlayingId() {
        return mPlayingId;
    }
    
    
}
