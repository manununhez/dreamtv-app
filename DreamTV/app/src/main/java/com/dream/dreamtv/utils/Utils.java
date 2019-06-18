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

package com.dream.dreamtv.utils;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;

import com.dream.dreamtv.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * A collection of utility methods, all static.
 */
public class Utils {

    /*
     * Making sure public utility methods remain static
     */
    private Utils() {
    }

    public static AlertDialog.Builder getAlertDialog(Context context, String title, String message, String buttonPosstiveText, DialogInterface.OnCancelListener listener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.Theme_AppCompat));
        dialog.setTitle(title);
        dialog.setMessage(message);

        dialog.setPositiveButton(buttonPosstiveText, (dialog1, which) -> dialog1.cancel());


        dialog.setOnCancelListener(listener);
        return dialog;
    }

    public static AlertDialog.Builder getAlertDialogWithChoice(Context context, String title, String message, String yesButton, DialogInterface.OnClickListener yesClickListener,
                                                               String noButton, DialogInterface.OnClickListener noClickListener, DialogInterface.OnCancelListener listener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.Theme_AppCompat));
        dialog.setTitle(title);
        dialog.setMessage(message);

        dialog.setPositiveButton(yesButton, yesClickListener);
        dialog.setNegativeButton(noButton, noClickListener);

        dialog.setOnCancelListener(listener);

        // Must call show() prior to fetching text view
//        AlertDialog dialogM = dialog.show();
//
//        TextView messageView = (TextView) dialogM.findViewById(android.R.id.message);
//        messageView.setGravity(Gravity.CENTER);
        return dialog;
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

    /**
     * Will read the content from a given {@link InputStream} and return it as a String.
     *
     * @param inputStream The {@link InputStream} which should be read.
     * @return Returns <code>null</code> if the the {@link InputStream} could not be read. Else
     * returns the content of the {@link InputStream} as String.
     */
    public static String inputStreamToString(InputStream inputStream) {
        try {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes, 0, bytes.length);
            return new String(bytes);
        } catch (IOException e) {
            return null;
        }
    }
}
