package com.dream.dreamtv.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.DialogFragment;
import android.speech.RecognizerIntent;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.dream.dreamtv.adapter.MySubtitleAdapter;
import com.dream.dreamtv.beans.JsonResponseBaseBean;
import com.dream.dreamtv.beans.Reason;
import com.dream.dreamtv.beans.ReasonList;
import com.dream.dreamtv.beans.Subtitle;
import com.dream.dreamtv.beans.SubtitleJson;
import com.dream.dreamtv.beans.User;
import com.dream.dreamtv.beans.UserTask;
import com.dream.dreamtv.conn.ConnectionManager;
import com.dream.dreamtv.conn.ResponseListener;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.JsonUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReasonsDialogFragment extends DialogFragment {

    private LinearLayout llComments;
    private LinearLayout llReasons;
    private LinearLayout llButtonsOptions1;
    private LinearLayout llButtonsOptions2;
    private RadioGroup rgReasons;
    private List<Reason> reasonList;
    private List<Integer> selectedReasons = new ArrayList<>();
    private ImageButton btnRecord;
    private Button btnOk;
    private Button btnSave;
    private Button btnCancel;
    private TextView voiceInput;
    private TextView tvTitle;
    private Dialog viewRoot;
    private UserTask userTask;
    private SubtitleJson subtitle;
    private ScrollView scrollViewAdvanced;
    private ScrollView scrollViewBeginner;
    private ListView mListView;
    private Subtitle selectedSubtitle;
    private int subtitleOriginalPosition;
    private int currentSubtitlePosition;
    private int idTask;
    private int taskState;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private OnDialogClosedListener mCallback;

    public static ReasonsDialogFragment newInstance(SubtitleJson subtitle, int subtitlePosition, int idTask) {
        ReasonsDialogFragment f = new ReasonsDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable("SubtitleJson", subtitle);
        args.putInt("subtitleOriginalPosition", subtitlePosition);
        args.putInt("idTask", idTask);
        f.setArguments(args);
        return f;
    }

    public static ReasonsDialogFragment newInstance(SubtitleJson subtitle, int subtitlePosition, int idTask, UserTask userTask, int taskState) {
        ReasonsDialogFragment f = new ReasonsDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable("SubtitleJson", subtitle);
        args.putInt("subtitleOriginalPosition", subtitlePosition);
        args.putInt("idTask", idTask);
        args.putInt("taskState", taskState);
        args.putParcelable("userTask", userTask);
        f.setArguments(args);
        return f;
    }

    public ReasonsDialogFragment() {
        // Required empty public constructor
    }


    // Container Activity must implement this interface
    public interface OnDialogClosedListener {
        void onDialogClosed(Subtitle selectedSubtitle, int subtitleOriginalPosition);
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity context) {
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
        subtitleOriginalPosition = getArguments().getInt("subtitleOriginalPosition");
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

//        TextView tvCurrentSubtitle = (TextView) viewRoot.findViewById(R.id.tvCurrentSubtitle);
//        TextView tvPreviousSubtitle = (TextView) viewRoot.findViewById(R.id.tvPreviousSubtitle);
//        TextView tvNextSubtitle = (TextView) viewRoot.findViewById(R.id.tvNextSubtitle);
        llButtonsOptions1 = (LinearLayout) viewRoot.findViewById(R.id.llButtonsOptions1);
        llButtonsOptions2 = (LinearLayout) viewRoot.findViewById(R.id.llButtonsOptions2);
        tvTitle = (TextView) viewRoot.findViewById(R.id.tvTitle);
        btnCancel = (Button) viewRoot.findViewById(R.id.btnCancel);
        btnSave = (Button) viewRoot.findViewById(R.id.btnSave);
        btnOk = (Button) viewRoot.findViewById(R.id.btnOk);

        scrollViewAdvanced = (ScrollView) viewRoot.findViewById(R.id.scrollViewAdvanced);
        scrollViewBeginner = (ScrollView) viewRoot.findViewById(R.id.scrollViewBeginner);

        llReasons = (LinearLayout) viewRoot.findViewById(R.id.llReasons);
        rgReasons = (RadioGroup) viewRoot.findViewById(R.id.rgReasons);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewRoot.dismiss();
//                mCallback.onDialogClosed();

            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewRoot.dismiss();
//                mCallback.onDialogClosed();

            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveReasons();
                Toast.makeText(getActivity(), getString(R.string.title_confirmation_saved_data), Toast.LENGTH_SHORT).show();
            }
        });
//        tvCurrentSubtitle.setText(Html.fromHtml(subtitle.subtitles.get(subtitleOriginalPosition - 1).text));
//        tvNextSubtitle.setText(Html.fromHtml(subtitle.subtitles.get(subtitleOriginalPosition).text));
//
//        if (subtitleOriginalPosition > 1)
//            tvPreviousSubtitle.setText(Html.fromHtml(subtitle.subtitles.get(subtitleOriginalPosition - 2).text));
//        else
//            tvPreviousSubtitle.setText("-------------");

        settingUpSubtitleNavigation(subtitleOriginalPosition);
        audioRecordSettings();

        //The cached reasons are verified
