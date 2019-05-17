package com.dream.dreamtv.ui;

import com.dream.dreamtv.model.Subtitle;

public interface ISubtitlePlayBackListener {
    void subtitleHandlerSyncConfig();
    void startSyncSubtitle(Long base);
    void stopSyncSubtitle();
    void showSubtitle(Subtitle subtitle);
}
