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
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EvaluationEyeActivity extends AppCompatActivity {

    private static final String TAG = "EvaluationEye";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    
    private TextView tvTimerCountText;
    private CountDownTimer testTimer;
    private RecyclerView rvEvaluationSteps;
    private PreviewView viewCameraPreview;
    private MaterialButton btnStartTest;

    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;
    private boolean isTestRunning = false;
    
    // Data from Step 1
    private float leftArmDrift = 0f;
    private float rightArmDrift = 0f;
    
    // Data for Step 2
    private float maxEyeDeviation = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation_eye);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        View root = findViewById(R.id.evaluationEyeRoot);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Receive data from Step 1
        leftArmDrift = getIntent().getFloatExtra("left_arm_drift", 0f);
        rightArmDrift = getIntent().getFloatExtra("right_arm_drift", 0f);

        tvTimerCountText = findViewById(R.id.tvTimerCountText);
        btnStartTest = findViewById(R.id.btnStartTest);
        viewCameraPreview = findViewById(R.id.viewCameraPreview);
        ImageView btnBack = findViewById(R.id.btnBack);

        cameraExecutor = Executors.newSingleThreadExecutor();
        
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();
        faceDetector = FaceDetection.getClient(options);

        setupRecyclerView();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnStartTest != null) {
            btnStartTest.setOnClickListener(v -> checkCameraPermissionAndStart());
        }
    }

    private void checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCameraAndCountdown();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private void startCameraAndCountdown() {
        isTestRunning = true;
        btnStartTest.setEnabled(false);
        btnStartTest.setText("Recording...");

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewCameraPreview.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeFace);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                
                // Fallback to back camera if front camera is not available (common in some emulators)
                try {
                    if (!cameraProvider.hasCamera(cameraSelector)) {
                        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    }
                } catch (Exception e) {
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                }

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));

        testTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimerCountText.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                isTestRunning = false;
                // Progress automatically to Step 3 (Facial Symmetry Test)
                Intent intent = new Intent(EvaluationEyeActivity.this, FacialSymmetryTestActivity.class);
                intent.putExtra("left_arm_drift", leftArmDrift);
                intent.putExtra("right_arm_drift", rightArmDrift);
                intent.putExtra("eye_deviation", maxEyeDeviation);
                startActivity(intent);
            }
        }.start();
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
                    if (!faces.isEmpty()) {
                        trackEyes(faces.get(0));
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void trackEyes(Face face) {
        // Simplified eye tracking logic: check for eye opening probability differences
        Float leftOpen = face.getLeftEyeOpenProbability();
        Float rightOpen = face.getRightEyeOpenProbability();

        if (leftOpen != null && rightOpen != null) {
            float deviation = Math.abs(leftOpen - rightOpen);
            if (deviation > maxEyeDeviation) {
                maxEyeDeviation = deviation;
            }
        }
    }

    private void setupRecyclerView() {
        rvEvaluationSteps = findViewById(R.id.rvEvaluationSteps);
        if (rvEvaluationSteps != null) {
            rvEvaluationSteps.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            List<StepModel> steps = new ArrayList<>();
            steps.add(new StepModel("STEP 2 OF 3", "Eye Tracking Test",
                    "Keep your head still and follow the moving object with your eyes. Position your face within the guide.", true));
            StepAdapter adapter = new StepAdapter(steps);
            rvEvaluationSteps.setAdapter(adapter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (testTimer != null) {
            testTimer.cancel();
        }
        cameraExecutor.shutdown();
        faceDetector.close();
    }

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

    private static class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepViewHolder> {
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

        static class StepViewHolder extends RecyclerView.ViewHolder {
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
