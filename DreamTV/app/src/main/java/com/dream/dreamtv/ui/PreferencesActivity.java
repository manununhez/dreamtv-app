package com.dream.dreamtv.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.JsonResponseBaseBean;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.network.ConnectionManager;
import com.dream.dreamtv.network.ResponseListener;
import com.dream.dreamtv.utils.CheckableTextView;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.JsonUtils;
import com.dream.dreamtv.utils.LocaleHelper;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class PreferencesActivity extends Activity {

    private static final String ABR_CHINESE = "zh";
    private static final String ABR_ENGLISH = "en";
    private static final String ABR_SPANISH = "es";
    private static final String ABR_ARABIC = "ar";
    private static final String ABR_FRENCH = "fr";
    private static final String ABR_POLISH = "pl";
    private Activity mActivity;
    private ListView mListView;
    private LinearLayout llBodyLanguages;
    private TextView tvSubtitleValue;
    private TextView tvAudioValue;
    private TextView tvTitle;
    private TextView tvTestingModeTitle;
    private TextView tvTextLanguageTitle;
    private TextView tvReasonDialogInterfaceTitle;
    private TextView tvVideoLanguagesTitle;
    private TextView tvAudioLabel;
    private TextView tvSubtitleLabel;
    private List<String> keyListForAdapter;
    private List<String> languagesKeyList;
    private List<String> languagesKeyListCode;
    private CheckableTextView btnSubtitle;
    private CheckableTextView btnAudio;
    private String selectedSubtitleLanguageCode;
    private String selectedAudioLanguageCode;
    private RadioButton rbYes;
    private RadioButton rbEnglish;
    private RadioButton rbPolish;
    private RadioButton rbAdvanced;
    private RadioButton rbBeginner;
    private Button btnSave;
    private ArrayAdapter<String> lvLanguagesAdapter;
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Get the activity
        mActivity = PreferencesActivity.this;

        // Get the widgets reference from XML layout
        mListView = findViewById(R.id.lv);

        LinearLayout llVideosSettings = findViewById(R.id.llVideosSettings);
        llBodyLanguages = findViewById(R.id.llBodyLanguages);

        rbEnglish = findViewById(R.id.rbEnglish);
        rbPolish = findViewById(R.id.rbPolish);
        rbAdvanced = findViewById(R.id.rbAdvanced);
        rbBeginner = findViewById(R.id.rbBeginner);


        RadioGroup rgInterfaceLanguage = findViewById(R.id.rgInterfaceLanguage);

        tvTitle = findViewById(R.id.tvTitle);
        tvTestingModeTitle = findViewById(R.id.tvTestingModeTitle);

        tvTextLanguageTitle = findViewById(R.id.tvTextLanguageTitle);
        tvAudioLabel = findViewById(R.id.tvAudioLabel);
        tvSubtitleLabel = findViewById(R.id.tvSubtitleLabel);
        tvReasonDialogInterfaceTitle = findViewById(R.id.tvReasonDialogInterfaceTitle);
        tvVideoLanguagesTitle = findViewById(R.id.tvVideoLanguagesTitle);
        tvSubtitleValue = findViewById(R.id.tvSubtitleValue);
        tvAudioValue = findViewById(R.id.tvAudioValue);

        btnSubtitle = findViewById(R.id.btnSubtitle);
        btnAudio = findViewById(R.id.btnAudio);

        btnSave = findViewById(R.id.btnSave);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        btnSubtitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    btnAudio.setChecked(false);
                    btnSubtitle.setChecked(true);
                    updateListItemChecked(true);
                }
            }
        });

        btnSubtitle.setOnCheckedChangeListener(new CheckableTextView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckableTextView view, boolean isChecked) {
                if (isChecked) {
                    tvVideoLanguagesTitle.setText(R.string.text_subtitle_languages);

                    int position = lvLanguagesAdapter.getPosition(getString(R.string.all_languages));

                    if (position >= 0) {
                        lvLanguagesAdapter.remove(getString(R.string.all_languages));
                        lvLanguagesAdapter.notifyDataSetChanged();
                    }

                }
            }
        });

        btnAudio.setOnCheckedChangeListener(new CheckableTextView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckableTextView view, boolean isChecked) {
                if (isChecked) {
                    tvVideoLanguagesTitle.setText(R.string.text_audio_languages);

                    int position = lvLanguagesAdapter.getPosition(getString(R.string.all_languages));

                    if (position < 0) {
                        lvLanguagesAdapter.add(getString(R.string.all_languages));
                        lvLanguagesAdapter.notifyDataSetChanged();
                    }
                }
            }
        });


        btnAudio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    btnSubtitle.setChecked(false);
                    btnAudio.setChecked(true);
                    updateListItemChecked(false);
                }
            }
        });

        llVideosSettings.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b)
                    llBodyLanguages.setVisibility(View.VISIBLE);
                else {
                    llBodyLanguages.setVisibility(View.GONE);
                    btnSubtitle.setChecked(false);
                    btnAudio.setChecked(false);

                }
            }
        });


        rgInterfaceLanguage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.rbEnglish) {
                    updateViews(Constants.LANGUAGE_ENGLISH);
                } else if (checkedId == R.id.rbPolish) {
                    updateViews(Constants.LANGUAGE_POLISH);
                }
            }

        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUSerPreferences();
            }
        });

        interfaceLanguageSettings();
        interfaceModeSettings();
        testingModeSettings();
        initializeLanguagesList();
        setupListView();


    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    private void testingModeSettings() {
        rbYes = findViewById(R.id.rbYes);
        RadioButton rbNot = findViewById(R.id.rbNot);

        String mode = ((DreamTVApp) getApplication()).getTestingMode();

        if (mode == null || mode.equals(getString(R.string.text_no_option)))
            rbNot.setChecked(true);
        else if (mode.equals(getString(R.string.text_yes_option)))
            rbYes.setChecked(true);

    }

    private void initializeLanguagesList() { //based on the 10 most spoken languages http://www.foxnewspoint.com/top-10-most-spoken-language-in-the-world-2017/

        languagesKeyList = new ArrayList<String>();
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
        User user = ((DreamTVApp) getApplication()).getUser();
        if (user != null)
            if (user.interface_language.equals(Constants.LANGUAGE_ENGLISH))
                rbEnglish.setChecked(true);
            else if (user.interface_language.equals(Constants.LANGUAGE_POLISH))
                rbPolish.setChecked(true);
    }

    private void interfaceModeSettings() {
        User user = ((DreamTVApp) getApplication()).getUser();
        if (user != null)
            if (user.interface_mode.equals(Constants.BEGINNER_INTERFACE_MODE))
                rbBeginner.setChecked(true);
            else if (user.interface_mode.equals(Constants.ADVANCED_INTERFACE_MODE))
                rbAdvanced.setChecked(true);
    }


    private void updateViews(String languageCode) {
        Context context = LocaleHelper.setLocale(this, languageCode);
        Resources resources = context.getResources();
        tvTitle.setText(resources.getString(R.string.title_video_settings));
        tvTestingModeTitle.setText(resources.getString(R.string.title_testing_mode_settings));
        rbPolish.setText(resources.getString(R.string.rb_option_text_polish));
        tvTextLanguageTitle.setText(resources.getString(R.string.title_text_language_settings));
        rbEnglish.setText(resources.getString(R.string.rb_option_text_english));
        tvReasonDialogInterfaceTitle.setText(resources.getString(R.string.title_reason_dialog_interface_settings));
        rbAdvanced.setText(resources.getString(R.string.rb_option_text_advanced));
        rbBeginner.setText(resources.getString(R.string.rb_option_text_beginner));
        tvVideoLanguagesTitle.setText(resources.getString(R.string.title_video_languages_settings));
        btnSubtitle.setText(resources.getString(R.string.btn_subtitle));
        btnAudio.setText(resources.getString(R.string.btn_audio));
        btnSave.setText(resources.getString(R.string.btn_save_settings));
        tvAudioLabel.setText(resources.getString(R.string.title_audio));
        tvSubtitleLabel.setText(resources.getString(R.string.title_subtitle));
        initializeLanguagesList();
    }

    private void saveUSerPreferences() {

        User user = new User();
        user.sub_language = selectedSubtitleLanguageCode;
        user.audio_language = selectedAudioLanguageCode;
        user.interface_mode = rbBeginner.isChecked() ? Constants.BEGINNER_INTERFACE_MODE :
                Constants.ADVANCED_INTERFACE_MODE; //interface mode updated
        user.interface_language = rbPolish.isChecked() ? Constants.LANGUAGE_POLISH :
                Constants.LANGUAGE_ENGLISH; //interface language updated

        final String jsonRequest = JsonUtils.getJsonRequest(this, user);

        ResponseListener responseListener = new ResponseListener(this, true, true, getString(R.string.title_loading_saving_data)) {

            @Override
            public void processResponse(String response) {
                DreamTVApp.Logger.d(response);
                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = JsonUtils.getJsonResponse(response, type);
                User user = jsonResponse.data;

                DreamTVApp dreamTVApp = ((DreamTVApp) getApplication());
                dreamTVApp.setUser(user);


                //Save testing mode
                if (rbYes.isChecked())
                    dreamTVApp.setTestingMode(getString(R.string.text_yes_option));
                else
                    dreamTVApp.setTestingMode(getString(R.string.text_no_option));

                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                Toast.makeText(PreferencesActivity.this, getString(R.string.title_confirmation_user_saved_data), Toast.LENGTH_SHORT).show();

                firebaseAnalyticsReportEvent(user);


                finish();

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                Toast.makeText(PreferencesActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                DreamTVApp.Logger.d(error.getMessage());
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                Toast.makeText(PreferencesActivity.this, jsonResponse.toString(), Toast.LENGTH_SHORT).show();
                DreamTVApp.Logger.d(jsonResponse.toString());
            }
        };

        ConnectionManager.put(this, ConnectionManager.Urls.USERS, null, jsonRequest, responseListener, this);

    }

    private void firebaseAnalyticsReportEvent(User user) {
        String mode = ((DreamTVApp) getApplication()).getTestingMode();
        Bundle bundle = new Bundle();

        if (mode == null || mode.equals(getString(R.string.text_no_option)))
            bundle.putString("testing_mode", "no");
        else if (mode.equals(getString(R.string.text_yes_option)))
            bundle.putString("testing_mode", "yes");

        //User Settings Saved - Analytics Report Event
        bundle.putString("sub_language", user.sub_language);
        bundle.putString("audio_language", user.audio_language);
        bundle.putString("interface_mode", user.interface_mode);
        bundle.putString("interface_language", user.interface_language);
        mFirebaseAnalytics.logEvent("pressed_save_settings_btn", bundle);
    }


    private void setupListView() {
        // Initialize a new ArrayAdapter
        lvLanguagesAdapter = new ArrayAdapter<>(
                mActivity,
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
        User user = ((DreamTVApp) getApplication()).getUser();
        if (user != null) {
            selectedSubtitleLanguageCode = user.sub_language;
            selectedAudioLanguageCode = user.audio_language;

            btnSubtitle.setChecked(true); //initiliaze checked
            btnAudio.setChecked(false); //initiliaze checked
            updateListItemChecked(true);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //We check if the language interface has changed. if not, we go back to the previous value
        String selectedMode = rbPolish.isChecked() ? Constants.LANGUAGE_POLISH :
                Constants.LANGUAGE_ENGLISH;
        User user = ((DreamTVApp) getApplication()).getUser();
        if (user != null)
            if (!user.interface_language.equals(selectedMode)) {
                LocaleHelper.setLocale(this, user.interface_language);
            }
    }


}
