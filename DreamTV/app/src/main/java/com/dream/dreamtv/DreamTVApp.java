package com.dream.dreamtv;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.dream.dreamtv.beans.ReasonList;
import com.dream.dreamtv.beans.User;
import com.dream.dreamtv.utils.LocaleHelper;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.SharedPreferenceUtils;
import com.google.gson.Gson;


/**
 * Created by manunez on 13/10/2015.
 */
public class DreamTVApp extends Application {

    public static final String TAG = "com.dream.dreamtv";
    private Gson gson;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, Constants.LANGUAGE_ENGLISH));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gson = new Gson();

    }

    public User getUser() {
        //todo control cuando viene null

        String userString = SharedPreferenceUtils.getValue(this, getString(R.string.user_preferences));
        return userString == null ? new User() : gson.fromJson(userString, User.class);
    }

    public void setUser(User user) {
        //todo controlar si es que viene token null, entonces no deberia actualizarse ese campo
        if (user.token != null && !user.token.equals("")) {
            DreamTVApp.Logger.d("(SetUser) Actualizacion de Token");
            SharedPreferenceUtils.save(this, this.getString(R.string.dreamTVApp_token), user.token);
        }

//        if(user.interface_mode == null || user.interface_mode.equals(""))
//            user.interface_mode = Constants.BEGINNER_INTERFACE_MODE; //default mode

        String userString = gson.toJson(user);
        SharedPreferenceUtils.save(this, getString(R.string.user_preferences), userString);
    }

    public void setReasons(ReasonList reasons) {
        String userString = gson.toJson(reasons);
        SharedPreferenceUtils.save(this, getString(R.string.reasons_preferences), userString);
    }

//    public void setLanguages(String languages) {
//        SharedPreferenceUtils.save(this, getString(R.string.languages_preferences), languages);
//    }
//
//    public String getLanguages() {
//        return SharedPreferenceUtils.getValue(this, getString(R.string.languages_preferences));
//    }
//
//    public void setUserVideoList() {
//
//    }

    public ReasonList getReasons() {
        String reasonString = SharedPreferenceUtils.getValue(this, getString(R.string.reasons_preferences));
        ReasonList reasonList = gson.fromJson(reasonString, ReasonList.class);
        return reasonList;
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
