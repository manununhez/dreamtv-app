package com.dream.dreamtv.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.app.DialogFragment;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.VolleyError;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.beans.JsonResponseBaseBean;
import com.dream.dreamtv.beans.Reason;
import com.dream.dreamtv.beans.ReasonList;
import com.dream.dreamtv.beans.SubtitleJson;
import com.dream.dreamtv.beans.User;
import com.dream.dreamtv.beans.UserTask;
import com.dream.dreamtv.conn.ConnectionManager;
import com.dream.dreamtv.conn.ResponseListener;
import com.dream.dreamtv.utils.LocaleHelper;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.JsonUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReasonsDialogFragment extends DialogFragment {

    private LinearLayout llComments;
    private LinearLayout llReasons;
    private RadioGroup rgReasons;
    private List<Reason> reasonList;
    private List<Integer> selectedReasons = new ArrayList<>();
    private ImageButton btnRecord;
    private TextView voiceInput;
    private Dialog viewRoot;
    private UserTask userTask;
    private SubtitleJson subtitle;
    private ScrollView scrollViewAdvanced;
    private ScrollView scrollViewBeginner;
    private int subtitlePosition;
    private int idTask;
    private int taskState;
    private static final int REQ_CODE_SPEECH_INPUT = 100;

    public static ReasonsDialogFragment newInstance(SubtitleJson subtitle, int subtitlePosition, int idTask) {
        ReasonsDialogFragment f = new ReasonsDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable("SubtitleJson", subtitle);
        args.putInt("subtitlePosition", subtitlePosition);
        args.putInt("idTask", idTask);
        f.setArguments(args);
        return f;
    }

    public static ReasonsDialogFragment newInstance(SubtitleJson subtitle, int subtitlePosition, int idTask, UserTask userTask, int taskState) {
        ReasonsDialogFragment f = new ReasonsDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable("SubtitleJson", subtitle);
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
        subtitlePosition = getArguments().getInt("subtitlePosition");
        subtitle = getArguments().getParcelable("SubtitleJson");
        idTask = getArguments().getInt("idTask");
        taskState = getArguments().getInt("taskState");
        userTask = getArguments().getParcelable("userTask");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        viewRoot = new Dialog(getActivity(), R.style.DefaultDialogFragmentStyle);
        viewRoot.setContentView(R.layout.fragment_reasons_dialog);

        TextView tvCurrentSubtitle = (TextView) viewRoot.findViewById(R.id.tvCurrentSubtitle);
        TextView tvPreviousSubtitle = (TextView) viewRoot.findViewById(R.id.tvPreviousSubtitle);
        TextView tvNextSubtitle = (TextView) viewRoot.findViewById(R.id.tvNextSubtitle);
        LinearLayout llButtonsOptions1 = (LinearLayout) viewRoot.findViewById(R.id.llButtonsOptions1);
        LinearLayout llButtonsOptions2 = (LinearLayout) viewRoot.findViewById(R.id.llButtonsOptions2);
        TextView tvTitle = (TextView) viewRoot.findViewById(R.id.tvTitle);
        Button btnCancel = (Button) viewRoot.findViewById(R.id.btnCancel);
        Button btnSave = (Button) viewRoot.findViewById(R.id.btnSave);
        Button btnOk = (Button) viewRoot.findViewById(R.id.btnOk);

        scrollViewAdvanced = (ScrollView) viewRoot.findViewById(R.id.scrollViewAdvanced);
        scrollViewBeginner = (ScrollView) viewRoot.findViewById(R.id.scrollViewBeginner);

        llReasons = (LinearLayout) viewRoot.findViewById(R.id.llReasons);
        rgReasons = (RadioGroup) viewRoot.findViewById(R.id.rgReasons);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewRoot.dismiss();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewRoot.dismiss();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveReasons();
                Toast.makeText(getActivity(), getString(R.string.title_confirmation_saved_data), Toast.LENGTH_SHORT).show();
            }
        });
        tvCurrentSubtitle.setText(Html.fromHtml(subtitle.subtitles.get(subtitlePosition - 1).text));
        tvNextSubtitle.setText(Html.fromHtml(subtitle.subtitles.get(subtitlePosition).text));

        if (subtitlePosition > 1)
            tvPreviousSubtitle.setText(Html.fromHtml(subtitle.subtitles.get(subtitlePosition - 2).text));
        else
            tvPreviousSubtitle.setText("-------------");

        audioRecordSettings();

        //The cached reasons are verified
//        ReasonList reasonL = ((DreamTVApp) getActivity().getApplication()).getReasons();
//        if (reasonL == null)
        getReasons();
