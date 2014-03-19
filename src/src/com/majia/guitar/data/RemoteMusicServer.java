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

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
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
public class RemoteMusicServer {
    
    private static final String VERSION_URL = "http://yogaguitar.duapp.com/version.php";
    private static final String GET_MUSICS_URL = "http://yogaguitar.duapp.com/musics.php";
    
    private RemoteMusicServer() {
        
    }
    
    public static Versions getVersions() {
        Versions versions = null;
        
        HttpUriRequest request = new HttpGet(VERSION_URL);
        try {
            VersionJson versionJson = HttpUtil.executeForJsonObject(request, VersionJson.class);
            versions = new Versions(versionJson);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        
        return versions;
    }
    
    public static MusicJson getMusics() {
        HttpUriRequest request = new HttpGet(GET_MUSICS_URL);
 
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
    
    
}
