package com.fish.ffmpegtranscoding.entry;

import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 任务清单。存储了当次前段所有的输入的任务信息。
 *
 * @author Charles
 * @date 2022/3/9
 */
public class TaskTicket {
    public static final String ABR = "abr";
    public static final String CBR = "cbr";
    public static final String GOP2X = "2x";
    public static final String GOP4X = "4x";
    public static final String H264HW = "h264_hw";
    public static final String H265HW = "h265_hw";
    public static final String X264LOWBITRATE = "x264_low_bitrate";

    private String videoSourcePath;
    private long sourceDuration;
    private int width;
    private int height;
    private int fps;
    private Set<Integer> bitrate = new LinkedHashSet<>();
    private HashMap<String, Boolean> bitrateControl = new HashMap<>();
    private HashMap<String, Boolean> gop = new HashMap<>();
    private HashMap<String, Boolean> encoder = new HashMap<>();
    private ArrayList<ConfigTicket> configTickets;

    {
        bitrateControl.put(ABR, false);
        bitrateControl.put(CBR, false);
        gop.put(GOP2X, false);
        gop.put(GOP4X, false);
        encoder.put(H264HW, false);
        encoder.put(H265HW, false);
        encoder.put(X264LOWBITRATE, false);
    }

    public TaskTicket() {
    }

    /**
     * 通过输入的参数集合，构建配置清单列表
     */
    public void buildConfigTaskTickets() {
        if (configTickets != null) {
            configTickets.clear();
        }
        ArrayList<ConfigTicket> tickets = new ArrayList<>();
        int ticketID = 1;
        for (String encKey : encoder.keySet()) {
            if (!encoder.get(encKey)) {
                continue;
            }
            for (String bcKey : bitrateControl.keySet()) {
                if (!bitrateControl.get(bcKey)) {
                    continue;
                }
                for (String gopKey : gop.keySet()) {
                    if (!gop.get(gopKey)) {
                        continue;
                    }
                    for (Integer integer : bitrate) {
                        ConfigTicket configTicket = new ConfigTicket();
                        configTicket.setTicketID(ticketID++);
                        configTicket.setVideoSourcePath(videoSourcePath);
                        configTicket.setWidth(width);
                        configTicket.setHeight(height);
                        configTicket.setFps(fps);
                        configTicket.setBitrate(integer);
                        if (gopKey.equals(GOP2X)) {
                            configTicket.setGop(2);
                        } else if (gopKey.equals(GOP4X)) {
                            configTicket.setGop(4);
                        }
                        configTicket.setBitrateControl(bcKey);
                        configTicket.setCodecId(encKey);
                        configTicket.setVideoCachePath(getVideoCachePath(configTicket));
                        tickets.add(configTicket);
                    }
                }
            }
        }
        configTickets = tickets;
    }

    private String getVideoCachePath(@NonNull ConfigTicket configTicket) {
        String donwloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String[] rexVideoSrc = videoSourcePath.replace(".mp4", "").split("/");
        //源文件_芯片_编码器_码控_GOP_码率_时间.mp4
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        String strDate = formatter.format(curDate);
        return String.format("%s/%s_%s_%s_%s_%sxFPS_%s_%s.mp4", donwloadPath, rexVideoSrc[rexVideoSrc.length - 1], Build.HARDWARE, configTicket.getCodecId(), configTicket.getBitrateControl(), configTicket.getGop(), configTicket.getBitrate(),strDate);
    }

    public ArrayList<ConfigTicket> getConfigTickets() {
        return configTickets;
    }

    public Set<Integer> getBitrate() {
        return bitrate;
    }

    public void setBitrate(Set<Integer> bitrate) {
        this.bitrate = bitrate;
    }

    public String getVideoSourcePath() {
        return videoSourcePath;
    }

    public void setVideoSourcePath(String videoSourcePath) {
        this.videoSourcePath = videoSourcePath;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getFps() {
        return fps;
    }

    public long getSourceDuration() {
        return sourceDuration;
    }

    public void setSourceDuration(long sourceDuration) {
        this.sourceDuration = sourceDuration;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public HashMap<String, Boolean> getBitrateControl() {
        return bitrateControl;
    }

    public void setBitrateControl(HashMap<String, Boolean> bitrateControl) {
        this.bitrateControl = bitrateControl;
    }

    public HashMap<String, Boolean> getGop() {
        return gop;
    }

    public void setGop(HashMap<String, Boolean> gop) {
        this.gop = gop;
    }

    public HashMap<String, Boolean> getEncoder() {
        return encoder;
    }

    public void setEncoder(HashMap<String, Boolean> encoder) {
        this.encoder = encoder;
    }
}
