package com.dream.dreamtv.ui.Preferences;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;
import androidx.leanback.preference.LeanbackSettingsFragmentCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.User;

import java.util.Objects;

import static com.dream.dreamtv.utils.Constants.PREF_ABR_POLISH;
import static com.dream.dreamtv.utils.Constants.PREF_SUBTITLE_SMALL_SIZE;
import static com.dream.dreamtv.utils.Constants.PREF_VIDEO_DURATION_MIN_5;


public class PreferencesFragment extends LeanbackSettingsFragmentCompat {

    @Override
    public void onPreferenceStartInitialScreen() {
        startPreferenceFragment(new PrefFragment());
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        final Bundle args = pref.getExtras();
        final Fragment f = getChildFragmentManager().getFragmentFactory().instantiate(
                requireActivity().getClassLoader(), pref.getFragment());
        f.setArguments(args);
        f.setTargetFragment(caller, 0);
        if (f instanceof PreferenceFragmentCompat
                || f instanceof PreferenceDialogFragmentCompat) {
            startPreferenceFragment(f);
        } else {
            startImmersiveFragment(f);
        }
        return true;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller,
                                           PreferenceScreen pref) {
        final Fragment fragment = new PrefFragment();
        final Bundle args = new Bundle(1);
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, pref.getKey());
        fragment.setArguments(args);
        startPreferenceFragment(fragment);
        return true;
    }


    /**
     * The fragment that is embedded in SettingsFragment
     */
    public static class PrefFragment extends LeanbackPreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            // Load the preferences from an XML resource
            setPreferencesFromResource(R.xml.preferences, rootKey);

            User user = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication()).getUser();

            if (user != null) {
                //TODO set settings value from Shared USER
                ListPreference listAppLanguagePreference = findPreference(getString(R.string.pref_key_list_app_languages));
                if (listAppLanguagePreference != null) {
                    listAppLanguagePreference.setValue(user.interfaceLanguage);
                }

                ListPreference listInterfaceModePreference = findPreference(getString(R.string.pref_key_list_interface_mode));
                if (listInterfaceModePreference != null) {
                    listInterfaceModePreference.setValue(user.interfaceMode);
                }

                ListPreference listAudioPreference = findPreference(getString(R.string.pref_key_list_audio_languages));
                if (listAudioPreference != null) {
                    listAudioPreference.setValue(user.audioLanguage);
                }

                ListPreference listSubtitlePreference = findPreference(getString(R.string.pref_key_list_subtitle_languages));
                if (listSubtitlePreference != null) {
                    listSubtitlePreference.setValue(user.subLanguage);
                }

            }


        }

    }


}