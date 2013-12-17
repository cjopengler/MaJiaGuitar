/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.stub;

import java.util.ArrayList;

import com.majia.guitar.data.IGuitarData;
import com.majia.guitar.data.MusicEntity;

/**
 * 
 * @author panxu
 * @since 2013-12-17
 */
public class StubData implements IGuitarData {
    private static volatile StubData sInstance = null;
    
    private final ArrayList<MusicEntity> mMusicEntities;
    
    
    public static StubData getInstance() {
        if (sInstance == null) {
            synchronized (StubData.class) {
                if (sInstance == null) {
                    sInstance = new StubData();
                }
            }
        }
        
        return sInstance;
    }
    
    private StubData() {
        mMusicEntities = new ArrayList<MusicEntity>();
        gennerateData();
    }

    private void gennerateData() {
        MusicEntity musicEntity1 = new MusicEntity(1, 1, "童年", "D Em G Em A D", null, null, null, "tonghua.amr", 1, null, null);
        mMusicEntities.add(musicEntity1);
        
        MusicEntity musicEntity2 = new MusicEntity(2, 2, "哈利欧姆", "Em Am D Em", null, null, null, "moon.amr", 1, null, null);
        mMusicEntities.add(musicEntity2);
        
        MusicEntity musicEntity3 = new MusicEntity(3, 3, "欢快", "D A D G", null, null, null, "qianglizhiwai.amr", 1, null, null);
        mMusicEntities.add(musicEntity3);
    }

    @Override
    public void updateMusics() {
        
    }


    @Override
    public synchronized final ArrayList<MusicEntity> query() {
        ArrayList<MusicEntity> musicEntities = new ArrayList<MusicEntity>(mMusicEntities.size());
        
        for (MusicEntity musicEntity : mMusicEntities) {
            
            musicEntities.add(new MusicEntity(musicEntity));
        }
        
        return musicEntities;
    }


    @Override
    public void updateVersion() {
        
    }
    
  
}
