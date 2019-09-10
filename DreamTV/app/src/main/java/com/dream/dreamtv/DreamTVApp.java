package com.dream.dreamtv;

import android.app.Application;
import android.content.Context;

import com.dream.dreamtv.utils.LocaleHelper;

import static com.dream.dreamtv.utils.Constants.LANGUAGE_POLISH;


/**
 * Created by manunez on 13/10/2015.
 */
public class DreamTVApp extends Application {


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, LANGUAGE_POLISH));
    }


    @Override
    public void onCreate() {
        super.onCreate();

    }

}
