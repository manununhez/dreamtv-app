/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.dreamproject.dreamtv.ui.categories;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.leanback.app.VerticalGridSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridPresenter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.dreamproject.dreamtv.R;
import com.dreamproject.dreamtv.ViewModelFactory;
import com.dreamproject.dreamtv.data.model.Card;
import com.dreamproject.dreamtv.data.model.Resource;
import com.dreamproject.dreamtv.data.model.Resource.Status;
import com.dreamproject.dreamtv.data.model.Task;
import com.dreamproject.dreamtv.di.InjectorUtils;
import com.dreamproject.dreamtv.presenter.CardPresenterSelector;
import com.dreamproject.dreamtv.ui.videoDetails.VideoDetailsActivity;
import com.dreamproject.dreamtv.utils.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

import static com.dreamproject.dreamtv.utils.Constants.EMPTY_ITEM;
import static com.dreamproject.dreamtv.utils.Constants.FIREBASE_KEY_TASK_CATEGORY_SELECTED;
import static com.dreamproject.dreamtv.utils.Constants.FIREBASE_KEY_TASK_SELECTED;
import static com.dreamproject.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_TASK_SELECTED;
import static com.dreamproject.dreamtv.utils.Constants.INTENT_CATEGORY;
import static com.dreamproject.dreamtv.utils.Constants.INTENT_EXTRA_TOPIC_NAME;
import static com.dreamproject.dreamtv.utils.Constants.INTENT_TASK;

/**
 * An example how to use leanback's {@link VerticalGridSupportFragment}.
 */
public class CategoryFragment extends VerticalGridSupportFragment {

    private static final int COLUMNS = 3;
    private static final int ZOOM_FACTOR = FocusHighlight.ZOOM_FACTOR_MEDIUM;

    private ArrayObjectAdapter mAdapter;
    private String title;
    private LoadingDialog loadingDialog;
    private FirebaseAnalytics mFirebaseAnalytics;
    private LiveData<Resource<Task[]>> categoryLiveData;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ViewModelFactory factory = InjectorUtils.provideViewModelFactory(requireActivity());
        CategoryViewModel mViewModel = new ViewModelProvider(this, factory).get(CategoryViewModel.class);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireActivity());

        instantiateLoading();

        categoryLiveData = mViewModel.searchByKeywordCategory(title);
        categoryLiveData.removeObservers(getViewLifecycleOwner());
        categoryLiveData.observe(getViewLifecycleOwner(), resource -> {
            Status status = resource.status;
            Task[] data = resource.data;

            if (status.equals(Status.LOADING)) {
                showLoading();
            } else if (status.equals(Status.SUCCESS)) {
                if (data != null)
                    createRows(data);

                dismissLoading();
            } else if (status.equals(Status.ERROR)) {
                dismissLoading();
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (categoryLiveData != null)
            categoryLiveData.removeObservers(getViewLifecycleOwner());
    }

    //********************************************
    // Loading and progress bar related functions
    //********************************************
    private void instantiateLoading() {
        loadingDialog = new LoadingDialog(getActivity(), getString(R.string.title_loading_retrieve_tasks));
        loadingDialog.setCanceledOnTouchOutside(false);
    }

    private void dismissLoading() {
        loadingDialog.dismiss();
    }

    private void showLoading() {
        if (!requireActivity().isFinishing())
            loadingDialog.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        title = requireActivity().getIntent().getStringExtra(INTENT_EXTRA_TOPIC_NAME);

        setTitle(title);

        setupRowAdapter();

        setOnItemViewClickedListener(new ItemViewClickedListener());
    }

    private void setupRowAdapter() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter(ZOOM_FACTOR);
        gridPresenter.setNumberOfColumns(COLUMNS);
        setGridPresenter(gridPresenter);

        PresenterSelector cardPresenterSelector = new CardPresenterSelector(getActivity());
        mAdapter = new ArrayObjectAdapter(cardPresenterSelector);
        setAdapter(mAdapter);

//        prepareEntranceTransition();
//        new Handler().postDelayed(() -> {
//            createRows();
//            startEntranceTransition();
//        }, 1000);
    }


    private void createRows(Task[] data) {
        List<Card> cards = new ArrayList<>();

        for (Task task : data) {
            cards.add(new Card(task, Card.Type.SIDE_INFO));
        }

        mAdapter.addAll(0, cards);
    }

    private void firebaseLoginEvents(String category, int taskId) {
        Bundle bundle = new Bundle();

        bundle.putString(FIREBASE_KEY_TASK_CATEGORY_SELECTED, category);
        bundle.putInt(FIREBASE_KEY_TASK_SELECTED, taskId);
        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_TASK_SELECTED, bundle);

    }

    private void goToVideoDetails(Card card, Task task) {
        Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
        intent.putExtra(INTENT_TASK, task);
        intent.putExtra(INTENT_CATEGORY, card.getCategory());

        startActivity(intent);

        firebaseLoginEvents(title, task.getTaskId());
    }

    public final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Card) {
                Card card = (Card) item;
                Task task = card.getTask();

                goToVideoDetails(card, task);

            } else
                Toast.makeText(getActivity(), EMPTY_ITEM, Toast.LENGTH_SHORT).show();

        }
    }
}
