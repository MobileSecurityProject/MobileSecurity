package com.door.soundchart;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class NoiseJammingService extends Service {

    MediaPlayer white_noise;
    boolean is_blocked;
    boolean is_jamming = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("NoiseJammingService ", "onStartCommand received start id " + startId + ": " + intent);

        boolean is_blocked = intent.getBooleanExtra("is_blocked", false);
        if (!is_blocked && !is_jamming) {
            white_noise = MediaPlayer.create(this, R.raw.white_5s);
            // play the white noise for 2 seconds
            white_noise.start();
            is_jamming = true;
            CountDownTimer cntr_aCounter = new CountDownTimer(2000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    //code fire after finish
                    white_noise.stop();
                    white_noise.reset();
                    is_jamming = false;
                }
            };cntr_aCounter.start();

            Log.d("NoiseJammingService", "white noise on play");
        } else {
            Log.d("NoiseJammingService ", "is blocked");
        }

        // the service doesn't restart
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        Log.d("NoiseJammingService", "on destroy");
        is_jamming = false;
        white_noise.stop();
        white_noise.reset();
        super.onDestroy();
    }
}
