/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;

import com.majia.guitar.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * 
 * @author panxu
 * @since 2013-12-15
 */
public class MusicFragment extends Fragment {
    
    private ListView mGuitarMusicListView;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View musicView = inflater.inflate(R.layout.music_fragment, container, false);
        
        mGuitarMusicListView = (ListView) musicView.findViewById(R.id.guitarMusicListView);
        mGuitarMusicListView.setAdapter(new GuitarMusicListAdapter(this.getActivity()));
        
        return musicView;
    }
}
