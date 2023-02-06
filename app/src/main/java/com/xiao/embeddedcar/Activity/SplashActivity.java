/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xiao.embeddedcar.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.xiao.embeddedcar.R;


/**
 * Splash Activity that inflates splash activity xml.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    /**
     * Number of seconds to count down before showing the app open ad. This simulates the time needed
     * to load the app.
     */
    private static final long COUNTER_TIME = 1;

    private long secondsRemaining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Create a timer so the SplashActivity will be displayed for a fixed amount of time.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) createTimer();
        else startMainActivity();//Android版本大于11
    }

    /**
     * Create the countdown timer, which counts down to zero and show the app open ad.
     */
    @SuppressLint("SetTextI18n")
    private void createTimer() {
        final TextView counterTextView = findViewById(R.id.timer);

        CountDownTimer countDownTimer = new CountDownTimer(SplashActivity.COUNTER_TIME * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                secondsRemaining = ((millisUntilFinished / 1000) + 1);
                counterTextView.setText("App is done loading in: " + secondsRemaining);
            }

            @Override
            public void onFinish() {
                secondsRemaining = 0;
                counterTextView.setText("Done.");
                startMainActivity();
            }
        };
        countDownTimer.start();
    }

    /**
     * Start the MainActivity.
     */
    public void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
    }
}