//        else {
//            reasonList = reasonL.data;
//            setupReasons();
//        }


        //controlUserTask. We repopulate the form with user task data
        if (userTask != null)
            if (taskState == Constants.SEE_AGAIN_CATEGORY) {
                repopulateFormWithUserTaskData();
                llButtonsOptions1.setVisibility(View.VISIBLE);
                llButtonsOptions2.setVisibility(View.GONE);
            } else if (taskState == Constants.CHECK_NEW_TASKS_CATEGORY) {
                tvTitle.setText(getString(R.string.title_reasons_dialog_2));
                llButtonsOptions1.setVisibility(View.GONE);
                llButtonsOptions2.setVisibility(View.VISIBLE);
            }


        return viewRoot;
    }

    private void repopulateFormWithUserTaskData() {
        voiceInput.setText(userTask.comments); //repopulate comments

        List<String> strArray = Arrays.asList(userTask.reason_id.substring(userTask.reason_id.indexOf("[") + 1, userTask.reason_id.indexOf("]")).split(", "));

        //Interface mode settings
        User user = ((DreamTVApp) getActivity().getApplication()).getUser();
        if (user.interface_mode.equals(Constants.BEGINNER_INTERFACE_MODE)) { //BEGINNER MODE
            for (int i = 0; i < rgReasons.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) rgReasons.getChildAt(i);
                radioButton.setEnabled(false);
                if (strArray.contains(String.valueOf(radioButton.getId()))) {
                    radioButton.setChecked(true);
                }

            }
        } else { //ADVANCED MODE
            //re-select the reasons id
            for (int i = 0; i < llReasons.getChildCount(); i++) {
                LinearLayout childAt = (LinearLayout) llReasons.getChildAt(i);
                CheckBox checkBox = (CheckBox) childAt.getChildAt(0); //0 -> checkbox, 1->Tooglebutton
                ToggleButton toggleButton = (ToggleButton) childAt.getChildAt(1); //0 -> checkbox, 1->Tooglebutton
                checkBox.setEnabled(false);
                toggleButton.setEnabled(false);
                if (strArray.contains(String.valueOf(toggleButton.getId()))) {
                    toggleButton.setChecked(true);
                    checkBox.setChecked(true);
                }
            }
        }

    }

    private void saveReasons() {
        UserTask userTask = new UserTask();
        userTask.comments = voiceInput.getText().toString();
        userTask.subtitle_position = subtitlePosition;
        userTask.subtitle_version = String.valueOf(subtitle.version_number);
        userTask.task_id = idTask;
        //Interface mode settings
        User user = ((DreamTVApp) getActivity().getApplication()).getUser();
        if (user.interface_mode.equals(Constants.BEGINNER_INTERFACE_MODE)) { //We add the selected radio button
            List<Integer> tempList = new ArrayList<Integer>();
            tempList.add(rgReasons.getCheckedRadioButtonId());
            userTask.reasonList = tempList.toString();
        } else //we add the selected checkbox ADVANCED
            userTask.reasonList = selectedReasons.toString();

        final String jsonRequest = JsonUtils.getJsonRequest(getActivity(), userTask);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_saving_reasons)) {

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

    @Override
    public void onResume() {
//        int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
//        int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        getDialog().getWindow().setLayout(width, height);

        super.onResume();
    }

    private void audioRecordSettings() {
        llComments = (LinearLayout) viewRoot.findViewById(R.id.llComments);
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
                getString(R.string.hint_voice_google_input));
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
        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_retrieve_options)) {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);

                ReasonList reasons = gson.fromJson(response, ReasonList.class);
                ((DreamTVApp) getActivity().getApplication()).setReasons(reasons); //save reasons in cache
                reasonList = reasons.data;
                DreamTVApp.Logger.d(reasonList.toString());

                //Interface mode settings
                User user = ((DreamTVApp) getActivity().getApplication()).getUser();
                if (user.interface_mode.equals(Constants.BEGINNER_INTERFACE_MODE))
                    setupReasonsRadioGroup();
                else
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
        llComments.setVisibility(View.VISIBLE);
        scrollViewAdvanced.setVisibility(View.VISIBLE);
        scrollViewBeginner.setVisibility(View.GONE);

        llReasons.removeAllViews();
        for (int i = 0; i < reasonList.size(); i++) {
            Reason reason = reasonList.get(i);

            int dpsToogle = 25;
            final float scale = getActivity().getResources().getDisplayMetrics().density;
            int pixels = (int) (dpsToogle * scale + 0.5f);

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View inflatedLayout = inflater.inflate(R.layout.reasons_button_layout, null);
            ToggleButton toggleButton = (ToggleButton) inflatedLayout.findViewById(R.id.toogleButton);
            final CheckBox chkBox = (CheckBox) inflatedLayout.findViewById(R.id.chkBox);
//            containerDestacado.addView(inflatedLayout);

//            final ToggleButton toggleButton = new ToggleButton(getActivity());
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
                        if (!selectedReasons.contains(view.getId())) {
                            selectedReasons.add(view.getId());
                            chkBox.setChecked(true);
                        }
                    } else { //estaba chequeado, pero se desactiva el mismo boton. Ahi se reinicia el selectedFeedbackReason
                        selectedReasons.remove(selectedReasons.indexOf(view.getId()));
                        chkBox.setChecked(false);
                    }
                }
            });

            if (inflatedLayout.getParent() != null)
                ((ViewGroup) inflatedLayout.getParent()).removeView(inflatedLayout); // <- fix

            llReasons.addView(inflatedLayout);
        }
    }

    private void setupReasonsRadioGroup() {
        llComments.setVisibility(View.GONE);
        scrollViewBeginner.setVisibility(View.VISIBLE);
        scrollViewAdvanced.setVisibility(View.GONE);

        rgReasons.removeAllViews();
        for (int i = 0; i < reasonList.size(); i++) {
            Reason reason = reasonList.get(i);

            int dpsToogle = 25;
            final float scale = getActivity().getResources().getDisplayMetrics().density;
            int pixels = (int) (dpsToogle * scale + 0.5f);

            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(reason.name);
            radioButton.setId(reason.id);
            radioButton.setGravity(Gravity.CENTER);
            radioButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.selector_2_1));

            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pixels);
            layoutParams.setMargins(0, 0, 0, 15);
            radioButton.setLayoutParams(layoutParams);

            rgReasons.addView(radioButton);
        }
    }
}
