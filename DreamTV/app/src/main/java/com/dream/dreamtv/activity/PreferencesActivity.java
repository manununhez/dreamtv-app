package com.dream.dreamtv.activity;

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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.beans.JsonResponseBaseBean;
import com.dream.dreamtv.beans.Languages;
import com.dream.dreamtv.beans.User;
import com.dream.dreamtv.conn.ConnectionManager;
import com.dream.dreamtv.conn.ResponseListener;
import com.dream.dreamtv.utils.LocaleHelper;
import com.dream.dreamtv.utils.CheckableTextView;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.JsonUtils;
import com.dream.dreamtv.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PreferencesActivity extends Activity {

    private Context mContext;
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
    private List<String> keyList;
    private List<String> keyListCode;
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
    private EditText etBaseURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Get the application context
        mContext = getApplicationContext();

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
                    //llBodyLanguages.setVisibility(View.VISIBLE);
                    checkTemporaryUserData(true);
                } /*else {
                    btnSubtitle.setChecked(false);
//                    if(!btnAudio.isChecked()) //si tampoco esta seleccionado audio significa que nos fuimos hacia otro sector del menu, fuera de la seccion de lenguajes
//                        llBodyLanguages.setVisibility(View.GONE);
                }*/
            }
        });

        btnSubtitle.setOnCheckedChangeListener(new CheckableTextView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckableTextView view, boolean isChecked) {
                if(isChecked)
                    tvVideoLanguagesTitle.setText(R.string.text_subtitle_languages);
            }
        });

        btnAudio.setOnCheckedChangeListener(new CheckableTextView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckableTextView view, boolean isChecked) {
                if(isChecked)
                tvVideoLanguagesTitle.setText(R.string.text_audio_languages);

            }
        });


        btnAudio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    btnSubtitle.setChecked(false);
                    btnAudio.setChecked(true);
                    //llBodyLanguages.setVisibility(View.VISIBLE);
                    checkTemporaryUserData(false);
                }/*else{
                    btnAudio.setChecked(false);
//                    if(!btnSubtitle.isChecked()) //si tampoco esta seleccionado subtitulado significa que nos fuimos hacia otro sector del menu, fuera de la seccion de lenguajes
//                        llBodyLanguages.setVisibility(View.GONE);

                }*/
            }
        });

        llVideosSettings.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b)
                    llBodyLanguages.setVisibility(View.VISIBLE);
                else{
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
//        getLanguages();
        setupLanguages();

//        setupBaseURL();
    }

//    private void setupBaseURL() {
//        etBaseURL = (EditText) findViewById(R.id.etBaseURL);
//
//        etBaseURL.setText(((DreamTVApp) getApplication()).getBaseURL());
//    }


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

    private void setupLanguages() { //based on the 10 most spoken languages http://www.foxnewspoint.com/top-10-most-spoken-language-in-the-world-2017/
        Map<String, String> languages = new TreeMap<>();

        languages.put(Constants.NONE_OPTIONS_CODE, getString(R.string.all_languages)); //Always as the first item of the list
        languages.put("zh", getString(R.string.language_chinese));
        languages.put("en", getString(R.string.language_english));
        languages.put("es", getString(R.string.language_spanish));
        languages.put("ar", getString(R.string.language_arabic));
        languages.put("fr", getString(R.string.language_french));
        languages.put("pl", getString(R.string.language_polish));


        keyList = new ArrayList<>(languages.values());
        keyListCode = new ArrayList<>(languages.keySet());

        settingListLanguage(keyList);
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
//        mTextView.setText(resources.getString(R.string.title_video_languages_type_settings));
        tvVideoLanguagesTitle.setText(resources.getString(R.string.title_video_languages_settings));
        btnSubtitle.setText(resources.getString(R.string.btn_subtitle));
        btnAudio.setText(resources.getString(R.string.btn_audio));
        btnSave.setText(resources.getString(R.string.btn_save_settings));
        tvAudioLabel.setText(getString(R.string.title_audio));
        tvSubtitleLabel.setText(getString(R.string.title_subtitle));
        setupLanguages();
    }

    private void saveUSerPreferences() {
        //developer mode. Save URL
//        DreamTVApp dreamTVApp = ((DreamTVApp) getApplication());
//        dreamTVApp.setBaseURL(etBaseURL.getText().toString());


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
//                Gson gson = new Gson();
//                User user = gson.fromJson(response, User.class);
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
                Toast.makeText(mContext, getString(R.string.title_confirmation_user_saved_data), Toast.LENGTH_SHORT).show();

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


    private void settingListLanguage(final List<String> keyList) {
        // Initialize a new ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                mActivity,
                R.layout.simple_list_item_single_choice,
                keyList
        );

        // Set the adapter for ListView
        mListView.setAdapter(adapter);

        // Set an item click listener for the ListView
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListView.clearChoices();
                mListView.setItemChecked(i, true);

                String item = (String) mListView.getItemAtPosition(i);

                if (btnSubtitle.isChecked()) {
                    selectedSubtitleLanguageCode = keyListCode.get(keyList.indexOf(item));
                    tvSubtitleValue.setText(keyList.get(keyListCode.indexOf(selectedSubtitleLanguageCode)));
                }

                if (btnAudio.isChecked()) {
                    selectedAudioLanguageCode = keyListCode.get(keyList.indexOf(item));
                    tvAudioValue.setText(keyList.get(keyListCode.indexOf(selectedAudioLanguageCode)));
                }

            }
        });


        //--------------
        User user = ((DreamTVApp) getApplication()).getUser();
        if (user != null) {
            selectedSubtitleLanguageCode = user.sub_language;
            selectedAudioLanguageCode = user.audio_language;

            btnSubtitle.setChecked(true); //initiliaze checked
            checkTemporaryUserData(true);
        }
    }

    private void checkTemporaryUserData(boolean isSubtitleButtonSelected) {
        mListView.clearChoices();

        if (selectedSubtitleLanguageCode == null || selectedSubtitleLanguageCode.isEmpty())
            tvSubtitleValue.setText("--");
        else
            tvSubtitleValue.setText(keyList.get(keyListCode.indexOf(selectedSubtitleLanguageCode)));

        if (selectedAudioLanguageCode == null || selectedAudioLanguageCode.isEmpty())
            tvAudioValue.setText("--");
        else
            tvAudioValue.setText(keyList.get(keyListCode.indexOf(selectedAudioLanguageCode)));

        String code = isSubtitleButtonSelected ? selectedSubtitleLanguageCode : selectedAudioLanguageCode;
        if (code != null && !code.isEmpty())
            mListView.setItemChecked(keyListCode.indexOf(code), true);
    }


    private void getLanguages() {
        ResponseListener responseListener = new ResponseListener(this, true, true,
                getString(R.string.title_loading_retrieve_languages)) {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                Languages languagesList = gson.fromJson(response, Languages.class);

                languagesList.languages.put(Constants.NONE_OPTIONS_CODE, getString(R.string.all_languages)); //null option in the list

                Map<String, String> orderedMap = Utils.MapUtil.sortByValue(languagesList.languages);

                keyList = new ArrayList<>(orderedMap.values());
                keyListCode = new ArrayList<>(orderedMap.keySet());

                settingListLanguage(keyList);

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

        ConnectionManager.get(this, ConnectionManager.Urls.LANGUAGES, null, responseListener, this);

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
