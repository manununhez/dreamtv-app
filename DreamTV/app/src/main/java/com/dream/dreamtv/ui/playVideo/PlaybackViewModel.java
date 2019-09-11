package com.dream.dreamtv.ui.playVideo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.data.model.api.ErrorReason;
import com.dream.dreamtv.data.model.api.Resource;
import com.dream.dreamtv.data.model.api.User;
import com.dream.dreamtv.data.model.api.UserTask;
import com.dream.dreamtv.data.model.api.UserTaskError;
import com.dream.dreamtv.repository.AppRepository;

import java.util.ArrayList;

public class PlaybackViewModel extends ViewModel {

    private final AppRepository mRepository;

    public PlaybackViewModel(AppRepository appRepository) {
        mRepository = appRepository;
    }

    void updateUserTask(UserTask userTask) {
        mRepository.updateUserTask(userTask);
    }

    String getListSubtitleSize() {
        return mRepository.getSubtitleSizePref();
    }

    ArrayList<ErrorReason> getReasons() {
        return mRepository.getReasons();
    }

    User getUser() {
        return mRepository.getUser();
    }

    public LiveData<Resource<UserTaskError[]>> saveErrorReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        return mRepository.saveErrorReasons(taskId, subtitleVersion, userTaskError);
    }

    public LiveData<Resource<UserTaskError[]>> updateErrorReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        return mRepository.saveErrorReasons(taskId, subtitleVersion, userTaskError);
    }
}

