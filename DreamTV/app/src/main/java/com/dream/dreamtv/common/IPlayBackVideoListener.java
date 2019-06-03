package com.dream.dreamtv.common;


public interface IPlayBackVideoListener {
    void setupVideoPlayer();
    void playVideoMode();
    void playVideo(Integer seekToPosition);
    void pauseVideo(Long position);
    void stopVideo();

}