//        ReasonList reasonL = ((DreamTVApp) getActivity().getApplication()).getReasons();
//        if (reasonL == null)
        getReasons();
//        else {
//            reasonList = reasonL.data;
//            setupReasons();
//        }

        currentSubtitlePosition = subtitleOriginalPosition; //We save the original subtitleOriginalPosition

        return viewRoot;
    }

    private void settingUpSubtitleNavigation(final int subtPosition) {

        mListView = (ListView) viewRoot.findViewById(R.id.lv);

        // Initialize a new ArrayAdapter
//        ArrayAdapter adapter = new ArrayAdapter<>(
//                getActivity(),
//                android.R.layout.simple_list_item_1,
//                subtitle.subtitles
//        );
        User user = ((DreamTVApp) getActivity().getApplication()).getUser();

        MySubtitleAdapter mySubtitleAdapter = new MySubtitleAdapter(getActivity(), subtitle.subtitles,
                subtPosition, user.interface_mode);

        // Set the adapter for ListView
        mListView.setAdapter(mySubtitleAdapter);

//        mListView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
       /* if (subtitleOriginalPosition - 3 > -1) {
            mListView.setSelection(subtitleOriginalPosition - 3);
            mListView.smoothScrollToPosition(subtitleOriginalPosition - 3);
//                    mListView.getChildAt(subtitleOriginalPosition - 3).requestFocus();
        } else if (subtitleOriginalPosition - 2 > -1) {
            mListView.setSelection(subtitleOriginalPosition - 2);
            mListView.smoothScrollToPosition(subtitleOriginalPosition - 2);

        } else */
//        Display display = viewRoot.getWindow().getWindowManager().getDefaultDisplay();
//
//        int h1 = mListView.getHeight();
//        int h2 = display.getHeight();
//
//        DreamTVApp.Logger.d("h1/2 - h2/2 -> "+(h1/2 - h2/2));
        if (subtPosition - 1 > -1) {
            mListView.setSelectionFromTop(subtPosition - 1, 280);
//            mListView.smoothScrollToPosition(subtitleOriginalPosition - 1);
//            mListView.smoothScrollByOffset(10);
        }
//            }
//        }, 500);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSubtitle = (Subtitle) adapterView.getItemAtPosition(i);
                DreamTVApp.Logger.d("CPRCurrentPosition: " + currentSubtitlePosition);
                DreamTVApp.Logger.d("CPRNewPosition: " + (i + 1));
                currentSubtitlePosition = i + 1;
//                subtitleOriginalPosition - 1
//                ListView ls = (ListView) view.getParent();
//                TextView txtviewNew = (TextView) ((RelativeLayout)view).getChildAt(1); //id/tvText -> subtitle_layout
//                txtviewNew.setBackgroundColor(getActivity().getResources().getColor(R.color.light_green, null));
//                txtviewNew.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getActivity().getResources().getDisplayMetrics()));
//                txtviewNew.setTypeface(txtviewNew.getTypeface(), Typeface.NORMAL);
//
//                TextView previousText = (TextView) ((RelativeLayout)ls.getAdapter().getView(subtitleOriginalPosition - 1, view, null)).getChildAt(1);
//                previousText.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9, getActivity().getResources().getDisplayMetrics()));
//                previousText.setTypeface(previousText.getTypeface(), Typeface.ITALIC);
//                previousText.setBackgroundColor(getActivity().getResources().getColor(R.color.blue, null));

                settingUpSubtitleNavigation(i + 1);

//                subtitleOriginalPosition = i + 1;
                // Display the selected item text on TextView
//                Toast.makeText(getActivity(), "selectedItem: " + selectedItem.text, Toast.LENGTH_SHORT).show();
//                Toast.makeText(getActivity(), "selectedPreviousItem: " + previousText.getText(), Toast.LENGTH_SHORT).show();
            }
        });
//        mListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                DreamTVApp.Logger.d("Position: " + i);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });


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
        DreamTVApp.Logger.d("repopulateFormWithUserTaskData()");
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

//                mCallback.onDialogClosed();
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

//        btnRecord.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if (b) {
////                    if (voiceInput.getText().equals("")) {
//                    voiceInput.setHint("Click here to send comments");
////                    }
//                } else {
//                    voiceInput.setHint("");
//
//                }
//            }
//        });
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

                controlUserTask(); //To verify if we receive usertask, to repopulate the dialog

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
            toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.selector_1));

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

        rgReasons.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                btnSave.requestFocus(); //Nos posicionamos en el boton guardar
            }
        });

        for (int i = 0; i < reasonList.size(); i++) {
            Reason reason = reasonList.get(i);

            int dpsToogle = 25;
            final float scale = getActivity().getResources().getDisplayMetrics().density;
            int pixels = (int) (dpsToogle * scale + 0.5f);

            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(reason.name);
            radioButton.setId(reason.id);
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

}
