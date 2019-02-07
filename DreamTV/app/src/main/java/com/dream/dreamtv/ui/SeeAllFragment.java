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

package com.dream.dreamtv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.VerticalGridSupportFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.FocusHighlight;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.presenter.VideoCardPresenter;
import com.dream.dreamtv.model.JsonResponseBaseBean;
import com.dream.dreamtv.model.TaskResponse;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.model.Video;
import com.dream.dreamtv.network.ConnectionManager;
import com.dream.dreamtv.network.ResponseListener;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An example how to use leanback's {@link VerticalGridSupportFragment}.
 */
public class SeeAllFragment extends VerticalGridSupportFragment {

    private static final int COLUMNS = 4;
    private static final int ZOOM_FACTOR = FocusHighlight.ZOOM_FACTOR_MEDIUM;
    private static final String EMPTY_ITEM = "Some item";

    public static final String PARAM_PAGE = "page";
    public static final String PARAM_TYPE = "type";

    private ArrayObjectAdapter mAdapter;
    private int currentPage = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.title_see_all_category));
        setupRowAdapter();
        setupEventListeners();
        getUserTasks(currentPage);

    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), getString(R.string.title_search), Toast.LENGTH_SHORT).show();

            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());


    }

    private void getUserTasks(int pagina) {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put(PARAM_PAGE, String.valueOf(pagina));


        //testing mode
        String mode = ((DreamTVApp) getActivity().getApplication()).getTestingMode();
        if (mode == null || mode.equals(getString(R.string.text_no_option)))
            urlParams.put(PARAM_TYPE, Constants.TASKS_ALL);
        else if (mode.equals(getString(R.string.text_yes_option)))
            urlParams.put(PARAM_TYPE, Constants.TASKS_TEST);


        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_retrieve_user_tasks)) {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<TaskResponse>>() {
                };
                JsonResponseBaseBean<TaskResponse> jsonResponse = JsonUtils.getJsonResponse(response, type);
                TaskResponse taskResponse = jsonResponse.data;

                DreamTVApp.Logger.d(taskResponse.toString());

                if (taskResponse.current_page < taskResponse.last_page) //Pagination
                    currentPage++;
                else
                    currentPage = -1;

                for (Task task : taskResponse.data) {
                    mAdapter.add(task.getVideo(Constants.CHECK_NEW_TASKS_CATEGORY)); //SeeAllFragments only appears in Check New Tasks Category
                }
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                DreamTVApp.Logger.d(error.getMessage());
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                DreamTVApp.Logger.d(jsonResponse.toString());
            }
        };


        ConnectionManager.get(getActivity(), ConnectionManager.Urls.TASKS, urlParams, responseListener, this);


    }

    private void setupRowAdapter() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter(ZOOM_FACTOR);
        gridPresenter.setNumberOfColumns(COLUMNS);
        setGridPresenter(gridPresenter);

        VideoCardPresenter videoCardPresenter = new VideoCardPresenter();

        mAdapter = new ArrayObjectAdapter(videoCardPresenter);
        setAdapter(mAdapter);

    }


    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                Video video = (Video) item;
                DreamTVApp.Logger.d("Item: " + item.toString());
                Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                intent.putExtra(Constants.VIDEO, video);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        Objects.requireNonNull(getActivity()),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        Constants.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);

            } else if (item instanceof String) {

                if (((String) item).contains(Objects.requireNonNull(getActivity()).getApplicationContext().getString(R.string.title_video_settings))) {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                Toast.makeText(getActivity(), EMPTY_ITEM, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {


            int selectedIndex = mAdapter.indexOf(item);

            if (selectedIndex != -1 && (((mAdapter.size() - 1 - COLUMNS) < selectedIndex)
                    && ((mAdapter.size() - 1) >= selectedIndex))) {
                if (currentPage != -1)
                    getUserTasks(currentPage);
            }
        }
    }
}
