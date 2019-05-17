package com.dream.dreamtv.ui.VideoDetails;


import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.repository.AppRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import static com.dream.dreamtv.utils.Constants.*;

class VideoDetailsViewModel extends ViewModel {

    private final AppRepository mRepository;


    VideoDetailsViewModel(AppRepository appRepository) {
        mRepository = appRepository;
    }


    void requestAddToList(TaskEntity taskEntity) {
        mRepository.requestAddToList(new TaskEntity(taskEntity.taskId, taskEntity.language,
                taskEntity.type, taskEntity.created, taskEntity.completed, taskEntity.modified,
                TASKS_MY_LIST_CAT, taskEntity.syncTime, taskEntity.video));
    }

    void requestRemoveFromList(TaskEntity taskEntity) {
        mRepository.requestRemoveFromList(taskEntity.taskId, TASKS_MY_LIST_CAT);
    }

    void fetchSubtitle(String videoId, String languageCode, int version) {
        mRepository.fetchSubtitle(videoId, languageCode, version);
    }

    void fetchUserTaskErrorDetails(int taskId) {
        mRepository.fetchTaskErrorDetails(taskId);
    }

    void createUserTask(TaskEntity taskEntity, int mSubtitleVersion) {
        mRepository.createUserTask(new TaskEntity(taskEntity.taskId, taskEntity.language,
                taskEntity.type, taskEntity.created, taskEntity.completed, taskEntity.modified,
                TASKS_CONTINUE_CAT, taskEntity.syncTime, taskEntity.video), mSubtitleVersion);
    }

    LiveData<TaskEntity> verifyIfTaskIsInList(TaskEntity taskEntity) {
        return mRepository.verifyIfTaskIsInList(taskEntity, TASKS_MY_LIST_CAT);
    }

    LiveData<Resource<SubtitleResponse>> responseFromFetchSubtitle() {
        return mRepository.responseFromFetchSubtitle();
    }

    LiveData<Resource<UserTask>> responseFromFetchUserTaskErrorDetails() {
        return mRepository.responseFromFetchUserTaskErrorDetails();
    }

    LiveData<Resource<UserTask>> responseFromCreateUserTask() {
        return mRepository.responseFromCreateUserTask();
    }
}

