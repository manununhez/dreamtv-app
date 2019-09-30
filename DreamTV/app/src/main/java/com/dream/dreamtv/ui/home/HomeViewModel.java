package com.dream.dreamtv.ui.home;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.data.model.Category.Type;
import com.dream.dreamtv.data.model.TasksList;
import com.dream.dreamtv.data.model.User;
import com.dream.dreamtv.data.model.VideoDuration;
import com.dream.dreamtv.data.model.VideoTopic;
import com.dream.dreamtv.data.networking.model.Resource;
import com.dream.dreamtv.repository.AppRepository;

public class HomeViewModel extends ViewModel {

    private final AppRepository mRepository;
    private final LiveData<Resource<VideoTopic[]>> categoriesLD;
    private LiveData<Resource<TasksList[]>> tasksLD;
    private MutableLiveData<Boolean> reloadTrigger;

    public HomeViewModel(AppRepository appRepository) {
        mRepository = appRepository;

        reloadTrigger = new MutableLiveData<>();

        //initSyncData
        mRepository.fetchReasons();
        mRepository.fetchVideoTestsDetails();

        categoriesLD = mRepository.fetchCategories();

//        tasksLD = Transformations.switchMap(reloadTrigger, input -> {
//            if (!input)
//                return AbsentLiveData.create();
//
//            return mRepository.fetchTasks(Type.ALL);
//
//        });

        syncTasks();

    }


    void syncTasks() {
        tasksLD = mRepository.fetchTasks(Type.ALL);
    }

    LiveData<Resource<TasksList[]>> fetchTasks() {
        return tasksLD;
    }

    void refreshTasks(boolean refreshAgain) {
        reloadTrigger.setValue(refreshAgain);
    }


    boolean getTestingMode() {
        return mRepository.getTestingModePref();
    }

    User getUser() {
        return mRepository.getUser();
    }

    void updateUser(User userData) {
        mRepository.updateUser(userData);
    }


    LiveData<Resource<VideoTopic[]>> fetchCategories() {
        return categoriesLD;
    }


    String getSubtitleSize() {
        return mRepository.getSubtitleSizePref();
    }

    VideoDuration getVideoDuration() {
        return mRepository.getVideoDurationPref();
    }

}

