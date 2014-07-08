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
import com.majia.guitar.ui.CommonTitleBarFragment.Args;
import com.majia.guitar.util.SDCardUtil;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
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
    
    
    private TextView mMusicView;
    private ImageView mVideoPlayImageView;
    
    private MusicEntity mMusicEntity;
    private DownloadManager mDownloadManager;
    
    private static final int MAX_VIDEO_AVILIABLE_SIZE = 100 * 1024 * 1024;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.music_detail_activity);
        
        GuitarData.getInstance().addListener(this);
        
        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        
        mMusicEntity = getIntent().getParcelableExtra(MUSIC_ENTITY);
        
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Args args = Args.buidArgs().setTitle(mMusicEntity.getName()).setShowBack(true);
        CommonTitleBarFragment titleBarFragment = CommonTitleBarFragment.newInstance(args);
        ft.add(R.id.titleBarContainer, titleBarFragment);
        ft.commit();
        
        mMusicView = (TextView) findViewById(R.id.musicTextView);
        mVideoPlayImageView = (ImageView) findViewById(R.id.videoPlayImageView);
        
        
        mMusicView.setText(mMusicEntity.getMusicAbstract());
        
        mVideoPlayImageView.setOnClickListener(new VideoPlayOnClickListener());
        
    }
    
    private class VideoPlayOnClickListener implements android.view.View.OnClickListener {

		@Override
		public void onClick(View view) {
			if (isVideoDownloaded()) {
				//直接播放本地
				 playVideo(mMusicEntity.getVideoLocal());
			} else {
				// 弹出dialog询问下载还是在线播放还是取消
				AlertDialog.Builder builder = new AlertDialog.Builder(
						MusicDetailActivity.this);

				builder.setMessage(R.string.video_instrument)
						.setTitle(mMusicEntity.getName())
						.setIcon(R.drawable.icon_music)
						.setNegativeButton(R.string.video_online_play,
								new OnlinePlayOnClickListener())
						.setNeutralButton(R.string.video_download,
								new OnVideoDownloadClickListener())
						.setPositiveButton(R.string.cancle,
								new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0,
									int arg1) {

							}
						});
						

				builder.create().show();
			}
		}
    	
    }
    
    @Override
    protected void onDestroy() {
        
        GuitarData.getInstance().removeListener(this);
        super.onDestroy();
    }
    
    
    private class OnlinePlayOnClickListener implements OnClickListener {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			if (isVideoDownloaded()) {
				playVideo(mMusicEntity.getVideoLocal());
			} else {
				playVideo(mMusicEntity.getVideoUrl());
			}
		}
    	
    }
    
   private class OnVideoDownloadClickListener implements OnClickListener {

	@Override
	public void onClick(DialogInterface arg0, int arg1) {


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
                
                startDownloadVideo();
                
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
                case DownloadManager.STATUS_PENDING:
                case DownloadManager.STATUS_PAUSED:
                case DownloadManager.STATUS_FAILED:
                {
                    switch (reason) {
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                    case DownloadManager.ERROR_FILE_ERROR:
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                    case DownloadManager.ERROR_CANNOT_RESUME:
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                    case DownloadManager.ERROR_UNKNOWN:

                    default:
                    	 try {
                             Intent downloadManagerIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                             downloadManagerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                             startActivity(downloadManagerIntent);
                         } catch (ActivityNotFoundException exception) {
                             Toast.makeText(MaJiaGuitarApplication.getInstance(), 
                                     R.string.video_download_view_exception, 
                                     Toast.LENGTH_LONG).show();
                         }
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
    public void onMusicEntityChange(final MusicEntity newMusicEntity) {
    	runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				mMusicEntity = newMusicEntity;
			}
		});
        
    }
    
    private void playVideo(String localUri) {
        Uri uri = Uri.parse(localUri);

        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setDataAndType(uri, "video/mp4");

        try {
        	startActivity(intent);
        } catch (ActivityNotFoundException activityNotFoundException) {
        	Toast.makeText(MaJiaGuitarApplication.getInstance(), 
        				   R.string.video_activity_not_found, 
        				   Toast.LENGTH_LONG).show();
        }
    }
    
    private void startDownloadVideo() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mMusicEntity.getVideoUrl()));
        request.setTitle(mMusicEntity.getName())
               .setDestinationInExternalFilesDir(MaJiaGuitarApplication.getInstance(), "video", mMusicEntity.getName() + ".mp4");
        long videoDownloadId = mDownloadManager.enqueue(request);
        GuitarData.getInstance().updateVideoDownloadId(mMusicEntity.getId(), videoDownloadId);
    }

}
