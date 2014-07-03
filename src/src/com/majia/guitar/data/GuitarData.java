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
    
    private static final String TAG = "GuitarData";
    
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
    
    private void notifyEntityUpdateListener(MusicEntity musicEntity) {
        for (IGuitarDataListener listener : mGuitarDataListeners) {
            listener.onMusicEntityChange(musicEntity);
        }
    }
    
   

    @Override
    public void updateMusics() {

    }
    
    private void insterMusicsIntoDB(List<MusicEntity> musicEntities) {
        
        
        List<MusicEntity> oldMusicEntities = queryMusicsFromDB();
        
        SQLiteDatabase database = mDataOpenHelper.getWritableDatabase();
        
        for (MusicEntity musicEntity : musicEntities) {
            
            ContentValues contentValues = new ContentValues();
            
        
            contentValues.put(DataOpenHelper.MusicColumn.DETAIL, 
                    musicEntity.getDetail());
            contentValues.put(DataOpenHelper.MusicColumn.DETAIL_IMG_URL, 
                    musicEntity.getDetailUrl());
            
            contentValues.put(DataOpenHelper.MusicColumn.DIFFICULTY, 
                    musicEntity.getDifficulty());
            contentValues.put(DataOpenHelper.MusicColumn.MUSIC_ABSTRACT, 
                    musicEntity.getMusicAbstract());
            contentValues.put(DataOpenHelper.MusicColumn.MUSIC_ID, 
                    musicEntity.getMusicId());
            contentValues.put(DataOpenHelper.MusicColumn.NAME, 
                    musicEntity.getName());
            
            contentValues.put(DataOpenHelper.MusicColumn.SOUND_INTERNAL_URL, 
                    musicEntity.getSoundUrl());
            
            contentValues.put(DataOpenHelper.MusicColumn.VIDEO_EXTERNAL_URL, 
                    musicEntity.getVideoUrl());
            
            boolean isUpdate = false;
            for (MusicEntity oldMusicEntity : oldMusicEntities) {
                if (musicEntity.getMusicId() == oldMusicEntity.getMusicId()) {
                    //执行update
                    isUpdate = true;
                    
                    
                    if (musicEntity.getName().equals(oldMusicEntity.getName()) &&
                        musicEntity.getDetail().equals(oldMusicEntity.getDetail()) &&
                        musicEntity.getDetailUrl().equals(oldMusicEntity.getDetailUrl()) &&
                        musicEntity.getDifficulty() == oldMusicEntity.getDifficulty() && 
                        musicEntity.getMusicAbstract().equals(oldMusicEntity.getMusicAbstract()) &&
                        musicEntity.getSoundUrl().equals(oldMusicEntity.getSoundUrl()) &&
                        musicEntity.getVideoUrl().equals(oldMusicEntity.getVideoUrl())
                        
                        ) {
                        //不需要更新了
                    } else {
                        String whereCluase = DataOpenHelper.MusicColumn.MUSIC_ID + " = " + musicEntity.getMusicId();
                        

                        database.update(DataOpenHelper.GUITAR_MUSIC_TABLE, contentValues, whereCluase, null);
     
                        oldMusicEntities.remove(oldMusicEntity);
                    }
                    
                    break;
                }
            }
            
            if (!isUpdate) {
                //instert
                contentValues.put(DataOpenHelper.MusicColumn.DETAIL_IMG_LOCAL, "");
                contentValues.put(DataOpenHelper.MusicColumn.SOUND_EXTERNAL_URL, "");
                contentValues.put(DataOpenHelper.MusicColumn.SOUND_LOCAL, "");
                contentValues.put(DataOpenHelper.MusicColumn.VIDEO_DOWNLOAD_ID, 0);
                contentValues.put(DataOpenHelper.MusicColumn.VIDEO_LOCAL, "");
                
                
                database.insert(DataOpenHelper.GUITAR_MUSIC_TABLE, null, contentValues);
            }
            
        }
        
    }
    
    private List<MusicEntity> queryMusicsFromDB() {
        
        List<MusicEntity> retMusicEntities = new ArrayList<MusicEntity>();
        Cursor cursor = null;
        
        try {
            SQLiteDatabase database = mDataOpenHelper.getWritableDatabase();
            cursor = database.query(DataOpenHelper.GUITAR_MUSIC_TABLE, null, null, null, null, null, DataOpenHelper.MusicColumn.MUSIC_ID);
            
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(DataOpenHelper.MusicColumn.ID));
                long musicId = cursor.getLong(cursor.getColumnIndex(DataOpenHelper.MusicColumn.MUSIC_ID));
                String name = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.NAME));
                String musicAbstract = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.MUSIC_ABSTRACT));
                String detail = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.DETAIL));
                String detailUrl = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.DETAIL_IMG_URL));
                String detailLocal = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.DETAIL_IMG_LOCAL));
                
                String songUrl = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.SOUND_INTERNAL_URL));
                String songLocal = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.SOUND_LOCAL));
                String videoUrl = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.VIDEO_EXTERNAL_URL));
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
    
    public MusicEntity queryById(long id) {
        String sql = DataOpenHelper.MusicColumn.MUSIC_ID + " = " + id;
        
        MusicEntity musicEntity = null;
        Cursor cursor = null;
        
        try {
            SQLiteDatabase database = mDataOpenHelper.getWritableDatabase();
            cursor = database.query(DataOpenHelper.GUITAR_MUSIC_TABLE, null, sql, null, null, null, null);
            
            while (cursor.moveToNext()) {
                long _id = cursor.getLong(cursor.getColumnIndex(DataOpenHelper.MusicColumn.ID));
                long musicId = cursor.getLong(cursor.getColumnIndex(DataOpenHelper.MusicColumn.MUSIC_ID));
                String name = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.NAME));
                String musicAbstract = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.MUSIC_ABSTRACT));
                String detail = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.DETAIL));
                String detailUrl = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.DETAIL_IMG_URL));
                String detailLocal = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.DETAIL_IMG_LOCAL));
                
                String songUrl = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.SOUND_INTERNAL_URL));
                String songLocal = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.SOUND_LOCAL));
                String videoUrl = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.VIDEO_EXTERNAL_URL));
                String videoLocal = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.VIDEO_LOCAL));
                int difficulty = cursor.getInt(cursor.getColumnIndex(DataOpenHelper.MusicColumn.DIFFICULTY));
                
                musicEntity = new MusicEntity(_id, musicId, 
                                                          name, musicAbstract, 
                                                          detail, detailUrl, detailLocal, 
                                                          songUrl, songLocal, difficulty, 
                                                          videoUrl, videoLocal);
                
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return musicEntity;
    
    }
    
    public long queryVideoDownloadId(long id) {

        String sql = DataOpenHelper.MusicColumn.MUSIC_ID + " = " + id;
        
        Cursor cursor = null;
        long downloadId = 0;
        
        try {
            SQLiteDatabase database = mDataOpenHelper.getWritableDatabase();
            cursor = database.query(DataOpenHelper.GUITAR_MUSIC_TABLE, null, sql, null, null, null, null);
            
            while (cursor.moveToNext()) {
                downloadId = cursor.getLong(cursor.getColumnIndex(DataOpenHelper.MusicColumn.VIDEO_DOWNLOAD_ID));
                
                
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return downloadId;
    
    
    }
    
    public MusicEntity queryByVideoDownloadId(long videodownloadId) {
        
        String sql = DataOpenHelper.MusicColumn.VIDEO_DOWNLOAD_ID + " = " + videodownloadId;
        
        MusicEntity musicEntity = null;
        Cursor cursor = null;
        
        try {
            SQLiteDatabase database = mDataOpenHelper.getWritableDatabase();
            cursor = database.query(DataOpenHelper.GUITAR_MUSIC_TABLE, null, sql, null, null, null, null);
            
            while (cursor.moveToNext()) {
                long _id = cursor.getLong(cursor.getColumnIndex(DataOpenHelper.MusicColumn.ID));
                long musicId = cursor.getLong(cursor.getColumnIndex(DataOpenHelper.MusicColumn.MUSIC_ID));
                String name = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.NAME));
                String musicAbstract = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.MUSIC_ABSTRACT));
                String detail = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.DETAIL));
                String detailUrl = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.DETAIL_IMG_URL));
                String detailLocal = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.DETAIL_IMG_LOCAL));
                
                String songUrl = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.SOUND_INTERNAL_URL));
                String songLocal = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.SOUND_LOCAL));
                String videoUrl = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.VIDEO_EXTERNAL_URL));
                String videoLocal = cursor.getString(cursor.getColumnIndex(DataOpenHelper.MusicColumn.VIDEO_LOCAL));
                int difficulty = cursor.getInt(cursor.getColumnIndex(DataOpenHelper.MusicColumn.DIFFICULTY));
                
                musicEntity = new MusicEntity(_id, musicId, 
                                                          name, musicAbstract, 
                                                          detail, detailUrl, detailLocal, 
                                                          songUrl, songLocal, difficulty, 
                                                          videoUrl, videoLocal);
                
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return musicEntity;
    
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
    
    public void updateMusicLocalUrl(long id, String localUrl) {
        SQLiteDatabase database = mDataOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        //values.put(DataOpenHelper.MusicColumn.ID, id);
        values.put(DataOpenHelper.MusicColumn.SOUND_LOCAL, localUrl);
        
        String where = DataOpenHelper.MusicColumn.ID + "=" + id;
        int row = database.update(DataOpenHelper.GUITAR_MUSIC_TABLE, values, where, null);
        
        Log.d(TAG, "row = " + row);
        
        MusicEntity musicEntity = queryById(id);
        notifyEntityUpdateListener(musicEntity);
    }
    
    public void updateVideoLocalUrl(long downloadId, String videoLocalUrl) {
        SQLiteDatabase database = mDataOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        //values.put(DataOpenHelper.MusicColumn.ID, id);
        values.put(DataOpenHelper.MusicColumn.VIDEO_LOCAL, videoLocalUrl);
        
        String where = DataOpenHelper.MusicColumn.VIDEO_DOWNLOAD_ID + "=" + downloadId;
        int row = database.update(DataOpenHelper.GUITAR_MUSIC_TABLE, values, where, null);
        
        Log.d(TAG, "row = " + row);
        
        MusicEntity musicEntity = queryByVideoDownloadId(downloadId);
        notifyEntityUpdateListener(musicEntity);
        
    }
    
    public void updateVideoDownloadId(long id, long videoDownloadId) {
        SQLiteDatabase database = mDataOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        //values.put(DataOpenHelper.MusicColumn.ID, id);
        values.put(DataOpenHelper.MusicColumn.VIDEO_DOWNLOAD_ID, videoDownloadId);
        
        String where = DataOpenHelper.MusicColumn.ID + "=" + id;
        int row = database.update(DataOpenHelper.GUITAR_MUSIC_TABLE, values, where, null);
        
    }
    
    
    
    public static interface IGuitarDataListener {
        
        void onMusicEntityChange(MusicEntity musicEntity);
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
            public static final String SOUND_INTERNAL_URL = "sound_internal_url";
            public static final String SOUND_EXTERNAL_URL = "sound_external_url";
            public static final String SOUND_LOCAL = "sound_local";
            public static final String VIDEO_INTERNAL_URL = "video_internal_url";
            public static final String VIDEO_EXTERNAL_URL = "video_external_url";
            public static final String VIDEO_LOCAL = "video_local";
            public static final String DIFFICULTY = "difficulty";
            public static final String VIDEO_DOWNLOAD_ID = "video_download_id";
            
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
                    MusicColumn.ID + " INTEGER PRIMARY KEY," +
                    MusicColumn.MUSIC_ID + " INTEGER," + 
                    MusicColumn.NAME + " TEXT," +
                    MusicColumn.MUSIC_ABSTRACT + " TEXT," +
                    MusicColumn.DETAIL + " TEXT," +
                    MusicColumn.DETAIL_IMG_URL + " TEXT," +
                    MusicColumn.DETAIL_IMG_LOCAL + " TEXT," +
                    MusicColumn.SOUND_INTERNAL_URL + " TEXT," +
                    MusicColumn.SOUND_EXTERNAL_URL + " TEXT," +
                    MusicColumn.SOUND_LOCAL + " TEXT," +
                    MusicColumn.VIDEO_INTERNAL_URL + " TEXT," +
                    MusicColumn.VIDEO_EXTERNAL_URL + " TEXT," +
                    MusicColumn.VIDEO_LOCAL + " TEXT," +
                    MusicColumn.DIFFICULTY + " INTEGER," +
                    MusicColumn.VIDEO_DOWNLOAD_ID + " INTEGER" +
                   ");");
            
            
            db.execSQL(
                    "CREATE UNIQUE INDEX MusicIndex ON guitar_musics (music_id);"
                    );
            } catch (SQLException exception) {
                Log.e(TAG, "exception: " + exception.getMessage());
            }
            
          

            
        }
    }
   

}
