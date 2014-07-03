/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;


import java.io.File;

import com.majia.guitar.MaJiaGuitarApplication;
import com.majia.guitar.R;
import com.majia.guitar.data.GuitarData;
import com.majia.guitar.data.GuitarData.IGuitarDataListener;
import com.majia.guitar.data.MusicEntity;
import com.majia.guitar.ui.TitleBarFragment.Args;
import com.majia.guitar.util.SDCardUtil;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author panxu
 * @since 2014-6-30
 */
public class MusicDetailActivity extends FragmentActivity implements IGuitarDataListener {
    public static final String MUSIC_ENTITY = "music_entity";
    private static final String TAG = "MusicDetailActivity";
    
    
    private TextView mNameTextView;
    private TextView mMusicView;
    private Button mPlayButton;
    private Button mDownloadButton;
    
    private MusicEntity mMusicEntity;
    private DownloadManager mDownloadManager;
    
    private static final int MAX_VIDEO_AVILIABLE_SIZE = 100 * 1024 * 1024;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_detail_activity);
        
        GuitarData.getInstance().addListener(this);
        
        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        
        mMusicEntity = getIntent().getParcelableExtra(MUSIC_ENTITY);
        
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Args args = Args.buidArgs().setTitle(R.string.music_detail).setShowBack(true);
        TitleBarFragment titleBarFragment = TitleBarFragment.newInstance(args);
        ft.add(R.id.titleBarContainer, titleBarFragment);
        ft.commit();
        
        mNameTextView = (TextView) findViewById(R.id.nameTextView);
        mMusicView = (TextView) findViewById(R.id.musicTextView);
        mPlayButton = (Button) findViewById(R.id.playButton);
        mDownloadButton = (Button) findViewById(R.id.downloadButton);
        
        mNameTextView.setText(mMusicEntity.getName());
        mMusicView.setText(mMusicEntity.getMusicAbstract());
        
        
        if (isVideoDownloaded()) {
            mPlayButton.setText(R.string.video_local_play);
        } else {
            mPlayButton.setText(R.string.video_online_play);
        }
        
        
        mPlayButton.setOnClickListener(new PlayOnClickListener());
        mDownloadButton.setOnClickListener(new DownloadOnClickListener());
    }
    
    @Override
    protected void onDestroy() {
        
        GuitarData.getInstance().removeListener(this);
        super.onDestroy();
    }
    
    private class PlayOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            
            Uri uri = null;
            
            if (isVideoDownloaded()) {
               uri = Uri.parse(mMusicEntity.getVideoLocal());
               playVideo(mMusicEntity.getVideoLocal());
            } else {
                playVideo(mMusicEntity.getVideoUrl()); 
            }
            
        }
        
    }
    
    private class DownloadOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            //应该做一个异步处理
            if (!isVideoDownloaded()) {
                GuitarData guitarData = GuitarData.getInstance();
                
                long storageVideoDownloadId = guitarData.queryVideoDownloadId(mMusicEntity.getId());
                
                if (storageVideoDownloadId == 0) {
                    
                    if (!SDCardUtil.existSDCard()) {
                        Toast.makeText(MaJiaGuitarApplication.getInstance(), R.string.common_download_error_no_sdcard,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    if (SDCardUtil.getSDFreeSize() < MAX_VIDEO_AVILIABLE_SIZE) {
                        Toast.makeText(MaJiaGuitarApplication.getInstance(), R.string.video_space_not_enough,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mMusicEntity.getVideoUrl()));
                    request.setTitle(mMusicEntity.getName());
                    long videoDownloadId = mDownloadManager.enqueue(request);
                    guitarData.updateVideoDownloadId(mMusicEntity.getId(), videoDownloadId);
                    
                } else {
                    //需要查询确定状态 可能是正在下载
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(storageVideoDownloadId);
                    
                    Cursor cursor = null;
                    int status = 0;
                    int reason = 0;
                    String downloadedUri = "";
                    String localUri = "";
                    String localFile = "";
                    
                    try {
                        
                        cursor = mDownloadManager.query(query);
                        
                        while (cursor.moveToNext()) {

                            status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                            reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                            downloadedUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI));
                            localUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            localFile = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                        }
                    } catch(Exception exception){
                        Log.d(TAG, "excepiton: " + exception.getMessage());
                    }
                    
                    finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                    
                    switch (status) {
                    case DownloadManager.STATUS_SUCCESSFUL:
                        if (isVideoDownloaded()) { 
                            //文件存在 更新数据库并播放
                            GuitarData.getInstance().updateVideoLocalUrl(storageVideoDownloadId, localFile);
                            playVideo(localFile);
                        } else {
                            //文件不存在重新下载
                            startDownloadVideo();
                        }
                        break;
                        
                    case DownloadManager.STATUS_RUNNING:
                        break;
                        
                    case DownloadManager.STATUS_PENDING:
                        break;
                        
                    case DownloadManager.STATUS_PAUSED:
                        break;
                        
                    case DownloadManager.STATUS_FAILED:
                    {
                        switch (reason) {
                        case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                            
                            break;
                            
                        case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                            break;
                            
                        case DownloadManager.ERROR_FILE_ERROR:
                            break;
                            
                        case DownloadManager.ERROR_HTTP_DATA_ERROR:
                            break;
                            
                        case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                            break;
                            
                        case DownloadManager.ERROR_CANNOT_RESUME:
                            break;
                            
                        case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                            break;
                            
                        case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                            break;
                            
                        case DownloadManager.ERROR_UNKNOWN:
                            break;


                        default:
                            break;
                        }
                    }
                        break;
                    
                   
                    default:
                        break;
                    }
                    
                    
                }
                
                //
                
            } else {
                playVideo(mMusicEntity.getVideoLocal());
            }
            
        }
        
    }
    
    private boolean isVideoDownloaded() {
        
        if (TextUtils.isEmpty(mMusicEntity.getVideoLocal())) {
            return false;
        }
        
        
        File file = new File(mMusicEntity.getVideoLocal());

        if (file.exists() && 
            file.length() > 0) {
            return true;
        } else {
            return false;
        }
        
    }

    @Override
    public void onMusicEntityChange(MusicEntity newMusicEntity) {
        mMusicEntity = newMusicEntity;
        
        if (isVideoDownloaded()) {
            mPlayButton.setText(R.string.video_local_play);
        } else {
            mPlayButton.setText(R.string.video_online_play);
        }
    }
    
    private void playVideo(String localUri) {
        Uri uri = Uri.parse(localUri);

        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setDataAndType(uri, "video/mp4");

        startActivity(intent);
    }
    
    private void startDownloadVideo() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mMusicEntity.getVideoUrl()));
        request.setTitle(mMusicEntity.getName())
               .setDestinationInExternalFilesDir(MaJiaGuitarApplication.getInstance(), "video", mMusicEntity.getName() + ".mp4");
        long videoDownloadId = mDownloadManager.enqueue(request);
        GuitarData.getInstance().updateVideoDownloadId(mMusicEntity.getId(), videoDownloadId);
    }

}
