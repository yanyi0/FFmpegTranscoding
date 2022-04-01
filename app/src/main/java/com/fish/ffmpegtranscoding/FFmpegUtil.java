package com.fish.ffmpegtranscoding;
import com.fish.ffmpegtranscoding.CmdList;
import android.util.Log;
// 封装FFmpeg命令的调用
public class FFmpegUtil {
    private static final String TAG = "FFmpegUtil";

    public static void execCmd(CmdList cmd, long duration, final OnVideoProcessListener listener) {
        String[] cmds = cmd.toArray(new String[cmd.size()]);
        Log.i(TAG, "cmd:" + cmd);
        listener.onProcessStart();
        Log.v("---------------FFmpegUtil execCmd---------"+TAG,cmd.toString());
        FFmpegCmd.exec(cmds, duration, new FFmpegCmd.OnCmdExecListener() {
            @Override
            public void onSuccess() {
                listener.onProcessSuccess();
            }

            @Override
            public void onFailure() {
                listener.onProcessFailure();
            }

            @Override
            public void onProgress(float progress) {
                listener.onProcessProgress(progress);
            }
        });
    }
}
