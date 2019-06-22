package com.dream.dreamtv.ui.search;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.repository.AppRepository;

class SearchViewModel extends ViewModel {

    private final AppRepository mRepository;

    SearchViewModel(AppRepository appRepository) {
        mRepository = appRepository;

    }

    LiveData<Resource<Task[]>> search(String query) {
        return mRepository.search(query);
    }


}
