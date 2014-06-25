/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data.json;

import java.util.List;

/**
 * 
 * @author panxu
 * @since 2014-3-19
 */
public class MusicJson {
    
    public List<Music> music;
    public MusicVersion ver;
    public int update;
    
    public static class Music {
        public String _id;
        public String name;
        public String music_abstract;
        public String detail;
        public String detail_img_url;
        public String sound_url;
        public String video_url;
        public String difficulty;
    }

    public static class MusicVersion {
        public String version_name;
        public String version_code;
    }
}
