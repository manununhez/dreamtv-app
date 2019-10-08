package com.manuelnunhez.dreamtv.ui.categories;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.manuelnunhez.dreamtv.data.model.Task;
import com.manuelnunhez.dreamtv.data.model.Resource;
import com.manuelnunhez.dreamtv.repository.AppRepository;

public class CategoryViewModel extends ViewModel {

    private final AppRepository mRepository;

    public CategoryViewModel(AppRepository appRepository) {
        mRepository = appRepository;

    }

    LiveData<Resource<Task[]>> searchByKeywordCategory(String category) {
        return mRepository.searchByKeywordCategory(category);
    }

}

