package com.dream.dreamtv.ui.VideoDetails;


import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.repository.AppRepository;

import java.util.Objects;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import static com.dream.dreamtv.utils.Constants.TASKS_CONTINUE_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_MY_LIST_CAT;

class VideoDetailsViewModel extends ViewModel {

    private final AppRepository mRepository;
//    private final MutableLiveData<SubtitleId> subtitleId;
//    private final LiveData<Resource<SubtitleResponse>> subtitle;


    VideoDetailsViewModel(AppRepository appRepository) {
        mRepository = appRepository;

//        subtitleId = new MutableLiveData<>();
//
//        subtitle = Transformations.switchMap(subtitleId, input -> {
//            if (input.isEmpty()) {
//                return AbsentLiveData.create();
//            }
//            return mRepository.fetchSubtitle(input.videoId, input.languageCode, input.version);
//        });
    }


    LiveData<Resource<Boolean>> requestAddToList(TaskEntity taskEntity) {
        return mRepository.requestAddToList(new TaskEntity(taskEntity.taskId, taskEntity.language,
                taskEntity.type, taskEntity.created, taskEntity.completed, taskEntity.modified,
                TASKS_MY_LIST_CAT, taskEntity.syncTime, taskEntity.video));
    }

    MutableLiveData<Resource<Boolean>> requestRemoveFromList(TaskEntity taskEntity) {
        return mRepository.requestRemoveFromList(taskEntity.taskId, TASKS_MY_LIST_CAT);
    }

    LiveData<Resource<SubtitleResponse>> fetchSubtitle(String videoId, String languageCode, int version) {
//        SubtitleId update = new SubtitleId(videoId, languageCode, version);
//        //Only if subtitle is a new input value
//        if (!Objects.equals(subtitleId.getValue(), update)) {
//            subtitleId.setValue(update);
//        }
//        return subtitle;
        return mRepository.fetchSubtitle(videoId, languageCode, version);
    }

    LiveData<Resource<UserTask>> fetchUserTaskErrorDetails(int taskId) {
        return mRepository.fetchTaskErrorDetails(taskId);
    }

    LiveData<Resource<UserTask>> createUserTask(TaskEntity taskEntity, int mSubtitleVersion) {
        return mRepository.createUserTask(new TaskEntity(taskEntity.taskId, taskEntity.language,
                taskEntity.type, taskEntity.created, taskEntity.completed, taskEntity.modified,
                TASKS_CONTINUE_CAT, taskEntity.syncTime, taskEntity.video), mSubtitleVersion);
    }

    void updateTaskByCategory(String category){
        mRepository.updateTasksCategory(category);
    }

    boolean verifyIfTaskIsInList(TaskEntity taskEntity) {
        return mRepository.verifyIfTaskIsInList(taskEntity);
    }


    static class SubtitleId {
        public final String videoId;
        public final String languageCode;
        int version;

        SubtitleId(String videoId, String languageCode, int version) {
            this.videoId = videoId == null ? null : videoId.trim();
            this.languageCode = languageCode == null ? null : languageCode.trim();
            this.version = version;
        }

        boolean isEmpty() {
            return videoId == null || languageCode == null || videoId.length() == 0 || languageCode.length() == 0;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SubtitleId that = (SubtitleId) o;
            return version == that.version &&
                    Objects.equals(videoId, that.videoId) &&
                    Objects.equals(languageCode, that.languageCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(videoId, languageCode, version);
        }
    }

}

