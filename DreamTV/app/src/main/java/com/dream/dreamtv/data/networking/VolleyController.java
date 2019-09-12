package com.dream.dreamtv.data.networking;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RequestQueue.RequestFinishedListener;
import com.android.volley.toolbox.StringRequest;
import com.dream.dreamtv.data.local.prefs.AppPreferencesHelper;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.android.volley.toolbox.Volley.newRequestQueue;

public class VolleyController {

    private static final String TAG = VolleyController.class.getSimpleName();
    private static final int TIMEOUT_MS = 60000; //60 segundos

    private final Context mContext;
    private AppPreferencesHelper mPreferencesHelper;
    private RequestQueue mRequestQueue;

    public VolleyController(Context context, AppPreferencesHelper appPreferencesHelper) {
        mContext = context;
        mPreferencesHelper = appPreferencesHelper;
    }


    //**************************************
     /*        VolleyController Configuration
     //************************************/

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = newRequestQueue(mContext);
        }
        return mRequestQueue;
    }

    @SuppressWarnings("unchecked")
    private <T> void addToRequestQueue(Request<T> req, Object tag) {
        // set the default tag if tag is empty
        req.setTag(tag == null ? TAG : tag);
        getRequestQueue().add(req);
        if (req instanceof RequestFinishedListener)
            getRequestQueue().addRequestFinishedListener((RequestFinishedListener<Object>) req);
    }


    private void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            Timber.d("Cancel pending request for: %s", tag.toString());
            mRequestQueue.cancelAll(tag);
        }
    }


    public void requestString(int method, final String webserviceUrl, final Map<String, String> params,
                              ResponseListener responseListener) {

        StringRequest stringRequest = new StringRequest(method, webserviceUrl, responseListener, responseListener) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> map = new HashMap<>();
                String string = "Bearer " + mPreferencesHelper.getAccessToken();
                map.put("Authorization", string);
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
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
        addToRequestQueue(stringRequest, webserviceUrl);
    }
}
