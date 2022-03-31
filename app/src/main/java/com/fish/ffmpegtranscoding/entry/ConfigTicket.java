package com.fish.ffmpegtranscoding.entry;

/**
 * 配置清单，执行单次任务时所需要的参数。
 * @author Charles
 * @date 2022/3/9
 */
public class ConfigTicket {
    private int ticketID;
    private String videoSourcePath;
    private String videoCachePath;
    private int width;
    private int height;
    private int fps;
    private int bitrate;
    private String codecId;
    private int gop;
    private String bitrateControl;

    @Override
    public String toString() {
        return String.format("配置%s:(%s*%s) (%sfps) (%skbps) (%s) (%s) (%sxGOP)",ticketID,width,height,fps,bitrate,codecId,bitrateControl,gop);
    }

    public int getTicketID() {
        return ticketID;
    }

    public void setTicketID(int ticketID) {
        this.ticketID = ticketID;
    }

    public String getBitrateControl() {
        return bitrateControl;
    }

    public void setBitrateControl(String bitrateControl) {
        this.bitrateControl = bitrateControl;
    }

    public int getGop() {
        return gop;
    }

    public void setGop(int gop) {
        this.gop = gop;
    }

    public String getCodecId() {
        return codecId;
    }

    public void setCodecId(String codecId) {
        this.codecId = codecId;
    }

    public String getVideoCachePath() {
        return videoCachePath;
    }

    public void setVideoCachePath(String videoCachePath) {
        this.videoCachePath = videoCachePath;
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

    public void setFps(int fps) {
        this.fps = fps;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }
}
