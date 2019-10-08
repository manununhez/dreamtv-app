package com.manuelnunhez.dreamtv.ui.preferences;


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

import com.manuelnunhez.dreamtv.R;
import com.manuelnunhez.dreamtv.ViewModelFactory;
import com.manuelnunhez.dreamtv.data.model.User;
import com.manuelnunhez.dreamtv.di.InjectorUtils;


public class AppPreferencesFragment extends LeanbackSettingsFragmentCompat {

    private static AppPreferencesViewModel mViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewModelFactory factory = InjectorUtils.provideViewModelFactory(requireActivity());
        mViewModel = ViewModelProviders.of(this, factory).get(AppPreferencesViewModel.class);
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
            setPreferencesFromResource(R.xml.app_preferences, rootKey);

            User user = mViewModel.getUser();

            if (user != null) {
                //TODO set settings value from Shared USER
                ListPreference listAppLanguagePreference = findPreference(getString(R.string.pref_key_list_app_languages));
                if (listAppLanguagePreference != null) {
                    listAppLanguagePreference.setValue(user.getSubLanguage());
                }

                ListPreference listInterfaceModePreference = findPreference(getString(R.string.pref_key_list_interface_mode));
                if (listInterfaceModePreference != null) {
                    listInterfaceModePreference.setValue(user.getInterfaceMode());
                }

            }


        }

    }


}