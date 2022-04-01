package com.fish.ffmpegtranscoding;


import android.util.Log;

public class FFmpegCmd
{
    static
    {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("ffmpegtranscoding");
    }

    private static OnCmdExecListener sOnCmdExecListener;
    private static long sDuration;

    public static native int exec(int argc, String[] argv);

    public static native void exit();

    public static void exec(String[] cmds, long duration, OnCmdExecListener listener)
    {
        Log.v("------------FFmpegCmd exec------------",cmds.toString());
        sOnCmdExecListener = listener;
        sDuration = duration;

        exec(cmds.length, cmds);
    }

    public static void onExecuted(int ret)
    {
        Log.v("------------FFmpegCmd onExecuted------------",Integer.toString(ret));
        if (sOnCmdExecListener != null)
        {
            if (ret == 0)
            {
                sOnCmdExecListener.onProgress(sDuration);
                sOnCmdExecListener.onSuccess();
            }
            else
            {
                sOnCmdExecListener.onFailure();
            }
        }
    }

    public static void onProgress(float progress)
    {
        Log.v("------------FFmpegCmd onProgress------------",Float.toString(progress));
        if (sOnCmdExecListener != null)
        {
            Log.v("------------FFmpegCmd onProgress------------",Float.toString(progress));
            if (sDuration != 0)
            {
                Log.v("------------FFmpegCmd onProgress------------",Long.toString(sDuration));
                sOnCmdExecListener.onProgress(progress / (sDuration / 1000) * 0.95f);
            }
        }
    }


    public interface OnCmdExecListener
    {
        void onSuccess();

        void onFailure();

        void onProgress(float progress);
    }


}
