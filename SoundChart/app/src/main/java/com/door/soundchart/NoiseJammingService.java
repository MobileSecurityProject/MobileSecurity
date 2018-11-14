package com.door.soundchart;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class NoiseJammingService extends Service {

    MediaPlayer white_noise;
    boolean is_blocked;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("NoiseJammingService ", "onStartCommand received start id " + startId + ": " + intent);

        boolean is_blocked = intent.getBooleanExtra("is_blocked", false);
        if (!is_blocked) {
            // play the white noise
            white_noise = MediaPlayer.create(this, R.raw.white_5s);
            white_noise.start();
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
        white_noise.stop();
        white_noise.reset();
        super.onDestroy();
    }
}

