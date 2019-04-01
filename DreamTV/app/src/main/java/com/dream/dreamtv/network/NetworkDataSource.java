package com.dream.dreamtv.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dream.dreamtv.R;
import com.dream.dreamtv.db.entity.UserEntity;
import com.dream.dreamtv.model.JsonResponseBaseBean;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.ui.Main.MainFragment;
import com.dream.dreamtv.utils.SharedPreferenceUtils;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static com.dream.dreamtv.utils.JsonUtils.getJsonResponse;

public class NetworkDataSource {
    private static final String TAG = NetworkDataSource.class.getSimpleName();
    private static final String URL_BASE = "http://www.dreamproject.pjwstk.edu.pl/api/";     // Facu Produccion
    private static final int TIMEOUT_MS = 60000; //60 segundos
    public static int MAX_RETRIES = 3;
    // For Singleton instantiation
    private static NetworkDataSource INSTANCE;
    private final Context context;
    // Volley requestQueue
    private RequestQueue mRequestQueue;
    private MutableLiveData<Resource<UserEntity>> responseFromLogin;

    private NetworkDataSource(Context context) {
        this.context = context.getApplicationContext();
        mRequestQueue = getRequestQueue();

        responseFromLogin = new MutableLiveData<>();


    }

    public static synchronized NetworkDataSource getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (NetworkDataSource.class) {
                INSTANCE = new NetworkDataSource(context);
                Log.d(TAG, "Made new NetworkDataSource");

            }
        }
        return INSTANCE;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(context);
        }
        return mRequestQueue;
    }

    private <T> void addToRequestQueue(Request<T> req, Object tag) {
        // set the default tag if tag is empty
        req.setTag(tag == null ? TAG : tag);
        getRequestQueue().add(req);
        if (req instanceof RequestQueue.RequestFinishedListener)
            getRequestQueue().addRequestFinishedListener((RequestQueue.RequestFinishedListener<Object>) req);
    }

//    /**
//     *
//     * @param context
//     * @param url
//     * @param urlParams
//     * @param listener
//     * @param tag
//     * @return
//     */
//    public static MySingletonVolley get(
//            final Context context,
//            @NonNull Urls url, final Map<String, String> urlParams,
//            ResponseListener listener, Object tag) {
//
//        StringBuilder urlParamsString = new StringBuilder();
//        if (urlParams != null && urlParams.entrySet().size() >= 1) {
//            int i = 0;
//            for (Map.Entry<String, String> entry : urlParams.entrySet()) {
//
//                String key = entry.getKey();
//                String value = entry.getValue();
//
//                if (i == 0)
//                    urlParamsString = new StringBuilder("?" + key + "=" + value);
//                else
//                    urlParamsString.append("&").append(key).append("=").append(value);
//
//                i++;
//
//            }
//        }
//
////        String URL_BASE = ((DreamTVApp) ((Activity)context).getApplication()).getBaseURL();
//        String uri = URL_BASE + url.value + "/" + urlParamsString;
//
//        Log.d(TAG,"Url: " + uri);
//
//        MySingletonVolley volley = MySingletonVolley.getInstance(context);
//
//        StringRequest request = new StringRequest(Method.GET, uri, listener, listener) {
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }
//
//
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> map = new HashMap<>();
////                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                String string = SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                Log.d(TAG,"TOKEN: " + string);
//                map.put("Authorization", string);
//                return map;
//            }
//
//
//        };
//
//        request.setShouldCache(false);
//
//        request.setRetryPolicy(new DefaultRetryPolicy(
//                TIMEOUT_MS,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//        ));
//
//        if (tag != null)
//            volley.addToRequestQueue(request, tag);
//        else
//            volley.addToRequestQueue(request, context);
//
//        return volley;
//    }

//
//    /**
//     *
//     * @param context
//     * @param url
//     * @param action
//     * @param json
//     * @param listener
//     * @param tag
//     * @return
//     */
//    public static MySingletonVolley post(
//            final Context context,
//            @NonNull Urls url, Actions action,
//            final String json, ResponseListener listener, Object tag) {
//
//        String uri = URL_BASE + url.value;
//
//        if (action != null)
//            uri += "/" + action.value;
//
//        Log.d(TAG,"Url: " + uri);
//
//        MySingletonVolley volley = MySingletonVolley.getInstance(context);
//
//        StringRequest request = new StringRequest(Method.POST, uri, listener, listener) {
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }
//
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                return super.getParams();
//            }
//
//
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> map = new HashMap<>();
////                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                String string = SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                Log.d(TAG,"TOKEN: " + string);
//                map.put("Authorization", string);
//                return map;
//            }
//        };
//
//        request.setShouldCache(false);
//
//        request.setRetryPolicy(new DefaultRetryPolicy(
//                TIMEOUT_MS,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//        ));
//
//        if (tag != null)
//            volley.addToRequestQueue(request, tag);
//        else
//            volley.addToRequestQueue(request, context);
//
//        return volley;
//    }

