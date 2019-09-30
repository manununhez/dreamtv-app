package com.dream.dreamtv.ui.playVideo.dialogs;


import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Html;
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

import com.dream.dreamtv.R;
import com.dream.dreamtv.data.model.ErrorReason;
import com.dream.dreamtv.data.model.Subtitle;
import com.dream.dreamtv.data.model.Subtitle.SubtitleText;
import com.dream.dreamtv.data.model.Task;
import com.dream.dreamtv.data.model.User;
import com.dream.dreamtv.data.model.UserTask;
import com.dream.dreamtv.data.model.UserTaskError;
import com.dream.dreamtv.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import timber.log.Timber;

import static android.util.TypedValue.applyDimension;
import static com.dream.dreamtv.utils.Constants.ARG_LIST_REASONS;
import static com.dream.dreamtv.utils.Constants.ARG_SELECTED_TASK;
import static com.dream.dreamtv.utils.Constants.ARG_SUBTITLE;
import static com.dream.dreamtv.utils.Constants.ARG_SUBTITLE_ORIGINAL_POSITION;
import static com.dream.dreamtv.utils.Constants.ARG_USER;
import static com.dream.dreamtv.utils.Constants.ARG_USER_TASK_ERROR;
import static com.dream.dreamtv.utils.Constants.ARG_USER_TASK_ERROR_COMPLETE;
import static com.dream.dreamtv.utils.Constants.BEGINNER_INTERFACE_MODE;


public class ErrorSelectionDialogFragment extends DialogFragment {
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static final String SPEECH_NOT_SUPPORTED = "speech_not_supported";
    private LinearLayout llComments;
    private LinearLayout llReasons;
    private RadioGroup rgReasons;
    private UserTask mUserTask;
    private Button btnSave;
    private Button btnSaveChanges;
    private Button btnDeleteChanges;
    private TextView voiceInput;
    private TextView tvTitle;
    private TextView tvSelectedSubtitle;
    private Dialog viewRoot;
    private Subtitle mSubtitle;
    private ScrollView scrollViewAdvanced;
    private ScrollView scrollViewBeginner;
    private ScrollView scrollViewBeginnerEdition;
    private SubtitleText selectedSubtitle;
    private SubtitleText goToThisSelectedSubtitle;
    private Task mSelectedTask;
    private OnListener mCallback;
    private List<String> selectedReasons = new ArrayList<>();
    private int mSubtitleOriginalPosition;
    private LinearLayout llReasonsEdit;
    private ArrayList<ErrorReason> mReasons;
    private User mUser;

    public ErrorSelectionDialogFragment() {
        // Required empty public constructor
    }

    public static ErrorSelectionDialogFragment newInstance(ArrayList<ErrorReason> reasons,
                                                           User user,
                                                           Subtitle mSubtitleResponse,
                                                           int subtitlePosition,
                                                           Task mSelectedTask,
                                                           UserTask userTask,
                                                           ArrayList<UserTaskError> userTaskErrorList) {

        ErrorSelectionDialogFragment f = newInstance(reasons, user, mSubtitleResponse,
                subtitlePosition, mSelectedTask, userTask);


        // Supply num input as an argument.
        Bundle args = f.getArguments();
        args.putParcelableArrayList(ARG_USER_TASK_ERROR, userTaskErrorList);
        f.setArguments(args);
        return f;
    }

    public static ErrorSelectionDialogFragment newInstance(ArrayList<ErrorReason> reasons,
                                                           User user,
                                                           Subtitle mSubtitleResponse,
                                                           int subtitlePosition,
                                                           Task mSelectedTask,
                                                           UserTask userTask) {
        ErrorSelectionDialogFragment f = new ErrorSelectionDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_LIST_REASONS, reasons);
        args.putParcelable(ARG_USER, user);
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

