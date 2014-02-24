package com.majia.guitar.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.majia.guitar.MaJiaGuitarApplication;


import android.os.Environment;
import android.util.Log;

public class MusicLog {
    private final static String TAG = MusicLog.class.getSimpleName();
    /** 日志总开关 **/
    private static final boolean IS_DEBUGING = true;

    // logcat level
    private static int LOGCAT_LEVEL = (IS_DEBUGING) ? 2 : 32;
    // log file level,must >=LOGCAT_LEVEL
    private static int FILE_LOG_LEVEL = (IS_DEBUGING) ? 2 : 32;

    final static int LOG_LEVEL_ERROR = 16;
    final static int LOG_LEVEL_WARN = 8;
    final static int LOG_LEVEL_INFO = 4;
    final static int LOG_LEVEL_DEBUG = 2;

    final static int LOG_LEVEL_VERBOSE = 0;
    private static boolean VERBOSE = (LOGCAT_LEVEL <= LOG_LEVEL_VERBOSE);
    private static boolean DEBUG = (LOGCAT_LEVEL <= LOG_LEVEL_DEBUG);
    private static boolean INFO = (LOGCAT_LEVEL <= LOG_LEVEL_INFO);
    private static boolean WARN = (LOGCAT_LEVEL <= LOG_LEVEL_WARN);
    private static boolean ERROR = (LOGCAT_LEVEL <= LOG_LEVEL_ERROR);

    /** 统一TAG **/
    private final static String LOG_TAG_STRING = "XiaoduRouter";
    /** 当前日志日志文件名 **/
    private final static String LOG_FILE_NAME = ".XiaoduRouter.log";
    /** 日志文件大小阀值，只在commit方法调用时构建文件 **/
    private final static long LOG_SIZE = 5 * 1024 * 1024;
    /** 日志格式，形如：[2010-01-22 13:39:1][D][com.a.c]error occured **/
    private final static String LOG_ENTRY_FORMAT = "[%tF %tT][%s][%s]%s";

    static PrintStream logStream;

    static boolean initialized = false;

    public static boolean isDebug() {
        return IS_DEBUGING;
    }

    /**
     * 不写文件的debug日志
     * 
     * @param logFile
     */
    private static void d(File logFile) {
        if (DEBUG) {
            Log.d(LOG_TAG_STRING, TAG + " : Log to file : " + logFile);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.d(LOG_TAG_STRING, tag + " : " + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG)
                write("D", tag, msg, null);
        }
    }

