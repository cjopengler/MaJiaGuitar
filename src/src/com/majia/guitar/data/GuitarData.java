/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import com.majia.guitar.MaJiaGuitarApplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


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

    @Override
    public ArrayList<MusicEntity> query() {
        return null;
    }

 

    @Override
    public Versions getVersions() {
        Versions versions = null;

        SharedPreferences dataSharedPreferences = MaJiaGuitarApplication.getInstance().getSharedPreferences(
                DATA_SHARE_PAREFENCE_NAME, 0);

        long musicVersionCode = dataSharedPreferences.getLong(MUSIC_VERSION_CODE, 0);
        String musicVersionName = dataSharedPreferences.getString(MUSIC_VERSION_NAME, "0.0.0.0");

        long apkVersionCode = dataSharedPreferences.getLong(APK_VERSION_CODE, 0);
        String apkVersionName = dataSharedPreferences.getString(APK_VERSION_NAME, "0.0.0.0");

        Version musicVersion = new Version(musicVersionName, musicVersionCode);
        Version apkVersion = new Version(apkVersionName, apkVersionCode);

        versions = new Versions(musicVersion, apkVersion);

        return versions;
    }

    @Override
    public void updateVersion(Versions versions) {

        Version oldMusicVersion = null;
        Version oldApkVersion = null;

        SharedPreferences dataSharedPreferences = MaJiaGuitarApplication.getInstance().getSharedPreferences(
                DATA_SHARE_PAREFENCE_NAME, 0);

        long musicVersionCode = dataSharedPreferences.getLong(MUSIC_VERSION_CODE, 0);
        String musicVersionName = dataSharedPreferences.getString(MUSIC_VERSION_NAME, "0.0.0.0");

        oldMusicVersion = new Version(musicVersionName, musicVersionCode);

        long apkVersionCode = dataSharedPreferences.getLong(APK_VERSION_CODE, 0);
        String apkVersioName = dataSharedPreferences.getString(APK_VERSION_NAME, "0.0.0.0");

        SharedPreferences.Editor dataEditor = dataSharedPreferences.edit();

        oldApkVersion = new Version(apkVersioName, apkVersionCode);

        if (versions.getApkVersion().versionCode() > apkVersionCode) {
            dataEditor.putLong(APK_VERSION_CODE, versions.getApkVersion().versionCode());
            dataEditor.putString(APK_VERSION_NAME, versions.getApkVersion().getVersionName());
        }

        if (versions.getMusicVersion().versionCode() > musicVersionCode) {
            dataEditor.putLong(MUSIC_VERSION_CODE, versions.getMusicVersion().versionCode());
            dataEditor.putString(MUSIC_VERSION_NAME, versions.getMusicVersion().getVersionName());
        }

        dataEditor.commit();

        notifyUpdateVersionListener(oldMusicVersion, versions.getMusicVersion(), oldApkVersion,
                versions.getApkVersion());

    }
    
    
    public static interface IGuitarDataListener {
        void onUpdateVersion(Version oldMusicVersion, Version newMusicVersion,
                             Version oldApkVersion, Version newApkVersion);
    }
    
    private static class DataOpenHelper extends SQLiteOpenHelper {
        
        public static final String GUITAR_MUSIC_TABLE = "guitar_musics";
        public static final String VERSION_TABLE = "version";
        
        public static final String MUSIC_VERSION_NAME = "musics_version_name";
        public static final String MUSIC_VERSION_CODE = "musics_version_code";
        
        public static final String APK_VERSION_NAME = "apk_version_name";
        public static final String APK_VERSION_CODE = "apk_version_code";
        
        private static final int DATABASE_VERSION = 1;
        private static final String NAME = "musics";
        private Context mContext;
        

        /**
         * @param context
         * @param name
         * @param factory
         * @param version
         */
        public DataOpenHelper(Context context) {
            super(context, NAME, null, DATABASE_VERSION);
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
                    "difficulty INTEGER," +
                   ");");
            
            
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