        mUser = getArguments().getParcelable(ARG_USER);
        mReasons = getArguments().getParcelableArrayList(ARG_LIST_REASONS);
        mSubtitleOriginalPosition = getArguments().getInt(ARG_SUBTITLE_ORIGINAL_POSITION);
        mSubtitle = getArguments().getParcelable(ARG_SUBTITLE);
        mSelectedTask = getArguments().getParcelable(ARG_SELECTED_TASK);
        mUserTask = getArguments().getParcelable(ARG_USER_TASK_ERROR_COMPLETE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        viewRoot = new Dialog(getActivity(), R.style.DefaultDialogFragmentStyle);
        viewRoot.setContentView(R.layout.fragment_reasons_dialog);

        tvTitle = viewRoot.findViewById(R.id.tvTitle);
        tvSelectedSubtitle = viewRoot.findViewById(R.id.tvSelectedSubtitle);
        btnSave = viewRoot.findViewById(R.id.btnSave);
        btnSaveChanges = viewRoot.findViewById(R.id.btnSaveChanges);
        btnDeleteChanges = viewRoot.findViewById(R.id.btnDeleteChanges);

        scrollViewAdvanced = viewRoot.findViewById(R.id.scrollViewAdvanced);
        scrollViewBeginner = viewRoot.findViewById(R.id.scrollViewBeginner);
        scrollViewBeginnerEdition = viewRoot.findViewById(R.id.scrollViewBeginnerEdition);

        llReasons = viewRoot.findViewById(R.id.llReasons);
        llReasonsEdit = viewRoot.findViewById(R.id.llReasonsEdit);
        rgReasons = viewRoot.findViewById(R.id.rgReasons);

        setupEventsListener();
        setupAudioRecord();
        setupReasons();


        setupSubtitleNavigationListView(mSubtitleOriginalPosition);

        repopulateFormWithUserTaskData(mSubtitleOriginalPosition); //To verify if we receive usertask, to repopulate the dialog


        //This is used to select one mSubtitle from the navigation list, and move the video forward or backward accorded to it
        goToThisSelectedSubtitle = mSubtitle.getSubtitles().get(mSubtitleOriginalPosition - 1); //mSubtitleOriginalPosition is the mSubtitle order, and the index starts in 1. To get a specific mSubtitle from the list, the index start in 0.
        //This is used to know which mSubtitle from the navigation list are we on, in order to perform save or update errors
        selectedSubtitle = mSubtitle.getSubtitles().get(mSubtitleOriginalPosition - 1); //mSubtitleOriginalPosition is the mSubtitle order, and the index starts in 1. To get a specific mSubtitle from the list, the index start in 0.

        return viewRoot;
    }

