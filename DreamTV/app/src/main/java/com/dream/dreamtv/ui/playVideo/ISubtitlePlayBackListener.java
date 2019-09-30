package com.dream.dreamtv.ui.playVideo;

import com.dream.dreamtv.data.model.Subtitle.SubtitleText;
import com.dream.dreamtv.data.model.UserTaskError;

import java.util.ArrayList;

public interface ISubtitlePlayBackListener {
    void subtitleHandlerSyncConfig();

    void startSyncSubtitle(Long base);

    void stopSyncSubtitle();

    void showSubtitle(SubtitleText subtitle);

    void showSubtitleWithErrors(SubtitleText subtitle);

    void showSubtitle(SubtitleText subtitle, ArrayList<UserTaskError> userTaskErrorListForSttlPos);
}
