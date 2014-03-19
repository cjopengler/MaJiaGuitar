/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;

import java.util.List;

import com.majia.guitar.R;
import com.majia.guitar.data.MusicEntity;
import com.majia.guitar.service.DataService;
import com.majia.guitar.service.MusicPlayService;
import com.majia.guitar.service.DataService.DataBinder;
import com.majia.guitar.service.DataService.IQueryMusicsCallback;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

/**
 * 
 * @author panxu
 * @since 2013-12-15
 */
public class MusicFragment extends Fragment implements IQueryMusicsCallback {
    
    private ListView mGuitarMusicListView;
    
    private ProgressBar mLoadingProgressBar;
    
    private DataService mDataService;
    private DataServiceConnection mDataServiceConnection;
    
    
    private GuitarMusicListAdapter mMusicListAdapter;
    
    
    public static MusicFragment newInstance() {
        return new MusicFragment();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View musicView = inflater.inflate(R.layout.music_fragment, container, false);
        
        mLoadingProgressBar = (ProgressBar) musicView.findViewById(R.id.loadingProgressBar);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        
        mGuitarMusicListView = (ListView) musicView.findViewById(R.id.guitarMusicListView);
        mGuitarMusicListView.setVisibility(View.GONE);
        
        mMusicListAdapter = new GuitarMusicListAdapter(this.getActivity());
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
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
    }
    
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
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
    }
}
