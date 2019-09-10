package com.dream.dreamtv.ui.welcome;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.dream.dreamtv.BuildConfig;
import com.dream.dreamtv.R;
import com.dream.dreamtv.ViewModelFactory;
import com.dream.dreamtv.data.model.api.Resource;
import com.dream.dreamtv.data.model.api.Resource.Status;
import com.dream.dreamtv.data.model.api.User;
import com.dream.dreamtv.di.InjectorUtils;
import com.dream.dreamtv.ui.home.HomeActivity;
import com.dream.dreamtv.utils.LocaleHelper;
import com.google.android.gms.common.AccountPicker;
import com.google.firebase.analytics.FirebaseAnalytics;

import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_EMAIL;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_PASSWORD;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_LOGIN;


public class WelcomeActivity extends FragmentActivity {
    private static final String TAG = WelcomeActivity.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final int REQUEST_CODE_PICK_ACCOUNT = 45687;

    private ProgressBar progressLoading;
    private FirebaseAnalytics mFirebaseAnalytics;
    private WelcomeViewModel mViewModel;
    private LiveData<Resource<User>> userDetailsLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        // Get the ViewModel from the factory
        ViewModelFactory factory = InjectorUtils.provideViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, factory).get(WelcomeViewModel.class);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        progressLoading = findViewById(R.id.pbLoading);

        new Handler().postDelayed(this::userRegistration, 2000);

    }


    private void showProgress() {
        progressLoading.setVisibility(View.VISIBLE);
    }

    private void dismissProgress() {
        progressLoading.setVisibility(View.GONE);
    }

    private void userRegistration() {

        if (DEBUG) Log.d(TAG, "userRegistration()");

        String accessToken = mViewModel.getAccessToken();
        User user = mViewModel.getUser();
        if (accessToken == null || user == null) //first time the app is initiated. The user has to select an account
            pickUserAccount();
        else
            login(user.email);


        userDetailsLiveData = mViewModel.fetchUserDetails();
        userDetailsLiveData.removeObservers(this);
        userDetailsLiveData.observe(this, userResource -> {
            Status status = userResource.status;
            User data = userResource.data;

            if (status.equals(Status.LOADING)) {
                showProgress();
            } else if (status.equals(Status.SUCCESS)) {

                if (data != null) {
                    if (!data.subLanguage.equals(LocaleHelper.getLanguage(this))) {
                        if (DEBUG)
                            Log.d(TAG, "fetchUserDetails() response!: userResource.data.subLanguage=" + data.subLanguage + " LocaleHelper.getLanguage(this):" + LocaleHelper.getLanguage(this));

                        LocaleHelper.setLocale(this, data.subLanguage);
                    }

                    goHome();
                }

                dismissProgress();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        userDetailsLiveData.removeObservers(this);
    }

    private void goHome() {
        Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void pickUserAccount() {
        if (DEBUG) Log.d(TAG, "pickUserAccount()");

        /*This will list all available accounts on device without any filtering*/

        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                null, false, null, null, null, null);

        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }


    private void firebaseLoginEvents(String logEventName) {

        User user = mViewModel.getUser();

        Bundle bundle = new Bundle();

        String email = (user == null || user.email == null) ? "" : user.email;
        String password = (user == null || user.password == null) ? "" : user.password;

        if (FIREBASE_LOG_EVENT_LOGIN.equals(logEventName)) {
            bundle.putString(FIREBASE_KEY_EMAIL, email);
            bundle.putString(FIREBASE_KEY_PASSWORD, password);
        }

        mFirebaseAnalytics.logEvent(logEventName, bundle);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
                if (DEBUG) Log.d(TAG, "onActivityResult() - Result from pickAccount()");

                // Receiving a result from the AccountPicker
                login(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
            }
        } else {
            finish();
        }
    }

    private void login(String email) {
        if (DEBUG) Log.d(TAG, ">>>REQUEST LOGIN");

        mViewModel.login(email, email); //TODO change password

        firebaseLoginEvents(FIREBASE_LOG_EVENT_LOGIN);
    }


}
