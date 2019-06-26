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
    private final MutableLiveData<UserTask> userTaskMutableLiveData;

    PlaybackViewModel(AppRepository appRepository) {
        mRepository = appRepository;

        userTaskMutableLiveData = new MutableLiveData<>();

    }


    void updateUserTask(UserTask userTask) {
        mRepository.updateUserTask(userTask);
    }

    LiveData<Resource<UserTaskError[]>> errorsUpdate(UserTaskError userTaskError, boolean saveError) {
        return mRepository.errorsUpdate(userTaskError, saveError);
    }

}