    public static void d(String tag, String msg, Throwable error) {
        if (DEBUG) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.d(LOG_TAG_STRING, tag + " : " + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG)
                write("D", tag, msg, error);
        }
    }

    /**
     * 不写文件的verbose日志
     * 
     * @param backfile
     */
    private static void v(File backfile) {
        if (VERBOSE) {
            Log.v(LOG_TAG_STRING, TAG + " : Create back log file : " + backfile.getName());
        }
    }

    public static void v(String tag, String msg) {
        if (DEBUG) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.v(LOG_TAG_STRING, tag + " : " + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG)
                write("V", tag, msg, null);
        }
    }

    public static void v(String tag, String msg, Throwable error) {
        if (DEBUG) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.v(LOG_TAG_STRING, tag + " : " + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG)
                write("V", tag, msg, error);
        }
    }

    public static void i(String tag, String msg) {
        if (INFO) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.i(LOG_TAG_STRING, tag + " : " + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_INFO)
                write("I", tag, msg, null);
        }
    }

    public static void i(String tag, String msg, Throwable error) {
        if (INFO) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.i(LOG_TAG_STRING, tag + " : " + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_INFO)
                write("I", tag, msg, error);
        }
    }

    /**
     * 不写文件的w
     */
    private static void w() {
        if (WARN) {
            Log.w(LOG_TAG_STRING, "Unable to create external cache directory");
        }
    }

    public static void w(String tag, String msg) {
        if (WARN) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.w(LOG_TAG_STRING, tag + " : " + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_WARN)
                write("W", tag, msg, null);
        }
    }

    public static void w(String tag, String msg, Throwable error) {
        if (WARN) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.w(LOG_TAG_STRING, tag + " : " + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_WARN)
                write("W", tag, msg, error);
        }
    }

    /**
     * 不写文件的错误日志
     * 
     * @param e
     */
    private static void e(String msg, Exception e) {
        if (ERROR) {
            Log.e(LOG_TAG_STRING, msg, e);
        }
    }

    public static void e(String tag, String msg) {
        if (ERROR) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.e(LOG_TAG_STRING, tag + " : " + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR)
                write("E", tag, msg, null);
        }
    }

    public static void e(String tag, String msg, Throwable error) {
        if (ERROR) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.e(LOG_TAG_STRING, tag + " : " + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR)
                write("E", tag, msg, error);
        }
    }

    public static void wtf(String tag, String msg) {
        if (ERROR) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.wtf(LOG_TAG_STRING, tag + " : " + msg);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR)
                write("E", tag, msg, null);
        }
    }

    public static void wtf(String tag, String msg, Throwable error) {
        if (ERROR) {
            tag = Thread.currentThread().getName() + ":" + tag;
            Log.wtf(LOG_TAG_STRING, tag + " : " + msg, error);
            if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR)
                write("E", tag, msg, error);
        }
    }

    private static void write(String level, String tag, String msg, Throwable error) {
        if (!initialized)
            init();
        if (logStream == null || logStream.checkError()) {
            initialized = false;
            return;
        }
        Date now = new Date();

        logStream.printf(LOG_ENTRY_FORMAT, now, now, level, tag, " : " + msg);
        logStream.println();

        if (error != null) {
            error.printStackTrace(logStream);
            logStream.println();
        }

    }

    private static synchronized void init() {
        if (initialized)
            return;
        try {
            File cacheRoot = getSDCacheFile(); // 改到应用目录

            if (cacheRoot != null) {
                File logFile = new File(cacheRoot, LOG_FILE_NAME);
                logFile.createNewFile();
                d(logFile);
                if (logStream != null) {
                    logStream.close();
                }
                logStream = new PrintStream(new FileOutputStream(logFile, true), true);
                initialized = true;
            }
        } catch (Exception e) {
            e("catch root error", e);
        }
    }

    private static boolean isSdCardAvailable() {
        File file = Environment.getExternalStorageDirectory();
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                && (file != null && file.exists());
    }

    private static File getSDCacheFile() {// 为什么要重复代码。1。log不确定能不能取到context。2log时机不确定
        if (isSdCardAvailable()) {
            File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
            File appCacheDir = new File(new File(dataDir, MaJiaGuitarApplication.getInstance().getPackageName()), "cache");
            if (!appCacheDir.exists()) {
                if (!appCacheDir.mkdirs()) {
                    w();
                    return null;
                }
            }
            return appCacheDir;

        } else {
            return null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (logStream != null)
            logStream.close();
    }

    /**
     * 判断，如果文件大于5M 则 开启新的日志文件，旧的日志命名为 netdisk_{日期}.log
     */
    public static synchronized String commit() {
        try {
            File cacheRoot = getSDCacheFile(); // 改到应用目录

            if (cacheRoot != null) {
                File logFile = new File(cacheRoot, LOG_FILE_NAME);
                File backfile = new File(cacheRoot, LOG_TAG_STRING + "_"
                        + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".log");
                logFile.renameTo(backfile);
                logFile.delete();
                logFile.createNewFile();
                v(backfile);
                if (logStream != null) {
                    logStream.close();
                }
                logStream = new PrintStream(new FileOutputStream(logFile, true), true);
                return backfile.getAbsolutePath();
            }
            return null;
        } catch (IOException e) {
            e("Create back log file & init log stream failed", e);
            return null;
        }
    }

    /**
     * 根据文件大小手动切分日志文件 默认为大于5M
     */
    public static synchronized void commitByFileSize() {
        try {
            File cacheRoot = getSDCacheFile(); // 改到应用目录

            if (cacheRoot != null) {
                File logFile = new File(cacheRoot, LOG_FILE_NAME);
                if (logFile.length() > LOG_SIZE) {
                    File backfile = new File(cacheRoot, LOG_TAG_STRING + "_"
                            + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".log");
                    logFile.renameTo(backfile);
                    logFile.delete();
                    logFile.createNewFile();
                    v(backfile);
                    if (logStream != null) {
                        logStream.close();
                    }
                    logStream = new PrintStream(new FileOutputStream(logFile, true), true);
                }
            }
        } catch (IOException e) {
            e("Create back log file & init log stream failed", e);
        }
    }

}
