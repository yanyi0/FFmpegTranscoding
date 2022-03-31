package com.fish.ffmpegtranscoding.callback;

/**
 * @author Charles
 * @date 2022/3/9
 */
public interface RecordProgressCallback {
    void begin(int ticketID, String ticketInfo, int totalDuration, int errorCode);
    void end(int ticketID, int errorCode);
    void progress(int currDuration);
}
