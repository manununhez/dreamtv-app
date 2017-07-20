package com.dream.dreamtv.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.app.DialogFragment;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.VolleyError;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.beans.JsonResponseBaseBean;
import com.dream.dreamtv.beans.Reason;
import com.dream.dreamtv.beans.ReasonList;
import com.dream.dreamtv.conn.ConnectionManager;
import com.dream.dreamtv.conn.ResponseListener;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReasonsDialogFragment extends DialogFragment {

    private LinearLayout llReasons;
    private List<Reason> reasonList;
    private List<Integer> selectedReasons = new ArrayList<>();
    private String subTitleText;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView mVoiceInputTv;


    public static ReasonsDialogFragment newInstance(String subTitleText) {
        ReasonsDialogFragment f = new ReasonsDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("subTitleText", subTitleText);
        f.setArguments(args);
        return f;
    }

    public ReasonsDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subTitleText = getArguments().getString("subTitleText");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View viewRoot = inflater.inflate(R.layout.fragment_reasons_dialog, container, false);
        TextView tvSubtitle = (TextView) viewRoot.findViewById(R.id.tvSubtitle);
        Button btnCancel = (Button) viewRoot.findViewById(R.id.btnCancel);
        Button btnSave = (Button) viewRoot.findViewById(R.id.btnSave);

        llReasons = (LinearLayout) viewRoot.findViewById(R.id.llReasons);
        mVoiceInputTv = (TextView) viewRoot.findViewById(R.id.voiceInput);


        getReasons();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        tvSubtitle.setText(Html.fromHtml(subTitleText));

        return viewRoot;
    }

    private void getReasons() {
        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Retrieving options...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                ReasonList reasons = gson.fromJson(response, ReasonList.class);
                reasonList = reasons.data;
                DreamTVApp.Logger.d(reasonList.toString());

                setupReasons();

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                DreamTVApp.Logger.d(error.getMessage());
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                DreamTVApp.Logger.d(jsonResponse.toString());
            }
        };

        ConnectionManager.get(getActivity(), ConnectionManager.Urls.REASONS, null, responseListener, this);

    }

    private void setupReasons() {
        llReasons.removeAllViews();
        for (int i = 0; i < reasonList.size(); i++) {
            Reason reason = reasonList.get(i);

            int dpsToogle = 40;
            final float scale = getActivity().getResources().getDisplayMetrics().density;
            int pixels = (int) (dpsToogle * scale + 0.5f);

            final ToggleButton toggleButton = new ToggleButton(getActivity());
            toggleButton.setBackgroundResource(R.color.black);
//            toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.custom_selector));
            toggleButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pixels);
            layoutParams.setMargins(0, 0, 0, 15);
            toggleButton.setId(reason.id);
            toggleButton.setTextOn(reason.name);
            toggleButton.setTextOff(reason.name);
            toggleButton.setText(reason.name);
            toggleButton.setLayoutParams(layoutParams);
            toggleButton.setFocusable(true);
            toggleButton.setFocusableInTouchMode(true);
            toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.selector_2_1));

            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((ToggleButton) view).isChecked()) { //si no esta chequeado y se hace check
                        //((RadioGroup) view.getParent()).check(view.getId());
                        if (!selectedReasons.contains(view.getId()))
                            selectedReasons.add(view.getId());
                    } else //estaba chequeado, pero se desactiva el mismo boton. Ahi se reinicia el selectedFeedbackReason
                        selectedReasons.remove(selectedReasons.indexOf(view.getId()));
                }
            });

            llReasons.addView(toggleButton);
        }
    }


}
