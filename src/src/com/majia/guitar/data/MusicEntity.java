/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data;


/**
 * 
 * @author panxu
 * @since 2013-12-17
 */
public class MusicEntity {
    private final long _id;
    private final long music_id;
    private final String name;
    private final String music_abstract;
    private final String detail_url;
    private final String detail_local;
    private final String sound_url;
    private final String sound_local;
    private final int difficulty;
    private final String video_url;
    private final String video_local;
    
    public MusicEntity(MusicEntity musicEntity) {
        this(musicEntity._id, 
             musicEntity.music_id, 
             musicEntity.name, 
             musicEntity.music_abstract, 
             musicEntity.detail_url, 
             musicEntity.detail_local, 
             musicEntity.sound_url, 
             musicEntity.sound_local, 
             musicEntity.difficulty, 
             musicEntity.video_url, 
             musicEntity.video_local);
    }
    
    public MusicEntity(long _id, long music_id, 
                        String name, String music_abstract,
                       String detail_url, String detail_local,
                       String sound_url, String sound_local,
                       int difficulty,
                       String video_url,
                       String video_local) {
        this._id = _id;
        this.music_id = music_id;
        this.name = name;
        this.music_abstract = music_abstract;
        this.detail_url = detail_url;
        this.detail_local = detail_local;
        this.sound_url = sound_url;
        this.sound_local = sound_local;
        this.difficulty = difficulty;
        this.video_url = video_url;
        this.video_local = video_local;
    }
    
    public final long getId() {
       return _id; 
    }
    
    public final long getMusicId() {
        return music_id;
    }
    
    public final String getName() {
        return name;
    }
    
    public final String getMusicAbstract() {
        return music_abstract;
    }
    
    public final String getDetailUrl() {
        return detail_url;
    }
    
    public final String getDetailLocal() {
        return detail_local;
    }
    
    public final String getSoundUrl() {
        return sound_url;
    }
    
    public final String getSoundLocal() {
        return sound_local;
    }
    
    public final String getVideoUrl() {
        return video_url;
    }
    
    public final String getVideoLocal() {
        return video_local;
    }
    
    public final int getDifficulty() {
        return difficulty;
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        super.clone();
        return new MusicEntity(this);
    }
}
