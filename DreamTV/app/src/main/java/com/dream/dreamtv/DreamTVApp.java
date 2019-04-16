package com.dream.dreamtv;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.dream.dreamtv.model.ErrorReason;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.utils.LocaleHelper;
import com.dream.dreamtv.utils.SharedPreferenceUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
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

    public void setToken(String token){
        Log.d(TAG,"Set Token updated");
        SharedPreferenceUtils.save(this, this.getString(R.string.dreamTVApp_token), token);

    }

    public String getToken(){
        return SharedPreferenceUtils.getValue(this, this.getString(R.string.dreamTVApp_token));

    }

    public User getUser() {
        String userString = SharedPreferenceUtils.getValue(this, getString(R.string.user_preferences));
        return userString == null ? null : gson.fromJson(userString, User.class);
    }

    public void setUser(User user) {
        String userString = gson.toJson(user);
        SharedPreferenceUtils.save(this, getString(R.string.user_preferences), userString);

    }

    public String getTestingMode() {
        return SharedPreferenceUtils.getValue(this, getString(R.string.testing_mode_preferences), getString(R.string.text_no_option));
    }

    public void setTestingMode(String mode) {
        SharedPreferenceUtils.save(this, getString(R.string.testing_mode_preferences), mode);
    }


    public String getSharingMode() {
        return SharedPreferenceUtils.getValue(this, getString(R.string.sharing_mode_preferences), getString(R.string.text_no_option));
    }

    public void setSharingMode(String mode) {
        SharedPreferenceUtils.save(this, getString(R.string.sharing_mode_preferences), mode);
    }

    public void setReasons(List<ErrorReason> reasons) {
        String reasonList = gson.toJson(reasons);
        SharedPreferenceUtils.save(this, getString(R.string.reasons_preferences), reasonList);
    }

    public List<ErrorReason> getReasons() {
        String errorReasonList = SharedPreferenceUtils.getValue(this, getString(R.string.reasons_preferences));
        Type listType = new TypeToken<ArrayList<ErrorReason>>(){}.getType();
        return new Gson().fromJson(errorReasonList, listType);
    }

}
