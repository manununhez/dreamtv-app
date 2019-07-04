package com.dream.dreamtv.ui.preferences;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;
import androidx.leanback.preference.LeanbackSettingsFragmentCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.User;

import java.util.Objects;


public class SubtitlePreferencesFragment extends LeanbackSettingsFragmentCompat {

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
            setPreferencesFromResource(R.xml.subtitle_preferences, rootKey);

//            pref_key_list_subtitle_languages
//            pref_key_subtitle_size
            User user = ((DreamTVApp) Objects.requireNonNull(getActivity()).getApplication()).getUser();

            if (user != null) {
                ListPreference listSubtitlePreference = findPreference(getString(R.string.pref_key_list_subtitle_languages));
                if (listSubtitlePreference != null) {
                    listSubtitlePreference.setValue(user.subLanguage);
                }

            }


        }

    }


}