package com.dream.dreamtv.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dream.dreamtv.R;
import com.dream.dreamtv.ui.Main.MainActivity;



public class SplashScreenActivity extends Activity {
    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);
        SplashWaiter launcher = new SplashWaiter();
        launcher.start();
    }

    private class SplashWaiter extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(3 * 1000);
            } catch (Exception e) {
                Log.d(TAG, "An error occurred");
            }

            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
    }
}
