package com.fish.ffmpegtranscoding;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.fish.ffmpegtranscoding.databinding.ActivityMainBinding;

public class MainFFmpegActivity extends AppCompatActivity {

    // Used to load the 'ffmpegtranscoding' library on application startup.
    static {
        System.loadLibrary("ffmpegtranscoding");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        tv.setText(stringFromJNI());
        tv.setText(ffmpegInfo());
        Log.v("------------",ffmpegInfo());
    }

    /**
     * A native method that is implemented by the 'ffmpegtranscoding' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native String ffmpegInfo();
}