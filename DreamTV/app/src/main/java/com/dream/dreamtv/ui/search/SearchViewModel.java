package com.dream.dreamtv.ui.search;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.data.model.api.Resource;
import com.dream.dreamtv.data.model.api.Task;
import com.dream.dreamtv.repository.AppRepository;

public class SearchViewModel extends ViewModel {

    private final AppRepository mRepository;

    public SearchViewModel(AppRepository appRepository) {
        mRepository = appRepository;

    }

    LiveData<Resource<Task[]>> search(String query) {
        return mRepository.search(query);
    }


}

