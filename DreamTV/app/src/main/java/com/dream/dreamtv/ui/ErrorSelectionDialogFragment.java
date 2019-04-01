package com.dream.dreamtv.ui;


import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.VolleyError;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.ErrorReason;
import com.dream.dreamtv.model.JsonResponseBaseBean;
import com.dream.dreamtv.model.Subtitle;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.network.NetworkDataSource;
import com.dream.dreamtv.network.ResponseListener;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.JsonUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * A simple {@link Fragment} subclass.
 */
public class ErrorSelectionDialogFragment extends DialogFragment {
    private static final String TAG = ErrorSelectionDialogFragment.class.getSimpleName();

    private static final String SUBTITLE_JSON = "SubtitleResponse";
    private static final String SUBTITLE_ORIGINAL_POSITION = "subtitleOriginalPosition";
    private static final String ID_TASK = "idTask";
    private static final String TASK_STATE = "taskState";
    private static final String USER_TASK = "userTask";
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static final String SPEECH_NOT_SUPPORTED = "speech_not_supported";
    private final List<Integer> selectedReasons = new ArrayList<>();
    private LinearLayout llComments;
    private LinearLayout llReasons;
    private LinearLayout llButtonsOptions1;
    private LinearLayout llButtonsOptions2;
    private RadioGroup rgReasons;
    private List<ErrorReason> errorReasonList;
    private ImageButton btnRecord;
    private Button btnOk;
    private Button btnSave;
    private Button btnCancel;
    private TextView voiceInput;
    private TextView tvTitle;
    private Dialog viewRoot;
    private UserTask userTask;
    private SubtitleResponse subtitle;
    private ScrollView scrollViewAdvanced;
    private ScrollView scrollViewBeginner;
    private Subtitle selectedSubtitle;
    private int subtitleOriginalPosition;
    private int currentSubtitlePosition;
    private int idTask;
    private int taskState;
    private OnDialogClosedListener mCallback;

    public ErrorSelectionDialogFragment() {
        // Required empty public constructor
    }

    public static ErrorSelectionDialogFragment newInstance(SubtitleResponse subtitle, int subtitlePosition, int idTask) {
        ErrorSelectionDialogFragment f = new ErrorSelectionDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(SUBTITLE_JSON, subtitle);
        args.putInt(SUBTITLE_ORIGINAL_POSITION, subtitlePosition);
        args.putInt(ID_TASK, idTask);
        f.setArguments(args);
        return f;
    }

    public static ErrorSelectionDialogFragment newInstance(SubtitleResponse subtitle, int subtitlePosition, int idTask, UserTask userTask, int taskState) {
        ErrorSelectionDialogFragment f = new ErrorSelectionDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(SUBTITLE_JSON, subtitle);
        args.putInt(SUBTITLE_ORIGINAL_POSITION, subtitlePosition);
        args.putInt(ID_TASK, idTask);
        args.putInt(TASK_STATE, taskState);
        args.putParcelable(USER_TASK, userTask);
        f.setArguments(args);
        return f;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnDialogClosedListener) {
            mCallback = (OnDialogClosedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDialogClosedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subtitleOriginalPosition = getArguments().getInt(SUBTITLE_ORIGINAL_POSITION);
        subtitle = getArguments().getParcelable(SUBTITLE_JSON);
        idTask = getArguments().getInt(ID_TASK);
        taskState = getArguments().getInt(TASK_STATE);
        userTask = getArguments().getParcelable(USER_TASK);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        viewRoot = new Dialog(getActivity(), R.style.DefaultDialogFragmentStyle);
        viewRoot.setContentView(R.layout.fragment_reasons_dialog);

        llButtonsOptions1 = viewRoot.findViewById(R.id.llButtonsOptions1);
        llButtonsOptions2 = viewRoot.findViewById(R.id.llButtonsOptions2);
        tvTitle = viewRoot.findViewById(R.id.tvTitle);
        btnCancel = viewRoot.findViewById(R.id.btnCancel);
        btnSave = viewRoot.findViewById(R.id.btnSave);
        btnOk = viewRoot.findViewById(R.id.btnOk);

        scrollViewAdvanced = viewRoot.findViewById(R.id.scrollViewAdvanced);
        scrollViewBeginner = viewRoot.findViewById(R.id.scrollViewBeginner);

        llReasons = viewRoot.findViewById(R.id.llReasons);
        rgReasons = viewRoot.findViewById(R.id.rgReasons);

        setupEventsListener();
        setupSubtitleNavigation(subtitleOriginalPosition);
        setupAudioRecord();

        //The cached reasons are verified
//        ReasonList reasonL = ((DreamTVApp) getActivity().getApplication()).setupReasons();
//        if (reasonL == null)
        setupReasons();
//        else {
//            errorReasonList = reasonL.data;
//            setupReasons();
//        }

        currentSubtitlePosition = subtitleOriginalPosition; //We save the original subtitleOriginalPosition

        return viewRoot;
    }

    private void setupEventsListener() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveReasons();
                Toast.makeText(getActivity(), getString(R.string.title_confirmation_saved_data), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setupSubtitleNavigation(final int subtPosition) {

        ListView mListView = viewRoot.findViewById(R.id.lv);

        // Initialize a new ArrayAdapter
        User user = ((DreamTVApp) getActivity().getApplication()).getUser();

        MySubtitleAdapter mySubtitleAdapter = new MySubtitleAdapter(getActivity(), subtitle.subtitles,
                subtPosition, user.interface_mode);

        // Set the adapter for ListView
        mListView.setAdapter(mySubtitleAdapter);

        if (subtPosition - 1 > -1) {
            mListView.setSelectionFromTop(subtPosition - 1, 280);
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSubtitle = (Subtitle) adapterView.getItemAtPosition(i);
                Log.d(TAG,"CPRCurrentPosition: " + currentSubtitlePosition);
                Log.d(TAG,"CPRNewPosition: " + (i + 1));
                currentSubtitlePosition = i + 1;

                setupSubtitleNavigation(i + 1);

            }
        });

    }

