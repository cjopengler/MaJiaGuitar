/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.majia.guitar.movie;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.FrameLayout.LayoutParams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.majia.guitar.MaJiaGuitarApplication;
import com.majia.guitar.R;
import com.majia.guitar.data.MusicEntity;

public class MoviePlayer implements
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        ControllerOverlay.Listener {
    @SuppressWarnings("unused")
    private static final String TAG = "MoviePlayer";

    private static final String KEY_VIDEO_POSITION = "video-position";
    private static final String KEY_RESUMEABLE_TIME = "resumeable-timeout";

    // These are constants in KeyEvent, appearing on API level 11.
    private static final int KEYCODE_MEDIA_PLAY = 126;
    private static final int KEYCODE_MEDIA_PAUSE = 127;

    // Copied from MediaPlaybackService in the Music Player app.
    private static final String SERVICECMD = "com.android.music.musicservicecommand";
    private static final String CMDNAME = "command";
    private static final String CMDPAUSE = "pause";

    private static final long BLACK_TIMEOUT = 500;

    // If we resume the acitivty with in RESUMEABLE_TIMEOUT, we will keep playing.
    // Otherwise, we pause the player.
    private static final long RESUMEABLE_TIMEOUT = 3 * 60 * 1000; // 3 mins

    private Context mContext;
    private final View mRootView;
    private final VideoView mVideoView;
    private final Uri mUri;
    private final Handler mHandler = new Handler();
    private final AudioBecomingNoisyReceiver mAudioBecomingNoisyReceiver;
    private final MovieControllerOverlay mController;
    private final MusicEntity mMusicEntity;

    private long mResumeableTime = Long.MAX_VALUE;
    private int mVideoPosition = 0;
    private boolean mHasPaused = false;
    private int mLastSystemUiVis = 0;

    // If the time bar is being dragged.
    private boolean mDragging;

    // If the time bar is visible.
    private boolean mShowing;
    
    private TextView mMusicTextView;

    private final Runnable mPlayingChecker = new Runnable() {
        @Override
        public void run() {
            if (mVideoView.isPlaying()) {
                mController.showPlaying();
            } else {
                mHandler.postDelayed(mPlayingChecker, 250);
            }
        }
    };

    private final Runnable mProgressChecker = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            mHandler.postDelayed(mProgressChecker, 1000 - (pos % 1000));
        }
    };

    public MoviePlayer(View rootView, final MovieActivity movieActivity,
            Uri videoUri, MusicEntity musicEntity, Bundle savedInstance, boolean canReplay) {
        mContext = movieActivity;
        mRootView = rootView;
        mVideoView = (VideoView) rootView.findViewById(R.id.surface_view);
        mUri = videoUri;
        mMusicEntity = musicEntity;

        mController = new MovieControllerOverlay(mContext);
        
        LinearLayout.LayoutParams layoutParams = 
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 
                                              LayoutParams.WRAP_CONTENT);
        
        /*
        layoutParams.addRule(RelativeLayout.BELOW, R.id.movieTopControlLayout);
        ((ViewGroup)rootView).addView(mController.getView(), layoutParams);*/
        ((ViewGroup)rootView).addView(mController.getView());
        
        
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View topView = layoutInflater.inflate(R.layout.movie_player_title_bar, null);
        ((ViewGroup)rootView).addView(topView, layoutParams);
        
        
        mMusicTextView = (TextView) rootView.findViewById(R.id.musicTextView);
        mMusicTextView.setText(mMusicEntity.getMusicAbstract());
        
        mController.setListener(this);
        mController.setCanReplay(canReplay);

        mVideoView.setOnErrorListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setVideoURI(mUri);
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mController.show();
                return true;
            }
        });

        // The SurfaceView is transparent before drawing the first frame.
        // This makes the UI flashing when open a video. (black -> old screen
        // -> video) However, we have no way to know the timing of the first
        // frame. So, we hide the VideoView for a while to make sure the
        // video has been drawn on it.
        mVideoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mVideoView.setVisibility(View.VISIBLE);
            }
        }, BLACK_TIMEOUT);

        setOnSystemUiVisibilityChangeListener();
        // Hide system UI by default
        showSystemUi(false);

        mAudioBecomingNoisyReceiver = new AudioBecomingNoisyReceiver();
        mAudioBecomingNoisyReceiver.register();

        Intent i = new Intent(SERVICECMD);
        i.putExtra(CMDNAME, CMDPAUSE);
        movieActivity.sendBroadcast(i);

        if (savedInstance != null) { // this is a resumed activity
            mVideoPosition = savedInstance.getInt(KEY_VIDEO_POSITION, 0);
            mResumeableTime = savedInstance.getLong(KEY_RESUMEABLE_TIME, Long.MAX_VALUE);
            mVideoView.start();
            mVideoView.suspend();
            mHasPaused = true;
        } else {
            
            startVideo();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setOnSystemUiVisibilityChangeListener() {
        if (!ApiHelper.HAS_VIEW_SYSTEM_UI_FLAG_HIDE_NAVIGATION) return;

        // When the user touches the screen or uses some hard key, the framework
        // will change system ui visibility from invisible to visible. We show
        // the media control and enable system UI (e.g. ActionBar) to be visible at this point
        mVideoView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                int diff = mLastSystemUiVis ^ visibility;
                mLastSystemUiVis = visibility;
                if ((diff & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0
                        && (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                    mController.show();
                    mRootView.setBackgroundColor(Color.BLACK);
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showSystemUi(boolean visible) {
        if (!ApiHelper.HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE) return;

        int flag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (!visible) {
            // We used the deprecated "STATUS_BAR_HIDDEN" for unbundling
            flag |= View.STATUS_BAR_HIDDEN | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        mVideoView.setSystemUiVisibility(flag);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_VIDEO_POSITION, mVideoPosition);
        outState.putLong(KEY_RESUMEABLE_TIME, mResumeableTime);
    }



    public void onPause() {
        mHasPaused = true;
        mHandler.removeCallbacksAndMessages(null);
        mVideoPosition = mVideoView.getCurrentPosition();
        mVideoView.suspend();
        mResumeableTime = System.currentTimeMillis() + RESUMEABLE_TIMEOUT;
    }

    public void onResume() {
        if (mHasPaused) {
            mVideoView.seekTo(mVideoPosition);
            mVideoView.resume();

            // If we have slept for too long, pause the play
            if (System.currentTimeMillis() > mResumeableTime) {
                pauseVideo();
            }
        }
        mHandler.post(mProgressChecker);
    }

    public void onDestroy() {
        mVideoView.stopPlayback();
        mAudioBecomingNoisyReceiver.unregister();
    }

    // This updates the time bar display (if necessary). It is called every
    // second by mProgressChecker and also from places where the time bar needs
    // to be updated immediately.
    private int setProgress() {
        if (mDragging || !mShowing) {
            return 0;
        }
        int position = mVideoView.getCurrentPosition();
        int duration = mVideoView.getDuration();
        mController.setTimes(position, duration, 0, 0);
        return position;
    }

    private void startVideo() {
        // For streams that we expect to be slow to start up, show a
        // progress spinner until playback starts.
        String scheme = mUri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "rtsp".equalsIgnoreCase(scheme)) {
            mController.showLoading();
            mHandler.removeCallbacks(mPlayingChecker);
            mHandler.postDelayed(mPlayingChecker, 250);
        } else {
            mController.showPlaying();
            mController.hide();
        }

        mVideoView.start();
        setProgress();
    }

    private void playVideo() {
        mVideoView.start();
        mController.showPlaying();
        setProgress();
    }

    private void pauseVideo() {
        mVideoView.pause();
        mController.showPaused();
    }

    // Below are notifications from VideoView
    @Override
    public boolean onError(MediaPlayer player, int arg1, int arg2) {
        mHandler.removeCallbacksAndMessages(null);
        // VideoView will show an error dialog if we return false, so no need
        // to show more message.
        mController.showErrorMessage("");
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mController.showEnded();
        onCompletion();
    }

    public void onCompletion() {
    }

    // Below are notifications from ControllerOverlay
    @Override
    public void onPlayPause() {
        if (mVideoView.isPlaying()) {
            pauseVideo();
        } else {
            playVideo();
        }
    }

    @Override
    public void onSeekStart() {
        mDragging = true;
    }

    @Override
    public void onSeekMove(int time) {
        mVideoView.seekTo(time);
    }

    @Override
    public void onSeekEnd(int time, int start, int end) {
        mDragging = false;
        mVideoView.seekTo(time);
        setProgress();
    }

    @Override
    public void onShown() {
        mShowing = true;
        setProgress();
        showSystemUi(true);
    }

    @Override
    public void onHidden() {
        mShowing = false;
        showSystemUi(false);
    }

    @Override
    public void onReplay() {
        startVideo();
    }

    // Below are key events passed from MovieActivity.
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Some headsets will fire off 7-10 events on a single click
        if (event.getRepeatCount() > 0) {
            return isMediaKey(keyCode);
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mVideoView.isPlaying()) {
                    pauseVideo();
                } else {
                    playVideo();
                }
                return true;
            case KEYCODE_MEDIA_PAUSE:
                if (mVideoView.isPlaying()) {
                    pauseVideo();
                }
                return true;
            case KEYCODE_MEDIA_PLAY:
                if (!mVideoView.isPlaying()) {
                    playVideo();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                // TODO: Handle next / previous accordingly, for now we're
                // just consuming the events.
                return true;
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return isMediaKey(keyCode);
    }

    private static boolean isMediaKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS
                || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE;
    }

    // We want to pause when the headset is unplugged.
    private class AudioBecomingNoisyReceiver extends BroadcastReceiver {

        public void register() {
            mContext.registerReceiver(this,
                    new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        }

        public void unregister() {
            mContext.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mVideoView.isPlaying()) pauseVideo();
        }
    }
}

