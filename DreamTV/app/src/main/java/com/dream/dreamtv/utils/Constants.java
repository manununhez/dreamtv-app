package com.dream.dreamtv.utils;

/**
 * Created by manuel on 8/22/17.
 */

public final class Constants {
    public static final String BEGINNER_INTERFACE_MODE = "beginner";
    public static final String ADVANCED_INTERFACE_MODE = "advanced";

    public static final String LANGUAGE_POLISH = "pl";

    public static final String ARG_LIST_REASONS = "ARG_LIST_REASONS";
    public static final String ARG_USER = "ARG_USER";
    public static final String ARG_SUBTITLE = "ARG_SUBTITLE";
    public static final String ARG_SUBTITLE_ORIGINAL_POSITION = "ARG_SUBTITLE_ORIGINAL_POSITION";
    public static final String ARG_SELECTED_TASK = "ARG_SELECTED_TASK";
    public static final String ARG_USER_TASK_ERROR = "ARG_USER_TASK_ERROR";
    public static final String ARG_USER_TASK_ERROR_COMPLETE = "ARG_USER_TASK_ERROR_COMPLETE";

    public static final String INTENT_TASK = "INTENT_TASK";
    public static final String INTENT_SUBTITLE = "INTENT_SUBTITLE";
    public static final String INTENT_USER_TASK = "INTENT_USER_TASK";
    public static final String INTENT_EXTRA_CALL_TASKS = "INTENT_EXTRA_CALL_TASKS";
    public static final String INTENT_EXTRA_RESTART = "INTENT_EXTRA_RESTART";
    public static final String INTENT_EXTRA_UPDATE_USER = "INTENT_EXTRA_UPDATE_USER";
    public static final String INTENT_EXTRA_TESTING_MODE = "INTENT_EXTRA_TESTING_MODE";
    public static final String INTENT_EXTRA_TOPIC_NAME = "CategoryName";
    public static final String INTENT_PLAY_FROM_BEGINNING = "INTENT_PLAY_FROM_BEGINNING";
    public static final String INTENT_CATEGORY = "INTENT_CATEGORY";

    public static final String EMPTY_ITEM = "Some item";


    //    FIREBASE LOG EVENTS
//    KEYS
    public static final String FIREBASE_KEY_QUERY = "query";
    public static final String FIREBASE_KEY_SETTINGS_CATEGORY_SELECTED = "settings_category_selected";
    public static final String FIREBASE_KEY_CATEGORY_SELECTED = "category_selected";
    public static final String FIREBASE_KEY_TASK_CATEGORY_SELECTED = "task_category_selected";
    public static final String FIREBASE_KEY_TASK_SELECTED = "task_selected";
    public static final String FIREBASE_KEY_PASSWORD = "password";
    public static final String FIREBASE_KEY_EMAIL = "email";
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
    public static final String FIREBASE_KEY_MIN_VIDEO_DURATION_PREFS = "min_video_duration_config";
    public static final String FIREBASE_KEY_MAX_VIDEO_DURATION_PREFS = "max_video_duration_config";
    public static final String FIREBASE_KEY_TASK_ID = "task_id";
    public static final String FIREBASE_KEY_SUBTITLE_SIZE_PREFS = "subtitle_size";
    public static final String FIREBASE_KEY_USER_TASK_ID = "user_task_id";
    public static final String FIREBASE_KEY_RATING = "rating";
    //    LOG EVENT NAMES
    public static final String FIREBASE_LOG_EVENT_LOGIN = "login_event_started";
    public static final String FIREBASE_LOG_EVENT_PRESSED_SAVE_SETTINGS_BTN = "settings_configuration_saved";
    public static final String FIREBASE_LOG_EVENT_SETTINGS = "settings_event_enter";
    public static final String FIREBASE_LOG_EVENT_CATEGORIES = "categories_event_enter";
    public static final String FIREBASE_LOG_EVENT_TASK_SELECTED = "task_selected_event_enter";
    public static final String FIREBASE_LOG_EVENT_SEARCH = "search_event";
    public static final String FIREBASE_LOG_EVENT_PRESSED_CANCEL_EXIT_DIALOG = "pressed_exitdialog_cancel_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_EXIT_EXIT_DIALOG = "pressed_exitdialog_exit_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_ADD_VIDEO_MY_LIST_BTN = "pressed_add_video_my_list_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_REMOVE_VIDEO_MY_LIST_BTN = "pressed_remove_video_my_list_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_PLAY_VIDEO_BTN = "pressed_play_video_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_CONTINUE_VIDEO = "pressed_continue_video_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_RESTART_VIDEO = "pressed_restart_video_btn";

