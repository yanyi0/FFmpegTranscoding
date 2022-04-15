package com.fish.ffmpegtranscoding;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import android.util.Log;
import android.os.Environment;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fish.ffmpegtranscoding.callback.RecordProgressCallback;
import com.fish.ffmpegtranscoding.entry.ConfigTicket;
import com.fish.ffmpegtranscoding.entry.TaskTicket;
import com.fish.ffmpegtranscoding.utils.RealPathFromUriUtils;

public class MainActivity extends AppCompatActivity implements RecordProgressCallback {
    // Used to load the 'ffmpegtranscoding' library on application startup.
    static {
        System.loadLibrary("ffmpegtranscoding");
    }
    public static final int REQUEST_PICK_IMAGE = 11101;

    private TextView mTvBitrate;
    private EditText mEtVideoBitrate;
    private TaskTicket mTaskTicket;
    // 设备根目录路径
    private String path = Environment.getExternalStorageDirectory().getAbsolutePath();
    private RecordProgressCallback mProgressCallback;
    private long startTime;
    private long endTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("------ffmpeg encoders------",ffmpegInfo());
        setContentView(R.layout.activity_main);
        registerPermission();
        registerSpinner();
        registerView();
        registerEvent();
        mTaskTicket = new TaskTicket();
        mProgressCallback = this;
    }

    private void registerView() {
        mTvBitrate = findViewById(R.id.tv_video_bitrate);
        mEtVideoBitrate = findViewById(R.id.et_video_bitrate);
    }

    private void registerPermission() {
        String[] permissionNeeded = {
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissionNeeded, 101);
            }
        }
    }

    private void registerEvent(){

    }

    private void registerSpinner() {
        Spinner spResolution = findViewById(R.id.sp_resolution);
        spResolution.setAdapter(getDefaultSpinnerAdapter(R.array.video_resolution));
        spResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String resolution = (String) adapterView.getItemAtPosition(pos);
                String[] wh = resolution.split("x");
                mTaskTicket.setWidth(Integer.parseInt(wh[0]));
                mTaskTicket.setHeight(Integer.parseInt(wh[1]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Spinner spFPS = findViewById(R.id.sp_fps);
        spFPS.setAdapter(getDefaultSpinnerAdapter(R.array.video_fps));
        spFPS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                String fps = (String) adapterView.getItemAtPosition(pos);
                mTaskTicket.setFps(Integer.parseInt(fps));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }
    public void clickSelectVideoSrc(View view){
        selectVideoSource();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void clickStartTranscoding(View view){
        Log.v("------------call ffmpeg----------",ffmpegInfo());
        if(TextUtils.isEmpty(mTaskTicket.getVideoSourcePath())){
            Toast.makeText(this, "请先选择视频源文件", Toast.LENGTH_SHORT).show();
            return ;
        }
        Log.v("------------call ffmpeg----------",mTaskTicket.getVideoSourcePath());
        mTaskTicket.buildConfigTaskTickets();
        showTaskListDialog(mTaskTicket.getConfigTickets());
    }

    private void showTaskListDialog(ArrayList<ConfigTicket> tickets){
        String[] showTasks = tickets.toString().replace("[","").replace("]","").split(",");
        long totalDuration = (showTasks.length * mTaskTicket.getSourceDuration()) / 1000;
        String title = String.format("共%s个任务，预计花费%s秒",showTasks.length,totalDuration);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alert = builder.setTitle(title)
                .setItems(showTasks,null)
                .setPositiveButton("开始", (dialog, which) -> {
                    startTime = System.currentTimeMillis()/1000;
                    Log.i("MainActivity","------------showTaskListDialog---------: "+tickets.get(0).getTicketID());
                    // 开始第一个任务 传ConfigTicket对象
//                    ZegoUtils.getInstance().startTranscoding(tickets.get(0));
                    ffmpeg_execte_task(tickets.get(0));
                })
                .setNegativeButton("取消", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .setCancelable(false)
                .create();
        alert.show();
    }
    public void clickVideoBitrateAdd(View view){
        //-------todo delete use test
//        test_ffmpeg_commandline();
        String bitrate = mEtVideoBitrate.getText().toString().trim();
        if(TextUtils.isEmpty(bitrate)){
            Toast.makeText(this, "请输入码率", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mTaskTicket.getBitrate().add(Integer.valueOf(bitrate));
        }catch (Exception e){
            Toast.makeText(this, "请输入正确的数字", Toast.LENGTH_SHORT).show();
        }
        mTvBitrate.setText(mTaskTicket.getBitrate().toString().replace("[","").replace("]",""));
        mEtVideoBitrate.setText("");
    }

    public void clickVideoBitrateDelete(View view){
        mTaskTicket.getBitrate().clear();
        mTvBitrate.setText("");
    }

    public void clickCheckBox(View view){
        boolean checked = ((CheckBox) view).isChecked();
        switch(view.getId()) {
            case R.id.cb_2x:
                mTaskTicket.getGop().put(TaskTicket.GOP2X,checked);
                break;
            case R.id.cb_4x:
                mTaskTicket.getGop().put(TaskTicket.GOP4X,checked);
                break;
            case R.id.cb_abr:
                mTaskTicket.getBitrateControl().put(TaskTicket.ABR,checked);
                break;
            case R.id.cb_cbr:
                mTaskTicket.getBitrateControl().put(TaskTicket.CBR,checked);
                break;
            case R.id.cb_h264_hw:
                mTaskTicket.getEncoder().put(TaskTicket.H264HW,checked);
                break;
            case R.id.cb_h265_hw:
                mTaskTicket.getEncoder().put(TaskTicket.H265HW,checked);
                break;
            case R.id.cb_x264_low_bitrate:
                mTaskTicket.getEncoder().put(TaskTicket.X264LOWBITRATE,checked);
                break;
            default:
                break;
        }
    }

    private SpinnerAdapter getDefaultSpinnerAdapter(int textArrayResId) {
        ArrayAdapter<CharSequence> resolutionAdapter = ArrayAdapter.createFromResource(this,
                textArrayResId, android.R.layout.simple_spinner_item);
        resolutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return resolutionAdapter;
    }

    private void selectVideoSource() {
        // TODO: 2022/3/7 选择视频源-高码率视频录制文件
        String[] mPermissionList = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(MainActivity.this, mPermissionList, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                boolean writeExternalStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readExternalStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (writeExternalStorage && readExternalStorage) {
                    getImage();
                } else {
                    Toast.makeText(this, "请设置必要权限", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    private void getImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_IMAGE:
                    if (data != null) {
                        mTaskTicket.setVideoSourcePath(RealPathFromUriUtils.getRealPathFromUri(this, data.getData()));
                        TextView tvVideoSrcPath = findViewById(R.id.tv_path_video_source);
                        tvVideoSrcPath.setText(mTaskTicket.getVideoSourcePath());

                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        Log.v("------------select source file-----------",mTaskTicket.getVideoSourcePath());
//                        retriever.setDataSource(mTaskTicket.getVideoSourcePath());
//                        mTaskTicket.setSourceDuration(Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
                    } else {
                        Toast.makeText(this, "图片损坏，请重新选择", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    @Override
    public void begin(int ticketID, String ticketInfo, int totalDuration, int errorCode) {
        TextView tvCurrTask = findViewById(R.id.tv_curr_task);
        tvCurrTask.setVisibility(View.VISIBLE);
        tvCurrTask.setText(String.format("(%s/%s)正在执行>>>%s",ticketID,mTaskTicket.getConfigTickets().size(),ticketInfo));
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(totalDuration);
    }
    private void ffmpeg_execte_task(ConfigTicket configTicket){
        long duration = 10;
        //call ffmpeg comandline
        //ffmpeg -i MyHeartWillGoOn.mp4 -c:a aac -c:v libx264 output.mp4
        Log.v("------------select source file-----------",configTicket.getVideoSourcePath());
        CmdList cmd = new CmdList();
        cmd.append("ffmpeg");
        cmd.append("-i");
        cmd.append(configTicket.getVideoSourcePath());
        cmd.append("-c:a");
        cmd.append("aac");
        cmd.append("-c:v");
        //编码器选择
        if (configTicket.getCodecId().equals("h264_hw"))
            cmd.append("h264_hlmediacodec");
        if (configTicket.getCodecId().equals("h265_hw"))
            cmd.append("hevc_hlmediacodec");
        if (configTicket.getCodecId().equals("x264_low_bitrate"))
            cmd.append("libx264");
        //分辨率
        String resolution = String.format("scale=%dx%d",configTicket.getWidth(),configTicket.getHeight());
        cmd.append("-vf");
        cmd.append(resolution);
        //帧率
        cmd.append("-r");
        cmd.append(Integer.toString(configTicket.getFps()));
        //gop
        cmd.append("-g");
        String gop = String.format("%d",configTicket.getGop() * configTicket.getFps());
        cmd.append(gop);
        String bitrate = String.format("%dk",configTicket.getBitrate());
        //码率控制
        if(configTicket.getBitrateControl().equals("ABR")){
            cmd.append("-b:v");
            cmd.append(bitrate);
        }
        //码率控制
        if (configTicket.getBitrateControl().equals("CBR")) {
            cmd.append("-b:v");
            cmd.append(bitrate);
            cmd.append("-maxrate");
            cmd.append(bitrate);
            cmd.append("-minrate");
            cmd.append(bitrate);
        }
        cmd.append(configTicket.getVideoCachePath());
        cmd.append("-y");
        //ffmpeg -i " + path + "/video.mp4 -vframes 100 -y -f gif -s 480×320 " + path + "/video_100.gif";
        Log.v("--------------final ffmpeg commandline ---------------",cmd.toString());
        FFmpegUtil.execCmd(cmd, duration, new OnVideoProcessListener() {
            @Override
            public void onProcessStart() {
                Log.v("----------onProcessStart----------","onProcessStart");
            }

            @Override
            public void onProcessProgress(float progress) {
                Log.v("----------onProcessProgress----------","onProcessProgress");
            }

            @Override
            public void onProcessSuccess() {
                Log.v("----------onProcessSuccess----------","onProcessSuccess");
                if(mProgressCallback != null){
                    mProgressCallback.end(configTicket.getTicketID(),0);
                }
            }

            @Override
            public void onProcessFailure() {
                Log.v("----------onProcessFailure----------","onProcessFailure");
            }
        });
    }

    @Override
    public void end(int ticketID, int errorCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tvCurrTask = findViewById(R.id.tv_curr_task);
            }
        });

        if(errorCode == 0){
            Log.i("------------------ffmpeg task end--------",Integer.toString(errorCode));
            if(ticketID == mTaskTicket.getConfigTickets().size()){
                Log.i("------------------all ffmpeg task complete--------",Integer.toString(errorCode));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endTime = System.currentTimeMillis()/1000;
                        Intent intent = new Intent(MainActivity.this,ResultActivity.class);
                        intent.putExtra("transcodeTime", String.valueOf(endTime - startTime));
                        startActivity(intent);

                        TextView tvCurrTask = findViewById(R.id.tv_curr_task);
                        tvCurrTask.setText("所有任务已完成，录制的视频在SD卡的Download目录下");
                        ProgressBar progressBar = findViewById(R.id.progressBar);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }else{
                Log.i("------------------ffmpeg task next--------",Integer.toString(ticketID));
                ffmpeg_execte_task(mTaskTicket.getConfigTickets().get(ticketID));
//                ZegoUtils.getInstance().startTranscoding(mTaskTicket.getConfigTickets().get(ticketID));
            }
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView tvCurrTask = findViewById(R.id.tv_curr_task);
                    tvCurrTask.setText("执行任务失败："+errorCode);
                }
            });
        }
    }
    private void test_ffmpeg_commandline(){
        String toFile = String.format("%s/output_mediacodec.mp4",path);
        Log.v("--------------root path ---------------",path);
        long duration = 10;
        //call ffmpeg comandline
        //ffmpeg -i MyHeartWillGoOn.mp4 -c:a aac -c:v libx264 output.mp4
        Log.v("------------select source file-----------",mTaskTicket.getVideoSourcePath());
        CmdList cmd = new CmdList();
        cmd.append("ffmpeg");
        cmd.append("-i");
        cmd.append(mTaskTicket.getVideoSourcePath());
        cmd.append("-c:a");
        cmd.append("aac");
        cmd.append("-c:v");
        cmd.append("h264_mediacodec");
        cmd.append(toFile);
        cmd.append("-y");
        //ffmpeg -i " + path + "/video.mp4 -vframes 100 -y -f gif -s 480×320 " + path + "/video_100.gif";
        cmd.append("ffmpeg");
        cmd.append("-i");
        cmd.append(mTaskTicket.getVideoSourcePath());
        cmd.append("-vframes");
        cmd.append("100");
        cmd.append("-y");
        cmd.append("-f");
        cmd.append("gif");
        cmd.append("-s");
        cmd.append("480x320");
        cmd.append("/storage/emulated/0/Huawei/Hisuite/Hisuitegallery/video_100.gif");
        //ffmpeg -i .mp4 .mkv
        cmd.append("ffmpeg");
        cmd.append("-i");
        cmd.append(mTaskTicket.getVideoSourcePath());
        cmd.append("/storage/emulated/0/Huawei/Hisuite/Hisuitegallery/video_100.mkv");
        cmd.append(toFile);
        cmd.append("-y");
        Log.v("--------------final ffmpeg commandline ---------------",cmd.toString());
        FFmpegUtil.execCmd(cmd, duration, new OnVideoProcessListener() {
            @Override
            public void onProcessStart() {
                Log.v("----------onProcessStart----------","onProcessStart");
            }

            @Override
            public void onProcessProgress(float progress) {
                Log.v("----------onProcessProgress----------","onProcessProgress");
            }

            @Override
            public void onProcessSuccess() {
                Log.v("----------onProcessSuccess----------","onProcessSuccess");
            }
            @Override
            public void onProcessFailure() {
                Log.v("----------onProcessFailure----------","onProcessFailure");
            }
        });
    }
    @Override
    public void progress(int currDuration) {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(currDuration);
    }
    /**
     * A native method that is implemented by the 'ffmpegtranscoding' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native String ffmpegInfo();
}