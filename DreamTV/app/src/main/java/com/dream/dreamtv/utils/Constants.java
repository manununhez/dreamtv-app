package com.dream.dreamtv.utils;

/**
 * Created by manuel on 8/22/17.
 */

public class Constants {
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_POLISH = "pl";

    public final static String STATE_PLAY = "PLAYING";
    public final static String STATE_BUFFERING = "BUFFERING";
    public final static String STATE_ENDED = "ENDED";
    public final static String STATE_PAUSED = "PAUSED";

    public static final String NONE_OPTIONS_CODE = "NN";
    public static final String BEGINNER_INTERFACE_MODE = "beginner";
    public static final String ADVANCED_INTERFACE_MODE = "advanced";

    public static final String SHARED_ELEMENT_NAME = "hero";
    public static final String VIDEO = "Video";

    public static final String TASKS_ALL = "all";
    public static final String TASKS_FINISHED = "finished";
    public static final String TASKS_TEST = "test";
    public static final String TASKS_USER = "user";
    public static final String TASKS_OTHER_USERS = "others";

    public static final int BACKGROUND_UPDATE_DELAY = 300;
    public static final int GRID_ITEM_WIDTH = 200;
    public static final int GRID_ITEM_HEIGHT = 200;

    public static final int MY_LIST_CATEGORY = 1250;
    public static final int CONTINUE_WATCHING_CATEGORY = 1251;
    public static final int CHECK_NEW_TASKS_CATEGORY = 1252;

    public static final int DETAIL_THUMB_WIDTH = 274;
    public static final int DETAIL_THUMB_HEIGHT = 274;

    public static final int CARD_WIDTH = 200;
    public static final int CARD_HEIGHT = 240;

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
