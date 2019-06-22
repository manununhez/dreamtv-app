package com.dream.dreamtv.ui.videoDetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.repository.AppRepository;
import com.dream.dreamtv.utils.AbsentLiveData;

class VideoDetailsViewModel extends ViewModel {

    private final AppRepository mRepository;
    private final MutableLiveData<Integer> userTaskMutableLiveData;
    private final LiveData<Resource<UserTask>> userTask;

    VideoDetailsViewModel(AppRepository appRepository) {
        mRepository = appRepository;

        userTaskMutableLiveData = new MutableLiveData<>();

        userTask = Transformations.switchMap(userTaskMutableLiveData, input -> {
            if (input == null) {
                return AbsentLiveData.create();
            }
            return mRepository.fetchUserTask();
        });

    }


    LiveData<Resource<Boolean>> requestAddToList(Task task) {
        return mRepository.requestAddToList(task.taskId, task.subLanguage, task.video.primaryAudioLanguageCode);
    }

    MutableLiveData<Resource<Boolean>> requestRemoveFromList(Task task) {
        return mRepository.requestRemoveFromList(task.taskId);
    }

    LiveData<Resource<SubtitleResponse>> fetchSubtitle(String videoId, String languageCode, int version) {
        return mRepository.fetchSubtitle(videoId, languageCode, version);
    }

    LiveData<Resource<UserTask>> fetchUserTask(int taskId) {
        userTaskMutableLiveData.setValue(taskId);
        return userTask;
//        return mRepository.fetchUserTask();
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