package com.example.library.billing;

import android.util.Log;

import com.example.library.BuildConfig;

/**
 * PaySdk log工具
 */
class PaySdkLog {

    /**
     * 是否开启log(默认情况，debug-开，release-关)
     */
    public static boolean enable = BuildConfig.DEBUG;
    private static final String TAG = "PaySdk";

    public static void d(String msg) {
        if (!enable) {
            return;
        }
        String stackInfo = getStackInfo();
        Log.d(TAG, stackInfo + msg);
    }

    private static String getStackInfo() {
        String[] infos = new String[]{"", "",""};
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length > 4) {
            infos[0] = elements[4].getFileName();
            infos[1] = elements[4].getMethodName();
            infos[2] = String.valueOf(elements[4].getLineNumber());
            return "("+infos[0]+":"+infos[2]+")"+"_"+infos[1]+" ";
        }

        return "";
    }
}
