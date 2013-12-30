/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;

import com.majia.guitar.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * @author panxu
 * @since 2013-12-15
 */
public class TabFragment extends Fragment {
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View tabView = inflater.inflate(R.layout.tab_fragment, container, false);
        
        View guitarMusicView = tabView.findViewById(R.id.guitarMusicsLayout);
        guitarMusicView.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_container, MusicFragment.newInstance());
                ft.commit();
            }
        });
        
        View updateView = tabView.findViewById(R.id.updatLayout);
        updateView.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_container, UpdateFragment.newInstance());
                ft.commit();
            }
        });
        
        
        
        return tabView;
    }
}
