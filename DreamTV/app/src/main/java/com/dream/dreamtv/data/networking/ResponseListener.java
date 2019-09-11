package com.dream.dreamtv.data.networking;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.dream.dreamtv.data.model.api.AuthResponse;
import com.dream.dreamtv.data.model.api.JsonResponseBaseBean;
import com.dream.dreamtv.utils.JsonUtils;
import com.google.gson.reflect.TypeToken;

import static com.dream.dreamtv.utils.JsonUtils.getJsonResponse;


/**
 * Created by gbogarin on 29/11/2015.
 */
public abstract class ResponseListener implements Listener<String>, ErrorListener {
    private static final String TAG = ResponseListener.class.getSimpleName();

    private final Context context;


    ResponseListener(Context context) {
        this.context = context.getApplicationContext();
    }


    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, "Response: onErrorResponse() " + error.getMessage());

        String errorMessage = VolleyErrorHelper.getMessage(error, context);

        if (errorMessage != null && !errorMessage.isEmpty()) {
            Log.d(TAG, errorMessage);
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
        }

        processError(error);
    }

    @Override
    public void onResponse(String response) {
        Log.d(TAG, "Response: onResponse() " + response);

        TypeToken type = new TypeToken<JsonResponseBaseBean>() {
        };
        JsonResponseBaseBean jsonResponse = getJsonResponse(response, type);

        if (jsonResponse != null && jsonResponse.success != null) {
            if (jsonResponse.success) {
                processResponse(response);
            } else {
                processError();
            }
        } else {
            processError();
        }
//        else {
//            processResponse(response);
//        }
    }

    protected abstract void processResponse(String response);

    public void processError(VolleyError error) {
    }

    public void processError() {
    }

}
