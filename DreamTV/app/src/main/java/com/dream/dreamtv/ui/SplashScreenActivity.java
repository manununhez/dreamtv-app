package com.dream.dreamtv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;


public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
            SplashScreenActivity.this.startActivity(intent);
            SplashScreenActivity.this.overridePendingTransition(0, 0);
            SplashScreenActivity.this.finish();
        }
    }
}
