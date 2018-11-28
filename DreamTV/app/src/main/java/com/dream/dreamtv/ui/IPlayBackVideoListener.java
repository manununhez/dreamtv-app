package com.dream.dreamtv.ui;


public interface IPlayBackVideoListener {
    void setupVideoPlayer();
    void playVideoMode();
    void playVideo(Integer seekToPosition);
    void pauseVideo(Long position);
    void stopVideo();

}
