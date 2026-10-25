package com.example.nuerosiren;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class EvaluationLandingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate your evaluation tab onboarding/landing layout view
        View view = inflater.inflate(R.layout.fragment_evaluation_landing, container, false);

        MaterialButton btnStart = view.findViewById(R.id.btnTriggerEvaluation);
        if (btnStart != null) {
            btnStart.setOnClickListener(v -> {
                // Initialize the testing sequence by starting Step 1
                Intent intent = new Intent(getActivity(), EvaluationArmsActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }
}