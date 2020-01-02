package com.dreamproject.dreamtv.data.networking;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.dreamproject.dreamtv.data.networking.model.JsonResponseBaseBean;
import com.google.gson.reflect.TypeToken;

import timber.log.Timber;

import static com.dreamproject.dreamtv.utils.JsonUtils.getJsonResponse;


/**
 * Created by gbogarin on 29/11/2015.
 */
public abstract class ResponseListener implements Listener<String>, ErrorListener {
    private final Context context;


    ResponseListener(Context context) {
        this.context = context.getApplicationContext();
    }


    @Override
    public void onErrorResponse(VolleyError error) {
        Timber.d("Response: onErrorResponse() %s", error.getMessage());

        String errorMessage = VolleyErrorHelper.getMessage(error, context);

        if (errorMessage != null && !errorMessage.isEmpty()) {
            Timber.d(errorMessage);
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
        }

        processError(error);
    }

    @Override
    public void onResponse(String response) {
        Timber.d("Response: onResponse() %s", response);

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
    }

    protected abstract void processResponse(String response);

    public void processError(VolleyError error) {
    }

    public void processError() {
    }

}
