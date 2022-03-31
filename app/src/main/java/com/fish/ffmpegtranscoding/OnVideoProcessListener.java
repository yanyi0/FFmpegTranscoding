package com.fish.ffmpegtranscoding;


public interface OnVideoProcessListener
{
    void onProcessStart();

    void onProcessProgress(float progress);

    void onProcessSuccess();

    void onProcessFailure();
}
