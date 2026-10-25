package com.example.nuerosiren;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EvaluationArmsActivity extends AppCompatActivity {

    private static final String TAG = "EvaluationArms";
    private static final int PERMISSION_REQUEST_CAMERA = 100;

    private TextView tvTimerCountText;
    private CountDownTimer testTimer;
    private RecyclerView rvEvaluationSteps;
    private PreviewView previewView;
    private MaterialButton btnStartTest;

    private ProcessCameraProvider cameraProvider;
    private ExecutorService cameraExecutor;
    private PoseDetector poseDetector;
    private boolean isTestRunning = false;
    
    // Test Data
    private float maxLeftArmDrift = 0f;
    private float maxRightArmDrift = 0f;
    private float initialLeftShoulderY = -1f;
    private float initialRightShoulderY = -1f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation_arms);

        tvTimerCountText = findViewById(R.id.tvTimerCountText);
        btnStartTest = findViewById(R.id.btnStartEvaluationTest);
        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Initialize Pose Detector
        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();
        poseDetector = PoseDetection.getClient(options);

        setupRecyclerView();

        View root = findViewById(R.id.evaluationArmsRoot);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnStartTest != null) {
            btnStartTest.setOnClickListener(v -> {
                if (!isTestRunning) {
                    startCountdownSequence();
                }
            });
        }

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::analyzePose);

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
        }
    }

    @androidx.annotation.OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    private void analyzePose(@NonNull ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
        
        poseDetector.process(image)
                .addOnSuccessListener(pose -> {
                    if (isTestRunning) {
                        evaluateArmStability(pose);
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void evaluateArmStability(Pose pose) {
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);

        if (leftShoulder != null && rightShoulder != null && leftWrist != null && rightWrist != null) {
            float leftWristY = leftWrist.getPosition().y;
            float rightWristY = rightWrist.getPosition().y;

            if (initialLeftShoulderY == -1) {
                initialLeftShoulderY = leftWristY;
                initialRightShoulderY = rightWristY;
            }

            float leftDrift = leftWristY - initialLeftShoulderY;
            float rightDrift = rightWristY - initialRightShoulderY;

            if (leftDrift > maxLeftArmDrift) maxLeftArmDrift = leftDrift;
            if (rightDrift > maxRightArmDrift) maxRightArmDrift = rightDrift;
        }
    }

    private void startCountdownSequence() {
        isTestRunning = true;
        btnStartTest.setEnabled(false);
        btnStartTest.setText("Recording...");

        testTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimerCountText.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                isTestRunning = false;
                // Transition to Step 2 (Eye Tracking Test)
                Intent intent = new Intent(EvaluationArmsActivity.this, EvaluationEyeActivity.class);
                intent.putExtra("left_arm_drift", maxLeftArmDrift);
                intent.putExtra("right_arm_drift", maxRightArmDrift);
                startActivity(intent);
            }
        }.start();
    }

    private void setupRecyclerView() {
        rvEvaluationSteps = findViewById(R.id.rvEvaluationSteps);
        if (rvEvaluationSteps != null) {
            rvEvaluationSteps.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            List<StepModel> steps = new ArrayList<>();
            steps.add(new StepModel("STEP 1 OF 3", "Arm Lift Test", 
                    "Lift both arms and hold them for 10 seconds. Position your phone at your side so your profile and arm movement are fully visible.", true));
            StepAdapter stepAdapter = new StepAdapter(steps);
            rvEvaluationSteps.setAdapter(stepAdapter);
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (testTimer != null) {
            testTimer.cancel();
        }
        cameraExecutor.shutdown();
        poseDetector.close();
    }

    // ==========================================
    // RECYCLERVIEW ADAPTER & MODEL
    // ==========================================
    private static class StepModel {
        String progress;
        String title;
        String description;
        boolean isRecording;

        public StepModel(String progress, String title, String description, boolean isRecording) {
            this.progress = progress;
            this.title = title;
            this.description = description;
            this.isRecording = isRecording;
        }
    }

    private class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepViewHolder> {
        private final List<StepModel> steps;

        public StepAdapter(List<StepModel> steps) {
            this.steps = steps;
        }

        @NonNull
        @Override
        public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_evaluation_step_card, parent, false);
            return new StepViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
            StepModel step = steps.get(position);
            holder.tvProgress.setText(step.progress);
            holder.tvTitle.setText(step.title);
            holder.tvDescription.setText(step.description);
            holder.layoutRecording.setVisibility(step.isRecording ? View.VISIBLE : View.GONE);
        }

        @Override
        public int getItemCount() {
            return steps.size();
        }

        class StepViewHolder extends RecyclerView.ViewHolder {
            TextView tvProgress, tvTitle, tvDescription;
            View layoutRecording;

            public StepViewHolder(@NonNull View itemView) {
                super(itemView);
                tvProgress = itemView.findViewById(R.id.tvStepProgressBadge);
                tvTitle = itemView.findViewById(R.id.tvEvaluationTestTitle);
                tvDescription = itemView.findViewById(R.id.tvEvaluationTestDescription);
                layoutRecording = itemView.findViewById(R.id.recordingStatusIndicatorBox);
            }
        }
    }
}