    private void controlUserTask() {
        //controlUserTask. We repopulate the form with user task data
        if (userTask != null)
            if (taskState == Constants.CONTINUE_WATCHING_CATEGORY) {
                repopulateFormWithUserTaskData();
                tvTitle.setText(getString(R.string.title_reasons_dialog_3));
                llButtonsOptions1.setVisibility(View.VISIBLE);
                llButtonsOptions2.setVisibility(View.GONE);
            } else if (taskState == Constants.CHECK_NEW_TASKS_CATEGORY) {
                tvTitle.setText(getString(R.string.title_reasons_dialog_2));
                llButtonsOptions1.setVisibility(View.GONE);
                llButtonsOptions2.setVisibility(View.VISIBLE);
            }

    }

    private void repopulateFormWithUserTaskData() {
        Log.d(TAG,"repopulateFormWithUserTaskData()");
        voiceInput.setText(userTask.comments); //repopulate comments

        List<String> strArray = Arrays.asList(userTask.reason_id.substring(userTask.reason_id.indexOf("[") + 1, userTask.reason_id.indexOf("]")).split(", "));

        //Interface mode settings
        User user = ((DreamTVApp) getActivity().getApplication()).getUser();
        if (user.interface_mode.equals(Constants.BEGINNER_INTERFACE_MODE)) { //BEGINNER MODE
            for (int i = 0; i < rgReasons.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) rgReasons.getChildAt(i);
                radioButton.setEnabled(false);
                radioButton.setFocusable(false);
                radioButton.setFocusableInTouchMode(false);
                if (strArray.contains(String.valueOf(radioButton.getId()))) {
                    radioButton.setChecked(true);
                }

            }
        } else { //ADVANCED MODE
            llComments.setEnabled(false);
            llComments.setFocusable(false);
            llComments.setFocusableInTouchMode(false);
            btnRecord.setFocusable(false);
            btnRecord.setEnabled(false);
            btnRecord.setFocusableInTouchMode(false);
            //re-select the reasons id
            for (int i = 0; i < llReasons.getChildCount(); i++) {
                LinearLayout childAt = (LinearLayout) llReasons.getChildAt(i);
                CheckBox checkBox = (CheckBox) childAt.getChildAt(0); //0 -> checkbox, 1->Tooglebutton
                ToggleButton toggleButton = (ToggleButton) childAt.getChildAt(1); //0 -> checkbox, 1->Tooglebutton
                checkBox.setEnabled(false);
                toggleButton.setEnabled(false);
                toggleButton.setFocusable(false);
                toggleButton.setFocusableInTouchMode(false);
                if (strArray.contains(String.valueOf(toggleButton.getId()))) {
                    toggleButton.setChecked(true);
                    checkBox.setChecked(true);
                }
            }
        }


        btnOk.requestFocus(); //Nos posicionamos en este boton
    }

    private void saveReasons() {
        UserTask userTask = new UserTask();
        userTask.comments = voiceInput.getText().toString();
        userTask.subtitle_position = currentSubtitlePosition;
        userTask.subtitle_version = String.valueOf(subtitle.version_number);
        userTask.task_id = idTask;
        //Interface mode settings
        User user = ((DreamTVApp) getActivity().getApplication()).getUser();
        if (user.interface_mode.equals(Constants.BEGINNER_INTERFACE_MODE)) { //We add the selected radio button
            List<Integer> tempList = new ArrayList<>();
            tempList.add(rgReasons.getCheckedRadioButtonId());
            userTask.reasonList = tempList.toString();
        } else //we add the selected checkbox ADVANCED
            userTask.reasonList = selectedReasons.toString();

        final String jsonRequest = JsonUtils.getJsonRequest(getActivity(), userTask);

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_saving_reasons)) {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                Log.d(TAG,response);

                dismiss();

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                Log.d(TAG,error.getMessage());
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                Log.d(TAG,jsonResponse.toString());
            }
        };

