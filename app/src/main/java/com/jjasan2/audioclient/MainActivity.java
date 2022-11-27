    package com.jjasan2.audioclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;

    public class MainActivity extends AppCompatActivity {

    Button play, pause, resume, stop;
    Switch start_service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start_service = findViewById(R.id.start_service);
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        resume = findViewById(R.id.resume);
        stop = findViewById(R.id.stop);

        start_service.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                Log.i("appDebug", "Service started");
            }
            else {
                Log.i("appDebug", "Service stopped");
            }
        });

        play.setOnClickListener(v -> {
            Log.i("appDebug", "Play button clicked");
        });

        pause.setOnClickListener(v -> {
            Log.i("appDebug", "Pause button clicked");
        });

        stop.setOnClickListener(v -> {
            Log.i("appDebug", "Stop button clicked");
        });

        resume.setOnClickListener(v -> {
            Log.i("appDebug", "Resume button clicked");
        });
    }
}