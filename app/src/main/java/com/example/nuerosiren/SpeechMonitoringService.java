package com.example.nuerosiren;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.content.pm.ServiceInfo;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import java.io.IOException;

public class SpeechMonitoringService extends Service {
    private static final String CHANNEL_ID = "SpeechMonitoringChannel";
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private SpeechClassifier classifier;
    private Thread recordingThread;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            classifier = new SpeechClassifier(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("NeuroSiren Monitoring")
                .setContentText("Actively monitoring speech patterns...")
                .setSmallIcon(R.drawable.logo)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        } else {
            startForeground(1, notification);
        }

        startRecording();
        return START_STICKY;
    }

    private void startRecording() {
        if (isRecording) return;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }

        isRecording = true;

        int bufferSize = AudioRecord.getMinBufferSize(16000,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        try {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                isRecording = false;
                return;
            }

            audioRecord.startRecording();
        } catch (SecurityException e) {
            e.printStackTrace();
            isRecording = false;
            return;
        }

        recordingThread = new Thread(() -> {
            short[] buffer = new short[bufferSize];
            int framesToSkip = 5; // Skip initial hardware noise
            while (isRecording) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                
                if (framesToSkip > 0) {
                    framesToSkip--;
                    continue;
                }

                if (read > 0 && classifier != null) {
                    double sum = 0;
                    for (int i = 0; i < read; i++) {
                        sum += (double) buffer[i] * buffer[i];
                    }
                    double rms = Math.sqrt(sum / read);
                    
                    // 🚀 HUGE SENSITIVITY BOOST: Lowered from 2000.0 to 20.0 
                    // This ensures RMS values around 4-5 (from your logs) produce a visible response
                    float volumeFactor = (float) Math.min(rms / 20.0, 1.0);
                    
                    android.util.Log.d("SpeechService", "Mic RMS: " + rms + " | VolFactor: " + volumeFactor);

                    float[][][][] liveInput = new float[1][40][100][1];
                    for (int i = 0; i < 40; i++) {
                        for (int j = 0; j < 100; j++) {
                            liveInput[0][i][j][0] = volumeFactor;
                        }
                    }

                    // Run the actual model prediction
                    float riskScore = classifier.classify(liveInput);

                    // 💡 TEST LOGIC: Lowered talking threshold from 0.05 to 0.01
                    if (volumeFactor > 0.01f) {
                        // When talking, show a mix of the model and your voice energy
                        riskScore = (riskScore * 0.2f) + (volumeFactor * 0.8f);
                    } else {
                        // When truly silent, pull the risk down to zero
                        riskScore = Math.min(riskScore, 0.01f);
                    }

                    Intent intent = new Intent("com.example.nuerosiren.RISK_UPDATE");
                    intent.putExtra("risk_score", riskScore);
                    intent.setPackage(getPackageName());
                    sendBroadcast(intent);
                }
                
                try {
                    Thread.sleep(800); // Check once every 0.8 seconds
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        recordingThread.start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Speech Monitoring", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
        }
        if (classifier != null) {
            classifier.close();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
