package com.manuelnunhez.dreamtv.data.networking;

import android.net.Uri;

import com.manuelnunhez.dreamtv.data.model.Category;
import com.manuelnunhez.dreamtv.data.model.VideoDuration;

import static com.manuelnunhez.dreamtv.data.networking.NetworkUtils.Urls.CATEGORIES;
import static com.manuelnunhez.dreamtv.data.networking.NetworkUtils.Urls.LOGIN;
import static com.manuelnunhez.dreamtv.data.networking.NetworkUtils.Urls.REASON_ERRORS;
import static com.manuelnunhez.dreamtv.data.networking.NetworkUtils.Urls.REGISTER;
import static com.manuelnunhez.dreamtv.data.networking.NetworkUtils.Urls.SUBTITLE;
import static com.manuelnunhez.dreamtv.data.networking.NetworkUtils.Urls.TASKS_BY_CATEGORY;
import static com.manuelnunhez.dreamtv.data.networking.NetworkUtils.Urls.TASKS_BY_KEYWORD_CATEGORIES;
import static com.manuelnunhez.dreamtv.data.networking.NetworkUtils.Urls.TASKS_SEARCH;
import static com.manuelnunhez.dreamtv.data.networking.NetworkUtils.Urls.USER;
import static com.manuelnunhez.dreamtv.data.networking.NetworkUtils.Urls.USER_DETAILS;
import static com.manuelnunhez.dreamtv.data.networking.NetworkUtils.Urls.USER_ERRORS;
import static com.manuelnunhez.dreamtv.data.networking.NetworkUtils.Urls.USER_TASKS;
import static com.manuelnunhez.dreamtv.data.networking.NetworkUtils.Urls.USER_TASK_MY_LIST;
import static com.manuelnunhez.dreamtv.data.networking.NetworkUtils.Urls.VIDEO_TESTS;
import static com.manuelnunhez.dreamtv.utils.Constants.PARAM_AUDIO_LANGUAGE;
import static com.manuelnunhez.dreamtv.utils.Constants.PARAM_CATEGORY;
import static com.manuelnunhez.dreamtv.utils.Constants.PARAM_INTERFACE_MODE;
import static com.manuelnunhez.dreamtv.utils.Constants.PARAM_LANG_CODE;
import static com.manuelnunhez.dreamtv.utils.Constants.PARAM_MAX_VIDEO_DURATION;
import static com.manuelnunhez.dreamtv.utils.Constants.PARAM_MIN_VIDEO_DURATION;
import static com.manuelnunhez.dreamtv.utils.Constants.PARAM_PAGE;
import static com.manuelnunhez.dreamtv.utils.Constants.PARAM_QUERY;
import static com.manuelnunhez.dreamtv.utils.Constants.PARAM_SUB_LANGUAGE;
import static com.manuelnunhez.dreamtv.utils.Constants.PARAM_TASK_ID;
import static com.manuelnunhez.dreamtv.utils.Constants.PARAM_TYPE;
import static com.manuelnunhez.dreamtv.utils.Constants.PARAM_VERSION;
import static com.manuelnunhez.dreamtv.utils.Constants.PARAM_VIDEO_ID;

