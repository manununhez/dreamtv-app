package com.dreamproject.dreamtv.ui.playVideo;


public interface IMediaPlayerListener {

    void playVideo();

    void pauseVideo(Long position);

    void stopVideo();

    void startVideoReproduction();

}
