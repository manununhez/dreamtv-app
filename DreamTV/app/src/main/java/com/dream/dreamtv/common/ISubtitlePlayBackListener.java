package com.dream.dreamtv.common;

import com.dream.dreamtv.model.Subtitle;
import com.dream.dreamtv.model.UserTaskError;

import java.util.ArrayList;

public interface ISubtitlePlayBackListener {
    void subtitleHandlerSyncConfig();
    void startSyncSubtitle(Long base);
    void stopSyncSubtitle();
    void showSubtitle(Subtitle subtitle);
    void showSubtitleWithErrors(Subtitle subtitle);
    void showSubtitle(Subtitle subtitle, ArrayList<UserTaskError> userTaskErrorListForSttlPos);
}
