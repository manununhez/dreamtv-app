/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.activity.DetailsActivity;
import com.dream.dreamtv.activity.MainActivity;
import com.dream.dreamtv.activity.PlaybackOverlayActivity;
import com.dream.dreamtv.activity.YoutubeActivityApiShowcase;
import com.dream.dreamtv.adapter.DetailsDescriptionPresenter;
import com.dream.dreamtv.beans.JsonResponseBaseBean;
import com.dream.dreamtv.beans.SubtitleJson;
import com.dream.dreamtv.beans.SubtitleVtt;
import com.dream.dreamtv.beans.Video;
import com.dream.dreamtv.conn.ConnectionManager;
import com.dream.dreamtv.conn.ResponseListener;
import com.dream.dreamtv.dialog.DialogChooseLanguageActivity;
import com.dream.dreamtv.utils.Utils;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;


/*
 * LeanbackDetailsFragment extends DetailsFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related videos.
 */
public class VideoDetailsFragment extends DetailsFragment {
    private static final String TAG = "VideoDetailsFragment";

    private static final int ACTION_PLAY_VIDEO = 1;
    private static final int ACTION_CHOOSE_SUBTITLE_LANG = 2;
    private static final int ACTION_ADD_MY_LIST = 3;
    private static final int CHOOSE_SUB_LANG_CODE = 152;

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;

    private static final int NUM_COLS = 10;

    private Video mSelectedVideo;

    private ArrayObjectAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;

    private BackgroundManager mBackgroundManager;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private DetailsOverviewRow rowPresenter;

    private String selectedLanguageCode = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);

        prepareBackgroundManager();

        mSelectedVideo = (Video) getActivity().getIntent()
                .getParcelableExtra(DetailsActivity.VIDEO);
        if (mSelectedVideo != null) {
            setupAdapter();
            setupDetailsOverviewRow();
            setupDetailsOverviewRowPresenter();
//            setupMovieListRow();
//            setupMovieListRowPresenter();
            updateBackground(mSelectedVideo.thumbnail);
            setOnItemViewClickedListener(new ItemViewClickedListener());
        } else {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    protected void updateBackground(String uri) {
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<GlideDrawable>(mMetrics.widthPixels, mMetrics.heightPixels) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable> glideAnimation) {
                        mBackgroundManager.setDrawable(resource);
                    }
                });
    }

    private void setupAdapter() {
        mPresenterSelector = new ClassPresenterSelector();
//        mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
        mAdapter = new ArrayObjectAdapter(mPresenterSelector);
        setAdapter(mAdapter);
    }


    private void setupDetailsOverviewRow() {
        Log.d(TAG, "doInBackground: " + mSelectedVideo.toString());
        rowPresenter = new DetailsOverviewRow(mSelectedVideo);
        rowPresenter.setImageDrawable(getResources().getDrawable(R.drawable.default_background));
        int width = Utils.convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_WIDTH);
        int height = Utils.convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_HEIGHT);

        Glide.with(getActivity())
                .load(mSelectedVideo.thumbnail)
                .centerCrop()
                .error(R.drawable.default_background)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
                        Log.d(TAG, "details overview card image url ready: " + resource);
                        rowPresenter.setImageDrawable(resource);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }
                });

        rowPresenter.addAction(new Action(ACTION_PLAY_VIDEO, getResources().getString(
                R.string.play_video)));
        rowPresenter.addAction(new Action(ACTION_CHOOSE_SUBTITLE_LANG, getResources().getString(
                R.string.dialog_choose_language_title)));
        rowPresenter.addAction(new Action(ACTION_ADD_MY_LIST, getResources().getString(
                R.string.add_to_my_list)));

        mAdapter.add(rowPresenter);
    }

    private void setupDetailsOverviewRowPresenter() {
        // Set detail background and style.
        DetailsOverviewRowPresenter detailsPresenter =
                new DetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
        detailsPresenter.setBackgroundColor(getResources().getColor(R.color.selected_background));
        detailsPresenter.setStyleLarge(true);

        // Hook up transition element.
        detailsPresenter.setSharedElementEnterTransition(getActivity(),
                DetailsActivity.SHARED_ELEMENT_NAME);

        detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == ACTION_PLAY_VIDEO) {
                    getSubtitleVtt(mSelectedVideo);
                } else if (action.getId() == ACTION_CHOOSE_SUBTITLE_LANG) {
                    Intent intent = new Intent(getActivity(), DialogChooseLanguageActivity.class);
                    intent.putExtra(DetailsActivity.VIDEO, mSelectedVideo);
                    startActivityForResult(intent, CHOOSE_SUB_LANG_CODE);
                } else {
                    Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
    }


    private void getSubtitleVtt(final Video video) {
        String url = ConnectionManager.Urls.VIDEOS.value + '/' + video.id + "/languages/" + (selectedLanguageCode.isEmpty() ? video.languages.get(1).code : selectedLanguageCode) + "/subtitles";
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("sub_format", "vtt");

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true) {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                SubtitleVtt mSubtitleVtt = gson.fromJson(response, SubtitleVtt.class);
                DreamTVApp.Logger.d(mSubtitleVtt.toString());

                mSelectedVideo.subtitle_vtt = mSubtitleVtt;
                getSubtitleJson(mSelectedVideo);
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

        ConnectionManager.get(getActivity(), url, urlParams, responseListener, this);

    }

    private void getSubtitleJson(final Video video) {
        String url = ConnectionManager.Urls.VIDEOS.value + '/' + video.id + "/languages/" + (selectedLanguageCode.isEmpty() ? video.languages.get(1).code : selectedLanguageCode) + "/subtitles";
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("sub_format", "json");

        ResponseListener responseListener = new ResponseListener(getActivity(), true, true) {

            @Override
            public void processResponse(String response) {
                Gson gson = new Gson();
                DreamTVApp.Logger.d(response);
                SubtitleJson mSubtitleJson = gson.fromJson(response, SubtitleJson.class);
                DreamTVApp.Logger.d(mSubtitleJson.toString());
                mSelectedVideo.subtitle_json = mSubtitleJson;

                Intent intent;

                if (mSelectedVideo.isFromYoutube()) {
//                        intent = new Intent(getActivity(), YoutubeActivityFragment.class);
                    intent = new Intent(getActivity(), YoutubeActivityApiShowcase.class);

                } else {
                    intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
                }
                intent.putExtra(DetailsActivity.VIDEO, mSelectedVideo);
                startActivity(intent);
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

        ConnectionManager.get(getActivity(), url, urlParams, responseListener, this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == CHOOSE_SUB_LANG_CODE) {
                selectedLanguageCode = data.getStringExtra("selectedCodeLanguage");
            }

    }

    //    private void setupMovieListRow() {
//        String subcategories[] = {getString(R.string.related_movies), "Mas videos", "More movies", "Documentaries"};
//        List<Movie> list = MovieList.list;
//
//        int i;
//        for (i = 0; i < subcategories.length; i++) {
//            if (i != 0) {
//                Collections.shuffle(list);
//            }
//            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
//            for (int j = 0; j < NUM_COLS; j++) {
//                listRowAdapter.add(list.get(j % 5));
//            }
//            HeaderItem header = new HeaderItem(i, subcategories[i]);
//            mAdapter.add(new ListRow(header, listRowAdapter));
//        }
//    }

    private void setupMovieListRowPresenter() {
        mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                Video video = (Video) item;
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(getResources().getString(R.string.movie), mSelectedVideo);
                intent.putExtra(getResources().getString(R.string.should_start), true);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            }
        }
    }
}
