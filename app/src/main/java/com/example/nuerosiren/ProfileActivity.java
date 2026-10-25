package com.example.nuerosiren;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nuerosiren.R;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
}