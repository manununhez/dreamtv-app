package com.dream.dreamtv.ui.PlayVideo;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.UserTaskError;
import com.dream.dreamtv.repository.AppRepository;

class PlaybackViewModel extends ViewModel {

    private final AppRepository mRepository;

    PlaybackViewModel(AppRepository appRepository) {
        mRepository = appRepository;
    }

    void updateUserTask(UserTask userTask) {
        mRepository.updateUserTask(userTask);
    }

    MutableLiveData<Resource<UserTaskError[]>> errorsUpdate(UserTaskError userTaskError, boolean saveError) {
        return mRepository.errorsUpdate(userTaskError, saveError);
    }

}

