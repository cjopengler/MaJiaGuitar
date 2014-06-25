/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import com.majia.guitar.MaJiaGuitarApplication;
import com.majia.guitar.data.json.MusicJson;
import com.majia.guitar.data.json.MusicJson.Music;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;


/**
 * 
 * @author panxu
 * @since 2014-2-24
 */
public class GuitarData implements IGuitarData {
    private static final String DATA_SHARE_PAREFENCE_NAME = "data";
    
    
    public static final String MUSIC_VERSION_NAME = "musics_version_name";
    public static final String MUSIC_VERSION_CODE = "musics_version_code";
    
    public static final String APK_VERSION_NAME = "apk_version_name";
    public static final String APK_VERSION_CODE = "apk_version_code";
    
    private final ReentrantLock mReentrantLock = new ReentrantLock();
    private final CopyOnWriteArrayList<IGuitarDataListener> mGuitarDataListeners =
                        new CopyOnWriteArrayList<GuitarData.IGuitarDataListener>();
    
    
    private DataOpenHelper mDataOpenHelper;
    
    private static final class GuitarDataHolder {
        private static final GuitarData INSTANCE = new GuitarData();
    }
    
    public static GuitarData getInstance() {
        return GuitarDataHolder.INSTANCE;
    }
    
    private GuitarData() {
        mDataOpenHelper = new DataOpenHelper(MaJiaGuitarApplication.getInstance());
        
    }
    
    public void addListener(IGuitarDataListener listener) {
        mGuitarDataListeners.addIfAbsent(listener);
    }
    
    public void removeListener(IGuitarDataListener listener) {
        mGuitarDataListeners.remove(listener);
    }
    
    private void notifyUpdateVersionListener(Version oldMusicVersion, Version newMusicVersion,
            Version oldApkVersion, Version newApkVersion) {
        for (IGuitarDataListener listener : mGuitarDataListeners) {
            listener.onUpdateVersion(oldMusicVersion, newMusicVersion, 
                    oldApkVersion, newApkVersion);
        }
    }

    @Override
    public void updateMusics() {

    }
    
    private void insterMusicsIntoDB(List<MusicEntity> musicEntities) {
        SQLiteDatabase database = mDataOpenHelper.getWritableDatabase();
        
        for (MusicEntity musicEntity : musicEntities) {
            ContentValues contentValues = new ContentValues();
            
            contentValues.put(DataOpenHelper.MusicColumn.DETAIL, 
                    musicEntity.getDetail());
            contentValues.put(DataOpenHelper.MusicColumn.DETAIL_IMG_LOCAL, 
                    musicEntity.getDetailUrl());
            
            contentValues.put(DataOpenHelper.MusicColumn.DIFFICULTY, 
                    musicEntity.getDifficulty());
            contentValues.put(DataOpenHelper.MusicColumn.MUSIC_ABSTRACT, 
                    musicEntity.getMusicAbstract());
            contentValues.put(DataOpenHelper.MusicColumn.MUSIC_ID, 
                    musicEntity.getMusicId());
            contentValues.put(DataOpenHelper.MusicColumn.NAME, 
                    musicEntity.getName());
            
            contentValues.put(DataOpenHelper.MusicColumn.SOUND_URL, 
                    musicEntity.getSoundUrl());
            
            contentValues.put(DataOpenHelper.MusicColumn.VIDEO_URL, 
                    musicEntity.getVideoUrl());
            
            
            database.replace(DataOpenHelper.GUITAR_MUSIC_TABLE, 
                    "", 
                    contentValues);
        }
    }
    