    @Override
    public void onResume() {
        Objects.requireNonNull(getDialog().getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        getDialog().setOnKeyListener((dialog, keyCode, event) -> {
            switch (keyCode) {

                case KeyEvent.KEYCODE_VOICE_ASSIST:
                    Timber.d("KEYCODE_VOICE_ASSIST - onKeyUp");
                    // Do something...

                    return true;

                case KeyEvent.KEYCODE_SEARCH:
                    Timber.d("KEYCODE_SEARCH - onKeyUp");
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
        mCallback.onDialogClosed(goToThisSelectedSubtitle, mSubtitleOriginalPosition);
    }

    /**
     * Receiving speech input
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == Activity.RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                voiceInput.setText(result.get(0));
            }
        }
    }

    private void setupEventsListener() {
        btnSave.setOnClickListener(view -> {
            UserTaskError userTaskError = prepareReasonsToSave();
            if (selectedReasons.size() > 0) {
                mCallback.onSaveReasons(mSelectedTask.getTaskId(), mSubtitle.getVersionNumber(), userTaskError);
                dismiss();
            } else {
                Toast.makeText(getActivity(), getActivity().getString(R.string.select_errors_to_save), Toast.LENGTH_LONG).show();
            }
        });
        btnSaveChanges.setOnClickListener(view -> {
            UserTaskError userTaskError = prepareReasonsToSave();
            mCallback.onUpdateReasons(mSelectedTask.getTaskId(), mSubtitle.getVersionNumber(), userTaskError);
            dismiss();
        });

        btnDeleteChanges.setOnClickListener(view -> clearOptions());

    }


    private void setupSubtitleNavigationListView(int subtitlePosition) {

        ListView mListView = viewRoot.findViewById(R.id.lv);

        // Initialize a new ArrayAdapter
        MySubtitleAdapter mySubtitleAdapter = new MySubtitleAdapter(getActivity(), mSubtitle.getSubtitles(),
                subtitlePosition, mUserTask.getUserTaskErrorList());

        // Set the adapter for ListView
        mListView.setAdapter(mySubtitleAdapter);

        if ((subtitlePosition - 1) > -1) {
            mListView.setSelectionFromTop(subtitlePosition - 1, 280);
        }

        mListView.setOnItemClickListener((adapterView, view, position, l) -> {
            goToThisSelectedSubtitle = (SubtitleText) adapterView.getItemAtPosition(position);
            Timber.d("CPRCurrentPosition: %s", mSubtitleOriginalPosition);
            Timber.d("CPRNewPosition: %s", (position + 1));

            setupSubtitleNavigationListView(position + 1);

            repopulateFormWithUserTaskData(position);

        });

        mListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Timber.d("OnItemSelected - Position: %s", position);
                selectedSubtitle = (SubtitleText) adapterView.getItemAtPosition(position);

                tvSelectedSubtitle.setText(Html.fromHtml(selectedSubtitle.getText()));

                repopulateFormWithUserTaskData(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setupAudioRecord() {
        llComments = viewRoot.findViewById(R.id.llComments);
        ImageButton btnRecord = viewRoot.findViewById(R.id.btnRecord);
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


        Timber.d(mReasons.toString());


        if (mUser.getInterfaceMode().equals(BEGINNER_INTERFACE_MODE))
            setupReasonsRadioGroup();
        else
            setupReasonsCheck();

    }

    private void setupReasonsCheck() {
        llComments.setVisibility(View.VISIBLE);
        scrollViewAdvanced.setVisibility(View.VISIBLE);
        scrollViewBeginner.setVisibility(View.GONE);
        scrollViewBeginnerEdition.setVisibility(View.GONE);

        llReasons.removeAllViews();
        for (int i = 0; i < mReasons.size(); i++) {
            ErrorReason errorReason = mReasons.get(i);

            int dpsToogle = 25;
            final float scale = getActivity().getResources().getDisplayMetrics().density;
            int pixels = (int) (dpsToogle * scale + 0.5f);

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View inflatedLayout = inflater.inflate(R.layout.reasons_button_layout, null);
            ToggleButton toggleButton = inflatedLayout.findViewById(R.id.toogleButton);
            final CheckBox chkBox = inflatedLayout.findViewById(R.id.chkBox);

            toggleButton.setBackgroundResource(R.color.colorAccent);
            toggleButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pixels);
            layoutParams.setMargins(0, 0, 0, 15);
            toggleButton.setContentDescription(errorReason.getReasonCode()); //instead of setId
            toggleButton.setTextOn(errorReason.getName());
            toggleButton.setTextOff(errorReason.getName());
            toggleButton.setText(errorReason.getName());
            toggleButton.setLayoutParams(layoutParams);
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
        scrollViewBeginnerEdition.setVisibility(View.GONE);
        scrollViewAdvanced.setVisibility(View.GONE);

        rgReasons.removeAllViews();
        llReasonsEdit.removeAllViews();


        for (int i = 0; i < mReasons.size(); i++) {
            ErrorReason errorReason = mReasons.get(i);

            int dpsToogle = 25;
            final float scale = getActivity().getResources().getDisplayMetrics().density;
            int pixels = (int) (dpsToogle * scale + 0.5f);

            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(errorReason.getName());
            radioButton.setAllCaps(true);
            radioButton.setContentDescription(errorReason.getReasonCode()); //instead of setId
            radioButton.setGravity(Gravity.CENTER);
            radioButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.selector_1));

            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pixels);
            layoutParams.setMargins(0, 0, 0, 15);
            radioButton.setLayoutParams(layoutParams);

            rgReasons.addView(radioButton);

            //-------- For multiple errors selected


            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View inflatedLayout = inflater.inflate(R.layout.reasons_button_layout, null);
            ToggleButton toggleButton = inflatedLayout.findViewById(R.id.toogleButton);

            toggleButton.setBackgroundResource(R.color.colorAccent);
            toggleButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            layoutParams.setMargins(0, 0, 0, 15);
            toggleButton.setContentDescription(errorReason.getReasonCode()); //instead of setId
            toggleButton.setTextOn(errorReason.getName());
            toggleButton.setTextOff(errorReason.getName());
            toggleButton.setText(errorReason.getName());
            toggleButton.setLayoutParams(layoutParams);
            toggleButton.setAllCaps(true);
            toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.selector_1));


            toggleButton.setEnabled(false);

            if (inflatedLayout.getParent() != null)
                ((ViewGroup) inflatedLayout.getParent()).removeView(inflatedLayout); // <- fix

            llReasonsEdit.addView(inflatedLayout);
        }
    }

    private void repopulateFormWithUserTaskData(int position) {
        clearOptions();

        ArrayList<UserTaskError> errors = mUserTask.getUserTaskErrorsForASpecificSubtitlePosition(position + 1);
        Timber.d("repopulateFormWithUserTaskData()");


        if (errors.size() > 0) { //if at least there are one error

            tvTitle.setText(getString(R.string.title_reasons_dialog_2));
            btnSave.setVisibility(View.GONE);
            btnSaveChanges.setVisibility(View.VISIBLE);
            btnDeleteChanges.setVisibility(View.VISIBLE);


            if (mUser.getInterfaceMode().equals(BEGINNER_INTERFACE_MODE)) { //PREF_BEGINNER_INTERFACE_MODE
                if (errors.size() == 1) {

                    rgReasons.setEnabled(false);

                    for (int i = 0; i < rgReasons.getChildCount(); i++) {
                        String selectedErrorCode = errors.get(0).getReasonCode();
                        RadioButton radioButton = (RadioButton) rgReasons.getChildAt(i);

                        if (Objects.equals(radioButton.getContentDescription(), selectedErrorCode)) {
                            radioButton.setChecked(true);
                            break;
                        }
                    }

                    scrollViewBeginner.setVisibility(View.VISIBLE);
                    scrollViewBeginnerEdition.setVisibility(View.GONE);
                } else {

                    //re-select the reasons id
                    for (int i = 0; i < llReasonsEdit.getChildCount(); i++) {
                        LinearLayout childAt = (LinearLayout) llReasonsEdit.getChildAt(i);
                        CheckBox checkBox = (CheckBox) childAt.getChildAt(0); //0 -> checkbox, 1->Tooglebutton
                        ToggleButton toggleButton = (ToggleButton) childAt.getChildAt(1); //0 -> checkbox, 1->Tooglebutton

                        for (UserTaskError userTaskError : errors)
                            if (userTaskError.getReasonCode().contentEquals(toggleButton.getContentDescription())) {
                                checkBox.setChecked(true);
                                toggleButton.setChecked(true);
                            }
                    }

                    scrollViewBeginner.setVisibility(View.GONE);
                    scrollViewBeginnerEdition.setVisibility(View.VISIBLE);
                }


            } else { //ADVANCED MODE
                voiceInput.setText(errors.get(0).getComment()); //repopulate comments

                //re-select the reasons id
                for (int i = 0; i < llReasons.getChildCount(); i++) {
                    LinearLayout childAt = (LinearLayout) llReasons.getChildAt(i);
                    ToggleButton toggleButton = (ToggleButton) childAt.getChildAt(1); //0 -> checkbox, 1->Tooglebutton

                    for (UserTaskError userTaskError : errors)
                        if (userTaskError.getReasonCode().contentEquals(toggleButton.getContentDescription())) {
                            toggleButton.performClick();
                        }
                }
            }
        } else {
            //Clear screen
            tvTitle.setText(getString(R.string.title_reasons_dialog));
            btnSave.setVisibility(View.VISIBLE);
            btnSaveChanges.setVisibility(View.GONE);
            btnDeleteChanges.setVisibility(View.GONE);

            clearOptions();
        }
    }

    private void clearOptions() {

        selectedReasons.clear(); //clear all previous selected reasons in checks

        if (mUser.getInterfaceMode().equals(BEGINNER_INTERFACE_MODE)) { //BEGINNER MODE

            scrollViewBeginner.setVisibility(View.VISIBLE);
            if (scrollViewBeginnerEdition.getVisibility() == View.VISIBLE)
                scrollViewBeginnerEdition.setVisibility(View.GONE);

            rgReasons.clearCheck(); //clear all previous checks in radio buttons to get the animation correctly

        } else { //ADVANCED MODE
            scrollViewAdvanced.setVisibility(View.VISIBLE);

            voiceInput.setText(""); //repopulate comments

            //re-select the reasons id
            for (int i = 0; i < llReasons.getChildCount(); i++) {
                LinearLayout childAt = (LinearLayout) llReasons.getChildAt(i);
                CheckBox checkBox = (CheckBox) childAt.getChildAt(0); //0 -> checkbox, 1->Tooglebutton
                ToggleButton toggleButton = (ToggleButton) childAt.getChildAt(1); //0 -> checkbox, 1->Tooglebutton

                toggleButton.setChecked(false);
                checkBox.setChecked(false);
            }
        }
    }

    private UserTaskError prepareReasonsToSave() {
        //Interface mode preferences. We retrieve the selected value. The advanced mode is already in selectedReasons
        if (mUser.getInterfaceMode().equals(Constants.BEGINNER_INTERFACE_MODE)) {
            for (int i = 0; i < rgReasons.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) rgReasons.getChildAt(i);

                //We add the selected radio button
                if (radioButton.isChecked())
                    selectedReasons.add(radioButton.getContentDescription().toString());
            }

        }

        return new UserTaskError(selectedErrorsListJson(selectedReasons),
                selectedSubtitle.getPosition(), voiceInput.getText().toString());
    }

    public List<ErrorReason> selectedErrorsListJson(List<String> selectedReasons) {
        List<ErrorReason> tempList = new ArrayList<>();

        for (String reasonCode : selectedReasons) {
            for (ErrorReason errorReason : mReasons) {
                if (reasonCode.equals(errorReason.getReasonCode()))
                    tempList.add(errorReason);
            }
        }

        return tempList;
    }


    // Container Activity must implement this interface
    public interface OnListener {
        void onDialogClosed(SubtitleText selectedSubtitle, int subtitleOriginalPosition);

        void onSaveReasons(int taskId, int subtitleVersion, UserTaskError userTaskError);

        void onUpdateReasons(int taskId, int subtitleVersion, UserTaskError userTaskError);
    }

    public class MySubtitleAdapter extends ArrayAdapter<SubtitleText> {

        private final Context context;
        private final List<SubtitleText> values;
        private final Integer currentSubtitlePosition;
        private UserTaskError[] userTaskErrors;

        MySubtitleAdapter(Context context, List<SubtitleText> values, int currentSubtitlePosition,
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
                holder.tvSubtitle = convertView.findViewById(R.id.tvSubtitle);
                holder.tvSubtitleError = convertView.findViewById(R.id.tvSubtitleError);
                holder.tvSubtitleSelected = convertView.findViewById(R.id.tvSubtitleSelected);
                holder.tvTime = convertView.findViewById(R.id.tvTime);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            if (position == currentSubtitlePosition - 1) {
                if (isPositionError(userTaskErrors, position)) {
                    holder.tvSubtitle.setVisibility(View.GONE);
                    holder.tvSubtitleSelected.setVisibility(View.GONE);
                    holder.tvSubtitleError.setVisibility(View.VISIBLE);
                    holder.tvSubtitleError.setTextSize(applyDimension(TypedValue.COMPLEX_UNIT_SP, 9,
                            context.getResources().getDisplayMetrics()));
                    holder.tvSubtitleError.setText(Html.fromHtml(values.get(position).getText()));
                } else {
                    holder.tvSubtitle.setVisibility(View.GONE);
                    holder.tvSubtitleError.setVisibility(View.GONE);
                    holder.tvSubtitleSelected.setVisibility(View.VISIBLE);
                    holder.tvSubtitleSelected.setTextSize(applyDimension(TypedValue.COMPLEX_UNIT_SP, 9,
                            context.getResources().getDisplayMetrics()));
                    holder.tvSubtitleSelected.setText(Html.fromHtml(values.get(position).getText()));
                }
            } else {
                if (isPositionError(userTaskErrors, position)) {
                    holder.tvSubtitle.setVisibility(View.GONE);
                    holder.tvSubtitleSelected.setVisibility(View.GONE);
                    holder.tvSubtitleError.setVisibility(View.VISIBLE);
                    holder.tvSubtitleError.setTextSize(applyDimension(TypedValue.COMPLEX_UNIT_SP, 8,
                            context.getResources().getDisplayMetrics()));
                    holder.tvSubtitleError.setText(Html.fromHtml(values.get(position).getText()));
                } else {
                    holder.tvSubtitleSelected.setVisibility(View.GONE);
                    holder.tvSubtitleError.setVisibility(View.GONE);
                    holder.tvSubtitle.setVisibility(View.VISIBLE);
                    holder.tvSubtitle.setTextSize(applyDimension(TypedValue.COMPLEX_UNIT_SP, 8,
                            context.getResources().getDisplayMetrics()));
                    holder.tvSubtitle.setText(Html.fromHtml(values.get(position).getText()));
                }

            }

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
            TextView tvSubtitle;
            TextView tvSubtitleError;
            TextView tvSubtitleSelected;
            TextView tvTime;
        }
    }


}
