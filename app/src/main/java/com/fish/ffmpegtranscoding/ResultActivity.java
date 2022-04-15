package com.fish.ffmpegtranscoding;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        // 使用Intent对象得到MainActivity中传过来的参数
        Intent intent = getIntent();
        String transcodeTime = intent.getStringExtra("transcodeTime");
        TextView textView = findViewById(R.id.text_view);
        textView.setText("耗时"+ transcodeTime+"s");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}