    private List<MusicEntity> queryMusicsFromDB() {
        
        List<MusicEntity> retMusicEntities = new ArrayList<MusicEntity>();
        Cursor cursor = null;
        
        try {
            SQLiteDatabase database = mDataOpenHelper.getWritableDatabase();
            cursor = database.query(DataOpenHelper.GUITAR_MUSIC_TABLE, null, null, null, null, null, null);
            
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(DataOpenHelper.MusicColumn.ID));
                long musicId = cursor.getLong(cursor.getColumnIndex(DataOpenHelper.MusicColumn.MUSIC_ID));
                String name = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.NAME));
                String musicAbstract = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.MUSIC_ABSTRACT));
                String detail = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.DETAIL));
                String detailUrl = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.DETAIL_IMG_URL));
                String detailLocal = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.DETAIL_IMG_LOCAL));
                
                String songUrl = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.SOUND_URL));
                String songLocal = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.SOUND_LOCAL));
                String videoUrl = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.VIDEO_URL));
                String videoLocal = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.VIDEO_LOCAL));
                int difficulty = cursor.getInt(cursor.getColumnIndex(DataOpenHelper.MusicColumn.DIFFICULTY));
                
                MusicEntity musicEntity = new MusicEntity(id, musicId, 
                                                          name, musicAbstract, 
                                                          detail, detailUrl, detailLocal, 
                                                          songUrl, songLocal, difficulty, 
                                                          videoUrl, videoLocal);
                
                retMusicEntities.add(musicEntity);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return retMusicEntities;
    }
    
    

    @Override
    public List<MusicEntity> query() {
        
        Version version = getVersion();
        
  
        MusicJson musicJson = MusicRemoteServer.getMusics(version.versionCode());;
        
        if (musicJson != null && musicJson.update == 1) {
            List<MusicEntity> musicEntities = new ArrayList<MusicEntity>();
            
            
            if (musicJson.music != null) {
                for (Music music : musicJson.music) {
                    musicEntities.add(new MusicEntity(music));
                }
            }
            
            insterMusicsIntoDB(musicEntities);
                
            if (musicJson.ver != null) {
                updateVersion(new Version(musicJson.ver.version_name, musicJson.ver.version_code), false);
            }
        }
      
        List<MusicEntity> retMusicEntities = queryMusicsFromDB();
        
        
        return retMusicEntities;
    }

 

    @Override
    public Version getVersion() {

        SharedPreferences dataSharedPreferences = MaJiaGuitarApplication.getInstance().getSharedPreferences(
                DATA_SHARE_PAREFENCE_NAME, 0);

        long musicVersionCode = dataSharedPreferences.getLong(MUSIC_VERSION_CODE, 0);
        String musicVersionName = dataSharedPreferences.getString(MUSIC_VERSION_NAME, "0.0.0.0");


        Version musicVersion = new Version(musicVersionName, musicVersionCode);


        return musicVersion;
    }
    

    @Override
    public void updateVersion(Version version, boolean isNotifyListener) {

    
        SharedPreferences dataSharedPreferences = MaJiaGuitarApplication.getInstance().getSharedPreferences(
                DATA_SHARE_PAREFENCE_NAME, 0);

        long musicVersionCode = dataSharedPreferences.getLong(MUSIC_VERSION_CODE, 0);

        SharedPreferences.Editor dataEditor = dataSharedPreferences.edit();



        if (version.versionCode() > musicVersionCode) {
            dataEditor.putLong(MUSIC_VERSION_CODE, version.versionCode());
            if (TextUtils.isEmpty(version.getVersionName())) {
                dataEditor.putString(MUSIC_VERSION_NAME, "");
            } else {
                dataEditor.putString(MUSIC_VERSION_NAME, version.getVersionName());
            }
            
        }

        dataEditor.commit();

    }
    
    
    public static interface IGuitarDataListener {
        void onUpdateVersion(Version oldMusicVersion, Version newMusicVersion,
                             Version oldApkVersion, Version newApkVersion);
    }
    
    private static class DataOpenHelper extends SQLiteOpenHelper {
        private static final String TAG = "DataOpenHelper";
        
        public static final String GUITAR_MUSIC_TABLE = "guitar_musics";
        public static final String VERSION_TABLE = "version";

        public final static class MusicColumn {
            public static final String ID = "_id";
            public static final String MUSIC_ID = "music_id";
            public static final String NAME = "name";
            public static final String MUSIC_ABSTRACT = "music_abstract";
            public static final String DETAIL = "detail";
            public static final String DETAIL_IMG_URL = "detail_img_url";
            public static final String DETAIL_IMG_LOCAL = "detail_img_local";
            public static final String SOUND_URL = "sound_url";
            public static final String SOUND_LOCAL = "sound_local";
            public static final String VIDEO_URL = "video_url";
            public static final String VIDEO_LOCAL = "video_local";
            public static final String DIFFICULTY = "difficulty";
            
            private MusicColumn() {
                
            }
        }
       
        
        private static final int DATABASE_VERSION = 1;
        private static final String DB_NAME = "musics";
        private Context mContext;
        

        /**
         * @param context
         * @param name
         * @param factory
         * @param version
         */
        public DataOpenHelper(Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            updateDatabase(mContext, db, DATABASE_VERSION, DATABASE_VERSION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            updateDatabase(mContext, db, oldVersion, newVersion);
            
        }
        
        private static void updateDatabase(Context context, SQLiteDatabase db, int fromVersion, int toVersion) {
            
            db.execSQL("DROP TRIGGER IF EXISTS " + GUITAR_MUSIC_TABLE);
            db.execSQL("DROP TRIGGER IF EXISTS " + VERSION_TABLE);

            try {
            db.execSQL("CREATE TABLE IF NOT EXISTS guitar_musics (" +
                    "_id INTEGER PRIMARY KEY," +
                    "music_id INTEGER," + 
                    "name TEXT," +
                    "music_abstract TEXT," +
                    "detail TEXT," +
                    "detail_img_url TEXT," +
                    "detail_img_local TEXT," +
                    "sound_url TEXT," +
                    "sound_local TEXT," +
                    "video_url TEXT," +
                    "video_local TEXT," +
                    "difficulty INTEGER" +
                   ");");
            
            
            db.execSQL(
                    "CREATE UNIQUE INDEX MusicIndex ON guitar_musics (music_id);"
                    );
            } catch (SQLException exception) {
                Log.e(TAG, "exception: " + exception.getMessage());
            }
            
           /* db.execSQL("CREATE TABLE IF NOT EXISTS version (" +
                    "_id INTEGER PRIMARY KEY," +
                    "musics_version_name TEXT," +
                    "musics_version_code INTEGER," +
                    "apk_version_name TEXT," +
                    "apk_version_code INTEGER," +
                   ");");
            
            
            
            db.execSQL("INSERT INTO version (" +
            		                        "musics_version_name," +
            		                        "musics_version_code," +
            		                        "apk_version_name," +
            		                        "apk_version_code)" +
            		                        " VALUES ('0.0.0.0', " +
            		                                  "0, " +
            		                                  "'0.0.0.0', " +
            		                                  "0)");*/

            
        }
    }
   

}
