package com.dream.dreamtv.network;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.JsonResponseBaseBean;
import com.dream.dreamtv.utils.JsonUtils;
import com.dream.dreamtv.utils.LoadingDialog;
import com.google.gson.reflect.TypeToken;


/**
 * Created by gbogarin on 29/11/2015.
 */
public abstract class ResponseListener implements Listener<String>, ErrorListener, RequestQueue.RequestFinishedListener {
    private static final String TAG = ResponseListener.class.getSimpleName();

    private final Context context;
    private LoadingDialog loadingDialog;
    private View customLoading;
    private boolean showErrorMessage = false;

    private ResponseListener(Context context) {
        this.context = context.getApplicationContext();
    }

    public ResponseListener(Context context, View customLoading) {
        this(context);
        this.customLoading = customLoading;
        if (customLoading != null)
            customLoading.setVisibility(View.VISIBLE);
    }

    private ResponseListener(Context context, boolean displayLoading, String loadingMessage) {
        this(context);
        if (displayLoading) {
            loadingDialog = new LoadingDialog(context, loadingMessage);
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.show();
        }
    }

    public ResponseListener(Context context, boolean displayLoading, boolean showErrorMessage, String loadingMessage) {
        this(context, displayLoading, loadingMessage);
        this.showErrorMessage = showErrorMessage;

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG,"Response: " + error.getMessage());
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }

        if (customLoading != null)
            customLoading.setVisibility(View.GONE);

        String errorMessage = VolleyErrorHelper.getMessage(error, context);
        Log.d(TAG,errorMessage);

        if (errorMessage != null && !errorMessage.isEmpty()) {
            Toast.makeText(context.getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
        } else
            error = new VolleyError("Unexpected error");

        processError(error);
    }

    @Override
    public void onResponse(String response) {
        if (loadingDialog != null) {
            if (loadingDialog.isShowing())
                loadingDialog.dismiss();
        }

        if (customLoading != null)
            customLoading.setVisibility(View.GONE);

        Log.d(TAG,"onResponse: " + response);
//
        TypeToken typeToken = new TypeToken<JsonResponseBaseBean>() {
        };
        final JsonResponseBaseBean jsonResponse =
                JsonUtils.getJsonResponse(response, typeToken, false);

        if (jsonResponse!= null && jsonResponse.success != null) {
            if (jsonResponse.success) {
                processResponse(response);
            } else {
                if (showErrorMessage) {
                    if (jsonResponse.data == null || jsonResponse.data.equals("")) {
                        Toast.makeText(context, context.getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, jsonResponse.data.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
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
