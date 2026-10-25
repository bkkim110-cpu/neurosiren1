package com.example.nuerosiren;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Locale;

public class HomeCriticalFragment extends Fragment {

    private TextView tvCountdownClock;
    private CountDownTimer countDownTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the high-visibility crimson critical screen layout
        View view = inflater.inflate(R.layout.fragment_home_critical, container, false);

        tvCountdownClock = view.findViewById(R.id.tvCountdownClock);

        // Start a real 30-second automated emergency ticker countdown
        startEmergencyCountdown(30000);

        // Emergency Control Button click hook
        view.findViewById(R.id.btnOpenEmergencyControls).setOnClickListener(v -> {
            // Cancel timer if they interact with controls
            if (countDownTimer != null) countDownTimer.cancel();
            // Open your dispatch window sheet here
        });

        // Profile Icon Click Action
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

        return view;
    }

    private void startEmergencyCountdown(long durationMillis) {
        countDownTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;

                // Formats numbers cleanly into classic "00:30" display structures
                String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                tvCountdownClock.setText(timeFormatted);
            }

            @Override
            public void onFinish() {
                tvCountdownClock.setText("00:00");
                // Trigger automated silent call dispatch protocols here!
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Safety guard: stop background timer if user leaves screen
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}