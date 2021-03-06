/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;

import java.util.List;

import com.majia.guitar.MaJiaGuitarApplication;
import com.majia.guitar.R;
import com.majia.guitar.data.ApkVersion;
import com.majia.guitar.data.IUpdateApkVersion.UpdateListener;
import com.majia.guitar.data.MusicEntity;
import com.majia.guitar.data.UpdateApkVersion;
import com.majia.guitar.service.DataService;
import com.majia.guitar.service.MusicPlayService;
import com.majia.guitar.service.DataService.DataBinder;
import com.majia.guitar.service.DataService.IQueryMusicsCallback;
import com.majia.guitar.ui.component.PullDownListView;
import com.majia.guitar.ui.component.PullDownListView.OnRefreshListener;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 
 * @author panxu
 * @since 2013-12-15
 */
public class MusicFragment extends Fragment implements IQueryMusicsCallback, 
													   OnItemClickListener, 
													   OnRefreshListener,
													   UpdateListener {
    
    private ListView mGuitarMusicListView;
    private PullDownListView mPullDownListView;
    
    private ProgressBar mLoadingProgressBar;
    
    private DataService mDataService;
    private DataServiceConnection mDataServiceConnection;
    
    
    private GuitarMusicListAdapter mMusicListAdapter;
    
    private final Handler mUIHandler = new Handler(Looper.getMainLooper());
    
    private WakeLock mWakeLock;
    
    public static MusicFragment newInstance() {
        return new MusicFragment();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PowerManager pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                                   PowerManager.ON_AFTER_RELEASE, "MusicFragment");
        
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View musicView = inflater.inflate(R.layout.music_fragment, container, false);
        
        mPullDownListView = (PullDownListView) musicView.findViewById(R.id.pullDownView);
        mPullDownListView.setOnRefreshListener(this);
        
        mLoadingProgressBar = (ProgressBar) musicView.findViewById(R.id.loadingProgressBar);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        
        mGuitarMusicListView = (ListView) musicView.findViewById(android.R.id.list);
        mGuitarMusicListView.setVisibility(View.GONE);
        
        mGuitarMusicListView.setOnItemClickListener(this);
        
        mMusicListAdapter = new GuitarMusicListAdapter(this.getActivity(), mGuitarMusicListView);
        mMusicListAdapter.onCreate(this);
        mGuitarMusicListView.setAdapter(mMusicListAdapter);
        
        return musicView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        mDataServiceConnection = new DataServiceConnection();
        
        boolean result = getActivity().bindService(new Intent(getActivity(), DataService.class), 
                                   mDataServiceConnection, 
                                   Service.BIND_AUTO_CREATE);
        
        Log.d("TAG", "result is " + result);
        
        ApkVersion apkVersion = UpdateApkVersion.getInstance().getApkVersion();
        if (apkVersion.versionCode > MaJiaGuitarApplication.getInstance().getVersionCode()) {
        	//弹出升级提示
        	startActivity(new Intent(getActivity(), VersionInfoActivity.class));
        	
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mWakeLock.acquire();
        
        
    }
    
    @Override
    public void onPause() {
        super.onPause();
        mWakeLock.release();
        Intent serviceIntent = new Intent(this.getActivity(), MusicPlayService.class);
        serviceIntent.setAction(MusicPlayService.CMD_STOP);
        getActivity().startService(serviceIntent);
    }
    
    @Override
    public void onDestroyView() {
        Intent serviceIntent = new Intent(this.getActivity(), MusicPlayService.class);
        
        serviceIntent.setAction(MusicPlayService.CMD_STOP);
        
        this.getActivity().startService(serviceIntent);
        
        super.onDestroyView();
    }
    
    @Override
    public void onDestroy() {
        getActivity().unbindService(mDataServiceConnection);
        mMusicListAdapter.onDestroy(this);
        super.onDestroy();
    }
    
    private class DataServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDataService = ((DataBinder) service).getDataService();
            
            mDataService.queryMusics(MusicFragment.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("TAG", "name is " + name);
        }
        
    }

    @Override
    public void onMusics(final List<MusicEntity> musics) {
        mLoadingProgressBar.setVisibility(View.GONE);
        mGuitarMusicListView.setVisibility(View.VISIBLE);
        mMusicListAdapter.update(musics);
        
        mPullDownListView.onRefreshComplete();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MusicEntity musicEntity = (MusicEntity) mMusicListAdapter.getItem(position);
        Intent intent = new Intent(getActivity(), MusicDetailActivity.class);
        intent.putExtra(MusicDetailActivity.MUSIC_ENTITY, musicEntity);
        startActivity(intent);
    }

	@Override
	public void onRefresh() {
		if (mDataService == null) {
			mDataServiceConnection = new DataServiceConnection();
	        
	        boolean result = getActivity().bindService(new Intent(getActivity(), DataService.class), 
	                                   mDataServiceConnection, 
	                                   Service.BIND_AUTO_CREATE);
		} else {
			mDataService.queryMusics(MusicFragment.this);
		}
	}

	@Override
	public void onLoadMore() {
		
	}

	@Override
	public void onUpdate(ApkVersion apkVersion) {
		mUIHandler.post(new Runnable() {
			
			@Override
			public void run() {
				if (getActivity() != null && isResumed()) {
					//弹出升级提示
		        	startActivity(new Intent(getActivity(), VersionInfoActivity.class));
				}
			}
		});
	}
}
