package com.dream.dreamtv.utils;

/**
 * Created by manuel on 8/22/17.
 */

public class Constants {
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_POLISH = "pl";

    public static final String NONE_OPTIONS_CODE = "NN";
    public static final String BEGINNER_INTERFACE_MODE = "beginner";
    public static final String ADVANCED_INTERFACE_MODE = "advanced";

    public static final String SHARED_ELEMENT_NAME = "hero";
    public static final String USER_DATA = "UserData";

    public static final String TASKS_ALL = "all";
    public static final String TASKS_CONTINUE = "continue";
    public static final String TASKS_FINISHED = "finished";
    public static final String TASKS_TEST = "test";
    public static final String TASKS_USER = "user";
    public static final String TASKS_OTHER_USERS = "others";

    public static final int BACKGROUND_UPDATE_DELAY = 300;

    public static final int MY_LIST_CATEGORY = 1250;
    public static final int CONTINUE_WATCHING_CATEGORY = 1251;
    public static final int CHECK_NEW_TASKS_CATEGORY = 1252;


//    FIREBASE LOG EVENTS
//    KEYS
    public static final String FIREBASE_KEY_SUBTITLE_NAVEGATION = "subtitle_navigation";
    public static final String FIREBASE_KEY_TESTING_MODE = "testing_mode";
    public static final String FIREBASE_KEY_SUB_LANGUAGE = "sub_language";
    public static final String FIREBASE_KEY_AUDIO_LANGUAGE = "audio_language";
    public static final String FIREBASE_KEY_INTERFACE_MODE = "interface_mode";
    public static final String FIREBASE_KEY_INTERFACE_LANGUAGE = "interface_language";
    public static final String FIREBASE_KEY_VIDEO_ID = "video_id";
    public static final String FIREBASE_KEY_PRIMARY_AUDIO_LANGUAGE = "primary_audio_language";
    public static final String FIREBASE_KEY_ORIGINAL_LANGUAGE = "original_language";
    public static final String FIREBASE_KEY_VIDEO_PROJECT_NAME = "project";
    public static final String FIREBASE_KEY_VIDEO_DURATION = "duration";
//    LOG EVENT NAMES
    public static final String FIREBASE_LOG_EVENT_PRESSED_ADD_VIDEO_MY_LIST_BTN = "pressed_add_video_my_list_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_REMOVE_VIDEO_MY_LIST_BTN = "pressed_remove_video_my_list_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_SAVE_SETTINGS_BTN = "pressed_save_settings_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_PLAY_VIDEO_BTN = "pressed_play_video_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSED = "pressed_playback_video_pause_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY = "pressed_playback_video_play_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_STOP_VIDEO = "pressed_playback_video_stop_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_CONTINUE_VIDEO = "pressed_continue_video_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_RESTART_VIDEO = "pressed_restart_video_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_SHOW_ERRORS = "pressed_show_errors_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS = "pressed_dismiss_errors_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO = "pressed_playback_video_bwd_btn";//backward
    public static final String FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO = "pressed_playback_video_fwd_btn"; //forward

    public enum Actions {
        PLAY("play"),
        PAUSE("pause"),
        FORWARD("forward"),
        REWIND("rewind");
        final String value;

        Actions(String value) {
            this.value = value;
        }
    }

}
