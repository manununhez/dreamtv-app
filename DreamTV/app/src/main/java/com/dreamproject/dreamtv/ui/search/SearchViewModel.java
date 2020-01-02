package com.dreamproject.dreamtv.ui.search;


import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.dreamproject.dreamtv.data.model.Task;
import com.dreamproject.dreamtv.data.model.Resource;
import com.dreamproject.dreamtv.repository.AppRepository;
import com.dreamproject.dreamtv.utils.AbsentLiveData;

public class SearchViewModel extends ViewModel {

    private final MutableLiveData<String> _query = new MutableLiveData<>();
    final LiveData<String> query = new LiveData<String>() {
        @Nullable
        @Override
        public String getValue() {
            return _query.getValue();
        }
    };
    private final AppRepository mRepository;

    private MutableLiveData<String> searchMLD;
    private LiveData<Resource<Task[]>> search;

    public SearchViewModel(AppRepository appRepository) {
        mRepository = appRepository;

        searchMLD = new MutableLiveData<>();


        search = Transformations.switchMap(searchMLD, input -> {
            if (input.isEmpty()) {
                return AbsentLiveData.create();
            }
            return mRepository.search(input);
        });


    }

    LiveData<Boolean> resultsFound() {
        MediatorLiveData<Boolean> _resultsFound = new MediatorLiveData<>();

        _resultsFound.addSource(search, resource -> {
            Resource.Status status = resource.status;
            Task[] results = resource.data;
            if (status.equals(Resource.Status.SUCCESS)) {

                if (results != null && results.length > 0)
                    _resultsFound.setValue(true);
                else
                    _resultsFound.setValue(false);
            }
        });

        return _resultsFound;
    }

    LiveData<Resource<Task[]>> search() {
        return search;
    }


    void doQueryAndSearch(String query) {
        _query.setValue(query);

        searchMLD.setValue(query);
    }
}

