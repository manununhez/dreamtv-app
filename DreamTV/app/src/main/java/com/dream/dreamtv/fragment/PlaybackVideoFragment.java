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
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRow.FastForwardAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.PlayPauseAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.RepeatAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.RewindAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.ShuffleAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipNextAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipPreviousAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.ThumbsDownAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.ThumbsUpAction;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.activity.VideoDetailsActivity;
import com.dream.dreamtv.beans.Video;
import com.dream.dreamtv.utils.Constants;


/*
 * Class for video playback with media control
 */
public class PlaybackVideoFragment extends android.support.v17.leanback.app.PlaybackOverlayFragment {
    private static final String TAG = "PlaybackControlsFragmnt";

    private static final boolean SHOW_DETAIL = true;
    private static final boolean HIDE_MORE_ACTIONS = false;
    private static final int PRIMARY_CONTROLS = 5;
    private static final boolean SHOW_IMAGE = true;//PRIMARY_CONTROLS <= 5;
    private static final int BACKGROUND_TYPE = PlaybackVideoFragment.BG_LIGHT;

    public static final int DEFAULT_UPDATE_PERIOD = 1000;
    public static final int UPDATE_PERIOD = 16;
    public static final int SIMULATED_BUFFERED_TIME = 10000;

    private ArrayObjectAdapter mRowsAdapter;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private PlayPauseAction mPlayPauseAction;
    private FastForwardAction mFastForwardAction;
    private RewindAction mRewindAction;
    private PlaybackControlsRow mPlaybackControlsRow;
    //    private ArrayList<Video> mItems = new ArrayList<Video>();
//    private int mCurrentItem;
    private Handler mHandler;
    private Runnable mRunnable;
    private Video mSelectedVideo;
    private OnPlayPauseClickedListener mCallback;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSelectedVideo = (Video) getActivity()
                .getIntent().getParcelableExtra(Constants.VIDEO);

        mHandler = new Handler();

        setBackgroundType(BACKGROUND_TYPE);
        setFadingEnabled(false);

        setupRows();

        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                       RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.i(TAG, "onItemSelected: " + item + " row " + row);
            }
        });
        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                      RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.i(TAG, "onItemClicked: " + item + " row " + row);
            }
        });

//        togglePlayback(mPlayPauseAction.getIndex() == PlayPauseAction.PLAY);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof OnPlayPauseClickedListener) {
            mCallback = (OnPlayPauseClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlayPauseClickedListener");
        }
    }

    private void setupRows() {

        ClassPresenterSelector ps = new ClassPresenterSelector();

        PlaybackControlsRowPresenter playbackControlsRowPresenter;
        if (SHOW_DETAIL) {
            playbackControlsRowPresenter = new PlaybackControlsRowPresenter(
                    new DescriptionPresenter());
        } else {
            playbackControlsRowPresenter = new PlaybackControlsRowPresenter();
        }

        playbackControlsRowPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            public void onActionClicked(Action action) {
                if (action.getId() == mPlayPauseAction.getId()) {
//                    DreamTVApp.Logger.d("Button PLAY From the pannel");
                    togglePlayback(mPlayPauseAction.getIndex() == PlayPauseAction.PLAY ? Constants.Actions.PLAY : Constants.Actions.PAUSE);
                } else if (action.getId() == mFastForwardAction.getId()) {
                    fastForwardAction(Constants.Actions.FORWARD);
                } else if (action.getId() == mRewindAction.getId()) {
                    rewindAction(Constants.Actions.REWIND);
                }

                if (action instanceof PlaybackControlsRow.MultiAction) {
                    ((PlaybackControlsRow.MultiAction) action).nextIndex();
                    notifyChanged(action);
                }
            }
        });
        playbackControlsRowPresenter.setSecondaryActionsHidden(HIDE_MORE_ACTIONS);
        ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
        ps.addClassPresenter(ListRow.class, new ListRowPresenter());
        mRowsAdapter = new ArrayObjectAdapter(ps);

        addPlaybackControlsRow();
//        addOtherRows();

        setAdapter(mRowsAdapter);
    }

    private void fastForwardAction(Constants.Actions actions) {
        mCallback.onFragmentPlayPause(mSelectedVideo,
                mPlaybackControlsRow.getCurrentTime(), actions, mPlaybackControlsRow);
//        Toast.makeText(getActivity(), "TODO: Fast Forward", Toast.LENGTH_SHORT).show();

    }

    private void rewindAction(Constants.Actions actions) {
        mCallback.onFragmentPlayPause(mSelectedVideo,
                mPlaybackControlsRow.getCurrentTime(), actions, mPlaybackControlsRow);
//        Toast.makeText(getActivity(), "TODO: Rewind", Toast.LENGTH_SHORT).show();

    }
