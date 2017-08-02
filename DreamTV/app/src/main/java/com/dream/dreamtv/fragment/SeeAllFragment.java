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

package com.dream.dreamtv.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.VerticalGridFragment;
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
import com.dream.dreamtv.activity.VideoDetailsActivity;
import com.dream.dreamtv.activity.PreferencesActivity;
import com.dream.dreamtv.adapter.VideoCardPresenter;
import com.dream.dreamtv.beans.JsonResponseBaseBean;
import com.dream.dreamtv.beans.TaskList;
import com.dream.dreamtv.beans.Task;
import com.dream.dreamtv.beans.Video;
import com.dream.dreamtv.conn.ConnectionManager;
import com.dream.dreamtv.conn.ResponseListener;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * An example how to use leanback's {@link VerticalGridFragment}.
 */
public class SeeAllFragment extends VerticalGridFragment {

    private static final int COLUMNS = 4;
    private static final int ZOOM_FACTOR = FocusHighlight.ZOOM_FACTOR_MEDIUM;

    private ArrayObjectAdapter mAdapter;
    private int currentPage = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("See all");
        setupRowAdapter();
        setupEventListeners();
        getUserTasks(currentPage);

    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Implement Search()", Toast.LENGTH_SHORT).show();

            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());


    }

    private void getUserTasks(int pagina) {
//        SharedPreferenceUtils.save(getActivity(), getString(R.string.dreamTVApp_token), "$2y$10$RCahpKrpkDxcqQvTo4IRju2VXiXoL3be4jJRuHRdc0SbGc4mdvqia");

//        Task task = new Task();
//        task.type_task = new String[]{"Review", "Approve"};
//        task.team = "ted";
//        task.limit = 5;
//        task.offset = 0;

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("page", String.valueOf(pagina));

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Retrieving users tasks...") {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                TaskList taskList = gson.fromJson(response, TaskList.class);
                DreamTVApp.Logger.d(taskList.toString());

                if (taskList.current_page < taskList.last_page) //Pagination
                    currentPage++;
                else
                    currentPage = -1;

                for (Task task : taskList.data) {
                    mAdapter.add(task.getVideo(MainFragment.CHECK_NEW_TASKS_CATEGORY)); //SeeAllFragments only appears in Check New Tasks Category
                }
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                DreamTVApp.Logger.d(error.getMessage());
//                loadVideos(null); //the settings section is displayed anyway
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
                DreamTVApp.Logger.d(jsonResponse.toString());
//                loadVideos(null); //the settings section is displayed anyway
            }
        };

        ConnectionManager.get(getActivity(), ConnectionManager.Urls.USER_TASKS, urlParams, responseListener, this);

    }

//    private void getUserTasks(int offset) {
//        Task task = new Task();
//        task.type_task = new String[]{"Review", "Approve"};
//        task.team = "ted";
//        task.limit = 10;
//        task.offset = offset;
//
//        final String jsonRequest = JsonUtils.getJsonRequest(getActivity(), task);
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, "Retrieving users tasks...") {
//
//            @Override
//            public void processResponse(String response) {
//                Gson gson = new Gson();
//                DreamTVApp.Logger.d(response);
//                TaskList taskList = gson.fromJson(response, TaskList.class);
//                DreamTVApp.Logger.d(taskList.toString());
//
//                currentOffset += taskList.meta.limit;
//                total_count = taskList.meta.total_count;
//                for (Task task : taskList.objects) {
//                    task.video.language_code = task.subtitle_language;
//                    mAdapter.add(task.video);
//                }
////                mAdapter.addAll(0, taskList.objects);
////                loadVideos(taskList);
//            }
//
//            @Override
//            public void processError(VolleyError error) {
//                super.processError(error);
//                DreamTVApp.Logger.d(error.getMessage());
//            }
//
//            @Override
//            public void processError(JsonResponseBaseBean jsonResponse) {
//                super.processError(jsonResponse);
//                DreamTVApp.Logger.d(jsonResponse.toString());
//            }
//        };
//
//        ConnectionManager.post(getActivity(), ConnectionManager.Urls.USER_TASKS, null, jsonRequest, responseListener, this);
//
//    }


    private void setupRowAdapter() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter(ZOOM_FACTOR);
        gridPresenter.setNumberOfColumns(COLUMNS);
        setGridPresenter(gridPresenter);

//        PresenterSelector cardPresenterSelector = new CardPresenterSelector(getActivity());

//        presenter = new ImageCardViewPresenter(mContext, themeResId);
        VideoCardPresenter videoCardPresenter = new VideoCardPresenter();

        mAdapter = new ArrayObjectAdapter(videoCardPresenter);
        setAdapter(mAdapter);

//        prepareEntranceTransition();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                createRows();
//                startEntranceTransition();
//            }
//        }, 1000);
    }

//    private void createRows() {
//        String json = Utils.inputStreamToString(getResources()
//                .openRawResource(R.raw.grid_example));
//        CardRow row = new Gson().fromJson(json, CardRow.class);
//        mAdapter.addAll(0, row.getCards());
//    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                Video video = (Video) item;
                DreamTVApp.Logger.d("Item: " + item.toString());
                Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                intent.putExtra(VideoDetailsActivity.VIDEO, video);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        VideoDetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);

            } else if (item instanceof String) {
//                if (((String) item).indexOf(getString(R.string.error_fragment)) >= 0) {
//                    Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
//                    startActivity(intent);
                if (((String) item).contains(getString(R.string.personal_settings))) {
                    Intent intent = new Intent(getActivity(), PreferencesActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                Toast.makeText(getActivity(), "Some item", Toast.LENGTH_SHORT).show();
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
//                Toast.makeText(getActivity(), "Ultima Fila Index: " + selectedIndex, Toast.LENGTH_SHORT).show();
//                Toast.makeText(getActivity(), "Offset: " + currentOffset, Toast.LENGTH_SHORT).show();
                if (currentPage != -1)
                    getUserTasks(currentPage);
            }
//            if (item instanceof Video) {
//                Video video = ((Video) item);
//                if (video.thumbnail != null)
//                    try {
//                        mBackgroundURI = new URI(video.thumbnail);
//                    } catch (URISyntaxException e) {
//                        e.printStackTrace();
//                    }
//                startBackgroundTimer();
//            }

        }
    }
}
