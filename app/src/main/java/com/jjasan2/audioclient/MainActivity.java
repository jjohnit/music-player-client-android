    package com.jjasan2.audioclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.jjasan2.clipserver.ClipServerServices;

    enum Status {
        SERVICE_STARTED,
        SERVICE_STOPPED,
        SERVICE_BINDED,
        SERVICE_UNBINDED,
        PLAYING,
        PAUSED
    }

    public class MainActivity extends AppCompatActivity {

        // For logging
        protected final String TAG = "appDebug";

        // Declare views
        Button play, pause, resume, stop;
        EditText songIndex;
        View playback_view;
        Switch start_service;

        // Enum for statuses
        Status status;
        // Flag for disabling the resume button in initial playback
        boolean initial_playback = true;

        // aidl class for connecting to clip server
        ClipServerServices clipServerServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assign the values for views
        start_service = findViewById(R.id.start_service);
        songIndex = findViewById(R.id.clip_number);
        playback_view = findViewById(R.id.playback_view);
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        resume = findViewById(R.id.resume);
        stop = findViewById(R.id.stop);

        // Start or stop service based on the change in toggle button.
        start_service.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                if(bindService(true)) {
                    // Show the playback buttons when service is started
                    enableButtons(Status.SERVICE_STARTED);
                    status = Status.SERVICE_BINDED;
                    Log.i(TAG, "Service started");
                }
            }
            else {
                Toast.makeText(this, "Playback will be stopped", Toast.LENGTH_LONG).show();
                // Unbind the service on stop service
                bindService(false);
                // Stop the service
                try {
                    clipServerServices.stopService();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                // Hide the playback buttons when service is stopped
                enableButtons(Status.SERVICE_STOPPED);
                status = Status.SERVICE_STOPPED;
                Log.i(TAG, "Service stopped");
            }
        });

        // Enable playback buttons only when when the user adds a clip number
        songIndex.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String clipNumberText = songIndex.getText().toString();
                // When the input is valid, enable the necessary buttons
                if (clipNumberText.length() > 0 && Integer.parseInt(clipNumberText) > 0)
                    enableButtons(Status.PAUSED);
            }
        });

        play.setOnClickListener(v -> {
            try {
                if(status != Status.SERVICE_BINDED){
                    bindService(true);
                }
                clipServerServices.play(Integer.parseInt(songIndex.getText().toString()));
                enableButtons(Status.PLAYING);
                Log.i(TAG, "Play button clicked");
            } catch (RemoteException e) {
                Log.i(TAG, e.toString());
            }
        });

        pause.setOnClickListener(v -> {
            try {
                Log.i(TAG, "Pause button clicked : " + clipServerServices.pause());
                enableButtons(Status.PAUSED);
            } catch (RemoteException e) {
                Log.i(TAG, e.toString());
            }
        });

        stop.setOnClickListener(v -> {
            try {
                Log.i(TAG, "Stop button clicked : " + clipServerServices.stop());
                // Unbind the service on stop
                bindService(false);
                enableButtons(Status.SERVICE_STARTED);
            } catch (RemoteException e) {
                Log.i(TAG, e.toString());
            }
        });

        resume.setOnClickListener(v -> {
            try {
                Log.i(TAG, "Resume button clicked : " + clipServerServices.resume());
                enableButtons(Status.PLAYING);
            } catch (RemoteException e) {
                Log.i(TAG, e.toString());
            }
        });
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder iService) {
            clipServerServices = ClipServerServices.Stub.asInterface(iService);
//            try {
//                clipServerServices.startService();
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
            status = Status.SERVICE_STARTED;
            Log.i(TAG, "onServiceConnected: ");
        }

        public void onServiceDisconnected(ComponentName className) {
            clipServerServices = null;
            status = Status.SERVICE_STOPPED;
            Log.i(TAG, "onServiceDisconnected : Service stopped");
        }

        @Override
        public void onBindingDied(ComponentName name) {
            ServiceConnection.super.onBindingDied(name);
            Log.i(TAG, "onBindingDied: ");
        }

        @Override
        public void onNullBinding(ComponentName name) {
            ServiceConnection.super.onNullBinding(name);
            Log.i(TAG, "onNullBinding: ");
        }
    };

    protected boolean bindService(boolean bind) {
        boolean bindStatus;
        if(bind){
            Intent i = new Intent(ClipServerServices.class.getName());

            ResolveInfo info = getPackageManager().resolveService(i, PackageManager.MATCH_ALL);
            i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

            bindStatus = bindService(i, this.mConnection, 0);
            if (bindStatus) {
                status = Status.SERVICE_BINDED;
                Log.i(TAG, "bindService() succeeded!");
                return true;
            } else {
                status = Status.SERVICE_UNBINDED;
                Log.i(TAG, "bindService() failed!");
                Toast.makeText(this, "Binding service failed", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        else {
            if(status == Status.SERVICE_BINDED){
                unbindService(this.mConnection);
                Log.i(TAG, "Service unbinded");
            }
            // Flag for disabling the resume button in initial playback
            initial_playback = true;
            status = Status.SERVICE_UNBINDED;
            enableButtons(Status.SERVICE_STARTED);
            return true;
        }
    }

//        @Override
//        protected void onStart() {
//            super.onStart();
//
//            bindService();
//        }

        protected void enableButtons(Status status){
            switch (status){
                case SERVICE_STARTED:
                    songIndex.setEnabled(true);
                    play.setEnabled(false);
                    pause.setEnabled(false);
                    resume.setEnabled(false);
                    stop.setEnabled(false);
                    songIndex.setText("");
                    playback_view.setVisibility(View.VISIBLE);
                    break;
                case SERVICE_STOPPED:
                    playback_view.setVisibility(View.INVISIBLE);
                    break;
                case PLAYING:
                    songIndex.setEnabled(false);
                    play.setEnabled(false);
                    pause.setEnabled(true);
                    resume.setEnabled(false);
                    stop.setEnabled(true);
                    break;
                case PAUSED:
                    songIndex.setEnabled(true);
                    play.setEnabled(true);
                    pause.setEnabled(false);
                    stop.setEnabled(true);
                    if(initial_playback){
                        resume.setEnabled(false);
                        initial_playback = false;
                    }
                    else
                        resume.setEnabled(true);
                    break;
            }
        }
    }