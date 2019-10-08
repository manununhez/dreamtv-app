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

package com.manuelnunhez.dreamtv.presenter;

import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.Presenter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.manuelnunhez.dreamtv.R;
import com.manuelnunhez.dreamtv.data.model.Task;

import timber.log.Timber;


/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class VideoCardPresenter extends Presenter {
    private static final String TAG = "VideoCardPresenter";

    private static final int CARD_WIDTH = 413;
    private static final int CARD_HEIGHT = 276;
    private static int sSelectedBackgroundColor;
    private static int sDefaultBackgroundColor;
    private Drawable mDefaultCardImage;

    private static void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
        // Both background colors should be set because the view's background is temporarily visible
        // during animations.
        view.setBackgroundColor(color);
        view.findViewById(R.id.info_field).setBackgroundColor(color);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Timber.d("onCreateViewHolder");

        sDefaultBackgroundColor = ContextCompat.getColor(parent.getContext(), R.color.default_background);
        sSelectedBackgroundColor = ContextCompat.getColor(parent.getContext(), R.color.default_background);
        mDefaultCardImage = parent.getResources().getDrawable(R.drawable.movie, null);

        ImageCardView cardView = new ImageCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        Task task = ((Task) item);
        ImageCardView cardView = (ImageCardView) viewHolder.view;

        Timber.d("onBindViewHolder");
//        String videoTypeTitle = video.video_type != null ? ("[" + video.video_type.toLowerCase() + "] ") : "";
        cardView.setTitleText(task.getVideoTitleTranslated());
        cardView.setContentText(task.getVideoDescriptionTranslated());

        if (task.getVideo().thumbnail != null) {
            // Set card size from dimension resources.
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);

            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(mDefaultCardImage)
                    .error(mDefaultCardImage)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);

            Glide.with(viewHolder.view.getContext())
                    .load(task.getVideo().thumbnail)
                    .apply(options)
                    .into(cardView.getMainImageView());
        }

    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        Timber.d("onUnbindViewHolder");
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        // Remove references to images so that the garbage collector can free up memory
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}
