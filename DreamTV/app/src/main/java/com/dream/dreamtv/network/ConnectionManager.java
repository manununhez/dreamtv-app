package com.dream.dreamtv.network;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.dream.dreamtv.R;
import com.dream.dreamtv.utils.SharedPreferenceUtils;

import java.util.HashMap;
import java.util.Map;

public class ConnectionManager {
    private static final String TAG = ConnectionManager.class.getSimpleName();

    private static final String URL_BASE = "http://www.dreamproject.pjwstk.edu.pl/api/";     // Facu Produccion

    public static int MAX_RETRIES = 3;
    private static final int TIMEOUT_MS = 60000; //60 segundos


    public enum Urls {
        USERS("users"),

        REASONS("reasons"),

        USER_VIDEOS("users/videos"),
        USER_VIDEO_DETAILS("users/video"),

        USER_TASKS("users/task"),

        TASKS("tasks"),

        LANGUAGES("languages"),

        SUBTITLE("subtitle"),

        VIDEO_TESTS("videotests");


        final String value;

        Urls(String value) {
            this.value = value;
        }
    }

    public enum Actions {
        CREATE("create"),
        UPDATE("update"),
        DELETE("delete");

        final String value;

        Actions(String value) {
            this.value = value;
        }
    }


    private ConnectionManager() {
    }

    /**
     *
     * @param context
     * @param url
     * @param urlParams
     * @param listener
     * @param tag
     * @return
     */
    public static MySingletonVolley get(
            final Context context,
            @NonNull Urls url, final Map<String, String> urlParams,
            ResponseListener listener, Object tag) {

        StringBuilder urlParamsString = new StringBuilder();
        if (urlParams != null && urlParams.entrySet().size() >= 1) {
            int i = 0;
            for (Map.Entry<String, String> entry : urlParams.entrySet()) {

                String key = entry.getKey();
                String value = entry.getValue();

                if (i == 0)
                    urlParamsString = new StringBuilder("?" + key + "=" + value);
                else
                    urlParamsString.append("&").append(key).append("=").append(value);

                i++;

            }
        }

//        String URL_BASE = ((DreamTVApp) ((Activity)context).getApplication()).getBaseURL();
        String uri = URL_BASE + url.value + "/" + urlParamsString;

        Log.d(TAG,"Url: " + uri);

        MySingletonVolley volley = MySingletonVolley.getInstance(context);

        StringRequest request = new StringRequest(Method.GET, uri, listener, listener) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }


            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> map = new HashMap<>();
//                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
                String string = SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
                Log.d(TAG,"TOKEN: " + string);
                map.put("Authorization", string);
                return map;
            }


        };

        request.setShouldCache(false);

        request.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        if (tag != null)
            volley.addToRequestQueue(request, tag);
        else
            volley.addToRequestQueue(request, context);

        return volley;
    }


    /**
     *
     * @param context
     * @param url
     * @param action
     * @param json
     * @param listener
     * @param tag
     * @return
     */
    public static MySingletonVolley post(
            final Context context,
            @NonNull Urls url, Actions action,
            final String json, ResponseListener listener, Object tag) {

//        String URL_BASE = ((DreamTVApp) ((Activity)context).getApplication()).getBaseURL();
        String uri = URL_BASE + url.value;

        if (action != null)
            uri += "/" + action.value;

        Log.d(TAG,"Url: " + uri);

        MySingletonVolley volley = MySingletonVolley.getInstance(context);

        StringRequest request = new StringRequest(Method.POST, uri, listener, listener) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return json.getBytes();
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> map = new HashMap<>();
//                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
                String string = SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
                Log.d(TAG,"TOKEN: " + string);
                map.put("Authorization", string);
                return map;
            }
        };

        request.setShouldCache(false);

        request.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        if (tag != null)
            volley.addToRequestQueue(request, tag);
        else
            volley.addToRequestQueue(request, context);

        return volley;
    }


    /**
     *
     * @param context
     * @param url
     * @param action
     * @param json
     * @param listener
     * @param tag
     * @return
     */
    public static MySingletonVolley put(
            final Context context,
            @NonNull Urls url, Actions action,
            final String json, ResponseListener listener, Object tag) {

//        String URL_BASE = ((DreamTVApp) ((Activity)context).getApplication()).getBaseURL();
        String uri = URL_BASE + url.value;

        if (action != null)
            uri += "/" + action.value;

        Log.d(TAG,"Url: " + uri);

        MySingletonVolley volley = MySingletonVolley.getInstance(context);

        StringRequest request = new StringRequest(Method.PUT, uri, listener, listener) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return json.getBytes();
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> map = new HashMap<>();
//                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
                String string = SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
                Log.d(TAG,"TOKEN: " + string);
                map.put("Authorization", string);
                return map;
            }
        };

        request.setShouldCache(false);

        request.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        if (tag != null)
            volley.addToRequestQueue(request, tag);
        else
            volley.addToRequestQueue(request, context);

        return volley;
    }

    /**
     *
     * @param context
     * @param url
     * @param urlParams
     * @param listener
     * @param tag
     * @return
     */
    public static MySingletonVolley delete(
            final Context context,
            @NonNull Urls url, final Map<String, String> urlParams,
            ResponseListener listener, Object tag) {

        StringBuilder urlParamsString = new StringBuilder();
        if (urlParams != null && urlParams.entrySet().size() >= 1) {
            int i = 0;
            for (Map.Entry<String, String> entry : urlParams.entrySet()) {

                String key = entry.getKey();
                String value = entry.getValue();

                if (i == 0)
                    urlParamsString = new StringBuilder("?" + key + "=" + value);
                else
                    urlParamsString.append("&").append(key).append("=").append(value);

                i++;

            }
        }

//        String URL_BASE = ((DreamTVApp) ((Activity)context).getApplication()).getBaseURL();
        String uri = URL_BASE + url.value + "/" + urlParamsString;

        Log.d(TAG,"Url: " + uri);

        MySingletonVolley volley = MySingletonVolley.getInstance(context);

        StringRequest request = new StringRequest(Method.DELETE, uri, listener, listener) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }


            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> map = new HashMap<>();
//                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
                String string = SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
                Log.d(TAG,"TOKEN: " + string);
                map.put("Authorization", string);
                return map;
            }


        };

        request.setShouldCache(false);

        request.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        if (tag != null)
            volley.addToRequestQueue(request, tag);
        else
            volley.addToRequestQueue(request, context);

        return volley;
    }
}
