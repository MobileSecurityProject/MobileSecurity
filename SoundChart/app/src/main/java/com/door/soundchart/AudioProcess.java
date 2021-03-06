package com.door.soundchart;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.charting.visuals.SciChartSurface;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @ref: https://www.linuxidc.com/Linux/2012-01/51770p2.htm
 */

public class AudioProcess {
    private ArrayList<short[]> inBuf = new ArrayList<short[]>(); // raw audio data
    private ArrayList<double[]> outBuf = new ArrayList<double[]>(); // processed data
    private boolean isRecording = false;
    private int length = 1024;
    private int half_len = 512;
    private int batchNum = 50;
    private int sampleRate = 44100;
    private XyDataSeries lineData;
    private SciChartSurface surface;
    private Handler mHandler;
    public static final int DETECT_ULTRASOUND = 100;
    public static final int NONE_DETECT = 200;

    private int x = 0;
    public AudioProcess(SciChartSurface surface, XyDataSeries lineData, Handler mHandler)
    {
        this.surface = surface;
        this.lineData = lineData;
        this.mHandler = mHandler;
    }

    private int shift = 30;
    public int frequence = 0;

    public void start() {
        isRecording = true;
        new RecordThread().start();
    }

    public void stop() {
        isRecording = false;
        inBuf.clear();
    }


    // a lot of hard coded stuffs...
    public void identifyHighFreq(double[] data) {
        int cnt = 0;
        for (int i = 0; i < 112; i++) {
            if (data[i] >= 2000000000) {
                cnt++;
            }
        }

        if (cnt > 50) {

            Message msg = new Message();
            msg.what = DETECT_ULTRASOUND;
            msg.obj = "DETECT SUCCESSFULLY";
            mHandler.sendMessage(msg);
        } else {
            Message msg = new Message();
            msg.what = NONE_DETECT;
            msg.obj = "NOTHING DETECTED";
            mHandler.sendMessage(msg);
        }
    }



    class RecordThread extends Thread {
        private AudioRecord audioRecord;
        private FFT convert;
        int resultOfRead;

        public RecordThread() {
            this.audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    10 * AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            );
            this.convert = new FFT(length);
            resultOfRead = 0;
        }

        public void run() {
            try {
                short[] buffer = new short[length];
                audioRecord.startRecording();
                while (isRecording) {
                    // fft batch
                    double[] power = new double[half_len];
                    boolean isDataReady = false;
                    for (int i = 0; i < batchNum; i++) {
                        resultOfRead = audioRecord.read(buffer, 0, length); // read blocking
                        Log.d("ReadLen:", String.valueOf(resultOfRead));
                        if (resultOfRead > 0) {
                            isDataReady = true;
                        }

                        synchronized (inBuf) {
                            inBuf.add(buffer);
                        }
                        short[] tmpBuf = new short[length];
                        System.arraycopy(buffer, 0, tmpBuf, 0, length);

                        double[] x = new double[length];
                        double[] y = new double[length];
                        for (int j = 0; j < length; j++) {
                            x[j] = (double) tmpBuf[j];
                        }
                        convert.fft(x, y);
                        for (int j = 0; j < half_len; j++) {
                            power[j] += x[j + half_len] * x[j + half_len] + y[j + half_len] * y[j + half_len];
                        }

                        if (isDataReady) {
                            break;
                        }
                    }

                    if (isDataReady) {
                        synchronized (power) {
//                            for (int j = 0; j < half_len; j++) {
//                                power[j] = Math.log(power[j]);
//                            }
                            Log.d("POWER:", Arrays.toString(power));
                            outBuf.add(power);
                            x = 0;
                            for (int i = 511; i >= 0; i--) {
                                synchronized (lineData){
                                    lineData.append(x,  power[i]);
                                    ++x;
                                }
                            }
                            synchronized (lineData){
                                lineData.append(512, Double.NaN);
                            }
                            identifyHighFreq(power);
                        }
                    }

                }
                audioRecord.stop();
            } catch (Exception e) {
                // TODO: handle exception
                Log.i("Exception", e.toString());
            }

        }
    }

    // Drawing process
    class DetectThread extends Thread {

        public DetectThread() {

        }
        private int x = 0;
//        @SuppressWarnings("unchecked")
//        public void run() {
//
//                lineData.append(x, Math.sin(x * 0.1));
//                ArrayList<int[]>buf = new ArrayList<int[]>();
//                synchronized (outBuf) {
//                    if (outBuf.size() == 0) {
//                        continue;
//                    }
//                    buf = (ArrayList<int[]>)outBuf.clone();
//                    outBuf.clear();
//                }
//                // drawing with the processed data
//                for(int i = 0; i < buf.size(); i++){
//                    int[]tmpBuf = buf.get(i);
//                    Log.d("OUTPUT", Arrays.toString(tmpBuf));
//                    // TODO: SimpleDraw tmpBuf
//                }
//                ++x;
//
//            UpdateSuspender.using(surface, new Runnable() {
//                @Override
//                public void run() {
////                    lineData.append(x, Math.sin(x * 0.1));
//
//                    // Zoom series to fit the
////                    Log.d("Draw:", "DRAWWWWWW");
////                    synchronized (lineData){
////                        surface.zoomExtents();
////                    }
////                    ++x;
//                }
//            });

//        }
    }

}

