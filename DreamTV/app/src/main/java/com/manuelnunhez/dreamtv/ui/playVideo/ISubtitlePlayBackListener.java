package com.manuelnunhez.dreamtv.ui.playVideo;

import com.manuelnunhez.dreamtv.data.model.Subtitle.SubtitleText;

public interface ISubtitlePlayBackListener {

    void setupSubtitleScreening();

    void startSyncSubtitle(Long base);

    void stopSyncSubtitle();

    int getSubtitleNavigationSeekToValue(SubtitleText selectedSubtitle);
}