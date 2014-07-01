/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.util;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;

/**
 * 
 * @author panxu
 * @since 2014-7-1
 */
public class SDCardUtil {
    private SDCardUtil() {
        
    }
    
    public static boolean existSDCard() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }
    
    public static long getSDFreeSize() {
        // 取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        // 获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        // 空闲的数据块的数量
        long freeBlocks = sf.getAvailableBlocks();
        // 返回SD卡空闲大小
        return freeBlocks * blockSize; //单位Byte
       
    }
    
    public static long getSDAllSize(){
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory(); 
        StatFs sf = new StatFs(path.getPath()); 
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize(); 
        //获取所有数据块数
        long allBlocks = sf.getBlockCount();
        return (allBlocks * blockSize);
      }
}
