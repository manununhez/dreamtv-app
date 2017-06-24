package com.dream.dreamtv.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;

/**
 * Created by gbogarin on 30/11/2015.
 *
 */
public class LoadingDialog extends Dialog {

    public LoadingDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProgressBar progressBar = new ProgressBar(getContext());
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(progressBar);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
    }

}