//
//    /**
//     *
//     * @param context
//     * @param url
//     * @param action
//     * @param json
//     * @param listener
//     * @param tag
//     * @return
//     */
//    public static MySingletonVolley put(
//            final Context context,
//            @NonNull Urls url, Actions action,
//            final String json, ResponseListener listener, Object tag) {
//
////        String URL_BASE = ((DreamTVApp) ((Activity)context).getApplication()).getBaseURL();
//        String uri = URL_BASE + url.value;
//
//        if (action != null)
//            uri += "/" + action.value;
//
//        Log.d(TAG,"Url: " + uri);
//
//        MySingletonVolley volley = MySingletonVolley.getInstance(context);
//
//        StringRequest request = new StringRequest(Method.PUT, uri, listener, listener) {
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }
//
//            @Override
//            public byte[] getBody() {
//                return json.getBytes();
//            }
//
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> map = new HashMap<>();
////                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                String string = SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                Log.d(TAG,"TOKEN: " + string);
//                map.put("Authorization", string);
//                return map;
//            }
//        };
//
//        request.setShouldCache(false);
//
//        request.setRetryPolicy(new DefaultRetryPolicy(
//                TIMEOUT_MS,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//        ));
//
//        if (tag != null)
//            volley.addToRequestQueue(request, tag);
//        else
//            volley.addToRequestQueue(request, context);
//
//        return volley;
//    }
//
//    /**
//     *
//     * @param context
//     * @param url
//     * @param urlParams
//     * @param listener
//     * @param tag
//     * @return
//     */
//    public static MySingletonVolley delete(
//            final Context context,
//            @NonNull Urls url, final Map<String, String> urlParams,
//            ResponseListener listener, Object tag) {
//
//        StringBuilder urlParamsString = new StringBuilder();
//        if (urlParams != null && urlParams.entrySet().size() >= 1) {
//            int i = 0;
//            for (Map.Entry<String, String> entry : urlParams.entrySet()) {
//
//                String key = entry.getKey();
//                String value = entry.getValue();
//
//                if (i == 0)
//                    urlParamsString = new StringBuilder("?" + key + "=" + value);
//                else
//                    urlParamsString.append("&").append(key).append("=").append(value);
//
//                i++;
//
//            }
//        }
//
////        String URL_BASE = ((DreamTVApp) ((Activity)context).getApplication()).getBaseURL();
//        String uri = URL_BASE + url.value + "/" + urlParamsString;
//
//        Log.d(TAG,"Url: " + uri);
//
//        MySingletonVolley volley = MySingletonVolley.getInstance(context);
//
//        StringRequest request = new StringRequest(Method.DELETE, uri, listener, listener) {
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }
//
//
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> map = new HashMap<>();
////                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                String string = SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                Log.d(TAG,"TOKEN: " + string);
//                map.put("Authorization", string);
//                return map;
//            }
//
//
//        };
//
//        request.setShouldCache(false);
//
//        request.setRetryPolicy(new DefaultRetryPolicy(
//                TIMEOUT_MS,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//        ));
//
//        if (tag != null)
//            volley.addToRequestQueue(request, tag);
//        else
//            volley.addToRequestQueue(request, context);
//
//        return volley;
//    }


    //**************************************
     /*        Volley Configuration
     //************************************/

    private <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    private void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            Log.d(TAG, "Cancel pending request for: " + tag.toString());
            mRequestQueue.cancelAll(tag);
        }
    }


    private void requestString(int method, final String webserviceUrl, final Map<String, String> params,
                               Response.Listener<String> listener, Response.ErrorListener errorListener) {

        StringRequest stringRequest = new StringRequest(method, webserviceUrl, listener, errorListener) {
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
                Log.d(TAG, "TOKEN: " + string);
                map.put("Authorization", string);
                return map;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };

        stringRequest.setShouldCache(false);

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Adding String request to request queue
        INSTANCE.addToRequestQueue(stringRequest, webserviceUrl);
    }

    /**
     * Get the current weather of city by name. Used in {@link MainFragment}
     * to implement the search.
     *
     * @param {@link String} location with the name of the city.
     */
    @SuppressWarnings("unchecked")
    public void login(String email, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
        Uri loginUri = Uri.parse(URL_BASE.concat(Urls.LOGIN.value)).buildUpon().build();

        requestString(Method.POST, loginUri.toString(), params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, "login Response JSON: " + response);

                //*******POSTING VALUE TO LIVEDATA
                Resource<UserEntity> resourceResponse = null;
                if (jsonResponse.success) {
                    User user = jsonResponse.data;
                    resourceResponse = Resource.success(user.getEntity());
                } else
                    resourceResponse = Resource.error(jsonResponse.message, null);

                responseFromLogin.postValue(resourceResponse); //post the value

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO do something error
                responseFromLogin.postValue(Resource.<UserEntity>error(error.getMessage(), null));

                Log.d(TAG, "requestCurrentWeathersByCityIDs Error response: " + error.getMessage());

            }
        });

    }

//    private void getRequestString(final String webserviceUrl,
//                                  Response.Listener<String> listener, Response.ErrorListener errorListener) {
//
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, webserviceUrl, listener, errorListener) {
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }
//
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<>();
//                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                Log.d(TAG,"TOKEN: " + string);
//                map.put("Authorization", string);
//                return map;
//            }
//
//            @Override
//            public byte[] getBody() throws AuthFailureError {
//                return super.getBody();
//            }
//        };
//
//        stringRequest.setShouldCache(false);
//
//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
//                TIMEOUT_MS,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//        ));
//
//        // Adding String request to request queue
//        INSTANCE.addToRequestQueue(stringRequest, webserviceUrl);
//    }

    /**
     * Get the current weather of a list of cities ID.
     *
     * @return {@link LiveData} representing the response of the request requestCurrentWeathersByCityIDs()
     */
    public LiveData<Resource<UserEntity>> responseFromLogin() {
        return responseFromLogin;
    }


    //********************
    //  Network requests
    //********************

    public enum Urls {
        LOGIN("login"),

        REGISTER("register"),

        USER("user"),

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


//    public enum Actions {
//        CREATE("create"),
//        UPDATE("update"),
//        DELETE("delete");
//
//        final String value;
//
//        Actions(String value) {
//            this.value = value;
//        }
//    }


}
