package com.example.nuerosiren;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FacialSymmetryTestActivity extends AppCompatActivity {

    private static final String TAG = "FacialSymmetry";
    private static final int PERMISSION_REQUEST_CAMERA = 100;

    private PreviewView previewView;
    private TextView tvCountdownNumber;
    private MaterialButton btnStartTest;
    private CountDownTimer testTimer;

    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;
    private boolean isTestRunning = false;

    // Accumulated data
    private float leftArmDrift, rightArmDrift, eyeDeviation;
    private float maxFacialAsymmetry = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facial_symmetry_test);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Receive data from previous steps
        leftArmDrift = getIntent().getFloatExtra("left_arm_drift", 0f);
        rightArmDrift = getIntent().getFloatExtra("right_arm_drift", 0f);
        eyeDeviation = getIntent().getFloatExtra("eye_deviation", 0f);

        previewView = findViewById(R.id.viewFinder);
        tvCountdownNumber = findViewById(R.id.tvCountdownNumber);
        btnStartTest = findViewById(R.id.btnStartTestSequence);

        cameraExecutor = Executors.newSingleThreadExecutor();

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .build();
        faceDetector = FaceDetection.getClient(options);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnStartTest != null) {
            btnStartTest.setOnClickListener(v -> {
                if (allPermissionsGranted()) {
                    startCameraAndTest();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
                }
            });
        }
    }

    private void startCameraAndTest() {
        isTestRunning = true;
        btnStartTest.setEnabled(false);
        btnStartTest.setText("Recording...");

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));

        testTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdownNumber.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                isTestRunning = false;
                completeEvaluation();
            }
        }.start();
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeFace);

        // DEFAULT TO FRONT CAMERA
        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

        // FALLBACK TO BACK CAMERA IF FRONT IS MISSING (common in emulators)
        try {
            if (!cameraProvider.hasCamera(cameraSelector)) {
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
            }
        } catch (Exception e) {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        }

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
        }
    }

    @androidx.annotation.OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    private void analyzeFace(@NonNull ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    if (!faces.isEmpty() && isTestRunning) {
                        checkSymmetry(faces.get(0));
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void checkSymmetry(Face face) {
        FaceLandmark leftMouth = face.getLandmark(FaceLandmark.MOUTH_LEFT);
        FaceLandmark rightMouth = face.getLandmark(FaceLandmark.MOUTH_RIGHT);

        if (leftMouth != null && rightMouth != null) {
            float diff = Math.abs(leftMouth.getPosition().y - rightMouth.getPosition().y);
            if (diff > maxFacialAsymmetry) {
                maxFacialAsymmetry = diff;
            }
        }
    }

    private void completeEvaluation() {
        Intent intent = new Intent(this, EvaluationCompleteActivity.class);
        intent.putExtra("left_arm_drift", leftArmDrift);
        intent.putExtra("right_arm_drift", rightArmDrift);
        intent.putExtra("eye_deviation", eyeDeviation);
        intent.putExtra("facial_asymmetry", maxFacialAsymmetry);
        startActivity(intent);
        finish();
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA && allPermissionsGranted()) {
            startCameraAndTest();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (testTimer != null) testTimer.cancel();
        cameraExecutor.shutdown();
        faceDetector.close();
    }
}