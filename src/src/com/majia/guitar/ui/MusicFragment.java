/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;

import com.majia.guitar.R;
import com.majia.guitar.service.MusicPlayService;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
public class MusicFragment extends Fragment {
    
    private ListView mGuitarMusicListView;
    
    private ProgressBar mLoadingProgressBar;
    
    public static MusicFragment newInstance() {
        return new MusicFragment();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View musicView = inflater.inflate(R.layout.music_fragment, container, false);
        
        mLoadingProgressBar = (ProgressBar) musicView.findViewById(R.id.loadingProgressBar);
        
        mGuitarMusicListView = (ListView) musicView.findViewById(R.id.guitarMusicListView);
        //mGuitarMusicListView.setAdapter(new GuitarMusicListAdapter(this.getActivity()));
        
        return musicView;
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
        
        super.onDestroy();
    }
}