    public static final String FIREBASE_LOG_EVENT_PRESSED_VIDEO_PAUSE = "pressed_playback_video_pause_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_VIDEO_PLAY = "pressed_playback_video_play_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_VIDEO_STOP = "pressed_playback_video_stop_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_BACKWARD_VIDEO = "pressed_playback_video_bwd_btn";//backward
    public static final String FIREBASE_LOG_EVENT_PRESSED_FORWARD_VIDEO = "pressed_playback_video_fwd_btn"; //forward
    public static final String FIREBASE_LOG_EVENT_PRESSED_REMOTE_BACK_BTN = "pressed_remote_back_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_SHOW_ERRORS = "pressed_show_errors_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_SHOW_PROGRESS_PLAYER = "pressed_show_progress_player";
    public static final String FIREBASE_LOG_EVENT_PRESSED_DISMISS_PROGRESS_PLAYER = "pressed_dismiss_progress_player";
    public static final String FIREBASE_LOG_EVENT_VIDEO_COMPLETED = "video_completed_event";

    public static final String FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS = "pressed_dismiss_errors_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_T = "pressed_dismiss_errors_btn_t";
    public static final String FIREBASE_LOG_EVENT_PRESSED_DISMISS_ERRORS_F = "pressed_dismiss_errors_btn_f";
    public static final String FIREBASE_LOG_EVENT_PRESSED_SAVED_ERRORS = "pressed_save_errors_btn";
    public static final String FIREBASE_LOG_EVENT_PRESSED_UPDATED_ERRORS = "pressed_update_errors_btn";
    public static final String FIREBASE_LOG_EVENT_RATING_VIDEO = "rating_video";
    public static final String FIREBASE_LOG_EVENT_UPDATE_USER_TASK = "update_user_task";


    //Network parameters
    public static final String PARAM_MIN_VIDEO_DURATION = "min_duration";
    public static final String PARAM_MAX_VIDEO_DURATION = "max_duration";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_VIDEO_ID = "video_id";
    public static final String PARAM_LANG_CODE = "language_code";
    public static final String PARAM_LANG = "language";
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
    public static final String PARAM_TIME_WATCHED = "time_watched";
    public static final String PARAM_COMPLETED = "completed";
    public static final String PARAM_RATING = "rating";
    public static final String PARAM_SUB_POSITION = "subtitle_position";
    public static final String PARAM_REASON_CODE = "reason_code";
    public static final String PARAM_QUERY = "query";
    public static final String PARAM_CATEGORY = "category";


    public static final String YOUTUBE_VIDEO_ID = "videoId";
    public static final String YOUTUBE_CONTROLS = "control";
    public static final String YOUTUBE_AUTOPLAY = "autoplay";
    public static final String YOUTUBE_SHOW_RELATED_VIDEOS = "showRelatedVideos";
    public static final String YOUTUBE_SHOW_VIDEO_INFO = "showVideoInfo";
    public static final String YOUTUBE_VIDEO_ANNOTATION = "videoAnnotation";
    public static final String YOUTUBE_DEBUG = "debug";
    public static final String YOUTUBE_CLOSED_CAPTIONS = "closedCaptions";
    public static final String STATE_PLAY = "PLAYING";
    public static final String STATE_BUFFERING = "BUFFERING";
    public static final String STATE_UNSTARTED = "UNSTARTED";
    public static final String STATE_VIDEO_CUED = "VIDEO_CUED";
    public static final String STATE_ENDED = "ENDED";
    public static final String STATE_PAUSED = "PAUSED";


    public static final int VIDEO_COMPLETED_WATCHING_TRUE = 1;
    public static final int VIDEO_COMPLETED_WATCHING_FALSE = 0;

    public static final String SUBTITLE_LAST_VERSION = "last";

    public static final String STATUS_ERROR = "Status ERROR";

}
