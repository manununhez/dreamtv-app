/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.dream.dreamtv.ui.Settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.utils.CheckableTextView;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.InjectorUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;


public class SettingsFragment extends Fragment {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    private static final String ABR_CHINESE = "zh";
    private static final String ABR_ENGLISH = "en";
    private static final String ABR_SPANISH = "es";
    private static final String ABR_ARABIC = "ar";
    private static final String ABR_FRENCH = "fr";
    private static final String ABR_POLISH = "pl";
    private ListView mListView;
    private LinearLayout llBodyLanguages;
    private TextView tvSubtitleValue;
    private TextView tvAudioValue;
    private List<String> keyListForAdapter;
    private List<String> languagesKeyList;
    private List<String> languagesKeyListCode;
    private CheckableTextView btnSubtitle;
    private CheckableTextView btnAudio;
    private String selectedSubtitleLanguageCode;
    private String selectedAudioLanguageCode;
    private RadioButton rbYes;
    private RadioButton rbNot;
    private RadioButton rbEnglish;
    private RadioButton rbPolish;
    private RadioButton rbAdvanced;
    private RadioButton rbBeginner;
    private Button btnSave;
    private ArrayAdapter<String> lvLanguagesAdapter;
    private FirebaseAnalytics mFirebaseAnalytics;
    private LinearLayout llVideosSettings;
    private SettingsViewModel mViewModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Get the widgets reference from XML layout
        mListView = view.findViewById(R.id.lv);

        llVideosSettings = view.findViewById(R.id.llVideosSettings);
        llBodyLanguages = view.findViewById(R.id.llBodyLanguages);

        rbEnglish = view.findViewById(R.id.rbEnglish);
        rbPolish = view.findViewById(R.id.rbPolish);
        rbAdvanced = view.findViewById(R.id.rbAdvanced);
        rbBeginner = view.findViewById(R.id.rbBeginner);


        tvSubtitleValue = view.findViewById(R.id.tvSubtitleValue);
        tvAudioValue = view.findViewById(R.id.tvAudioValue);

        btnSubtitle = view.findViewById(R.id.btnSubtitle);
        btnAudio = view.findViewById(R.id.btnAudio);
        btnSave = view.findViewById(R.id.btnSave);

        rbYes = view.findViewById(R.id.rbYes);
        rbNot = view.findViewById(R.id.rbNot);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SettingsViewModelFactory factory = InjectorUtils.provideSettingsViewModelFactory(Objects.requireNonNull(getActivity()));
//        mViewModel = ViewModelProviders.of(this, factory).get(SettingsViewModel.class);
        mViewModel = ViewModelProviders.of(this, factory).get(SettingsViewModel.class);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());


        interfaceLanguageSettings();
        interfaceModeSettings();
        testingModeSettings();
//        shareModeSettings();
        initializeLanguagesList();
        setupListView();

        setupEventsListener();

        Log.d(TAG, "OnCreateSettingsActivity");

    }

    private void setupEventsListener() {
        btnSubtitle.setOnFocusChangeListener((view, b) -> {
            if (b) {
                btnAudio.setChecked(false);
                btnSubtitle.setChecked(true);
                updateListItemChecked(true);
            }
        });

        btnSubtitle.setOnCheckedChangeListener((view, isChecked) -> {
            if (isChecked) {
                int position = lvLanguagesAdapter.getPosition(getString(R.string.all_languages));

                if (position >= 0) {
                    lvLanguagesAdapter.remove(getString(R.string.all_languages));
                    lvLanguagesAdapter.notifyDataSetChanged();
                }

            }
        });

        btnAudio.setOnCheckedChangeListener((view, isChecked) -> {
            if (isChecked) {
                int position = lvLanguagesAdapter.getPosition(getString(R.string.all_languages));

                if (position < 0) {
                    lvLanguagesAdapter.add(getString(R.string.all_languages));
                    lvLanguagesAdapter.notifyDataSetChanged();
                }
            }
        });


        btnAudio.setOnFocusChangeListener((view, b) -> {
            if (b) {
                btnSubtitle.setChecked(false);
                btnAudio.setChecked(true);
                updateListItemChecked(false);
            }
        });

        llVideosSettings.setOnFocusChangeListener((view, b) -> {
            if (b)
                llBodyLanguages.setVisibility(View.VISIBLE);
            else {
                llBodyLanguages.setVisibility(View.GONE);
                btnSubtitle.setChecked(false);
                btnAudio.setChecked(false);

            }
        });



        btnSave.setOnClickListener(view -> saveUSerPreferences());

    }


    private void testingModeSettings() {
        String mode = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication()).getTestingMode();

        if (mode.equals(getString(R.string.text_no_option)))
            rbNot.setChecked(true);
        else if (mode.equals(getString(R.string.text_yes_option)))
            rbYes.setChecked(true);

    }


    private void initializeLanguagesList() { //based on the 10 most spoken languages http://www.foxnewspoint.com/top-10-most-spoken-language-in-the-world-2017/

        languagesKeyList = new ArrayList<>();
        languagesKeyListCode = new ArrayList<>();

        languagesKeyList.add(getString(R.string.language_chinese));
        languagesKeyList.add(getString(R.string.language_english));
        languagesKeyList.add(getString(R.string.language_spanish));
        languagesKeyList.add(getString(R.string.language_arabic));
        languagesKeyList.add(getString(R.string.language_french));
        languagesKeyList.add(getString(R.string.language_polish));
        languagesKeyList.add(getString(R.string.all_languages));

        languagesKeyListCode.add(ABR_CHINESE);
        languagesKeyListCode.add(ABR_ENGLISH);
        languagesKeyListCode.add(ABR_SPANISH);
        languagesKeyListCode.add(ABR_ARABIC);
        languagesKeyListCode.add(ABR_FRENCH);
        languagesKeyListCode.add(ABR_POLISH);
        languagesKeyListCode.add(Constants.NONE_OPTIONS_CODE);


        keyListForAdapter = new ArrayList<>(languagesKeyList); //list to modifying in adapter, without affecting the languages list

    }

    private void interfaceLanguageSettings() {
        User user = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication()).getUser();
        if (user != null)
            if (user.interface_language.equals(Constants.LANGUAGE_ENGLISH))
                rbEnglish.setChecked(true);
            else if (user.interface_language.equals(Constants.LANGUAGE_POLISH))
                rbPolish.setChecked(true);
    }

    private void interfaceModeSettings() {
        User user = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication()).getUser();
        if (user != null)
            if (user.interface_mode.equals(Constants.BEGINNER_INTERFACE_MODE))
                rbBeginner.setChecked(true);
            else if (user.interface_mode.equals(Constants.ADVANCED_INTERFACE_MODE))
                rbAdvanced.setChecked(true);
    }


    private void saveUSerPreferences() {

        User user = new User();
        user.sub_language = selectedSubtitleLanguageCode;
        user.audio_language = selectedAudioLanguageCode;
        user.interface_mode = rbBeginner.isChecked() ? Constants.BEGINNER_INTERFACE_MODE :
                Constants.ADVANCED_INTERFACE_MODE; //interface mode updated
        user.interface_language = rbPolish.isChecked() ? Constants.LANGUAGE_POLISH :
                Constants.LANGUAGE_ENGLISH; //interface language updated


        DreamTVApp dreamTVApp = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication());
        //Save testing mode
        if (rbYes.isChecked())
            dreamTVApp.setTestingMode(getString(R.string.text_yes_option));
        else
            dreamTVApp.setTestingMode(getString(R.string.text_no_option));


        firebaseAnalyticsReportEvent(user);


