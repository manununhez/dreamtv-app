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
import android.graphics.Point;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.dream.dreamtv.R;

import java.io.IOException;
import java.io.InputStream;

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

        dialog.setPositiveButton(buttonPosstiveText, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


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

    /**
     * Returns the screen/display size
     */
    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    /**
     * Shows a (long) toast
     */
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows a (long) toast.
     */
    public static void showToast(Context context, int resourceId) {
        Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_LONG).show();
    }

    public static int convertDpToPixel(Context ctx, int dp) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    /**
     * Formats time in milliseconds to hh:mm:ss string format.
     */
    public static String formatMillis(int millis) {
        String result = "";
        int hr = millis / 3600000;
        millis %= 3600000;
        int min = millis / 60000;
        millis %= 60000;
        int sec = millis / 1000;
        if (hr > 0) {
            result += hr + ":";
        }
        if (min >= 0) {
            if (min > 9) {
                result += min + ":";
            } else {
                result += "0" + min + ":";
            }
        }
        if (sec > 9) {
            result += sec;
        } else {
            result += "0" + sec;
        }
        return result;
    }

    /**
     * Will read the content from a given {@link InputStream} and return it as a {@link String}.
     *
     * @param inputStream The {@link InputStream} which should be read.
     * @return Returns <code>null</code> if the the {@link InputStream} could not be read. Else
     * returns the content of the {@link InputStream} as {@link String}.
     */
    public static String inputStreamToString(InputStream inputStream) {
        try {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes, 0, bytes.length);
            String json = new String(bytes);
            return json;
        } catch (IOException e) {
            return null;
        }
    }
}
