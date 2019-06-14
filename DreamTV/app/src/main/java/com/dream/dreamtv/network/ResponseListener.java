package com.dream.dreamtv.network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.dream.dreamtv.model.JsonResponseBaseBean;
import com.dream.dreamtv.utils.JsonUtils;
import com.google.gson.reflect.TypeToken;


/**
 * Created by gbogarin on 29/11/2015.
 */
public abstract class ResponseListener implements Listener<String>, ErrorListener, RequestQueue.RequestFinishedListener {
    private static final String TAG = ResponseListener.class.getSimpleName();

    private final Context context;


    ResponseListener(Context context) {
        this.context = context.getApplicationContext();
    }


    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, "Response: onErrorResponse() " + error.getMessage());

        String errorMessage = VolleyErrorHelper.getMessage(error, context);
        Log.d(TAG, errorMessage);
        if (errorMessage != null && !errorMessage.isEmpty()) {
            Toast.makeText(context.getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
        }

        processError(error);
    }

    @Override
    public void onResponse(String response) {
        Log.d(TAG, "Response: onResponse() " + response);
//
        TypeToken typeToken = new TypeToken<JsonResponseBaseBean>() {
        };
        final JsonResponseBaseBean jsonResponse =
                JsonUtils.getJsonResponse(response, typeToken, false);

        if (jsonResponse != null && jsonResponse.success != null) {
            if (jsonResponse.success) {
                processResponse(response);
            } else {
                processError(jsonResponse);
            }
        } else {
            processResponse(response);
        }
    }

    protected abstract void processResponse(String response);

    public void processError(VolleyError error) {
    }

    public void processError(JsonResponseBaseBean jsonResponse) {
    }

    @Override
    public void onRequestFinished(Request request) {
    }
}
