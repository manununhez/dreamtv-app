package com.dream.dreamtv.ui.playVideo;


public interface IPlayBackVideoListener {
    void setupVideoPlayer();
    void playVideoMode();
    void playVideo(Integer seekToPosition);
    void pauseVideo(Long position);
    void stopVideo();

}
