/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data;

import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * 
 * @author panxu
 * @since 2014-2-24
 */
public class GuitarData implements IGuitarData {
    private static final class GuitarDataHolder {
        private static final GuitarData INSTANCE = new GuitarData();
    }
    
    public static GuitarData getInstance() {
        return GuitarDataHolder.INSTANCE;
    }
    
    private GuitarData() {
        
    }

    @Override
    public void updateMusics() {

    }

    @Override
    public ArrayList<MusicEntity> query() {
        return null;
    }

 

    @Override
    public Versions getVersions() {
        return null;
    }

    @Override
    public void updateVersion(Versions versions) {
        
    }
    
    
    private static class DataOpenHelper extends SQLiteOpenHelper {
        
        private static final int DATABASE_VERSION = 1;
        private static final String NAME = "musics";
        

        /**
         * @param context
         * @param name
         * @param factory
         * @param version
         */
        public DataOpenHelper(Context context) {
            super(context, NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            
        }
        
        private static void updateDatabase(Context context, SQLiteDatabase db,
                int fromVersion, int toVersion) {
            
            db.execSQL("DROP TRIGGER IF EXISTS guitar_musics");
            db.execSQL("DROP TRIGGER IF EXISTS version");

            db.execSQL("CREATE TABLE IF NOT EXISTS guitar_musics (" +
                    "_id INTEGER PRIMARY KEY," +
                    "_data TEXT," +
                    "_size INTEGER," +
                    "_display_name TEXT," +
                    "mime_type TEXT," +
                    "title TEXT," +
                    "date_added INTEGER," +
                    "date_modified INTEGER," +
                    "description TEXT," +
                    "picasa_id TEXT," +
                    "isprivate INTEGER," +
                    "latitude DOUBLE," +
                    "longitude DOUBLE," +
                    "datetaken INTEGER," +
                    "orientation INTEGER," +
                    "mini_thumb_magic INTEGER," +
                    "bucket_id TEXT," +
                    "bucket_display_name TEXT" +
                   ");");
            
            
            db.execSQL("CREATE TABLE IF NOT EXISTS version (" +
                    "_id INTEGER PRIMARY KEY," +
                    "musics_version_name TEXT," +
                    "musics_version_code INTEGER," +
                    "apk_version_name TEXT," +
                    "apk_version_code INTEGER," +
                   ");");

            
        }
    }
   

}
