package com.dream.dreamtv.ui.categories;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.data.model.Task;
import com.dream.dreamtv.data.networking.model.Resource;
import com.dream.dreamtv.repository.AppRepository;

public class CategoryViewModel extends ViewModel {

    private final AppRepository mRepository;

    public CategoryViewModel(AppRepository appRepository) {
        mRepository = appRepository;

    }

    LiveData<Resource<Task[]>> searchByKeywordCategory(String category) {
        return mRepository.searchByKeywordCategory(category);
    }

}

