package com.dream.dreamtv.ui.Main;


import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.dream.dreamtv.repository.AppRepository;

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link AppRepository}
 */
public class MainViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppRepository mRepository;

    public MainViewModelFactory(AppRepository repository) {
        this.mRepository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MainViewModel(mRepository);
    }
}
