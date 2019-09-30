package com.dream.dreamtv.ui.playVideo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.data.model.ErrorReason;
import com.dream.dreamtv.data.model.User;
import com.dream.dreamtv.data.model.UserTask;
import com.dream.dreamtv.data.model.UserTaskError;
import com.dream.dreamtv.data.networking.model.Resource;
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

    LiveData<Resource<UserTaskError[]>> saveErrorReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        return mRepository.saveErrorReasons(taskId, subtitleVersion, userTaskError);
    }

    LiveData<Resource<UserTaskError[]>> updateErrorReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        return mRepository.updateErrorReasons(taskId, subtitleVersion, userTaskError);
    }
}

