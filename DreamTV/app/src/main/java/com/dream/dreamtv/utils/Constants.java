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

    public static final String ARG_SUBTITLE = "ARG_SUBTITLE";
    public static final String ARG_SUBTITLE_ORIGINAL_POSITION = "ARG_SUBTITLE_ORIGINAL_POSITION";
    public static final String ARG_SELECTED_TASK = "ARG_SELECTED_TASK";
    public static final String ARG_USER_TASK_ERROR = "ARG_USER_TASK_ERROR";

    public static final String INTENT_USER_DATA_TASK = "INTENT_USER_DATA_TASK";
    public static final String INTENT_USER_DATA_SUBTITLE = "INTENT_USER_DATA_SUBTITLE";
    public static final String INTENT_USER_DATA_TASK_ERRORS = "INTENT_USER_DATA_TASK_ERRORS";
    public static final String INTENT_EXTRA_CALL_TASKS = "callTasks";
    public static final String INTENT_EXTRA_RESTART = "restart";

    public static final String ABR_CHINESE = "zh";
    public static final String ABR_ENGLISH = "en";
    public static final String ABR_SPANISH = "es";
    public static final String ABR_ARABIC = "ar";
    public static final String ABR_FRENCH = "fr";
    public static final String ABR_POLISH = "pl";

    //Types of tasks and categories
    public static final String TASKS_ALL_CAT = "all";
    public static final String TASKS_CONTINUE_CAT = "continue";
    public static final String TASKS_FINISHED_CAT = "finished";
    public static final String TASKS_MY_LIST_CAT = "myList";
    public static final String TASKS_TEST_CAT = "test";
    public static final String SETTINGS_CAT = "settings";

    public static final int BACKGROUND_UPDATE_DELAY = 300;

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


    //Network parameters
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_VIDEO_ID = "video_id";
    public static final String PARAM_LANG_CODE = "language_code";
    public static final String PARAM_VERSION = "version";
    public static final String PARAM_TASK_ID = "task_id";
    public static final String PARAM_INTERFACE_MODE = "interface_mode";
    public static final String PARAM_INTERFACE_LANGUAGE = "interface_language";
    public static final String PARAM_SUB_LANGUAGE = "sub_language";
    public static final String PARAM_AUDIO_LANGUAGE = "audio_language";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_SUB_LANGUAGE_CONFIG = "sub_language_config";
    public static final String PARAM_AUDIO_LANGUAGE_CONFIG = "audio_language_config";
    public static final String PARAM_SUB_VERSION = "subtitle_version";


    public static final String YOUTUBE_VIDEO_ID = "videoId";
    public static final String YOUTUBE_AUTOPLAY = "autoplay";
    public static final String YOUTUBE_SHOW_RELATED_VIDEOS = "showRelatedVideos";
    public static final String YOUTUBE_SHOW_VIDEO_INFO = "showVideoInfo";
    public static final String YOUTUBE_VIDEO_ANNOTATION = "videoAnnotation";
    public static final String YOUTUBE_DEBUG = "debug";
    public static final String YOUTUBE_CLOSED_CAPTIONS = "closedCaptions";
    public static final String STATE_PLAY = "PLAYING";
    public static final String STATE_BUFFERING = "BUFFERING";
    public static final String STATE_ENDED = "ENDED";
    public static final String STATE_PAUSED = "PAUSED";

}
