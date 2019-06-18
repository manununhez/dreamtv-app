package com.dream.dreamtv.ui.Categories;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.repository.AppRepository;

class CategoryViewModel extends ViewModel {

    private final AppRepository mRepository;

    CategoryViewModel(AppRepository appRepository) {
        mRepository = appRepository;

    }

    LiveData<Resource<Task[]>> searchByKeywordCateory(String category){
        return mRepository.searchByKeywordCategory(category);
    }

}

