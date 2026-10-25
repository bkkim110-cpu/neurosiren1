package com.example.nuerosiren;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private TextView tvDescription;
    private View layoutVoiceIndicator;
    private MaterialButton btnStartMic;
    private boolean isMonitoring = false;
    private float sessionMaxRisk = 0f;
    private CountDownTimer evaluationTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvDescription = view.findViewById(R.id.tvStatusDescription);
        layoutVoiceIndicator = view.findViewById(R.id.layoutVoiceIndicator);
        btnStartMic = view.findViewById(R.id.btnStartMic);

        btnStartMic.setOnClickListener(v -> {
            if (getActivity() instanceof DashboardActivity) {
                DashboardActivity activity = (DashboardActivity) getActivity();
                if (!isMonitoring) {
                    // Start Monitoring
                    activity.checkPermissionsAndStartService();
                    isMonitoring = true;
                    sessionMaxRisk = 0f;
                    
                    // Start 10-second evaluation countdown
                    startEvaluationUIThread();
                } else {
                    // Stop Monitoring
                    if (evaluationTimer != null) evaluationTimer.cancel();
                    activity.stopMonitoringService();
                    isMonitoring = false;
                    btnStartMic.setText("Start Monitoring");
                    btnStartMic.setIconResource(R.drawable.record);
                    showAnalysisReport();
                }
            }
        });

        View btnProfile = view.findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                if (getActivity() instanceof DashboardActivity) {
                    com.google.android.material.bottomnavigation.BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation_bar);
                    if (nav != null) {
                        nav.setSelectedItemId(R.id.nav_profile);
                    }
                }
            });
        }

        // Initial setup from arguments
        if (getArguments() != null) {
            updateUI(getArguments().getFloat("risk_percentage", 0f));
        }

        return view;
    }

    private void startEvaluationUIThread() {
        evaluationTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnStartMic.setText("Evaluating... (" + (millisUntilFinished / 1000) + "s)");
            }

            @Override
            public void onFinish() {
                btnStartMic.setText("Stop Monitoring");
                btnStartMic.setIconResource(R.drawable.check);
            }
        }.start();
    }

    private void showAnalysisReport() {
        String analysis;
        if (sessionMaxRisk < 35) {
            analysis = "Your speech patterns are stable and within normal neurological ranges.";
        } else if (sessionMaxRisk < 75) {
            analysis = "Slight irregularities detected. It is recommended to perform a physical evaluation test.";
        } else {
            analysis = "High risk detected. Please seek medical attention or contact your emergency contacts immediately.";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Monitoring Session Complete")
                .setMessage(String.format(Locale.getDefault(), 
                    "Peak Risk Score: %.1f%%\n\nAnalysis: %s", sessionMaxRisk, analysis))
                .setPositiveButton("Dismiss", null)
                .show();
    }

    /**
     * Called by DashboardActivity to update the UI in real-time
     */
    public void updateUI(float riskScore) {
        if (riskScore > sessionMaxRisk) {
            sessionMaxRisk = riskScore;
        }

        if (tvDescription != null) {
            tvDescription.setText(String.format(Locale.getDefault(), 
                "No signs of neurological distress. Current Risk: %.1f%%", riskScore));
        }
        
        // Show "Voice Detected" if there is any activity (even small)
        if (layoutVoiceIndicator != null) {
            layoutVoiceIndicator.setVisibility(riskScore > 0.1f ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
