package com.example.nuerosiren;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class EvaluationCompleteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation_complete);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Retrieve data from intents
        float leftArmDrift = getIntent().getFloatExtra("left_arm_drift", 0f);
        float rightArmDrift = getIntent().getFloatExtra("right_arm_drift", 0f);
        float eyeDeviation = getIntent().getFloatExtra("eye_deviation", 0f);
        float facialAsymmetry = getIntent().getFloatExtra("facial_asymmetry", 0f);

        // Set up the dynamic checklist
        RecyclerView rvDiagnosticBreakdown = findViewById(R.id.rvDiagnosticBreakdown);
        rvDiagnosticBreakdown.setLayoutManager(new LinearLayoutManager(this));

        List<DiagModel> diagList = new ArrayList<>();
        
        // Analyze Arm Test
        String armStatus = (leftArmDrift > 50 || rightArmDrift > 50) ? "Warning" : "Normal";
        diagList.add(new DiagModel("Arm Lift", armStatus, R.drawable.circle_with_check));

        // Analyze Eye Test
        String eyeStatus = (eyeDeviation > 0.3) ? "Irregular" : "Normal";
        diagList.add(new DiagModel("Eye Movement", eyeStatus, R.drawable.circle_with_check));

        // Analyze Face Test
        String faceStatus = (facialAsymmetry > 20) ? "Drooping" : "Normal";
        diagList.add(new DiagModel("Facial Symmetry", faceStatus, R.drawable.circle_with_check));

        DiagAdapter adapter = new DiagAdapter(diagList);
        rvDiagnosticBreakdown.setAdapter(adapter);

        // Update overall headline if necessary
        TextView tvHeadline = findViewById(R.id.tvMainStatusTitle);
        if (armStatus.equals("Warning") || eyeStatus.equals("Irregular") || faceStatus.equals("Drooping")) {
            tvHeadline.setText("Irregularities Detected");
            tvHeadline.setTextColor(Color.parseColor("#DC2626"));
        }

        // Footer Actions - Return to Dashboard (HomeFragment)
        View.OnClickListener returnHomeListener = v -> {
            Intent intent = new Intent(EvaluationCompleteActivity.this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        };

        findViewById(R.id.btnReturnHome).setOnClickListener(returnHomeListener);
        findViewById(R.id.btnClose).setOnClickListener(returnHomeListener);
    }

    private class DiagAdapter extends RecyclerView.Adapter<DiagAdapter.DiagViewHolder> {
        private final List<DiagModel> list;
        public DiagAdapter(List<DiagModel> list) { this.list = list; }

        @NonNull
        @Override
        public DiagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diagnostic_breakdown, parent, false);
            return new DiagViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull DiagViewHolder holder, int position) {
            DiagModel item = list.get(position);
            holder.title.setText(item.title);
            holder.statusText.setText(item.status);
            holder.statusText.setBackground(null);

            boolean isNormal = item.status.equalsIgnoreCase("Normal");

            if (isNormal) {
                // Soft green badge
                holder.statusBadgeCard.setCardBackgroundColor(Color.parseColor("#E6F4EA"));
                holder.statusText.setTextColor(Color.parseColor("#137333"));
                holder.badgeIcon.setImageResource(R.drawable.circle_with_check);
                holder.badgeIcon.setColorFilter(Color.parseColor("#137333"));

                // Left icon
                holder.iconCard.setCardBackgroundColor(Color.parseColor("#E6F4EA"));
                holder.icon.setImageResource(R.drawable.circle_with_check);
                holder.icon.setColorFilter(Color.parseColor("#137333"));
            } else {
                // Warning / Irregular / Drooping - Soft Red badge
                holder.statusBadgeCard.setCardBackgroundColor(Color.parseColor("#FCE8E6"));
                holder.statusText.setTextColor(Color.parseColor("#C5221F"));
                holder.badgeIcon.setImageResource(R.drawable.circle_with_check);
                holder.badgeIcon.setColorFilter(Color.parseColor("#C5221F"));

                // Left icon
                holder.iconCard.setCardBackgroundColor(Color.parseColor("#FCE8E6"));
                holder.icon.setImageResource(R.drawable.circle_with_check);
                holder.icon.setColorFilter(Color.parseColor("#C5221F"));
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        class DiagViewHolder extends RecyclerView.ViewHolder {
            TextView title, statusText;
            ImageView icon, badgeIcon;
            MaterialCardView iconCard, statusBadgeCard;

            public DiagViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tvDiagnosticTitle);
                statusText = itemView.findViewById(R.id.tvBadgeStatusText);
                icon = itemView.findViewById(R.id.ivDiagnosticIcon);
                badgeIcon = itemView.findViewById(R.id.ivBadgeStatusDot);
                iconCard = itemView.findViewById(R.id.cardItemIconIndicator);
                statusBadgeCard = itemView.findViewById(R.id.layoutStatusBadge);
            }
        }
    }

    private static class DiagModel {
        String title, status; int iconRes;
        public DiagModel(String t, String s, int i) { this.title = t; this.status = s; this.iconRes = i; }
    }
}