//        NetworkDataSource.post(getActivity(), NetworkDataSource.Urls.USER_TASKS, null, jsonRequest, responseListener, this);

    }

    @Override
    public void onResume() {
        Objects.requireNonNull(getDialog().getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                switch (keyCode) {

                    case KeyEvent.KEYCODE_VOICE_ASSIST:
                        Log.d(this.getClass().getName(), "KEYCODE_VOICE_ASSIST - onKeyUp");
                        // Do something...

                        return true;

                    case KeyEvent.KEYCODE_SEARCH:
                        Log.d(this.getClass().getName(), "KEYCODE_SEARCH - onKeyUp");
                        // Do something...
                        promptSpeechInput();
                        return true;

                    default:
                        return false;
                }

            }
        });

        super.onResume();
    }

    private void setupAudioRecord() {
        llComments = viewRoot.findViewById(R.id.llComments);
        btnRecord = viewRoot.findViewById(R.id.btnRecord);
        voiceInput = viewRoot.findViewById(R.id.voiceInput);

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
                    SPEECH_NOT_SUPPORTED,
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

    private void setupReasons() {

        errorReasonList = ((DreamTVApp) getActivity().getApplication()).getReasons();

        Log.d(TAG, errorReasonList.toString());

        //Interface mode settings
        User user = ((DreamTVApp) getActivity().getApplication()).getUser();
        if (user.interface_mode.equals(Constants.BEGINNER_INTERFACE_MODE))
            setupReasonsRadioGroup();
        else
            setupReasonsCheck();

        controlUserTask(); //To verify if we receive usertask, to repopulate the dialog

    }
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_retrieve_options)) {
//
//            @Override
//            public void processResponse(String response) {
//                Gson gson = new Gson();
//                Log.d(TAG,response);
//
//                TypeToken type = new TypeToken<JsonResponseBaseBean<List<ErrorReason>>>() {
//                };
//                JsonResponseBaseBean<List<ErrorReason>> jsonResponse = JsonUtils.getJsonResponse(response, type);
//
//                errorReasonList = jsonResponse.data;
//                Log.d(TAG, errorReasonList.toString());
//
//                //Interface mode settings
//                User user = ((DreamTVApp) getActivity().getApplication()).getUser();
//                if (user.interface_mode.equals(Constants.BEGINNER_INTERFACE_MODE))
//                    setupReasonsRadioGroup();
//                else
//                    setupReasons();
//
//                controlUserTask(); //To verify if we receive usertask, to repopulate the dialog
//
//            }
//
//            @Override
//            public void processError(VolleyError error) {
//                super.processError(error);
//                Log.d(TAG,error.getMessage());
//            }
//
//            @Override
//            public void processError(JsonResponseBaseBean jsonResponse) {
//                super.processError(jsonResponse);
//                Log.d(TAG,jsonResponse.toString());
//            }
//        };
//
//        NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.REASONS, null, responseListener, this);
//
//    }

    private void setupReasonsCheck() {
        llComments.setVisibility(View.VISIBLE);
        scrollViewAdvanced.setVisibility(View.VISIBLE);
        scrollViewBeginner.setVisibility(View.GONE);

        llReasons.removeAllViews();
        for (int i = 0; i < errorReasonList.size(); i++) {
            ErrorReason errorReason = errorReasonList.get(i);

            int dpsToogle = 25;
            final float scale = getActivity().getResources().getDisplayMetrics().density;
            int pixels = (int) (dpsToogle * scale + 0.5f);

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View inflatedLayout = inflater.inflate(R.layout.reasons_button_layout, null);
            ToggleButton toggleButton = inflatedLayout.findViewById(R.id.toogleButton);
            final CheckBox chkBox = inflatedLayout.findViewById(R.id.chkBox);

            toggleButton.setBackgroundResource(R.color.gray);
            toggleButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pixels);
            layoutParams.setMargins(0, 0, 0, 15);
            toggleButton.setId(errorReason.id);
            toggleButton.setTextOn(errorReason.name);
            toggleButton.setTextOff(errorReason.name);
            toggleButton.setText(errorReason.name);
            toggleButton.setLayoutParams(layoutParams);
            toggleButton.setFocusable(true);
            toggleButton.setFocusableInTouchMode(true);
            toggleButton.setAllCaps(true);
            toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.selector_1));

            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((ToggleButton) view).isChecked()) { //si no esta chequeado y se hace check
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

        rgReasons.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                btnSave.requestFocus(); //Nos posicionamos en el boton guardar
            }
        });

        for (int i = 0; i < errorReasonList.size(); i++) {
            ErrorReason errorReason = errorReasonList.get(i);

            int dpsToogle = 25;
            final float scale = getActivity().getResources().getDisplayMetrics().density;
            int pixels = (int) (dpsToogle * scale + 0.5f);

            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(errorReason.name);
            radioButton.setAllCaps(true);
            radioButton.setId(errorReason.id);
            radioButton.setGravity(Gravity.CENTER);
            radioButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.selector_1));

            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pixels);
            layoutParams.setMargins(0, 0, 0, 15);
            radioButton.setLayoutParams(layoutParams);

            rgReasons.addView(radioButton);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mCallback.onDialogClosed(selectedSubtitle, subtitleOriginalPosition);

    }

    // Container Activity must implement this interface
    public interface OnDialogClosedListener {
        void onDialogClosed(Subtitle selectedSubtitle, int subtitleOriginalPosition);
    }

}
