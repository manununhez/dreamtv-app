package com.dream.dreamtv;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.dream.dreamtv.model.Reason;
import com.dream.dreamtv.model.ReasonList;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.utils.LocaleHelper;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.SharedPreferenceUtils;
import com.google.gson.Gson;

import java.util.List;


/**
 * Created by manunez on 13/10/2015.
 */
public class DreamTVApp extends Application {

    private static final String TAG = DreamTVApp.class.getSimpleName();
    private Gson gson;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    @Override
    public void onCreate() {
        super.onCreate();
        gson = new Gson();

    }

    public User getUser() {
        //todo control cuando viene null

        String userString = SharedPreferenceUtils.getValue(this, getString(R.string.user_preferences));
        return userString == null ? null : gson.fromJson(userString, User.class);
    }

    public void setUser(User user) {
        //todo controlar si es que viene token null, entonces no deberia actualizarse ese campo
        if (user.token != null && !user.token.isEmpty()) {
            Log.d(TAG,"(SetUser) Actualizacion de Token");
            SharedPreferenceUtils.save(this, this.getString(R.string.dreamTVApp_token), user.token);
        }


        String userString = gson.toJson(user);
        SharedPreferenceUtils.save(this, getString(R.string.user_preferences), userString);

    }

    public String getTestingMode() {
        return SharedPreferenceUtils.getValue(this, getString(R.string.testing_mode_preferences));
    }

    public void setTestingMode(String mode) {
        SharedPreferenceUtils.save(this, getString(R.string.testing_mode_preferences), mode);
    }

}
