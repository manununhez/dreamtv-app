package com.dream.dreamtv.ui;

import com.dream.dreamtv.model.Subtitle;
import com.dream.dreamtv.model.UserTask;

public interface IPlayBackVideoListener {
    void setupVideoPlayer();
    void playVideoMode();
    void playVideo(Integer seekToPosition);
    void pauseVideo(Long position);
    void stopVideo();

}
