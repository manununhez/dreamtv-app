package com.dream.dreamtv;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.dream.dreamtv.model.ErrorReason;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.model.VideoTests;
import com.dream.dreamtv.utils.LocaleHelper;
import com.dream.dreamtv.utils.SharedPreferenceUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
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
        //TODO change the way the token is stored
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


    public void setReasons(ErrorReason[] reasonsList) {
        String reasons = gson.toJson(Arrays.asList(reasonsList);
        SharedPreferenceUtils.save(this, getString(R.string.reasons_preferences), reasons);
    }

    public List<ErrorReason> getReasons() {
        String errorReasonList = SharedPreferenceUtils.getValue(this, getString(R.string.reasons_preferences));
        Type listType = new TypeToken<ArrayList<ErrorReason>>(){}.getType();
        return new Gson().fromJson(errorReasonList, listType);
    }

    public void setVideoTests(VideoTests[] videoTestsList) {
        String videoTests = gson.toJson(Arrays.asList(videoTestsList);
        SharedPreferenceUtils.save(this, getString(R.string.video_tests_preferences), videoTests);
    }

    public List<VideoTests> getVideoTests() {
        String videoTestsList = SharedPreferenceUtils.getValue(this, getString(R.string.video_tests_preferences));
        Type listType = new TypeToken<ArrayList<VideoTests>>(){}.getType();
        return new Gson().fromJson(videoTestsList, listType);
    }
}
