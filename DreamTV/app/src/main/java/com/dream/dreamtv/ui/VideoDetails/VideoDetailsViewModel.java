package com.dream.dreamtv.ui.VideoDetails;


import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.repository.AppRepository;
import com.dream.dreamtv.utils.Constants;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

class VideoDetailsViewModel extends ViewModel {

    private final AppRepository mRepository;


    public VideoDetailsViewModel(AppRepository appRepository) {
        mRepository = appRepository;
    }


    public void requestAddToList(TaskEntity taskEntity) {
        mRepository.requestAddToList(new TaskEntity(taskEntity.task_id, taskEntity.language, taskEntity.type, taskEntity.created, taskEntity.completed, taskEntity.modified,
                Constants.TASKS_MY_LIST, taskEntity.sync_time, taskEntity.video));
    }

    public void requestRemoveFromList(TaskEntity taskEntity) {
        mRepository.requestRemoveFromList(taskEntity.task_id, Constants.TASKS_MY_LIST);
    }

    public LiveData<TaskEntity> verifyIfTaskIsInList(TaskEntity taskEntity) {
        return mRepository.verifyIfTaskIsInList(taskEntity, Constants.TASKS_MY_LIST);
    }


}