//    // When user click to "Rewind".
//    public void doRewind(View view)  {
//        int currentPosition = this.mediaPlayer.getCurrentPosition();
//        int duration = this.mediaPlayer.getDuration();
//        // 5 seconds.
//        int SUBTRACT_TIME = 5000;
//
//        if(currentPosition - SUBTRACT_TIME > 0 )  {
//            this.mediaPlayer.seekTo(currentPosition - SUBTRACT_TIME);
//        }
//    }
//
//    // When user click to "Fast-Forward".
//    public void doFastForward(View view)  {
//        int currentPosition = this.mediaPlayer.getCurrentPosition();
//        int duration = this.mediaPlayer.getDuration();
//        // 5 seconds.
//        int ADD_TIME = 5000;
//
//        if(currentPosition + ADD_TIME < duration)  {
//            this.mediaPlayer.seekTo(currentPosition + ADD_TIME);
//        }
//    }

    public void togglePlayback(Constants.Actions actions) {
        mCallback.onFragmentPlayPause(mSelectedVideo,
                mPlaybackControlsRow.getCurrentTime(), actions, mPlaybackControlsRow);
        if (actions.value.equals(Constants.Actions.PLAY.value)) {
//            startProgressAutomation();
            setFadingEnabled(true);
            mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlayPauseAction.PAUSE));
        } else {
//            stopProgressAutomation();
            setFadingEnabled(false);
//            mCallback.onFragmentPlayPause(mSelectedVideo,
//                    mPlaybackControlsRow.getCurrentTime(), actions, mPlaybackControlsRow);
            mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlayPauseAction.PLAY));
        }
        notifyChanged(mPlayPauseAction);
    }

    public void togglePlaybackWithoutVideoView(boolean playPause) {
        if (playPause) {
//            startProgressAutomation();
            setFadingEnabled(true);
            mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlayPauseAction.PAUSE));
        } else {
//            stopProgressAutomation();
            setFadingEnabled(false);
            mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlayPauseAction.PLAY));
        }
        notifyChanged(mPlayPauseAction);
    }

    private int getDuration() {

        return mSelectedVideo.duration * 1000;
    }

    private void addPlaybackControlsRow() {
        if (SHOW_DETAIL) {
            mPlaybackControlsRow = new PlaybackControlsRow(mSelectedVideo);
        } else {
            mPlaybackControlsRow = new PlaybackControlsRow();
        }
        mRowsAdapter.add(mPlaybackControlsRow);

        updatePlaybackRow();

        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
//        mSecondaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);
//        mPlaybackControlsRow.setSecondaryActionsAdapter(mSecondaryActionsAdapter);

        mPlayPauseAction = new PlayPauseAction(getActivity());
        mFastForwardAction = new FastForwardAction(getActivity());
        mRewindAction = new RewindAction(getActivity());

        mPrimaryActionsAdapter.add(mRewindAction);
        mPrimaryActionsAdapter.add(mPlayPauseAction);
        mPrimaryActionsAdapter.add(mFastForwardAction);

    }

    private void notifyChanged(Action action) {
        ArrayObjectAdapter adapter = mPrimaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
            return;
        }
//        adapter = mSecondaryActionsAdapter;
//        if (adapter.indexOf(action) >= 0) {
//            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
//            return;
//        }
    }

    private void updatePlaybackRow() {
        if (mPlaybackControlsRow.getItem() != null) {
            Video item = (Video) mPlaybackControlsRow.getItem();
            item.title = mSelectedVideo.title;
            item.project = mSelectedVideo.project;
        }
        if (SHOW_IMAGE) {
            updateVideoImage(mSelectedVideo.thumbnail);
        }
        mRowsAdapter.notifyArrayItemRangeChanged(0, 1);
        mPlaybackControlsRow.setTotalTime(getDuration());
        mPlaybackControlsRow.setCurrentTime(0);
        mPlaybackControlsRow.setBufferedProgress(0);
    }


    @Override
    public void onStop() {
//        stopProgressAutomation();
        super.onStop();
    }

    protected void updateVideoImage(String uri) {
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .into(new SimpleTarget<GlideDrawable>(Constants.CARD_WIDTH, Constants.CARD_HEIGHT) {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        mPlaybackControlsRow.setImageDrawable(resource);
                        mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
                    }
                });
    }

    // Container Activity must implement this interface
    public interface OnPlayPauseClickedListener {
        void onFragmentPlayPause(Video video, int position, Constants.Actions actions, PlaybackControlsRow mPlaybackControlsRow);
    }

    static class DescriptionPresenter extends AbstractDetailsDescriptionPresenter {
        @Override
        protected void onBindDescription(ViewHolder viewHolder, Object item) {
            viewHolder.getTitle().setText(((Video) item).title);
            viewHolder.getSubtitle().setText(((Video) item).project);
            viewHolder.getBody().setText(((Video) item).description);
        }
    }
}
