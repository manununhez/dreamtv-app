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

package com.dreamproject.dreamtv.data.model;

import android.content.Context;

import java.net.URI;
import java.net.URISyntaxException;

import timber.log.Timber;

/**
 * This is a generic example of a custom data object, containing info we might want to keep with
 * each card on the home screen
 */
public class Card {

    private Category.Type mCategory;
    private String mTitle;
    private String mDescription;
    private String mExtraText;
    private String mImageUrl;
    private String mLocalImageResource = null;
    private Card.Type mType;
    private Task mTask;

    private Card() {
        //non-instantiable
    }

    private Card(Card.Type cardType, String title) {
        mType = cardType;
        mTitle = !title.isEmpty() ? title : "<Title>";
    }

    public Card(Task task, Card.Type cardType) {
        this(cardType, task.getVideoTitleTranslated());

        mDescription = !task.getVideo().project.isEmpty() ? task.getVideo().project : "<Project>";
        mExtraText = !task.getVideoDescriptionTranslated().isEmpty() ? task.getVideoDescriptionTranslated() : "<Description>";
        mImageUrl = task.getVideo().thumbnail;
        mTask = task;
    }

    public Card(Task task, Card.Type cardType, Category.Type category) {
        this(task, cardType);

        mCategory = category;

    }

    public Card(String title, Card.Type cardType, String localImageResource) {
        this(cardType, title);

        mLocalImageResource = localImageResource;
    }

    public Task getTask() {
        return mTask;
    }

    public void setTask(Task mTask) {
        this.mTask = mTask;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getLocalImageResource() {
        return mLocalImageResource;
    }

    public void setLocalImageResource(String localImageResource) {
        mLocalImageResource = localImageResource;
    }


    public Card.Type getType() {
        return mType;
    }

    public void setType(Type type) {
        mType = type;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }


    public String getExtraText() {
        return mExtraText;
    }

    public void setExtraText(String extraText) {
        mExtraText = extraText;
    }


    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public URI getImageURI() {
        if (getImageUrl() == null) return null;
        try {
            return new URI(getImageUrl());
        } catch (URISyntaxException e) {
            Timber.d(getImageUrl());
            return null;
        }
    }

    public int getLocalImageResourceId(Context context) {
        return context.getResources().getIdentifier(getLocalImageResourceName(), "drawable",
                context.getPackageName());
    }

    public String getLocalImageResourceName() {
        return mLocalImageResource;
    }


    public Category.Type getCategory() {
        return mCategory;
    }


    public enum Type {
        ICON,
        SINGLE_LINE,
        SIDE_INFO,
    }

}
