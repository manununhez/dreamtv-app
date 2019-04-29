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

package com.dream.dreamtv.presenter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.Card;

import androidx.leanback.widget.BaseCardView;

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
                    .centerCrop()
                    .placeholder(mDefaultCardImage)
                    .error(mDefaultCardImage)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);

            Glide.with(getContext())
                    .load(card.getTaskEntity().video.thumbnail)
                    .apply(options)
                    .into(imageView);
        }

        TextView primaryText = cardView.findViewById(R.id.primary_text);
        primaryText.setText(card.getTitle());

        TextView secondaryText = cardView.findViewById(R.id.secondary_text);
        secondaryText.setText(card.getDescription());

        TextView extraText = cardView.findViewById(R.id.extra_text);
        extraText.setText(card.getExtraText());
    }

}