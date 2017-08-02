package com.dream.dreamtv.fragment;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.VolleyError;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.beans.JsonResponseBaseBean;
import com.dream.dreamtv.beans.Reason;
import com.dream.dreamtv.beans.ReasonList;
import com.dream.dreamtv.beans.UserTask;
import com.dream.dreamtv.conn.ConnectionManager;
import com.dream.dreamtv.conn.ResponseListener;
import com.dream.dreamtv.utils.JsonUtils;
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
    private ImageButton btnRecord;
    private TextView voiceInput;
    private Dialog viewRoot;
    private UserTask userTask;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private int subtitlePosition;
    private int idTask;
    private int taskState;
    private int subtitleVersion;

    public static ReasonsDialogFragment newInstance(String subTitleText, int subtitlePosition, int idTask,
                                                    int subtitleVersion) {
        ReasonsDialogFragment f = new ReasonsDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("subTitleText", subTitleText);
        args.putInt("subtitleVersion", subtitleVersion);
        args.putInt("subtitlePosition", subtitlePosition);
        args.putInt("idTask", idTask);
        f.setArguments(args);
        return f;
    }

    public static ReasonsDialogFragment newInstance(String subTitleText, int subtitlePosition, int idTask,
                                                    int subtitleVersion, UserTask userTask, int taskState) {
        ReasonsDialogFragment f = new ReasonsDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("subTitleText", subTitleText);
        args.putInt("subtitleVersion", subtitleVersion);
        args.putInt("subtitlePosition", subtitlePosition);
        args.putInt("idTask", idTask);
        args.putInt("taskState", taskState);
        args.putParcelable("userTask", userTask);
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
        subtitleVersion = getArguments().getInt("subtitleVersion");
        subtitlePosition = getArguments().getInt("subtitlePosition");
        idTask = getArguments().getInt("idTask");
        taskState = getArguments().getInt("taskState");
        userTask = getArguments().getParcelable("userTask");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        viewRoot = new Dialog(getActivity(), R.style.DefaultDialogFragmentStyle);
        viewRoot.setContentView(R.layout.fragment_reasons_dialog);

        TextView tvSubtitle = (TextView) viewRoot.findViewById(R.id.tvSubtitle);
        TextView tvTitle = (TextView) viewRoot.findViewById(R.id.tvTitle);
        Button btnCancel = (Button) viewRoot.findViewById(R.id.btnCancel);
        Button btnSave = (Button) viewRoot.findViewById(R.id.btnSave);

        llReasons = (LinearLayout) viewRoot.findViewById(R.id.llReasons);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewRoot.dismiss();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveReasons();
                Toast.makeText(getActivity(), "Thanks. Saved reason", Toast.LENGTH_SHORT).show();
            }
        });
        tvSubtitle.setText(Html.fromHtml(subTitleText));

        audioRecordSettings();

        //The cached reasons are verified
        ReasonList reasonL = ((DreamTVApp) getActivity().getApplication()).getReasons();
        if (reasonL == null)
            getReasons();
        else {
            reasonList = reasonL.data;
            setupReasons();
        }


        //controlUserTask. We repopulate the form with user task data
        if (userTask != null)
            if (taskState == MainFragment.SEE_AGAIN_CATEGORY)
                repopulateFormWithUserTaskData();
            else if (taskState == MainFragment.CHECK_NEW_TASKS_CATEGORY)
                tvTitle.setText("Others users think there is a mistake here. What do you think?");

        return viewRoot;
    }

    private void repopulateFormWithUserTaskData() {
        voiceInput.setText(userTask.comments); //repopulate comments

        //re-select the reasons id
        List<String> strArray = Arrays.asList(userTask.reason_id.substring(userTask.reason_id.indexOf("[") + 1, userTask.reason_id.indexOf("]")).split(", "));
        for (int i = 0; i < llReasons.getChildCount(); i++) {
            ToggleButton childAt = (ToggleButton) llReasons.getChildAt(i);
            if (strArray.contains(String.valueOf(childAt.getId())))
                childAt.setChecked(true);
        }

    }

    private void saveReasons() {
        UserTask userTask = new UserTask();
        userTask.comments = voiceInput.getText().toString();
        userTask.subtitle_position = subtitlePosition;
        userTask.subtitle_version = String.valueOf(subtitleVersion);
        userTask.task_id = idTask;
        userTask.reasonList = selectedReasons.toString();

        final String jsonRequest = JsonUtils.getJsonRequest(getActivity(), userTask);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Saving reasons...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);

                viewRoot.dismiss();
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

        ConnectionManager.post(getActivity(), ConnectionManager.Urls.USER_TASKS_SAVE, null, jsonRequest, responseListener, this);

    }

    private void audioRecordSettings() {
        btnRecord = (ImageButton) viewRoot.findViewById(R.id.btnRecord);
        voiceInput = (TextView) viewRoot.findViewById(R.id.voiceInput);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        });
    }


    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Please, Say the reason");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getActivity(),
                    "speech_not_supported",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == Activity.RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    voiceInput.setText(result.get(0));
                }
                break;
            }

        }
    }

    private void getReasons() {
        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Retrieving options...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);

                ReasonList reasons = gson.fromJson(response, ReasonList.class);
                ((DreamTVApp) getActivity().getApplication()).setReasons(reasons); //save reasons in cache
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

            int dpsToogle = 25;
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
