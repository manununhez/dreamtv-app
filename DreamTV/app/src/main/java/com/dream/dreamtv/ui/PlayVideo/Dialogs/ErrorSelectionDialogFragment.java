package com.dream.dreamtv.ui.PlayVideo.Dialogs;


import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.ErrorReason;
import com.dream.dreamtv.model.Subtitle;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.UserTaskError;
import com.dream.dreamtv.utils.Constants;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.util.TypedValue.applyDimension;
import static com.dream.dreamtv.utils.Constants.ARG_SELECTED_TASK;
import static com.dream.dreamtv.utils.Constants.ARG_SUBTITLE;
import static com.dream.dreamtv.utils.Constants.ARG_SUBTITLE_ORIGINAL_POSITION;
import static com.dream.dreamtv.utils.Constants.ARG_USER_TASK_ERROR;
import static com.dream.dreamtv.utils.Constants.ARG_USER_TASK_ERROR_COMPLETE;
import static com.dream.dreamtv.utils.Constants.BEGINNER_INTERFACE_MODE;


public class ErrorSelectionDialogFragment extends DialogFragment {
    private static final String TAG = ErrorSelectionDialogFragment.class.getSimpleName();

    // Container Activity must implement this interface
    public interface OnListener {
        void onDialogClosed(Subtitle selectedSubtitle, int subtitleOriginalPosition);

        void onSaveReasons(UserTaskError userTaskError);
    }

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static final String SPEECH_NOT_SUPPORTED = "speech_not_supported";
    private LinearLayout llComments;
    private LinearLayout llReasons;
    private LinearLayout llButtonsOptions1;
    private LinearLayout llButtonsOptions2;
    private RadioGroup rgReasons;
    private UserTask userTask;
    private ImageButton btnRecord;
    private Button btnOk;
    private Button btnSave;
    private Button btnCancel;
    private TextView voiceInput;
    private TextView tvTitle;
    private Dialog viewRoot;
    private SubtitleResponse subtitle;
    private ScrollView scrollViewAdvanced;
    private ScrollView scrollViewBeginner;
    private Subtitle selectedSubtitle;
    private TaskEntity mSelectedTask;
    private OnListener mCallback;
    private List<String> selectedReasons = new ArrayList<>();
    private List<ErrorReason> errorReasonList;
    private int subtitleOriginalPosition;


    public ErrorSelectionDialogFragment() {
        // Required empty public constructor
    }

    public static ErrorSelectionDialogFragment newInstance(SubtitleResponse mSubtitleResponse, int subtitlePosition,
                                                           TaskEntity mSelectedTask,
                                                           UserTask userTask,
                                                           ArrayList<UserTaskError> userTaskErrorList) {
        ErrorSelectionDialogFragment f = new ErrorSelectionDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(ARG_SUBTITLE, mSubtitleResponse);
        args.putInt(ARG_SUBTITLE_ORIGINAL_POSITION, subtitlePosition);
        args.putParcelable(ARG_SELECTED_TASK, mSelectedTask);
        args.putParcelable(ARG_USER_TASK_ERROR_COMPLETE, userTask);
        args.putParcelableArrayList(ARG_USER_TASK_ERROR, userTaskErrorList);
        f.setArguments(args);
        return f;
    }

