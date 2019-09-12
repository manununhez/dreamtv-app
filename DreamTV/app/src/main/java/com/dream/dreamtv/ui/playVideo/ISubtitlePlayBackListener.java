package com.dream.dreamtv.ui.playVideo;

import com.dream.dreamtv.data.networking.model.Subtitle;
import com.dream.dreamtv.data.networking.model.UserTaskError;

import java.util.ArrayList;

public interface ISubtitlePlayBackListener {
    void subtitleHandlerSyncConfig();
    void startSyncSubtitle(Long base);
    void stopSyncSubtitle();
    void showSubtitle(Subtitle subtitle);
    void showSubtitleWithErrors(Subtitle subtitle);
    void showSubtitle(Subtitle subtitle, ArrayList<UserTaskError> userTaskErrorListForSttlPos);
}
