package com.dream.dreamtv.ui.videoDetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.data.model.Category;
import com.dream.dreamtv.data.model.Subtitle;
import com.dream.dreamtv.data.model.Task;
import com.dream.dreamtv.data.model.UserTask;
import com.dream.dreamtv.data.model.VideoTest;
import com.dream.dreamtv.data.networking.model.Resource;
import com.dream.dreamtv.repository.AppRepository;
import com.dream.dreamtv.utils.AbsentLiveData;

import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class VideoDetailsViewModel extends ViewModel {
    private final AppRepository mRepository;
    private final MutableLiveData<SubtitleId> subtitleIdMLD;
    private final LiveData<Resource<Subtitle>> subtitle;

    public VideoDetailsViewModel(AppRepository appRepository) {
        mRepository = appRepository;

        this.subtitleIdMLD = new MutableLiveData<>();

        subtitle = Transformations.switchMap(subtitleIdMLD, input -> {
            if (input.isEmpty()) {
                return AbsentLiveData.create();
            }
            return mRepository.fetchSubtitle(input.videoId, input.languageCode, input.version);
        });

    }


    void setSubtitleId(String videoId, String languageCode, String version) {
        SubtitleId update = new SubtitleId(videoId, languageCode, version);

        if (Objects.equals(subtitleIdMLD.getValue(), update)) {
            Timber.d("Getting old subtitle");
            return;
        }
        Timber.d("Getting new subtitle");
        this.subtitleIdMLD.setValue(update);
    }

    void updateTaskByCategory() {
        //TODO this can be later updated to update only the necessary categories, like only my list and/or finished
        mRepository.fetchTasks();
    }

    boolean verifyIfTaskIsInList(Task task) {
        return mRepository.verifyIfTaskIsInList(task);
    }


    LiveData<Resource<Boolean>> requestAddToList(Task task) {
        return mRepository.requestAddToList(task);
    }

    LiveData<Resource<Boolean>> requestRemoveFromList(Task task) {
        return mRepository.requestRemoveFromList(task);
    }

    LiveData<Resource<Subtitle>> fetchSubtitle() {
        return subtitle;
    }

    LiveData<Resource<UserTask>> fetchUserTask() {
        return mRepository.fetchUserTask();
    }


    LiveData<Resource<UserTask>> createUserTask(Task task, int mSubtitleVersion) {
        return mRepository.createUserTask(task.getTaskId(), mSubtitleVersion);
    }

    List<VideoTest> getVideoTests() {
        return mRepository.getVideoTests();
    }


    static class SubtitleId {
        public final String videoId;
        public final String languageCode;
        public final String version;

        SubtitleId(String videoId, String languageCode, String version) {
            this.videoId = videoId;
            this.languageCode = languageCode;
            this.version = version;
        }

        boolean isEmpty() {
            return videoId == null || languageCode == null || version == null ||
                    videoId.length() == 0 || languageCode.length() == 0 || version.length() == 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SubtitleId that = (SubtitleId) o;
            return Objects.equals(videoId, that.videoId) &&
                    Objects.equals(languageCode, that.languageCode) &&
                    Objects.equals(version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(videoId, languageCode, version);
        }
    }
}