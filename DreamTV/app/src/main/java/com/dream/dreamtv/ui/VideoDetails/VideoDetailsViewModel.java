package com.dream.dreamtv.ui.VideoDetails;


import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.repository.AppRepository;
import com.dream.dreamtv.utils.Constants;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

class VideoDetailsViewModel extends ViewModel {

    private final AppRepository mRepository;


    VideoDetailsViewModel(AppRepository appRepository) {
        mRepository = appRepository;
    }


    void requestAddToList(TaskEntity taskEntity) {
        mRepository.requestAddToList(new TaskEntity(taskEntity.task_id, taskEntity.language, taskEntity.type, taskEntity.created, taskEntity.completed, taskEntity.modified,
                Constants.TASKS_MY_LIST, taskEntity.sync_time, taskEntity.video));
    }

    void requestRemoveFromList(TaskEntity taskEntity) {
        mRepository.requestRemoveFromList(taskEntity.task_id, Constants.TASKS_MY_LIST);
    }

    LiveData<TaskEntity> verifyIfTaskIsInList(TaskEntity taskEntity) {
        return mRepository.verifyIfTaskIsInList(taskEntity, Constants.TASKS_MY_LIST);
    }

    LiveData<Resource<SubtitleResponse>> responseFromFetchSubtitle(){
        return mRepository.responseFromFetchSubtitle();
    }


    public void fetchSubtitle(String videoId, String languageCode, int version) {
        mRepository.fetchSubtitle(videoId, languageCode, version);
    }

    public void fetchTaskErrorDetails(int taskId) {
        mRepository.fetchTaskErrorDetails(taskId);
    }

    public LiveData<Object> responseFromFetchTaskDetails() {
        return null;
    }
}

