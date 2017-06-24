package com.dream.dreamtv.conn;

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.utils.SharedPreferenceUtils;

import java.util.HashMap;
import java.util.Map;

public class ConnectionManager {
    public static final String URL_BASE = "https://amara.org/api/";     // Produccion

    public static int MAX_RETRIES = 3;
    public static int TIMEOUT_MS = 20000; //20 segundos


    public enum Urls {
        VIDEOS("videos"),
        TED_PROJECTS("teams/ted/projects");
        public String value;

        Urls(String value) {
            this.value = value;
        }
    }

    public enum Actions {
        CREATE("create"),
        UPDATE("update"),
        DELETE("delete");

        public String value;

        Actions(String value) {
            this.value = value;
        }
    }


    private ConnectionManager() {
    }

    public static MySingletonVolley get(
            final Context context,
            @NonNull Urls url, final Map<String, String> urlParams,
            ResponseListener listener, Object tag) {

        String urlParamsString = "";
        if (urlParams.entrySet().size() >= 1) {
            int i = 0;
            for (Map.Entry<String, String> entry : urlParams.entrySet()) {

                String key = entry.getKey();
                String value = entry.getValue();

                if (i == 0)
                    urlParamsString = "?" + key + "=" + value;
                else
                    urlParamsString += "&" + key + "=" + value;

                i++;

            }
        }

        String uri = URL_BASE + url.value + "/" + urlParamsString;

        DreamTVApp.Logger.d("Url: " + uri);

        MySingletonVolley volley = MySingletonVolley.getInstance(context);

        StringRequest request = new StringRequest(Method.GET, uri, listener, listener) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
//                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.DreamTVApp_token));
//                DreamTVApp.Logger.d("TOKEN: " + string);
//                map.put("Authorization", string);
                return map;
            }

//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                return urlParams;
//            }
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

    public static MySingletonVolley post(
            final Context context,
            @NonNull Urls url, Actions action,
            final String json, ResponseListener listener, Object tag) {

        String uri = URL_BASE + url.value;

        if (action != null)
            uri += "/" + action.value;

        DreamTVApp.Logger.d("Url: " + uri);

        MySingletonVolley volley = MySingletonVolley.getInstance(context);

        StringRequest request = new StringRequest(Method.POST, uri, listener, listener) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return json.getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
                DreamTVApp.Logger.d("TOKEN: " + string);
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
     * @param context     Context
     * @param url         Urls
     * @param params      Parámetros (para diferenciar las urls)
     * @param action      Actions
     * @param json        String con el json
     * @param listener    ResponseListener
     * @param tag         Tag
     * @param shouldCache Si se debe cachear o no
     * @return MySingletonVolley
     */
    public static MySingletonVolley post(
            final Context context,
            @NonNull Urls url, String params, Actions action,
            final String json, ResponseListener listener, Object tag, boolean shouldCache) {

        String uri = URL_BASE + url.value;

        if (action != null)
            uri += "/" + action.value;

        if (params != null)
            uri += "?" + params;

        DreamTVApp.Logger.d("Url: " + uri);

        MySingletonVolley volley = MySingletonVolley.getInstance(context);

        StringRequest request = new StringRequest(Method.POST, uri, listener, listener) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return json.getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
//                map.put("Content-Type", "application/json; charset=utf-8");
                String string = "bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
                DreamTVApp.Logger.d("TOKEN: " + string);
                map.put("Authorization", string);
                return map;
            }

        };

        request.setShouldCache(shouldCache);
        if (shouldCache)
            DreamTVApp.Logger.d("Cache key: " + request.getCacheKey());

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
