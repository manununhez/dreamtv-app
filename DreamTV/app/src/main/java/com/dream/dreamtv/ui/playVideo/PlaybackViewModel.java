package com.dream.dreamtv.ui.playVideo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.UserTaskError;
import com.dream.dreamtv.repository.AppRepository;

import java.util.Objects;

class PlaybackViewModel extends ViewModel {

    private final AppRepository mRepository;

    PlaybackViewModel(AppRepository appRepository) {
        mRepository = appRepository;
    }

    void updateUserTask(UserTask userTask) {
        mRepository.updateUserTask(userTask);
    }

    LiveData<Resource<UserTaskError[]>> errorsUpdate(int taskId, int subtitleVersion, UserTaskError userTaskError, boolean saveError) {
        return mRepository.errorsUpdate(taskId, subtitleVersion, userTaskError, saveError);
    }

}

