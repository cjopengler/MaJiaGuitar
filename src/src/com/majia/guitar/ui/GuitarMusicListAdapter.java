/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.majia.guitar.MaJiaGuitarApplication;
import com.majia.guitar.R;
import com.majia.guitar.data.IGuitarData;
import com.majia.guitar.data.MusicEntity;
import com.majia.guitar.data.download.DownloadInfo;
import com.majia.guitar.data.download.IDownloadData.IDownloadListener;
import com.majia.guitar.data.download.MusicDownloadData;
import com.majia.guitar.service.MusicDownloadService;
import com.majia.guitar.service.MusicPlayService;
import com.majia.guitar.service.MusicPlayService.IPlayingListener;
import com.majia.guitar.service.MusicPlayService.MusicBinder;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author panxu
 * @since 2013-12-17
 */
public class GuitarMusicListAdapter extends BaseAdapter 
                                    implements IDownloadListener,
                                               IFragmentLifeCycle,
                                               IPlayingListener {
    
    private static final String TAG = "GuitarMusicListAdapter";

    private final Context mContext;
    private final List<MusicEntity> mMusicEntities;
    private ListView mListView;
    private ProgressDialog mProgressDialog;
    private Handler mUIHandler;
    
    private ServiceConnection mMusicPlayConnection;
    private MusicPlayService mMusicPlayService;
    
    public GuitarMusicListAdapter(Context context, ListView listView) {
        mContext = context;
        mMusicEntities = new ArrayList<MusicEntity>();
        mListView = listView;
        
        mUIHandler = new Handler(Looper.getMainLooper());
        
        mMusicPlayConnection = null;
        mMusicPlayService = null;
        
    }
    
    public void update(List<MusicEntity> musicEntities) {
        mMusicEntities.clear();
        mMusicEntities.addAll(musicEntities);
        
        notifyDataSetChanged();
    }
    
    @Override
    public int getCount() {
        
        return mMusicEntities.size();
    }

    @Override
    public Object getItem(int position) {
        return mMusicEntities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mMusicEntities.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.guitar_music_item, null);
        }
        
        
        
        MusicEntity musicEntity = mMusicEntities.get(position);
        
        convertView.setTag(musicEntity.getId());
        
        TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        nameTextView.setText(musicEntity.getName());
        
        TextView musicTextView = (TextView) convertView.findViewById(R.id.musicTextView);
        musicTextView.setText(musicEntity.getMusicAbstract());
        
        ImageView songImageView = (ImageView) convertView.findViewById(R.id.songImageView);
        songImageView.setTag(musicEntity);
        
        if (mMusicPlayService != null) {
            long playingId = mMusicPlayService.getPlayingId();
            setPlayStatus(MusicEntity.INVALIDATE_ID, playingId);
        }
        
        songImageView.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                MusicEntity musicEntity = (MusicEntity) v.getTag();
                
                if (!playMusic(musicEntity)) {
                    //下载
                    if (mProgressDialog == null) {
                        mProgressDialog = new ProgressDialog(mContext);
                        mProgressDialog.setTitle(R.string.downloading);
                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mProgressDialog.setCancelable(false);
                    }
                    
                    mProgressDialog.show();
                    
                    Intent intent = new Intent(mContext, MusicDownloadService.class);
                    intent.putExtra(MusicDownloadService.INTENT_MUSIC_ENTITY, musicEntity);
                    mContext.startService(intent);
                }
                
            }
        });
        
        
        
        return convertView;
    }

    @Override
    public void onDownload(final long id, final DownloadInfo downloadInfo) {
        
        mUIHandler.post(new Runnable() {
            
            @Override
            public void run() {
                int status = downloadInfo.getStatus();
                
                View view = mListView.findViewWithTag(id);
                
                Log.d(TAG, downloadInfo.toString());
                
                if (view != null) {
                
                    
                
                    switch (status) {
                    case DownloadInfo.DOWNLOAD_IS_ONGOING:
                        mProgressDialog.setMax((int) downloadInfo.getTotalSize());
                        
                        mProgressDialog.setProgress((int) downloadInfo.getDownloadSize());
                        break;
                        
                    case DownloadInfo.DOWNLOAD_FINISH_SUCCESS:
                        mProgressDialog.dismiss();
                        
                        ImageView songImageView = (ImageView) view.findViewById(R.id.songImageView);
                        
                        MusicEntity oldMusicEntity = (MusicEntity) songImageView.getTag();
                        
                        MusicEntity updateMusicEntity = new MusicEntity(oldMusicEntity.getId(),
                                                                        oldMusicEntity.getMusicId(),
                                                                        oldMusicEntity.getName(), 
                                                                        oldMusicEntity.getMusicAbstract(), 
                                                                        oldMusicEntity.getDetail(), 
                                                                        oldMusicEntity.getDetailUrl(), 
                                                                        oldMusicEntity.getDetailLocal(), 
                                                                        oldMusicEntity.getSoundUrl(), 
                                                                        downloadInfo.getDownloadPath(), 
                                                                        oldMusicEntity.getDifficulty(), 
                                                                        oldMusicEntity.getVideoUrl(), 
                                                                        oldMusicEntity.getVideoUrl());
                        
                        int oldIndex = mMusicEntities.indexOf(oldMusicEntity);
                        
                        mMusicEntities.remove(oldIndex);
                        mMusicEntities.add(oldIndex, updateMusicEntity);
                        playMusic(updateMusicEntity);
                        
                        notifyDataSetChanged();
                        
                        break;
                        
                    case DownloadInfo.DOWNLOAD_FINISH_ERROR:
                        mProgressDialog.dismiss();
                        Toast.makeText(MaJiaGuitarApplication.getInstance(), R.string.about_download_error, Toast.LENGTH_LONG).show();
                        break;

                    case DownloadInfo.DOWNLOAD_FINISH_IO_ERROR:
                        mProgressDialog.dismiss();
                        Toast.makeText(MaJiaGuitarApplication.getInstance(), R.string.about_download_error_io, Toast.LENGTH_LONG).show();
                        break;
                    
                    case DownloadInfo.DOWNLOAD_FINISH_NET_ERROR:
                        mProgressDialog.dismiss();
                        Toast.makeText(MaJiaGuitarApplication.getInstance(), R.string.about_download_error_net, Toast.LENGTH_LONG).show();
                        break;
            
                    default:
                        break;
                    }
                }
                
            }
        });
       
    }

    @Override
    public void onCreate(Fragment fragment) {
        MusicDownloadData.getInstance().addListener(this);
        
        if (mMusicPlayConnection == null) {
            mMusicPlayConnection = new MusicServiceConnection();
            
            try {
            
            boolean bindOK = mContext.bindService(new Intent(mContext, MusicPlayService.class), 
                             mMusicPlayConnection, 
                             Service.BIND_AUTO_CREATE);
            
            Log.d(TAG, "bindOK: " + bindOK);
            } catch (Exception exception) {
                Log.d(TAG, "exception e: " + exception.getMessage());
            }
        }
        
    }

    @Override
    public void onDestroy(Fragment fragment) {
        MusicDownloadData.getInstance().removeListener(this);
        
        if (mMusicPlayService != null) {
            mMusicPlayService.setPlayingListener(null);
        }
        
        mContext.unbindService(mMusicPlayConnection);
        mMusicPlayConnection = null;
        mMusicPlayService = null;
    }
    
    private boolean playMusic(MusicEntity musicEntity) {
        boolean isPlayed = false;
        if (!TextUtils.isEmpty(musicEntity.getSoundLocal())) {
            File musicFile = new File(musicEntity.getSoundLocal());
            
            if (musicFile != null &&
                musicFile.exists() &&
                musicFile.length() > 0) {
                
                Uri uri = Uri.parse(musicEntity.getSoundLocal());
            
                Intent serviceIntent = new Intent(MusicPlayService.CMD_PLAY, uri, mContext, MusicPlayService.class);
                serviceIntent.putExtra(MusicPlayService.EXTRA_PLAY_ID, musicEntity.getId());
                mContext.startService(serviceIntent);
                
                isPlayed = true;
            }
        }
        
        return isPlayed;
    }

    private class MusicServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMusicPlayService = ((MusicBinder) service).getService();
            
            mMusicPlayService.setPlayingListener(GuitarMusicListAdapter.this);
            
            long playingId = mMusicPlayService.getPlayingId();
            
            setPlayStatus(MusicEntity.INVALIDATE_ID, playingId);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicPlayConnection = null;
            mMusicPlayService = null;
        }
        
    }

    @Override
    public void onChanged(long prePlayingId, long currentPlayingId) {
        setPlayStatus(prePlayingId, currentPlayingId);
    }
    
    private void setPlayStatus(long prePlayingId, long playingId) {
        View currentPlayingView = mListView.findViewWithTag(playingId);
        

        if (currentPlayingView != null) {
            ImageView songImageView = (ImageView) currentPlayingView.findViewById(R.id.songImageView);
            
            songImageView.setImageResource(R.drawable.music_pause_selector);
        }
        
        View prePlayingView = mListView.findViewWithTag(prePlayingId);
        
        if (prePlayingView != null) {
            ImageView songImageView = (ImageView) prePlayingView.findViewById(R.id.songImageView);
            
            songImageView.setImageResource(R.drawable.music_play_selector);
        }
    }
}
