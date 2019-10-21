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

package com.manuelnunhez.dreamtv.presenter.sideInfoPresenter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.leanback.widget.BaseCardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.manuelnunhez.dreamtv.R;
import com.manuelnunhez.dreamtv.data.model.Card;
import com.manuelnunhez.dreamtv.data.model.Task;
import com.manuelnunhez.dreamtv.utils.TimeUtils;

import static com.manuelnunhez.dreamtv.utils.Constants.VIDEO_COMPLETED_WATCHING_TRUE;

/**
 * This Presenter will display a card consisting of an image on the left side of the card followed
 * by text on the right side. The image and text have equal width. The text will work like a info
 * box, thus it will be hidden if the parent row is inactive. This behavior is unique to this card
 * and requires a special focus handler.
 */
public class SideInfoCardPresenter extends AbstractCardPresenter<BaseCardView> {

    private static final String TAG = SideInfoCardPresenter.class.getSimpleName();

    public SideInfoCardPresenter(Context context) {
        super(context);
    }


    @Override
    protected BaseCardView onCreateView() {
        final BaseCardView cardView = new BaseCardView(getContext(), null,
                R.style.SideInfoCardStyle);
        cardView.setFocusable(true);
        cardView.addView(LayoutInflater.from(getContext()).inflate(R.layout.side_info_card, null));
        return cardView;
    }


    @Override
    public void onBindViewHolder(Card card, BaseCardView cardView) {
        ImageView imageView = cardView.findViewById(R.id.main_image);
        int width = (int) getContext().getResources()
                .getDimension(R.dimen.sidetext_image_card_width);
        int height = (int) getContext().getResources()
                .getDimension(R.dimen.sidetext_image_card_height);

        Task task = card.getTask();


        if (card.getLocalImageResourceName() != null) {

            int resourceId = getContext().getResources()
                    .getIdentifier(card.getLocalImageResourceName(),
                            "drawable", getContext().getPackageName());
            RequestOptions myOptions = new RequestOptions()
                    .override(width, height);


            Glide.with(getContext())
                    .asBitmap()
                    .load(resourceId)
                    .apply(myOptions)
                    .into(imageView);
        } else {
            int mDefaultCardImage = R.drawable.movie;

            RequestOptions options = new RequestOptions()
                    .centerInside()
                    .placeholder(mDefaultCardImage)
                    .error(mDefaultCardImage)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);

            Glide.with(getContext())
                    .load(task.getVideo().thumbnail)
                    .apply(options)
                    .into(imageView);
        }


        //-------------- VIDEO TITLE
        TextView primaryText = cardView.findViewById(R.id.primary_text);
        primaryText.setText(card.getTitle());

        //-------------- VIDEO INFO
        TextView secondaryText = cardView.findViewById(R.id.secondary_text);
        secondaryText.setText(getContext().getString(R.string.title_video_details_main, task.getVideo().audioLanguage,
                task.getSubLanguage()));

        //-------------- VIDEO DESCRIPTION
//        TextView extraText = cardView.findViewById(R.id.extra_text);
//        extraText.setText(card.getExtraText());

        //-------------- VIDEO DURATION
        String timeFormatted = TimeUtils.getTimeFormatMinSecsDoublePoints(task.getVideo().getVideoDurationInMs());
        TextView tvVideoDuration = cardView.findViewById(R.id.tvVideoDuration);
        tvVideoDuration.setText(timeFormatted);

        //-------------- ERRORS REPORT
        TextView errors = cardView.findViewById(R.id.tvErrorsSelected);
        if (task.getUserTasks().length > 0 && task.getUserTasks()[0].getUserTaskErrorList().length > 0) { //TODO what happened if we have more than one userTask, e.g same tasks, different subtVersions???
            if (task.getUserTasks()[0].getCompleted() == VIDEO_COMPLETED_WATCHING_TRUE)
                errors.setText(getContext().getResources()
                        .getQuantityString(R.plurals.errors_found_finished,
                                task.getUserTasks()[0].getUserTaskErrorList().length, task.getUserTasks()[0].getUserTaskErrorList().length));
            else
                errors.setText(getContext().getResources()
                        .getQuantityString(R.plurals.errors_found_continue,
                                task.getUserTasks()[0].getUserTaskErrorList().length, task.getUserTasks()[0].getUserTaskErrorList().length));
        } else
            errors.setVisibility(View.GONE);


        //-------------- VIDEO PROGRESS
        ProgressBar pbContinueWatching = cardView.findViewById(R.id.pbContinueWatching);

        if (task.getUserTasks().length > 0 && task.getUserTasks()[0].getTimeWatched() > 0) {  //TODO what happened if we have more than one userTask, e.g same tasks, different subtVersions???
            int videoProgress = (int) (((float) task.getUserTasks()[0].getTimeWatched() / (float) task.getVideo().getVideoDurationInMs()) * 100);
            pbContinueWatching.setProgress(videoProgress);
        } else
            pbContinueWatching.setVisibility(View.GONE);

        //-------------- RATING
        LinearLayout llTask = cardView.findViewById(R.id.llTask);
        RatingBar rbTask = cardView.findViewById(R.id.rbTask);

        if (task.getUserTasks().length > 0 && task.getUserTasks()[0].getRating() > 0)
            rbTask.setRating(task.getUserTasks()[0].getRating());
        else
            llTask.setVisibility(View.GONE);

    }

}
