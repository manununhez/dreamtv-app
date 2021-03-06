package com.dreamproject.dreamtv.ui.welcome;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.dreamproject.dreamtv.R;
import com.dreamproject.dreamtv.ViewModelFactory;
import com.dreamproject.dreamtv.data.model.Resource;
import com.dreamproject.dreamtv.data.model.Resource.Status;
import com.dreamproject.dreamtv.data.model.User;
import com.dreamproject.dreamtv.di.InjectorUtils;
import com.dreamproject.dreamtv.ui.home.HomeActivity;
import com.dreamproject.dreamtv.utils.LocaleHelper;
import com.google.android.gms.common.AccountPicker;
import com.google.firebase.analytics.FirebaseAnalytics;

import timber.log.Timber;

import static com.dreamproject.dreamtv.utils.Constants.FIREBASE_KEY_EMAIL;
import static com.dreamproject.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_LOGIN;


public class WelcomeActivity extends FragmentActivity {
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
        mViewModel = new ViewModelProvider(this, factory).get(WelcomeViewModel.class);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        progressLoading = findViewById(R.id.pbLoading);

        new Handler().postDelayed(this::userRegistration, 2000);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (userDetailsLiveData != null)
            userDetailsLiveData.removeObservers(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
                Timber.d("onActivityResult() - Result from pickAccount()");

                // Receiving a result from the AccountPicker
                login(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
            }
        } else {
            finish();
        }
    }

    private void showProgress() {
        progressLoading.setVisibility(View.VISIBLE);
    }

    private void dismissProgress() {
        progressLoading.setVisibility(View.GONE);
    }

    private void userRegistration() {

        Timber.d("userRegistration()");

        String accessToken = mViewModel.getAccessToken();
        User user = mViewModel.getUser();
        if (accessToken == null || user == null) //first time the app is initiated. The user has to select an account
            pickUserAccount();
        else
            login(user.getEmail());


        userDetailsLiveData = mViewModel.fetchUserDetails();
        userDetailsLiveData.removeObservers(this);
        userDetailsLiveData.observe(this, userResource -> {
            Status status = userResource.status;
            User data = userResource.data;

            if (status.equals(Status.LOADING)) {
                showProgress();
            } else if (status.equals(Status.SUCCESS)) {

                if (data != null) {
                    if (!data.getSubLanguage().equals(LocaleHelper.getLanguage(this))) {

                        Timber.d("fetchUserDetails() response!: userResource.data.subLanguage=" + data.getSubLanguage() + " LocaleHelper.getLanguage(this):" + LocaleHelper.getLanguage(this));

                        LocaleHelper.setLocale(this, data.getSubLanguage());
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
        Timber.d("pickUserAccount()");

        /*This will list all available accounts on device without any filtering*/

        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                null, false, null, null, null, null);

        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    private void firebaseLoginEvents() {

        User user = mViewModel.getUser();

        Bundle bundle = new Bundle();

        String email = (user == null || user.getEmail() == null) ? "" : user.getEmail();

        bundle.putString(FIREBASE_KEY_EMAIL, email);

        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_LOGIN, bundle);
    }

    private void login(String email) {
        Timber.d(">>>REQUEST LOGIN");

        mViewModel.login(email, email); //TODO change password

        firebaseLoginEvents();
    }


}
