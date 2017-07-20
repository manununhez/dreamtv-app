package com.dream.dreamtv.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.dream.dreamtv.utils.CheckableTextView;
import com.dream.dreamtv.utils.JsonUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PreferencesActivity extends Activity {

    private Context mContext;
    private Activity mActivity;

    private RelativeLayout mRelativeLayout;
    private ListView mListView;
    private TextView mTextView;
    private TextView tvSubtitleValue;
    private TextView tvAudioValue;
    //    private List<String> selectedLanguagesCode;
    private List<String> keyList;
    private List<String> keyListCode;
    private CheckableTextView btnSubtitle;
    private CheckableTextView btnAudio;
    private String selectedSubtitleLanguageCode;
    private String selectedAudioLanguageCode;


    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;

    //    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    //    private PlayButton   mPlayButton = null;
    private MediaPlayer mPlayer = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};


    boolean mStartPlaying = true;
    boolean mStartRecording = true;
    Button btnPlay;
    Button btnRecord;
    Button btnStop;


    private TextView voiceInput;
    private final int REQ_CODE_SPEECH_INPUT = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Get the application context
        mContext = getApplicationContext();

        // Get the activity
        mActivity = PreferencesActivity.this;

        // Get the widgets reference from XML layout
        mRelativeLayout = (RelativeLayout) findViewById(R.id.rl);
        mListView = (ListView) findViewById(R.id.lv);

        mTextView = (TextView) findViewById(R.id.tv);
        tvSubtitleValue = (TextView) findViewById(R.id.tvSubtitleValue);
        tvAudioValue = (TextView) findViewById(R.id.tvAudioValue);

        btnSubtitle = (CheckableTextView) findViewById(R.id.btnSubtitle);
        btnAudio = (CheckableTextView) findViewById(R.id.btnAudio);

        voiceInput = (TextView) findViewById(R.id.voiceInput);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnRecord = (Button) findViewById(R.id.btnRecord);
        btnStop = (Button) findViewById(R.id.btnStop);


        btnSubtitle.setOnCheckedChangeListener(new CheckableTextView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckableTextView view, boolean isChecked) {
                if (isChecked) {
                    btnAudio.setChecked(false);
                    checkTemporaryUserData(true);
                }
            }
        });

        btnAudio.setOnCheckedChangeListener(new CheckableTextView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckableTextView view, boolean isChecked) {
                if (isChecked) {
                    btnSubtitle.setChecked(false);
                    checkTemporaryUserData(false);
                }
            }
        });

        Button btnSave = (Button) findViewById(R.id.btnSave);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                DreamTVApp.Logger.d("Codes:" + selectedLanguagesCode.toString());
                Toast.makeText(mContext, "Save", Toast.LENGTH_SHORT).show();
                updateUserData();
            }
        });

        getLanguages();


        audioConfig();

    }

    private void audioConfig(){
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    btnRecord.setText("Stop recording");
                } else {
                    btnRecord.setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                onPlay(mStartPlaying);
//                if (mStartPlaying) {
//                    btnPlay.setText("Stop playing");
//                } else {
//                    btnPlay.setText("Start playing");
//                }
//                mStartPlaying = !mStartPlaying;

                promptSpeechInput();
            }
        });


        // Record to the external cache directory for visibility
//        audiofile = File.createTempFile("sound", ".3gp", sampleDir);

        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";

    }
    private void updateUserData() {
        User user = new User();
        user.sub_language = selectedSubtitleLanguageCode;
        user.audio_language = selectedAudioLanguageCode;
//        user.languages = Arrays.toString(selectedLanguagesCode.toArray(new String[selectedLanguagesCode.size()]));

        final String jsonRequest = JsonUtils.getJsonRequest(this, user);

        ResponseListener responseListener = new ResponseListener(this, true, true, "Saving user data...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                User user = gson.fromJson(response, User.class);

                ((DreamTVApp) getApplication()).setUser(user);

                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
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
                android.R.layout.simple_list_item_multiple_choice,
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

//                selectedLanguagesCode = new ArrayList<String>();

//                if (checked) {
//                    // If the current item is checked
//                    int key = clickedItemPositions.keyAt(index);
//                    String item = (String) mListView.getItemAtPosition(key);
                String item = (String) mListView.getItemAtPosition(i);


                // Display the checked items on TextView
//                        mTextView.setText(mTextView.getText() + item + " | ");

                if (btnSubtitle.isChecked()) {
                    selectedSubtitleLanguageCode = keyListCode.get(keyList.indexOf(item));
                    tvSubtitleValue.setText(keyList.get(keyListCode.indexOf(selectedSubtitleLanguageCode)));
                }

                if (btnAudio.isChecked()) {
                    selectedAudioLanguageCode = keyListCode.get(keyList.indexOf(item));
                    tvAudioValue.setText(keyList.get(keyListCode.indexOf(selectedAudioLanguageCode)));
                }

//                        selectedLanguagesCode.add(keyListCode.get(keyList.indexOf(item)));
//                }
//                for (int index = 0; index < clickedItemPositions.size(); index++) {
//                    // Get the checked status of the current item
//                    boolean checked = clickedItemPositions.valueAt(index);
//
//                    if (checked) {
//                        // If the current item is checked
//                        int key = clickedItemPositions.keyAt(index);
//                        String item = (String) mListView.getItemAtPosition(key);
//
//
//                        // Display the checked items on TextView
////                        mTextView.setText(mTextView.getText() + item + " | ");
//
//                        if (btnSubtitle.isChecked())
//                            selectedSubtitleLanguageCode = keyListCode.get(keyList.indexOf(item));
//
//                        if (btnAudio.isChecked())
//                            selectedAudioLanguageCode = keyListCode.get(keyList.indexOf(item));
//
////                        selectedLanguagesCode.add(keyListCode.get(keyList.indexOf(item)));
//                    }
//                }
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
//        la primera vez comprobamos los items ya seleccionados por el usuario
//        User user = ((DreamTVApp) getApplication()).getUser();

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
                "Retrieving languages...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                Languages languagesList = gson.fromJson(response, Languages.class);

                languagesList.languages.put("NN", "- None"); //null option in the list

                Map<String, String> orderedMap = MapUtil.sortByValue(languagesList.languages);

                keyList = new ArrayList<String>(orderedMap.values());
                keyListCode = new ArrayList<String>(orderedMap.keySet());

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




//    --------- AUDIO RECORD

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }


    /**
     * Showing google speech input dialog
     * */
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
            Toast.makeText(getApplicationContext(),
                    "speech_not_supported",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    voiceInput.setText(result.get(0));
                }
                break;
            }

        }
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setOutputFile(mFileName);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }


    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}