class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static final String BASE_URL = "http://www.dreamproject.pjwstk.edu.pl/api/";

    private NetworkUtils() {
    }

    private static Uri taskUrlFormatter(String uriString, Category.Type paramType, int page, VideoDuration videoDuration) {

        Uri uri;

        if (videoDuration.getMinDuration() > 0 && videoDuration.getMaxDuration() > 0)
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .appendQueryParameter(PARAM_PAGE, String.valueOf(page))
                    .appendQueryParameter(PARAM_MIN_VIDEO_DURATION, String.valueOf(videoDuration.getMinDuration()))
                    .appendQueryParameter(PARAM_MAX_VIDEO_DURATION, String.valueOf(videoDuration.getMaxDuration()))
                    .build();

        else if (videoDuration.getMinDuration() > 0)
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .appendQueryParameter(PARAM_PAGE, String.valueOf(page))
                    .appendQueryParameter(PARAM_MIN_VIDEO_DURATION, String.valueOf(videoDuration.getMinDuration()))
                    .build();

        else if (videoDuration.getMaxDuration() > 0)
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .appendQueryParameter(PARAM_PAGE, String.valueOf(page))
                    .appendQueryParameter(PARAM_MAX_VIDEO_DURATION, String.valueOf(videoDuration.getMaxDuration()))
                    .build();

        else
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .appendQueryParameter(PARAM_PAGE, String.valueOf(page))
                    .build();

        return uri;
    }

    private static Uri taskUrlFormatter(String uriString, Category.Type paramType, VideoDuration videoDuration) {

        Uri uri;

        if (videoDuration.getMinDuration() > 0 && videoDuration.getMaxDuration() > 0)
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .appendQueryParameter(PARAM_MIN_VIDEO_DURATION, String.valueOf(videoDuration.getMinDuration()))
                    .appendQueryParameter(PARAM_MAX_VIDEO_DURATION, String.valueOf(videoDuration.getMaxDuration()))
                    .build();

        else if (videoDuration.getMinDuration() > 0)
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .appendQueryParameter(PARAM_MIN_VIDEO_DURATION, String.valueOf(videoDuration.getMinDuration()))
                    .build();

        else if (videoDuration.getMaxDuration() > 0)
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .appendQueryParameter(PARAM_MAX_VIDEO_DURATION, String.valueOf(videoDuration.getMaxDuration()))
                    .build();

        else
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .build();


        return uri;
    }

    static String getLoginURL() {
        return String.valueOf(Uri.parse(BASE_URL.concat(LOGIN.value)).buildUpon().build());
    }

    static String getRegisterURL() {
        return String.valueOf(Uri.parse(BASE_URL.concat(REGISTER.value)).buildUpon().build());
    }

    static String getUserDetailsURL() {
        return String.valueOf(Uri.parse(BASE_URL.concat(USER_DETAILS.value)).buildUpon().build());
    }

    static String getUserURL(String interfaceMode, String subLanguage, String audioLanguage) {
        return String.valueOf(Uri.parse(BASE_URL.concat(USER.value)).buildUpon()
                .appendQueryParameter(PARAM_INTERFACE_MODE, interfaceMode)
                .appendQueryParameter(PARAM_SUB_LANGUAGE, subLanguage)
                .appendQueryParameter(PARAM_AUDIO_LANGUAGE, audioLanguage)
                .build());
    }

    static String getErrorReasonsURL() {
        return String.valueOf(Uri.parse(BASE_URL.concat(REASON_ERRORS.value)).buildUpon().build());

    }

    static String getCategoriesURL() {
        return String.valueOf(Uri.parse(BASE_URL.concat(CATEGORIES.value)).buildUpon()
                .build());
    }

    static String getVideoTestsURL() {
        return String.valueOf(Uri.parse(BASE_URL.concat(VIDEO_TESTS.value)).buildUpon()
                .build());
    }

    static String getSubtitleURL(String videoId, String languageCode, String version) {
        return String.valueOf(Uri.parse(BASE_URL.concat(SUBTITLE.value)).buildUpon()
                .appendQueryParameter(PARAM_VIDEO_ID, videoId)
                .appendQueryParameter(PARAM_LANG_CODE, languageCode)
                .appendQueryParameter(PARAM_VERSION, version)
                .build());
    }

    static String getTasksURL(Category.Type category, int page, VideoDuration videoDuration) {
        return String.valueOf(taskUrlFormatter(BASE_URL.concat(TASKS_BY_CATEGORY.value),
                category, page, videoDuration));
    }

    static String getTasksURL(Category.Type category, VideoDuration videoDuration) {
        return String.valueOf(taskUrlFormatter(BASE_URL.concat(TASKS_BY_CATEGORY.value),
                category, videoDuration));
    }

    static String searchByCategoryURL(String category) {
        return String.valueOf(Uri.parse(BASE_URL.concat(TASKS_BY_KEYWORD_CATEGORIES.value)).buildUpon()
                .appendQueryParameter(PARAM_CATEGORY, category)
                .build());
    }

    static String searchURL(String query) {
        return String.valueOf(Uri.parse(BASE_URL.concat(TASKS_SEARCH.value)).buildUpon()
                .appendQueryParameter(PARAM_QUERY, query)
                .build());
    }

    static String userTaskURL() {
        return String.valueOf(Uri.parse(BASE_URL.concat(USER_TASKS.value)).buildUpon()
                .build());
    }

    static String userErrorsURL() {
        return String.valueOf(Uri.parse(BASE_URL.concat(USER_ERRORS.value)).buildUpon()
                .build());
    }

    static String addTaskToUserListURL() {
        return String.valueOf(Uri.parse(BASE_URL.concat(USER_TASK_MY_LIST.value)).buildUpon()
                .build());
    }

    static String removeTaskFromUserListURL(int taskId) {
        return String.valueOf(Uri.parse(BASE_URL.concat(USER_TASK_MY_LIST.value)).buildUpon()
                .appendQueryParameter(PARAM_TASK_ID, String.valueOf(taskId))
                .build());
    }

    enum Urls {
        LOGIN("login"),

        REGISTER("register"),

        USER_DETAILS("details"),

        TASKS_BY_KEYWORD_CATEGORIES("tasks/search/category"),

        TASKS_BY_CATEGORY("tasks/categories"),

        TASKS_SEARCH("tasks/search"),

        USER_TASKS("usertasks"),

        USER_ERRORS("usertask/errors"),

        USER_TASK_MY_LIST("usertask/list"),

        VIDEO_TESTS("videotests"),

        CATEGORIES("categories"),

        REASON_ERRORS("errors"),

        SUBTITLE("amara/subtitle"),

        USER("user");


        final String value;

        Urls(String value) {
            this.value = value;
        }
    }
}
