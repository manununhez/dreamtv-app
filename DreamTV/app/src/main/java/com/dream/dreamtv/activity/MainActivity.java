/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.dream.dreamtv.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import com.dream.dreamtv.R;
import com.dream.dreamtv.utils.Dpad;
import com.dream.dreamtv.utils.LocaleHelper;

import static android.content.ContentValues.TAG;


/*
 * MainActivity class that loads MainFragment
 */
public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    private Dpad mDpad;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDpad = new Dpad();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        // Check if this event if from a D-pad and process accordingly.
//        if (Dpad.isDpadDevice(event)) {
//
//            int press = mDpad.getDirectionPressed(event);
//            switch (press) {
//                case Dpad.DOWN:
//                    // Do something for LEFT direction press
//                    Toast.makeText(this, "DOWN", Toast.LENGTH_SHORT).show();
//
//                    return true;
//                case Dpad.LEFT:
//                    // Do something for LEFT direction press
//                    Toast.makeText(this, "LEFT", Toast.LENGTH_SHORT).show();
//
//                    return true;
//                case Dpad.RIGHT:
//                    // Do something for RIGHT direction press
//                    Toast.makeText(this, "RIGHT", Toast.LENGTH_SHORT).show();
//
//                    return true;
//                case Dpad.UP:
//                    // Do something for UP direction press
//                    Toast.makeText(this, "UP", Toast.LENGTH_SHORT).show();
//                    return true;
//                default:
//                    return super.onKeyDown(keyCode, event);
//
//            }
//        }
//
//        return false;
//
//    }
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        PlaybackVideoFragment playbackVideoFragment = (PlaybackVideoFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_MEDIA_PLAY:
//                playbackVideoFragment.togglePlayback(false);
//                return true;
//            case KeyEvent.KEYCODE_MEDIA_PAUSE:
//                playbackVideoFragment.togglePlayback(false);
//                return true;
//            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
//                if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
//                    playbackVideoFragment.togglePlayback(false);
//                } else {
//                    playbackVideoFragment.togglePlayback(true);
//                }
//                return true;
//            default:
//                return super.onKeyUp(keyCode, event);
//        }
//    }


//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//
//        // You should make a constant instead of hard code number 3.
//        if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == 3) {
//            Toast.makeText(this, "Hello, you just press BACK", Toast.LENGTH_LONG).show();
//
//        }
//        return true;
//    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_DPAD_CENTER:
//                Log.d(this.getClass().getName(), "KEYCODE_DPAD_CENTER - onKeyDown");
//                return true;
//
//            default:
//                return super.onKeyDown(keyCode, event);
//
//        }
//    }


//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//
//        switch (keyCode) {
//
//            case KeyEvent.KEYCODE_DPAD_CENTER:
//                Log.d(this.getClass().getName(), "KEYCODE_DPAD_CENTER - onKeyUp");
//                // Do something...
//
//                return true;
//            case KeyEvent.KEYCODE_DPAD_DOWN:
//                Log.d(this.getClass().getName(), "KEYCODE_DPAD_DOWN");
//                // Do something...
//
//                return true;
//            case KeyEvent.KEYCODE_DPAD_DOWN_LEFT:
//                Log.d(this.getClass().getName(), "KEYCODE_DPAD_DOWN_LEFT");
//                // Do something...
//
//                return true;
//            case KeyEvent.KEYCODE_DPAD_DOWN_RIGHT:
//                Log.d(this.getClass().getName(), "KEYCODE_DPAD_DOWN_RIGHT");
//                // Do something...
//
//                return true;
//            case KeyEvent.KEYCODE_DPAD_LEFT:
//                Log.d(this.getClass().getName(), "KEYCODE_DPAD_LEFT");
//                // Do something...
//
//                return true;
//            case KeyEvent.KEYCODE_DPAD_RIGHT:
//                Log.d(this.getClass().getName(), "KEYCODE_DPAD_RIGHT");
//                // Do something...
//
//                return true;
//            case KeyEvent.KEYCODE_DPAD_UP:
//                Log.d(this.getClass().getName(), "KEYCODE_DPAD_UP");
//                // Do something...
//
//                return true;
//            case KeyEvent.KEYCODE_DPAD_UP_LEFT:
//                Log.d(this.getClass().getName(), "KEYCODE_DPAD_UP_LEFT");
//                // Do something...
//
//                return true;
//            case KeyEvent.KEYCODE_DPAD_UP_RIGHT:
//                Log.d(this.getClass().getName(), "KEYCODE_DPAD_UP_RIGHT");
//                // Do something...
//
//                return true;
//            case KeyEvent.KEYCODE_VOICE_ASSIST:
//                Log.d(this.getClass().getName(), "KEYCODE_VOLUME_DOWN");
//                // Do something...
//
//                return true;
//            case KeyEvent.KEYCODE_VOLUME_UP:
//                Log.d(this.getClass().getName(), "KEYCODE_VOLUME_UP");
//                // Do something...
//
//                return true;
//
//            default:
//                return super.onKeyUp(keyCode, event);
//        }
//    }

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//
//        int action = event.getAction();
//        int keyCode = event.getKeyCode();
//
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_DPAD_CENTER:
//                if (action == KeyEvent.ACTION_UP) {
//                    Log.d(this.getClass().getName(), "KEYCODE_DPAD_CENTER - dispatchKeyEvent");
//                    return true;
//                }
//            default:
//                return super.dispatchKeyEvent(event);
//        }
//    }


//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        return super.dispatchKeyEvent(event);
//
//    }


//    @Override
//    public boolean dispatchGenericMotionEvent(MotionEvent event) {
//        // Check if this event if from a D-pad and process accordingly.
//        if (Dpad.isDpadDevice(event)) {
//
//            int press = mDpad.getDirectionPressed(event);
//            switch (press) {
//                case Dpad.DOWN:
//                    // Do something for LEFT direction press
//                    Toast.makeText(this, "DOWN", Toast.LENGTH_SHORT).show();
//
//                    return true;
//                case Dpad.LEFT:
//                    // Do something for LEFT direction press
//                    Toast.makeText(this, "LEFT", Toast.LENGTH_SHORT).show();
//
//                    return true;
//                case Dpad.RIGHT:
//                    // Do something for RIGHT direction press
//                    Toast.makeText(this, "RIGHT", Toast.LENGTH_SHORT).show();
//
//                    return true;
//                case Dpad.UP:
//                    // Do something for UP direction press
//                    Toast.makeText(this, "UP", Toast.LENGTH_SHORT).show();
//                    return true;
//                default:
//                    return super.dispatchGenericMotionEvent(event);
//
//            }
//        }
//
//        return false;
//
//    }

//    @Override
//    public boolean onGenericMotionEvent(MotionEvent event) {
//        // Check if this event if from a D-pad and process accordingly.
//        if (Dpad.isDpadDevice(event)) {
//
//            int press = mDpad.getDirectionPressed(event);
//            switch (press) {
//                case Dpad.DOWN:
//                    // Do something for LEFT direction press
//                    Toast.makeText(this, "DOWN", Toast.LENGTH_SHORT).show();
//
//                    return true;
//                case Dpad.LEFT:
//                    // Do something for LEFT direction press
//                    Toast.makeText(this, "LEFT", Toast.LENGTH_SHORT).show();
//
//                    return true;
//                case Dpad.RIGHT:
//                    // Do something for RIGHT direction press
//                    Toast.makeText(this, "RIGHT", Toast.LENGTH_SHORT).show();
//
//                    return true;
//                case Dpad.UP:
//                    // Do something for UP direction press
//                    Toast.makeText(this, "UP", Toast.LENGTH_SHORT).show();
//                    return true;
//                default:
//                    return super.onGenericMotionEvent(event);
//
//            }
//        }
//
//        return false;
//
//
//        // Check if this event is from a joystick movement and process accordingly.
//
//    }


//    @Override
//    public boolean onGenericMotionEvent(MotionEvent event) {
//        return super.onGenericMotionEvent(event);
//    }
}
