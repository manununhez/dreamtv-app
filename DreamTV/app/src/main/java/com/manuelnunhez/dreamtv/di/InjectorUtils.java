package com.manuelnunhez.dreamtv.di;

import android.content.Context;

import com.manuelnunhez.dreamtv.data.local.prefs.AppPreferencesHelper;
import com.manuelnunhez.dreamtv.data.networking.NetworkDataSourceImpl;
import com.manuelnunhez.dreamtv.data.networking.VolleyController;
import com.manuelnunhez.dreamtv.repository.AppRepository;
import com.manuelnunhez.dreamtv.ViewModelFactory;
import com.manuelnunhez.dreamtv.utils.AppExecutors;

/**
 * Provides static methods to inject the various classes needed for this weather application
 */
public class InjectorUtils {

    private static VolleyController provideVolley(Context context) {
        AppPreferencesHelper appPreferencesHelper = providePreferencesHelper(context);
        return new VolleyController(context, appPreferencesHelper);
    }

    private static AppRepository provideRepository(Context context) {
        AppExecutors executors = AppExecutors.getInstance();
        VolleyController volley = provideVolley(context);
        NetworkDataSourceImpl networkDataSource = NetworkDataSourceImpl.getInstance(context.getApplicationContext(),
                executors, volley);
        AppPreferencesHelper preferencesHelper = providePreferencesHelper(context);

        return AppRepository.getInstance(networkDataSource, executors, preferencesHelper);
    }

    private static AppPreferencesHelper providePreferencesHelper(Context context) {
        return new AppPreferencesHelper(context);
    }

    public static ViewModelFactory provideViewModelFactory(Context context) {
        AppRepository repository = provideRepository(context.getApplicationContext());
        return new ViewModelFactory(repository);
    }

}