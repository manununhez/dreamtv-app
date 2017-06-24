package com.dream.dreamtv;

import android.app.Application;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by manunez on 13/10/2015.
 */
public class DreamTVApp extends Application {

    public static final String TAG = "com.cuatroqstudios.geo";
//    private Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();
//        gson = new Gson();

    }

    public static class Logger {
        public static void i(String message) {
            if (message == null) {
                message = "Sin mensaje";
            }
            Log.i(TAG, message);
        }

        public static void w(String message) {
            if (message == null) {
                message = "Sin mensaje";
            }
            Log.w(TAG, message);
        }

        public static void e(String message) {
            if (message == null) {
                message = "Sin mensaje";
            }
            Log.e(TAG, message);
        }

        public static void d(String message) {
            if (message == null) {
                message = "Sin mensaje";
            }
            Log.d(TAG, message);
        }

    }

}
