package com.dream.dreamtv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.dream.dreamtv.activity.MainActivity;


public class SplashScreen extends AppCompatActivity {

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
                DreamTVApp.Logger.e("ocurrio un error");
            }

            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
//            intent.putExtra("tryLogin", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            SplashScreen.this.startActivity(intent);
            SplashScreen.this.overridePendingTransition(0, 0);
            SplashScreen.this.finish();
        }
    }
}