    public static ErrorSelectionDialogFragment newInstance(SubtitleResponse mSubtitleResponse,
                                                           int subtitlePosition, TaskEntity mSelectedTask,
                                                           UserTask userTask) {
        ErrorSelectionDialogFragment f = new ErrorSelectionDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(ARG_SUBTITLE, mSubtitleResponse);
        args.putInt(ARG_SUBTITLE_ORIGINAL_POSITION, subtitlePosition);
        args.putParcelable(ARG_SELECTED_TASK, mSelectedTask);
        args.putParcelable(ARG_USER_TASK_ERROR_COMPLETE, userTask);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnListener) {
            mCallback = (OnListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subtitleOriginalPosition = getArguments().getInt(ARG_SUBTITLE_ORIGINAL_POSITION);
        subtitle = getArguments().getParcelable(ARG_SUBTITLE);
        mSelectedTask = getArguments().getParcelable(ARG_SELECTED_TASK);
        userTask = getArguments().getParcelable(ARG_USER_TASK_ERROR_COMPLETE);
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
        setupAudioRecord();
        setupReasons();


        setupSubtitleNavigationListView(subtitleOriginalPosition);

        repopulateFormWithUserTaskData(subtitleOriginalPosition); //To verify if we receive usertask, to repopulate the dialog


        selectedSubtitle = subtitle.subtitles.get(subtitleOriginalPosition - 1); //subtitleOriginalPosition is the subtitle order, and the index starts in 1. To get a specific subtitle from the list, the index start in 0.

        return viewRoot;
    }

    @Override
    public void onResume() {
        Objects.requireNonNull(getDialog().getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        getDialog().setOnKeyListener((dialog, keyCode, event) -> {
            switch (keyCode) {

                case KeyEvent.KEYCODE_VOICE_ASSIST:
                    Log.d(TAG, "KEYCODE_VOICE_ASSIST - onKeyUp");
                    // Do something...

                    return true;

                case KeyEvent.KEYCODE_SEARCH:
                    Log.d(TAG, "KEYCODE_SEARCH - onKeyUp");
                    // Do something...
                    promptSpeechInput();
                    return true;

                default:
                    return false;
            }

        });

        super.onResume();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mCallback.onDialogClosed(selectedSubtitle, subtitleOriginalPosition);
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
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    voiceInput.setText(result.get(0));
                }
                break;
            }

        }
    }

    private void setupEventsListener() {
        btnCancel.setOnClickListener(view -> dismiss());
        btnOk.setOnClickListener(view -> dismiss());
        btnSave.setOnClickListener(view -> {
            saveReasons();
        });

    }


    private DreamTVApp getApplication() {
        return ((DreamTVApp) getActivity().getApplication());
    }

    private void setupSubtitleNavigationListView(int subtitlePosition) {

        ListView mListView = viewRoot.findViewById(R.id.lv);

        // Initialize a new ArrayAdapter
//        User user = ((DreamTVApp) getActivity().getApplication()).getUser();

        MySubtitleAdapter mySubtitleAdapter = new MySubtitleAdapter(getActivity(), subtitle.subtitles,
                subtitlePosition, userTask.getUserTaskErrorList());

        // Set the adapter for ListView
        mListView.setAdapter(mySubtitleAdapter);

        if ((subtitlePosition - 1) > -1) {
            mListView.setSelectionFromTop(subtitlePosition - 1, 280);
        }

        mListView.setOnItemClickListener((adapterView, view, i, l) -> {
            selectedSubtitle = (Subtitle) adapterView.getItemAtPosition(i);
            Log.d(TAG, "CPRCurrentPosition: " + subtitleOriginalPosition);
            Log.d(TAG, "CPRNewPosition: " + (i + 1));
//            currentSubtitlePosition = i + 1;

            setupSubtitleNavigationListView(i + 1);

        });

        mListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "OnItemSelected - Position: " + position);
                repopulateFormWithUserTaskData(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    private void setupAudioRecord() {
        llComments = viewRoot.findViewById(R.id.llComments);
        btnRecord = viewRoot.findViewById(R.id.btnRecord);
        voiceInput = viewRoot.findViewById(R.id.voiceInput);

        btnRecord.setOnClickListener(view -> promptSpeechInput());

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

    private void setupReasons() {

        errorReasonList = getApplication().getReasons();

        Log.d(TAG, errorReasonList.toString());

        //Interface mode settings
        User user = getApplication().getUser();

        if (user.interfaceMode.equals(BEGINNER_INTERFACE_MODE))
            setupReasonsRadioGroup();
        else
            setupReasonsCheck();

    }

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
            toggleButton.setContentDescription(errorReason.reasonCode); //instead of setId
            toggleButton.setTextOn(errorReason.name);
            toggleButton.setTextOff(errorReason.name);
            toggleButton.setText(errorReason.name);
            toggleButton.setLayoutParams(layoutParams);
//            toggleButton.setFocusable(true);
//            toggleButton.setFocusableInTouchMode(true);
            toggleButton.setAllCaps(true);
            toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.selector_1));

            toggleButton.setOnClickListener(view -> {
                if (((ToggleButton) view).isChecked()) { //si no esta chequeado y se hace check
                    if (!selectedReasons.contains(view.getContentDescription())) { //view.getId()
                        selectedReasons.add(view.getContentDescription().toString());
                        chkBox.setChecked(true);
                    }
                } else { //estaba chequeado, pero se desactiva el mismo boton. Ahi se reinicia el selectedFeedbackReason
                    selectedReasons.remove(view.getContentDescription()); //view.getId()
                    chkBox.setChecked(false);
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

        rgReasons.setOnCheckedChangeListener((radioGroup, i) -> {
            btnSave.requestFocus(); //Nos posicionamos en el boton guardar
        });

        for (int i = 0; i < errorReasonList.size(); i++) {
            ErrorReason errorReason = errorReasonList.get(i);

            int dpsToogle = 25;
            final float scale = getActivity().getResources().getDisplayMetrics().density;
            int pixels = (int) (dpsToogle * scale + 0.5f);

            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(errorReason.name);
            radioButton.setAllCaps(true);
            radioButton.setContentDescription(errorReason.reasonCode); //instead of setId
            radioButton.setGravity(Gravity.CENTER);
            radioButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.selector_1));

            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pixels);
            layoutParams.setMargins(0, 0, 0, 15);
            radioButton.setLayoutParams(layoutParams);

            rgReasons.addView(radioButton);
        }
    }


    private void repopulateFormWithUserTaskData(int position) {
        ArrayList<UserTaskError> errors = userTask.getUserTaskErrorsForASpecificSubtitlePosition(position);
        Log.d(TAG, "repopulateFormWithUserTaskData()");

        if (errors.size() > 0) { //if at least there are one error

            tvTitle.setText(getString(R.string.title_reasons_dialog_2));
//            llButtonsOptions1.setVisibility(View.VISIBLE);
//            llButtonsOptions2.setVisibility(View.GONE);

            voiceInput.setText(errors.get(0).getComment()); //repopulate comments

            //Interface mode settings
            User user = getApplication().getUser();
            if (user.interfaceMode.equals(BEGINNER_INTERFACE_MODE)) { //BEGINNER MODE
                for (int i = 0; i < rgReasons.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) rgReasons.getChildAt(i);
//                    radioButton.setEnabled(false);
//                    radioButton.setFocusable(false);
//                    radioButton.setFocusableInTouchMode(false);

                    for (UserTaskError userTaskError : errors)
                        if (userTaskError.getReasonCode().contentEquals(radioButton.getContentDescription()))
                            radioButton.setChecked(true);
                }
            } else { //ADVANCED MODE
//                llComments.setEnabled(false);
//                llComments.setFocusable(false);
//                llComments.setFocusableInTouchMode(false);
//                btnRecord.setFocusable(false);
//                btnRecord.setEnabled(false);
//                btnRecord.setFocusableInTouchMode(false);
                //re-select the reasons id
                for (int i = 0; i < llReasons.getChildCount(); i++) {
                    LinearLayout childAt = (LinearLayout) llReasons.getChildAt(i);
                    CheckBox checkBox = (CheckBox) childAt.getChildAt(0); //0 -> checkbox, 1->Tooglebutton
                    ToggleButton toggleButton = (ToggleButton) childAt.getChildAt(1); //0 -> checkbox, 1->Tooglebutton
//                    checkBox.setEnabled(false);
//                    toggleButton.setEnabled(false);
//                    toggleButton.setFocusable(false);
//                    toggleButton.setFocusableInTouchMode(false);

                    for (UserTaskError userTaskError : errors)
                        if (userTaskError.getReasonCode().contentEquals(toggleButton.getContentDescription())) {
                            toggleButton.setChecked(true);
                            checkBox.setChecked(true);
                        }
                }
            }


//            btnOk.requestFocus(); //Nos posicionamos en este boton
        } else {
            //Clear screen

            tvTitle.setText(getString(R.string.title_reasons_dialog));
//            llButtonsOptions1.setVisibility(View.GONE);
//            llButtonsOptions2.setVisibility(View.VISIBLE);

            voiceInput.setText(""); //repopulate comments

            //Interface mode settings
            User user = getApplication().getUser();
            if (user.interfaceMode.equals(BEGINNER_INTERFACE_MODE)) { //BEGINNER MODE
                for (int i = 0; i < rgReasons.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) rgReasons.getChildAt(i);
//                    radioButton.setEnabled(true);
//                    radioButton.setFocusable(true);
//                    radioButton.setFocusableInTouchMode(true);

                    radioButton.setChecked(false);

                }
            } else { //ADVANCED MODE
                llComments.setEnabled(true);
                llComments.setFocusable(true);
                llComments.setFocusableInTouchMode(true);
                btnRecord.setFocusable(true);
                btnRecord.setEnabled(true);
                btnRecord.setFocusableInTouchMode(true);
                //re-select the reasons id
                for (int i = 0; i < llReasons.getChildCount(); i++) {
                    LinearLayout childAt = (LinearLayout) llReasons.getChildAt(i);
                    CheckBox checkBox = (CheckBox) childAt.getChildAt(0); //0 -> checkbox, 1->Tooglebutton
                    ToggleButton toggleButton = (ToggleButton) childAt.getChildAt(1); //0 -> checkbox, 1->Tooglebutton
//                    checkBox.setEnabled(true);
//                    toggleButton.setEnabled(true);
//                    toggleButton.setFocusable(true);
//                    toggleButton.setFocusableInTouchMode(true);

                    toggleButton.setChecked(false);
                    checkBox.setChecked(false);
                }
            }
        }
    }

    private void saveReasons() {
        //Interface mode settings. We retrieve the selected value. The advanced mode is already in selectedReasons
        User user = ((DreamTVApp) getActivity().getApplication()).getUser();
        if (user.interfaceMode.equals(Constants.BEGINNER_INTERFACE_MODE)) {
            for (int i = 0; i < rgReasons.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) rgReasons.getChildAt(i);

                //We add the selected radio button
                if (radioButton.isChecked())
                    selectedReasons.add(radioButton.getContentDescription().toString());
            }

        }

        UserTaskError userTaskError = new UserTaskError(mSelectedTask.taskId,
                selectedErrorsListJson(selectedReasons), subtitle.versionNumber,
                selectedSubtitle.position, voiceInput.getText().toString());


        Toast.makeText(getActivity(), getString(R.string.title_confirmation_saved_data), Toast.LENGTH_SHORT).show();

        mCallback.onSaveReasons(userTaskError);

        dismiss();
    }


    public String selectedErrorsListJson(List<String> selectedReasons) {
        List<ErrorReason> tempList = new ArrayList<>();

        for (String reasonCode : selectedReasons) {
            for (ErrorReason errorReason : errorReasonList) {
                if (reasonCode.equals(errorReason.reasonCode))
                    tempList.add(errorReason);
            }
        }

        return new Gson().toJson(tempList);
    }



    public class MySubtitleAdapter extends ArrayAdapter<Subtitle> {

        private final String CHARS_S = "chars/s";
        private final Context context;
        private final List<Subtitle> values;
        private final Integer currentSubtitlePosition;
        private UserTaskError[] userTaskErrors;

        MySubtitleAdapter(Context context, List<Subtitle> values, int currentSubtitlePosition,
                          UserTaskError[] userTaskErrors) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
            this.currentSubtitlePosition = currentSubtitlePosition;
            this.userTaskErrors = userTaskErrors;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            ViewHolder holder;
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                convertView = Objects.requireNonNull(mInflater).inflate(R.layout.subtitle_layout, null);
                holder = new ViewHolder();
                holder.tvText = convertView.findViewById(R.id.tvText);
                holder.tvTime = convertView.findViewById(R.id.tvTime);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            if (position == currentSubtitlePosition - 1) {
                holder.tvText.setBackgroundColor(context.getResources().getColor(R.color.blue, null));
                holder.tvText.setTextSize(applyDimension(TypedValue.COMPLEX_UNIT_SP, 9,
                        context.getResources().getDisplayMetrics()));
                holder.tvText.setTypeface(holder.tvText.getTypeface(), Typeface.NORMAL);
            } else if (isPositionError(userTaskErrors, position)) {
                holder.tvText.setTextSize(applyDimension(TypedValue.COMPLEX_UNIT_SP, 8,
                        context.getResources().getDisplayMetrics()));
                holder.tvText.setTypeface(holder.tvText.getTypeface(), Typeface.ITALIC);
                holder.tvText.setBackgroundColor(context.getResources().getColor(R.color.green, null));
            } else {
                holder.tvText.setTextSize(applyDimension(TypedValue.COMPLEX_UNIT_SP, 8,
                        context.getResources().getDisplayMetrics()));
                holder.tvText.setTypeface(holder.tvText.getTypeface(), Typeface.ITALIC);
                holder.tvText.setBackgroundColor(context.getResources().getColor(R.color.black_opaque, null));
            }

            holder.tvText.setText(Html.fromHtml(values.get(position).text));


//            if (userInterfaceMode.equals(Constants.ADVANCED_INTERFACE_MODE)) { //Advanced MODE
//                holder.tvTime.setText(videoCurrentReadVeloc(values.get(position).text, (values.get(position).getEnd() - values.get(position).getStart())));
//
//            }
            return convertView;
        }

//        private String videoCurrentReadVeloc(String text, long millis) {
//            if (millis > 1000) //to avoid division by zero
//                return text.length() / (millis / 1000) + " " + CHARS_S;
//            else
//                return "0 " + CHARS_S;
//        }

        private boolean isPositionError(UserTaskError[] userTaskErrorsList, int position) {
            //userTaskErrorsList is ordered by subtitle_position asc
            for (UserTaskError userTaskError : userTaskErrorsList) {
                if ((userTaskError.getSubtitlePosition() - 1) > position)
                    return false;
                else if ((userTaskError.getSubtitlePosition() - 1) == position)
                    return true;
            }

            return false;
        }

        class ViewHolder {
            TextView tvText;
            TextView tvTime;
        }
    }


}
