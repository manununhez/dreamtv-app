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

package com.dream.dreamtv.ui.categories;

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
import androidx.lifecycle.ViewModelProviders;

import com.dream.dreamtv.R;
import com.dream.dreamtv.model.Card;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.presenter.CardPresenterSelector;
import com.dream.dreamtv.ui.videoDetails.VideoDetailsActivity;
import com.dream.dreamtv.utils.InjectorUtils;
import com.dream.dreamtv.utils.LoadingDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.dream.dreamtv.utils.Constants.EMPTY_ITEM;
import static com.dream.dreamtv.utils.Constants.INTENT_CATEGORY;
import static com.dream.dreamtv.utils.Constants.INTENT_EXTRA_TOPIC_NAME;
import static com.dream.dreamtv.utils.Constants.INTENT_TASK;

/**
 * An example how to use leanback's {@link VerticalGridSupportFragment}.
 */
public class CategoryFragment extends VerticalGridSupportFragment {

    private static final int COLUMNS = 3;
    private static final int ZOOM_FACTOR = FocusHighlight.ZOOM_FACTOR_MEDIUM;

    private ArrayObjectAdapter mAdapter;
    private String title;
    private LoadingDialog loadingDialog;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        CategoryViewModelFactory factory = InjectorUtils.provideCategoryViewModelFactory(Objects.requireNonNull(getActivity()));
        CategoryViewModel mViewModel = ViewModelProviders.of(this, factory).get(CategoryViewModel.class);

        instantiateLoading();

        LiveData<Resource<Task[]>> resourceLiveData = mViewModel.searchByKeywordCateory(title);
        resourceLiveData.observe(getViewLifecycleOwner(), resource -> {
            if (resource.status.equals(Resource.Status.LOADING)) {
                showLoading();
            } else if (resource.status.equals(Resource.Status.SUCCESS)) {
                if (resource.data != null)
                    createRows(resource.data);

                dismissLoading();
            } else if (resource.status.equals(Resource.Status.ERROR)) {
                dismissLoading();
            }
        });





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
        if (!Objects.requireNonNull(getActivity()).isFinishing())
            loadingDialog.show();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        title = Objects.requireNonNull(getActivity()).getIntent().getStringExtra(INTENT_EXTRA_TOPIC_NAME);

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
            cards.add(new Card(task, title));
        }

        mAdapter.addAll(0, cards);
    }

    public final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Card) {
                Card value = (Card) item;
                Task task = value.getTask();

                Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                intent.putExtra(INTENT_TASK, task);
                intent.putExtra(INTENT_CATEGORY, value.getCategory());

                startActivity(intent);

            } else
                Toast.makeText(getActivity(), EMPTY_ITEM, Toast.LENGTH_SHORT).show();

        }
    }
}
