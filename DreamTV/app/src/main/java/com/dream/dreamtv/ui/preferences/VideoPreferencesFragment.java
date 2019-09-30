package com.dream.dreamtv.ui.preferences;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;
import androidx.leanback.preference.LeanbackSettingsFragmentCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.dream.dreamtv.R;
import com.dream.dreamtv.ViewModelFactory;
import com.dream.dreamtv.data.model.User;
import com.dream.dreamtv.di.InjectorUtils;


public class VideoPreferencesFragment extends LeanbackSettingsFragmentCompat {

    private static PreferencesViewModel mViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewModelFactory factory = InjectorUtils.provideViewModelFactory(requireActivity());
        mViewModel = ViewModelProviders.of(this, factory).get(PreferencesViewModel.class);
    }

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
            setPreferencesFromResource(R.xml.video_preferences, rootKey);

            User user = mViewModel.getUser();

//            pref_key_video_duration
//            pref_key_list_audio_languages

            if (user != null) {
                ListPreference listAudioPreference = findPreference(getString(R.string.pref_key_list_audio_languages));
                if (listAudioPreference != null) {
                    listAudioPreference.setValue(user.getAudioLanguage());
                }
            }


        }

    }


}