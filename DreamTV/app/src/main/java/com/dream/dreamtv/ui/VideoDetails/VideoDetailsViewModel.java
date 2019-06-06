package com.dream.dreamtv.ui.VideoDetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.repository.AppRepository;

class VideoDetailsViewModel extends ViewModel {

    private final AppRepository mRepository;

    VideoDetailsViewModel(AppRepository appRepository) {
        mRepository = appRepository;

    }


    LiveData<Resource<Boolean>> requestAddToList(Task task) {
        return mRepository.requestAddToList(task.taskId, task.language, task.video.primaryAudioLanguageCode);
    }

    MutableLiveData<Resource<Boolean>> requestRemoveFromList(Task task) {
        return mRepository.requestRemoveFromList(task.taskId);
    }

    LiveData<Resource<SubtitleResponse>> fetchSubtitle(String videoId, String languageCode, int version) {
        return mRepository.fetchSubtitle(videoId, languageCode, version);
    }

    LiveData<Resource<UserTask>> fetchUserTask() {
        return mRepository.fetchUserTask();
    }

    LiveData<Resource<UserTask>> createUserTask(Task task, int mSubtitleVersion) {
        return mRepository.createUserTask(task.taskId, mSubtitleVersion);
    }

    void updateTaskByCategory(String category) {
        mRepository.updateTasksCategory(category);
    }

    boolean verifyIfTaskIsInList(Task task) {
        return mRepository.verifyIfTaskIsInList(task);
    }

}