package com.dreamproject.dreamtv.ui.playVideo;


public interface IPlaybackVideoListener {

    void setupInfoPlayer();

    void setupVideoPlayer();

    void playbackHandlerSync();

    void stateVideoPlaying(Long position);

    void stateVideoPaused(Long position);

    void stateVideoCompleted();

    void stateVideoTerminated();

    void updateSubtitleScreening();

    void updateProgressPlayer();

}