//        if (isChangesAudioSubVerified())
            mViewModel.requestUserUpdate(user);

        getActivity().finish();


    }

    private boolean isChangesAudioSubVerified() {
        return false;
    }

    private void firebaseAnalyticsReportEvent(User user) {
        String mode = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication()).getTestingMode();
        Bundle bundle = new Bundle();

        if (mode.equals(getString(R.string.text_no_option)))
            bundle.putBoolean(Constants.FIREBASE_KEY_TESTING_MODE, false);
        else if (mode.equals(getString(R.string.text_yes_option)))
            bundle.putBoolean(Constants.FIREBASE_KEY_TESTING_MODE, true);

        //User Settings Saved - Analytics Report Event
        bundle.putString(Constants.FIREBASE_KEY_SUB_LANGUAGE, user.sub_language);
        bundle.putString(Constants.FIREBASE_KEY_AUDIO_LANGUAGE, user.audio_language);
        bundle.putString(Constants.FIREBASE_KEY_INTERFACE_MODE, user.interface_mode);
        bundle.putString(Constants.FIREBASE_KEY_INTERFACE_LANGUAGE, user.interface_language);
        mFirebaseAnalytics.logEvent(Constants.FIREBASE_LOG_EVENT_PRESSED_SAVE_SETTINGS_BTN, bundle);
    }


    private void setupListView() {
        // Initialize a new ArrayAdapter
        lvLanguagesAdapter = new ArrayAdapter<>(
                Objects.requireNonNull(getActivity()),
                R.layout.simple_list_item_single_choice,
                keyListForAdapter
        );

        // Set the adapter for ListView
        mListView.setAdapter(lvLanguagesAdapter);

        // Set an item click listener for the ListView
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListView.clearChoices();
                mListView.setItemChecked(i, true);

                String item = (String) mListView.getItemAtPosition(i);

                if (btnSubtitle.isChecked()) {
                    selectedSubtitleLanguageCode = languagesKeyListCode.get(keyListForAdapter.indexOf(item));
                    tvSubtitleValue.setText(keyListForAdapter.get(languagesKeyListCode.indexOf(selectedSubtitleLanguageCode)));
                }

                if (btnAudio.isChecked()) {
                    selectedAudioLanguageCode = languagesKeyListCode.get(keyListForAdapter.indexOf(item));
                    tvAudioValue.setText(keyListForAdapter.get(languagesKeyListCode.indexOf(selectedAudioLanguageCode)));
                }

            }
        });


        //--------------
        User user = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication()).getUser();
        if (user != null) {
            selectedSubtitleLanguageCode = user.sub_language;
            selectedAudioLanguageCode = user.audio_language;

            btnSubtitle.setChecked(false); //initiliaze unchecked
            btnAudio.setChecked(true); //initiliaze checked
            updateListItemChecked(false);
        }
    }

    private void updateListItemChecked(boolean isSubtitleButtonSelected) {
        mListView.clearChoices();

        if (selectedSubtitleLanguageCode == null || selectedSubtitleLanguageCode.isEmpty())
            tvSubtitleValue.setText("--");
        else
            tvSubtitleValue.setText(languagesKeyList.get(languagesKeyListCode.indexOf(selectedSubtitleLanguageCode)));

        if (selectedAudioLanguageCode == null || selectedAudioLanguageCode.isEmpty())
            tvAudioValue.setText("--");
        else
            tvAudioValue.setText(languagesKeyList.get(languagesKeyListCode.indexOf(selectedAudioLanguageCode)));

        String code = isSubtitleButtonSelected ? selectedSubtitleLanguageCode : selectedAudioLanguageCode;
        if (code != null && !code.isEmpty())
            mListView.setItemChecked(languagesKeyListCode.indexOf(code), true);
    }


}
