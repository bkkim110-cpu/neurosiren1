package com.example.nuerosiren;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantly switch to your custom Dashboard layout screen
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);

        // Close MainActivity completely so it doesn't run in the background
        finish();
    }
}