package com.manuelnunhez.dreamtv.ui.playVideo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.manuelnunhez.dreamtv.data.model.ErrorReason;
import com.manuelnunhez.dreamtv.data.model.User;
import com.manuelnunhez.dreamtv.data.model.UserTask;
import com.manuelnunhez.dreamtv.data.model.UserTaskError;
import com.manuelnunhez.dreamtv.data.model.Resource;
import com.manuelnunhez.dreamtv.repository.AppRepository;

import java.util.ArrayList;

public class PlaybackViewModel extends ViewModel {

    static final int SUBTITLE_DELAY_IN_MS = 100;
    static final int DELAY_IN_MS = 1000;
    static final int AMOUNT_OF_SUBS_RANGE_FOR_VERIFICATION = 2;
    static final int DIFFERENCE_TIME_IN_MS = 1000;
    static final int BUFFER_VALUE_PB = 2;
    static final int PLAYER_PROGRESS_SHOW_DELAY = 5000;

    static final int DIFFERENCE_TIME_IN_SECS = 1;

    private final AppRepository mRepository;

    public PlaybackViewModel(AppRepository appRepository) {
        mRepository = appRepository;

    }

    void updateUserTask(UserTask userTask) {
        mRepository.updateUserTask(userTask);
    }

    int getListSubtitleSize() {
        return Integer.parseInt(mRepository.getSubtitleSizePref());
    }

    ArrayList<ErrorReason> getReasons() {
        return mRepository.getReasons();
    }

    User getUser() {
        return mRepository.getUser();
    }

    LiveData<Resource<UserTaskError[]>> saveErrorReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        return mRepository.saveErrorReasons(taskId, subtitleVersion, userTaskError);
    }

    LiveData<Resource<UserTaskError[]>> updateErrorReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        return mRepository.updateErrorReasons(taskId, subtitleVersion, userTaskError);
    }

}

