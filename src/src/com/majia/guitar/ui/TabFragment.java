/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;

import com.majia.guitar.MaJiaGuitarApplication;
import com.majia.guitar.R;
import com.majia.guitar.data.ApkVersion;
import com.majia.guitar.data.IUpdateApkVersion.UpdateListener;
import com.majia.guitar.data.UpdateApkVersion;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * 
 * @author panxu
 * @since 2013-12-15
 */
public class TabFragment extends Fragment implements UpdateListener {
    private ImageView mApkUpdateImageView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        UpdateApkVersion.getInstance().registListener(this);
        
    }
    
    @Override
    public void onDestroy() {
        
        UpdateApkVersion.getInstance().unregistListener(this);
        super.onDestroy();
    }

    
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
        
        mApkUpdateImageView = (ImageView) tabView.findViewById(R.id.apkUpdateImageView);
        View aboutLayout = tabView.findViewById(R.id.aboutLayout);
        aboutLayout.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mApkUpdateImageView.setVisibility(View.GONE);
            }
        });
        
        ApkVersion apkVersion = UpdateApkVersion.getInstance().getApkVersion();
        if (apkVersion.versionCode > MaJiaGuitarApplication.getInstance().getVersionCode()) {
            mApkUpdateImageView.setVisibility(View.VISIBLE);
        }
        
        return tabView;
    }

    @Override
    public void onUpdate(ApkVersion apkVersion) {
        mApkUpdateImageView.setVisibility(View.VISIBLE);
    }
}
