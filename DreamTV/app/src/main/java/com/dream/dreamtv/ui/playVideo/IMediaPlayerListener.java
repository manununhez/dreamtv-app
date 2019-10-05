package com.dream.dreamtv.ui.playVideo;


public interface IMediaPlayerListener {

    void playVideo();

    void pauseVideo(Long position);

    void stopVideo();

    void startVideoReproduction();

}
