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
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

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

        protected final String TAG = "appDebug";
        Button play, pause, resume, stop;
        EditText songIndex;
        Switch start_service;

        Status status;
        ClipServerServices clipServerServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start_service = findViewById(R.id.start_service);
        songIndex = findViewById(R.id.clip_number);
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        resume = findViewById(R.id.resume);
        stop = findViewById(R.id.stop);

        start_service.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                Log.i(TAG, "Service started");
            }
            else {
                Log.i(TAG, "Service stopped");
            }
        });

        play.setOnClickListener(v -> {
            try {
                String indextext =songIndex.getText().toString();
                if (indextext.length() > 0 && Integer.parseInt(indextext) > 0) {
                    clipServerServices.play(Integer.parseInt(indextext));
                }
                Log.i(TAG, "Play button clicked : " + indextext);
            } catch (RemoteException e) {
                Log.i(TAG, e.toString());
            }
        });

        pause.setOnClickListener(v -> {
            try {
                Log.i(TAG, "Pause button clicked : " + clipServerServices.pause());
            } catch (RemoteException e) {
                Log.i(TAG, e.toString());
            }
        });

        stop.setOnClickListener(v -> {
            try {
                Log.i(TAG, "Stop button clicked : " + clipServerServices.stop());
            } catch (RemoteException e) {
                Log.i(TAG, e.toString());
            }
        });

        resume.setOnClickListener(v -> {
            try {
                Log.i(TAG, "Resume button clicked : " + clipServerServices.resume());
            } catch (RemoteException e) {
                Log.i(TAG, e.toString());
            }
        });
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder iService) {

            clipServerServices = ClipServerServices.Stub.asInterface(iService);
            status = Status.SERVICE_STARTED;
        }

        public void onServiceDisconnected(ComponentName className) {

            clipServerServices = null;
            status = Status.SERVICE_STOPPED;

        }
    };

    protected void bindService() {
        Intent i = new Intent(ClipServerServices.class.getName());

        ResolveInfo info = getPackageManager().resolveService(i, PackageManager.MATCH_ALL);
        i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

        boolean bindStatus = bindService(i, this.mConnection, Context.BIND_AUTO_CREATE);
        if (bindStatus) {
            status = Status.SERVICE_BINDED;
            Log.i(TAG, "bindService() succeeded!");
        } else {
            status = Status.SERVICE_UNBINDED;
            Log.i(TAG, "bindService() failed!");
        }
    }

        @Override
        protected void onStart() {
            super.onStart();

            bindService();
        }
    }