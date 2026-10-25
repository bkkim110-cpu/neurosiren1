package com.example.nuerosiren;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.IOException;
import android.os.Build;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class DashboardActivity extends AppCompatActivity {

    // 🎯 THE SYSTEM STATE CONTROLLER
    private String appSafetyStatus = "healthy";
    private float currentRiskPercentage = 0f;
    private long monitoringStartTime = 0;
    private SpeechClassifier speechClassifier;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private final BroadcastReceiver riskUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.nuerosiren.RISK_UPDATE".equals(intent.getAction())) {
                float riskScore = intent.getFloatExtra("risk_score", 0f);
                android.util.Log.d("Dashboard", "Received Risk: " + riskScore);
                updateRiskLevel(riskScore);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Initialize Speech Classifier
        try {
            speechClassifier = new SpeechClassifier(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboardActivityRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);

        // Loads the correct initial home screen state when the app first opens
        if (savedInstanceState == null) {
            loadHomeState();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String idName = getResources().getResourceEntryName(item.getItemId());

            if (idName.equals("nav_home")) {
                // 🚀 CONNECTED: Instead of a hardcoded single screen, it checks your app state!
                loadHomeState();
                return true;
            } else if (idName.equals("nav_evaluation")) {
                selectedFragment = new EvaluationLandingFragment();
            } else if (idName.equals("nav_contacts")) {
                selectedFragment = new ContactsFragment();
            } else if (idName.equals("nav_profile")) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    public void checkPermissionsAndStartService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
        } else {
            monitoringStartTime = System.currentTimeMillis();
            startMonitoringService();
        }
    }

    public void stopMonitoringService() {
        monitoringStartTime = 0;
        Intent intent = new Intent(this, SpeechMonitoringService.class);
        stopService(intent);
    }

    private void startMonitoringService() {
        Intent intent = new Intent(this, SpeechMonitoringService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMonitoringService();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("com.example.nuerosiren.RISK_UPDATE");
        ContextCompat.registerReceiver(this, riskUpdateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(riskUpdateReceiver);
    }

    /**
     * Helper method to evaluate the system safety condition and launch
     * the matching variant sub-fragment housing configuration.
     */
    private void loadHomeState() {
        Fragment homeFragment;
        Bundle args = new Bundle();
        args.putFloat("risk_percentage", currentRiskPercentage);

        if (appSafetyStatus.equals("critical")) {
            homeFragment = new HomeCriticalFragment();
        } else if (appSafetyStatus.equals("risk")) {
            homeFragment = new HomeRiskFragment();
        } else {
            homeFragment = new HomeFragment();
        }

        homeFragment.setArguments(args);
        replaceFragment(homeFragment);
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboard_fragment_container, fragment)
                .commit();
    }

    // 💡 Helper method you can call from other fragments to change the status dynamically later
    public void setAppSafetyStatus(String newStatus) {
        if (!this.appSafetyStatus.equals(newStatus)) {
            this.appSafetyStatus = newStatus;
            
            // Only allow switching pages if 10 seconds have passed OR if it's healthy
            long elapsed = System.currentTimeMillis() - monitoringStartTime;
            if (elapsed > 10000 || newStatus.equals("healthy") || monitoringStartTime == 0) {
                runOnUiThread(this::loadHomeState); 
            }
        }
    }

    public void updateRiskLevel(float score) {
        this.currentRiskPercentage = score * 100;

        // Ensure UI updates on main thread
        runOnUiThread(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.dashboard_fragment_container);
            if (currentFragment instanceof HomeFragment) {
                ((HomeFragment) currentFragment).updateUI(currentRiskPercentage);
            }
        });

        String status;
        if (currentRiskPercentage <= 20) {
            status = "healthy";
        } else if (currentRiskPercentage <= 75) {
            status = "risk";
        } else {
            status = "critical";
        }
        setAppSafetyStatus(status);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechClassifier != null) {
            speechClassifier.close();
        }
    }
}