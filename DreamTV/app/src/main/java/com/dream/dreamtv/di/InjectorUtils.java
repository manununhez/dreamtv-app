package com.dream.dreamtv.di;

import android.content.Context;

import com.dream.dreamtv.data.local.prefs.AppPreferencesHelper;
import com.dream.dreamtv.data.networking.NetworkDataSourceImpl;
import com.dream.dreamtv.data.networking.VolleyController;
import com.dream.dreamtv.repository.AppRepository;
import com.dream.dreamtv.ViewModelFactory;
import com.dream.dreamtv.utils.AppExecutors;

/**
 * Provides static methods to inject the various classes needed for this weather application
 */
public class InjectorUtils {

    public static VolleyController provideVolley(Context context) {
        AppPreferencesHelper appPreferencesHelper = providePreferencesHelper(context);
        return new VolleyController(context, appPreferencesHelper);
    }

    public static AppRepository provideRepository(Context context) {
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