package com.dream.dreamtv.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PreferencesActivity extends Activity {

    private Context mContext;
    private Activity mActivity;

    private ListView mListView;
    //    private TextView mTextView;
    private TextView tvSubtitleValue;
    private TextView tvAudioValue;
    private TextView tvTitle;
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
    private RadioGroup rgInterfaceLanguage;
    private RadioButton rbEnglish;
    private RadioButton rbPolish;
    private RadioButton rbAdvanced;
    private RadioButton rbBeginner;
    private Button btnSave;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Get the application context
        mContext = getApplicationContext();

        // Get the activity
        mActivity = PreferencesActivity.this;

        // Get the widgets reference from XML layout
        mListView = (ListView) findViewById(R.id.lv);

        rbEnglish = (RadioButton) findViewById(R.id.rbEnglish);
        rbPolish = (RadioButton) findViewById(R.id.rbPolish);
        rbAdvanced = (RadioButton) findViewById(R.id.rbAdvanced);
        rbBeginner = (RadioButton) findViewById(R.id.rbBeginner);

        rgInterfaceLanguage = (RadioGroup) findViewById(R.id.rgInterfaceLanguage);

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTextLanguageTitle = (TextView) findViewById(R.id.tvTextLanguageTitle);
        tvAudioLabel = (TextView) findViewById(R.id.tvAudioLabel);
        tvSubtitleLabel = (TextView) findViewById(R.id.tvSubtitleLabel);
        tvReasonDialogInterfaceTitle = (TextView) findViewById(R.id.tvReasonDialogInterfaceTitle);
        tvVideoLanguagesTitle = (TextView) findViewById(R.id.tvVideoLanguagesTitle);
        tvSubtitleValue = (TextView) findViewById(R.id.tvSubtitleValue);
        tvAudioValue = (TextView) findViewById(R.id.tvAudioValue);

        btnSubtitle = (CheckableTextView) findViewById(R.id.btnSubtitle);
        btnAudio = (CheckableTextView) findViewById(R.id.btnAudio);

        btnSave = (Button) findViewById(R.id.btnSave);


        btnSubtitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    btnAudio.setChecked(false);
                    btnSubtitle.setChecked(true);
                    checkTemporaryUserData(true);
                }
            }
        });


        btnAudio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    btnSubtitle.setChecked(false);
                    btnAudio.setChecked(true);
                    checkTemporaryUserData(false);
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
                updateUserData();
            }
        });

        interfaceLanguageSettings();
        interfaceModeSettings();

//        getLanguages();
        setupLanguages();

    }

    private void setupLanguages() { //based on the 10 most spoken languages http://www.foxnewspoint.com/top-10-most-spoken-language-in-the-world-2017/
        Map<String, String> languages = new TreeMap<>();

        languages.put(Constants.NONE_OPTIONS_CODE, getString(R.string.all_languages)); //Always as the first item of the list
        languages.put("zh", getString(R.string.language_chinese));
        languages.put("en", getString(R.string.language_english));
        languages.put("es", getString(R.string.language_spanish));
        languages.put("ar", getString(R.string.language_arabic));
//        languages.put("hi", "Hindi");
//        languages.put("ru", "Russian");
//        languages.put("bn", "Bengali");
//        languages.put("pt", "Portuguese");
        languages.put("fr", getString(R.string.language_french));
        languages.put("pl", getString(R.string.language_polish));

//        Map<String, String> orderedMap = MapUtil.sortByValue(languages);


        keyList = new ArrayList<String>(languages.values());
        keyListCode = new ArrayList<String>(languages.keySet());

        settingListLanguage(keyList);
    }

    private void interfaceLanguageSettings() {
        User user = ((DreamTVApp) getApplication()).getUser();
        if (user.interface_language.equals(Constants.LANGUAGE_ENGLISH))
            rbEnglish.setChecked(true);
        else if (user.interface_language.equals(Constants.LANGUAGE_POLISH))
            rbPolish.setChecked(true);
    }

    private void interfaceModeSettings() {
        User user = ((DreamTVApp) getApplication()).getUser();
        if (user.interface_mode.equals(Constants.BEGINNER_INTERFACE_MODE))
            rbBeginner.setChecked(true);
        else if (user.interface_mode.equals(Constants.ADVANCED_INTERFACE_MODE))
            rbAdvanced.setChecked(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    private void updateViews(String languageCode) {
        Context context = LocaleHelper.setLocale(this, languageCode);
        Resources resources = context.getResources();
        tvTitle.setText(resources.getString(R.string.title_video_settings));
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

    private void updateUserData() {
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
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                User user = gson.fromJson(response, User.class);

                ((DreamTVApp) getApplication()).setUser(user);

                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                Toast.makeText(mContext, getString(R.string.title_confirmation_user_saved_data), Toast.LENGTH_SHORT).show();

                finish();
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

        ConnectionManager.post(this, ConnectionManager.Urls.USER_UPDATE, null, jsonRequest, responseListener, this);

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

        if (selectedSubtitleLanguageCode == null || selectedSubtitleLanguageCode.equals(""))
            tvSubtitleValue.setText("--");
        else
            tvSubtitleValue.setText(keyList.get(keyListCode.indexOf(selectedSubtitleLanguageCode)));

        if (selectedAudioLanguageCode == null || selectedAudioLanguageCode.equals(""))
            tvAudioValue.setText("--");
        else
            tvAudioValue.setText(keyList.get(keyListCode.indexOf(selectedAudioLanguageCode)));

        String code = isSubtitleButtonSelected ? selectedSubtitleLanguageCode : selectedAudioLanguageCode;
        if (code != null && !code.equals(""))
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

                Map<String, String> orderedMap = MapUtil.sortByValue(languagesList.languages);

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

    public static class MapUtil {
        public static <K, V extends Comparable<? super V>> Map<K, V>
        sortByValue(Map<K, V> map) {
            List<Map.Entry<K, V>> list =
                    new LinkedList<Map.Entry<K, V>>(map.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
                public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                    return (o1.getValue()).compareTo(o2.getValue());
                }
            });

            Map<K, V> result = new LinkedHashMap<K, V>();
            for (Map.Entry<K, V> entry : list) {
                result.put(entry.getKey(), entry.getValue());
            }
            return result;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //We check if the language interface has changed. if not, we go back to the previous value
        String selectedMode = rbPolish.isChecked() ? Constants.LANGUAGE_POLISH :
                Constants.LANGUAGE_ENGLISH;
        User user = ((DreamTVApp) getApplication()).getUser();
        if (!user.interface_language.equals(selectedMode)) {
            LocaleHelper.setLocale(this, user.interface_language);
        }
    }
}
