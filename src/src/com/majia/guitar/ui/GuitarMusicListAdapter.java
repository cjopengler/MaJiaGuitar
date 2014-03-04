/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;

import java.util.ArrayList;
import java.util.List;

import com.majia.guitar.R;
import com.majia.guitar.data.IGuitarData;
import com.majia.guitar.data.MusicEntity;
import com.majia.guitar.service.MusicPlayService;
import com.majia.guitar.stub.StubData;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author panxu
 * @since 2013-12-17
 */
public class GuitarMusicListAdapter extends BaseAdapter {

    private final Context mContext;
    private final ArrayList<MusicEntity> mMusicEntities;
    
    public GuitarMusicListAdapter(Context context) {
        mContext = context;
        IGuitarData guitarData = StubData.getInstance();
        mMusicEntities = guitarData.query();
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
        
        TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        nameTextView.setText(musicEntity.getName());
        
        TextView musicTextView = (TextView) convertView.findViewById(R.id.musicTextView);
        musicTextView.setText(musicEntity.getMusicAbstract());
        
        ImageView songImageView = (ImageView) convertView.findViewById(R.id.songImageView);
        songImageView.setTag(musicEntity);
        songImageView.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                MusicEntity musicEntity = (MusicEntity) v.getTag();
                Uri uri = Uri.parse(musicEntity.getSoundLocal());
                
                Intent serviceIntent = new Intent(MusicPlayService.CMD_PLAY, uri, mContext, MusicPlayService.class);
                mContext.startService(serviceIntent);
            }
        });
        
        return convertView;
    }

}
