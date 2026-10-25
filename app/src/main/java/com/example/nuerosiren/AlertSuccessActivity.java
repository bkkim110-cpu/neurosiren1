package com.example.nuerosiren;

// 🚀 EVERY REQUIRED IMPORT EXPLICITLY INCLUDED HERE:
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
import java.util.ArrayList;
import java.util.List;

public class AlertSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_success);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        RecyclerView rvDispatchStatuses = findViewById(R.id.rvDispatchStatuses);
        rvDispatchStatuses.setLayoutManager(new LinearLayoutManager(this));

        List<SuccessModel> list = new ArrayList<>();
        list.add(new SuccessModel("911 notified", "Dispatched at 10:42 AM", R.drawable.profile));
        list.add(new SuccessModel("Emergency contacts notified", "Alerted at 10:42 AM", R.drawable.person_add));

        SuccessAdapter adapter = new SuccessAdapter(list);
        rvDispatchStatuses.setAdapter(adapter);

        findViewById(R.id.btnReturnHome).setOnClickListener(v -> finish());
        findViewById(R.id.btnViewAlertDetails).setOnClickListener(v -> { /* Open details */ });
        findViewById(R.id.btnCallEmergencyAgain).setOnClickListener(v -> { /* Call protocol */ });
    }

    // ==========================================
    // SUCCESS RECYCLERVIEW ADAPTER
    // ==========================================
    private class SuccessAdapter extends RecyclerView.Adapter<SuccessAdapter.SuccessViewHolder> {
        private final List<SuccessModel> data;

        public SuccessAdapter(List<SuccessModel> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public SuccessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_status, parent, false);
            return new SuccessViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull SuccessViewHolder holder, int position) {
            SuccessModel item = data.get(position);
            holder.title.setText(item.title);
            holder.time.setText(item.time);
            holder.icon.setImageResource(item.iconRes);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class SuccessViewHolder extends RecyclerView.ViewHolder {
            TextView title, time;
            ImageView icon;

            public SuccessViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tvStatusRowTitle);
                time = itemView.findViewById(R.id.tvStatusRowTime);
                icon = itemView.findViewById(R.id.ivStatusRowIcon);
            }
        }
    }

    // ==========================================
    // LOCAL SUCCESS DATA MODEL Node
    // ==========================================
    private static class SuccessModel {
        String title, time;
        int iconRes;

        public SuccessModel(String t, String tm, int i) {
            this.title = t;
            this.time = tm;
            this.iconRes = i;
        }
    }
}