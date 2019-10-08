/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.manuelnunhez.dreamtv.utils;

import android.content.Context;

import com.manuelnunhez.dreamtv.R;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * A collection of utility methods, all static.
 */
public class TimeUtils {

    /*
     * Making sure public utility methods remain static
     */
    private TimeUtils() {
        //non-instantiable
    }


    public static String getTimeFormat(Context context, long millis) {
        return String.format(context.getString(R.string.time_format), MILLISECONDS.toMinutes(millis) % HOURS.toMinutes(1),
                MILLISECONDS.toSeconds(millis) % MINUTES.toSeconds(1));
    }


    public static long getUnixTimeNowInSecs() {
        return new Date(System.currentTimeMillis() / 1000L).getTime();

    }

    public static String getTimeFormatMinSecs(long millis) {
        return String.format(Locale.getDefault(), "%d min, %d s",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public static String getTimeFormatMinSecsDoublePoints(long millis) {
        return String.format(Locale.getDefault(), "%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

}
