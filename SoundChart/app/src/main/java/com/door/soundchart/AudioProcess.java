package com.door.soundchart;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author: Zi
 * @ref: https://www.linuxidc.com/Linux/2012-01/51770p2.htm
 */

public class AudioProcess {
    private ArrayList<short[]> inBuf = new ArrayList<short[]>(); // raw audio data
    private ArrayList<int[]> outBuf = new ArrayList<int[]>(); // processed data
    private boolean isRecording = false;
    private int length = 1024;
    private int half_len = 512;
    private int batchNum = 50;
    private int sampleRate = 44100;

    Context mContext;
    private int shift = 30;
    public int frequence = 0;

    //启动程序
    public void start() {
        isRecording = true;
        new RecordThread().start();
        new DrawThread().start();
    }

    //停止程序
    public void stop() {
        isRecording = false;
        inBuf.clear();
    }

    //录音线程
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
                    int[] power = new int[half_len];
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
                        synchronized (outBuf) {
                            Log.d("POWER:", Arrays.toString(power));
                            outBuf.add(power);
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
    class DrawThread extends Thread {
        public DrawThread() {

        }
            @SuppressWarnings("unchecked")
        public void run() {
            while (isRecording) {
                ArrayList<int[]>buf = new ArrayList<int[]>();
                synchronized (outBuf) {
                    if (outBuf.size() == 0) {
                        continue;
                    }
                    buf = (ArrayList<int[]>)outBuf.clone();
                    outBuf.clear();
                }
                // drawing with the processed data
                for(int i = 0; i < buf.size(); i++){
                    int[]tmpBuf = buf.get(i);
                    Log.d("OUTPUT", Arrays.toString(tmpBuf));
                    // TODO: SimpleDraw tmpBuf
                }
            }
        }
    }

}

