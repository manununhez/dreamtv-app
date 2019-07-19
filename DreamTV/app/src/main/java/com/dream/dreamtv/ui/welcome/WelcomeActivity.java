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
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.ui.home.HomeActivity;
import com.dream.dreamtv.utils.InjectorUtils;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        //        // Get the ViewModel from the factory
        WelcomeViewModelFactory factory = InjectorUtils.provideWelcomeViewModelFactory(this);
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

        String token = ((DreamTVApp) getApplication()).getToken();
        User user = ((DreamTVApp) getApplication()).getUser();
        if (token == null || user == null) //first time the app is initiated. The user has to select an account
            pickUserAccount();
        else
            login(user.email);


        LiveData<Resource<User>> userDetails = mViewModel.fetchUserDetails();
        userDetails.observe(this, userResource -> {
            if (userResource.status.equals(Resource.Status.LOADING)) {
                showProgress();
            } else if (userResource.status.equals(Resource.Status.SUCCESS)) {

                if (userResource.data != null) {
                    if (!userResource.data.subLanguage.equals(LocaleHelper.getLanguage(this))) {
                        if (DEBUG)
                            Log.d(TAG, "fetchUserDetails() response!: userResource.data.subLanguage=" + userResource.data.subLanguage + " LocaleHelper.getLanguage(this):" + LocaleHelper.getLanguage(this));

                        LocaleHelper.setLocale(this, userResource.data.subLanguage);

                    }

                    goHome();

                }
                dismissProgress();
            }
        });

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

        User user = ((DreamTVApp) getApplication()).getUser();

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
