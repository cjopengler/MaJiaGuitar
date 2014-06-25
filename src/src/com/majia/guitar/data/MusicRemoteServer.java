/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.majia.guitar.data.json.ApkVersionJson;
import com.majia.guitar.data.json.MusicJson;
import com.majia.guitar.data.json.MusicJson.Music;
import com.majia.guitar.data.json.MusicTempJson;
import com.majia.guitar.data.json.VersionJson;
import com.majia.guitar.util.HttpUtil;
import com.majia.guitar.util.MusicLog;

/**
 * 
 * @author panxu
 * @since 2014-2-24
 */
public class MusicRemoteServer implements IRemoteServer {
    
    private static final String HOST_URL = "http://yogaguitar.duapp.com/";
    private static final String VERSION_URL = HOST_URL + "version.php";
    private static final String GET_MUSICS_URL = HOST_URL + "musics.php?version_code=%d";
    private static final String QUERY_APK_VERSION = HOST_URL + "/apk_version.php?version_code=%d";
    
    private static final class Holder {
        public static final MusicRemoteServer INSTANCE = new MusicRemoteServer();
    }
    
    public static MusicRemoteServer getInstance() {
        return Holder.INSTANCE;
    }
    
    private MusicRemoteServer() {
        
    }
    
    
    
   
    
    @SuppressLint("DefaultLocale")
    public static MusicJson getMusics(long versionCode) {
        
        String serverUrl = String.format(GET_MUSICS_URL, versionCode);
        HttpUriRequest request = new HttpGet(serverUrl);
 
        MusicJson musicJson = null;
        try {
            musicJson = HttpUtil.executeForJsonObject(request, MusicJson.class);
            
        } catch (ClientProtocolException e) {
            
        } catch (JsonSyntaxException e) {
            
        } catch (IOException e) {
           
        }
        
        return musicJson;
    }
    
    
    
    private static class MuscJsonListResponseHandler implements ResponseHandler<List<MusicTempJson>> {

        private static final String TAG = "MuscJsonListResponseHandler";
        
        @Override
        public List<MusicTempJson> handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            
            List<MusicTempJson> result = new ArrayList<MusicTempJson>();
            
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {

                String entity = EntityUtils.toString(response.getEntity(), "UTF-8");

                Gson gson = new Gson();

  

                TypeToken<List<MusicTempJson>> typeToken = new TypeToken<List<MusicTempJson>>() {};

                
                
                result = gson.fromJson(entity, typeToken.getType());

            } else {
                MusicLog.e(TAG, "JsonResonponsHandler: get status error " + statusCode);
            }
            
            return result;
        }
        
    }



    @SuppressLint("DefaultLocale")
    @Override
    public ApkVersionJson queryApkVersion(int versionCode) throws JsonSyntaxException, ClientProtocolException, IOException {
        
        String serverUrl = String.format(QUERY_APK_VERSION, versionCode);
        
    
        ApkVersionJson apkVersionJson = HttpUtil.executeForJsonObject(new HttpGet(serverUrl), ApkVersionJson.class);

        return apkVersionJson;
    }
    
    
}
