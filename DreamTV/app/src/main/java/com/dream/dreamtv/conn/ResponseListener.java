package com.dream.dreamtv.conn;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.beans.JsonResponseBaseBean;
import com.dream.dreamtv.utils.LoadingDialog;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;



/**
 * Created by gbogarin on 29/11/2015.
 */
public abstract class ResponseListener implements Listener<String>, ErrorListener, RequestQueue.RequestFinishedListener {
    private Context context;
    protected LoadingDialog loadingDialog;
    protected View customLoading;
    protected boolean showErrorMessage = false;

    public ResponseListener(Context context) {
        this.context = context.getApplicationContext();
    }

    public ResponseListener(Context context, View customLoading) {
        this(context);
        this.customLoading = customLoading;
        if (customLoading != null)
            customLoading.setVisibility(View.VISIBLE);
    }

    public ResponseListener(Context context, boolean displayLoading) {
        this(context);
        if (displayLoading) {
            loadingDialog = new LoadingDialog(context);
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.show();
        }
    }

    public ResponseListener(Context context, boolean displayLoading, boolean showErrorMessage) {
        this(context, displayLoading);
        this.showErrorMessage = showErrorMessage;

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        DreamTVApp.Logger.d("Respuesta: " + error.getMessage());
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }

        if (customLoading != null)
            customLoading.setVisibility(View.GONE);

        String errorMessage = VolleyErrorHelper.getMessage(error, context);
        DreamTVApp.Logger.e(errorMessage);
        if (errorMessage != null && !errorMessage.equals("")) {
            Toast.makeText(context.getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
        }

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

        DreamTVApp.Logger.d("Respuesta: " + response);
//
//        TypeToken typeToken = new TypeToken<JsonResponseBaseBean>() {
//        };
//        final JsonResponseBaseBean jsonResponse =
//                JsonUtils.getJsonResponse(response, typeToken, false);
//
//        if (jsonResponse.success) {
            processResponse(response);
//        } else {
//            if (showErrorMessage) {
//                if (jsonResponse.data == null) {
//                    EventBus.getDefault().post(new ResponseListenerEvent("Error en la conexión"));
//                } else if (!jsonResponse.data.equals("")) {
//                    EventBus.getDefault().post(new ResponseListenerEvent(jsonResponse.data.toString()));
//                }
//            }
//            processError(jsonResponse);
//        }
    }

    public abstract void processResponse(String response);

    public void processError(VolleyError error) {
    }

    public void processError(JsonResponseBaseBean jsonResponse) {
    }

    @Override
    public void onRequestFinished(Request request) {
    }
}
