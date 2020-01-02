package com.dreamproject.dreamtv.di;

import android.content.Context;

import com.dreamproject.dreamtv.data.local.prefs.AppPreferencesHelper;
import com.dreamproject.dreamtv.data.networking.NetworkDataSourceImpl;
import com.dreamproject.dreamtv.data.networking.VolleyController;
import com.dreamproject.dreamtv.repository.AppRepository;
import com.dreamproject.dreamtv.ViewModelFactory;
import com.dreamproject.dreamtv.utils.AppExecutors;

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