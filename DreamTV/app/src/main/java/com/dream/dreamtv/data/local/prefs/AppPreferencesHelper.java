package com.dream.dreamtv.data.local.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.dream.dreamtv.R;
import com.dream.dreamtv.data.model.VideoDuration;
import com.dream.dreamtv.data.networking.model.ErrorReason;
import com.dream.dreamtv.data.model.User;
import com.dream.dreamtv.data.networking.model.VideoTest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dream.dreamtv.utils.Constants.BEGINNER_INTERFACE_MODE;
import static com.dream.dreamtv.utils.Constants.LANGUAGE_POLISH;

public class AppPreferencesHelper {
    private static final String PREF_SUBTITLE_SMALL_SIZE = "25";
    private static final String PREF_VIDEO_DURATION_ALL = "-1";
    private static final String PREF_VIDEO_DURATION_MIN_0 = "0";
    private static final String PREF_VIDEO_DURATION_MIN_5 = "300";
    private static final String PREF_VIDEO_DURATION_MIN_10 = "600";
    private static final String PREF_KEY_ACCESS_TOKEN = "accessToken";
    private static final String PREF_KEY_USER_PREFERENCES = "userPreferences";
    private static final String PREF_KEY_REASONS_PREFERENCES = "reasonsPreferences";
    private static final String PREF_KEY_VIDEO_TESTS_PREFERENCES = "videoTestsPreferences";

    private final SharedPreferences mPrefs;
    private final Gson gson;
    private final Context mContext;

    public AppPreferencesHelper(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        gson = new Gson();
        mContext = context;
    }

    public String getAccessToken() {
        return mPrefs.getString(PREF_KEY_ACCESS_TOKEN, null);
    }

    public void setAccessToken(String accessToken) {
        mPrefs.edit().putString(PREF_KEY_ACCESS_TOKEN, accessToken).apply();
    }


    public User getUser() {
        String userString = mPrefs.getString(PREF_KEY_USER_PREFERENCES, null);
        return userString == null ? null : gson.fromJson(userString, User.class);
    }

    public void setUser(User user) {
        String userString = gson.toJson(user);
        mPrefs.edit().putString(PREF_KEY_USER_PREFERENCES, userString).apply();

    }

    public ArrayList<ErrorReason> getReasons() {
        String errorReasonList = mPrefs.getString(PREF_KEY_REASONS_PREFERENCES, null);
        Type listType = new TypeToken<ArrayList<ErrorReason>>() {
        }.getType();

        return getReasonsByLanguage(gson.fromJson(errorReasonList, listType));

    }

    public void setReasons(ErrorReason[] reasonsList) {
        String reasons = gson.toJson(Arrays.asList(reasonsList));
        mPrefs.edit().putString(PREF_KEY_REASONS_PREFERENCES, reasons).apply();
    }


    private ArrayList<ErrorReason> getReasonsByLanguage(List<ErrorReason> list) {
        User user = getUser();
        ArrayList<ErrorReason> newList = new ArrayList<>();

        for (ErrorReason error : list) {
            if (error.language.equals(user.getSubLanguage()))
                newList.add(error);
        }

        return newList;
    }

    public List<VideoTest> getVideoTests() {
        String videoTestsList = mPrefs.getString(PREF_KEY_VIDEO_TESTS_PREFERENCES, null);
        Type listType = new TypeToken<ArrayList<VideoTest>>() {
        }.getType();
        return gson.fromJson(videoTestsList, listType);
    }

    public void setVideoTests(VideoTest[] videoTestList) {
        String videoTests = gson.toJson(Arrays.asList(videoTestList));
        mPrefs.edit().putString(PREF_KEY_VIDEO_TESTS_PREFERENCES, videoTests).apply();
    }

    public boolean getTestingModePref() {
        return mPrefs.getBoolean(mContext.getString(R.string.pref_key_testing_mode), false);
    }

    public String getSubtitleSizePref() {
        return mPrefs.getString(mContext.getString(R.string.pref_key_subtitle_size), PREF_SUBTITLE_SMALL_SIZE);
    }

    public String getAudioLanguagePref() {
        return mPrefs.getString(mContext.getString(R.string.pref_key_list_audio_languages), LANGUAGE_POLISH);
    }

    public String getInterfaceModePref() {
        return mPrefs.getString(mContext.getString(R.string.pref_key_list_interface_mode), BEGINNER_INTERFACE_MODE);
    }

    public String getInterfaceLanguagePref() {
        return mPrefs.getString(mContext.getString(R.string.pref_key_list_app_languages), LANGUAGE_POLISH);
    }

    public VideoDuration getVideoDurationPref() {
        String videoDurationString = mPrefs.getString(mContext.getString(R.string.pref_key_video_duration), PREF_VIDEO_DURATION_ALL);

        assert videoDurationString != null;

        switch (videoDurationString) {
            case PREF_VIDEO_DURATION_ALL:
                return new VideoDuration(
                        Integer.parseInt(PREF_VIDEO_DURATION_ALL),
                        Integer.parseInt(PREF_VIDEO_DURATION_ALL)
                );
            case PREF_VIDEO_DURATION_MIN_0:
                return new VideoDuration(
                        Integer.parseInt(PREF_VIDEO_DURATION_MIN_0),
                        Integer.parseInt(PREF_VIDEO_DURATION_MIN_5)
                );

            case PREF_VIDEO_DURATION_MIN_5:
                return new VideoDuration(
                        Integer.parseInt(PREF_VIDEO_DURATION_MIN_5),
                        Integer.parseInt(PREF_VIDEO_DURATION_MIN_10)
                );
            case PREF_VIDEO_DURATION_MIN_10:
                return new VideoDuration(
                        Integer.parseInt(PREF_VIDEO_DURATION_MIN_10),
                        Integer.parseInt(PREF_VIDEO_DURATION_ALL)
                );
            default:
                throw new RuntimeException("Video duration not contemplated!");

        }
    }


}
