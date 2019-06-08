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
import android.view.ContextThemeWrapper;

import com.bumptech.glide.Glide;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.Card;


public class ImageCardViewCustomPresenter extends AbstractCardPresenter<ImageCardViewCustom> {

    public ImageCardViewCustomPresenter(Context context, int cardThemeResId) {
        super(new ContextThemeWrapper(context, cardThemeResId));
    }

    public ImageCardViewCustomPresenter(Context context) {
        this(context, R.style.DefaultCardThemeCustom);
    }

    @Override
    protected ImageCardViewCustom onCreateView() {
        ImageCardViewCustom imageCardView = new ImageCardViewCustom(getContext());
//        imageCardView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getContext(), "Clicked on ImageCardView", Toast.LENGTH_SHORT).show();
//            }
//        });
        return imageCardView;
    }

    @Override
    public void onBindViewHolder(Card card, final ImageCardViewCustom cardView) {
        cardView.setTag(card);
        cardView.setTitleText(card.getTitle());
        cardView.setContentText(card.getDescription());
        if (card.getLocalImageResourceName() != null) {
            int resourceId = getContext().getResources()
                    .getIdentifier(card.getLocalImageResourceName(),
                            "drawable", getContext().getPackageName());
            Glide.with(getContext())
                    .asBitmap()
                    .load(resourceId)
                    .into(cardView.getMainImageView());
        }
    }

}