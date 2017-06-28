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

package com.dream.dreamtv.dialog;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist.Guidance;
import android.support.v17.leanback.widget.GuidedAction;
import android.widget.Toast;

import com.dream.dreamtv.R;
import com.dream.dreamtv.beans.Language;
import com.dream.dreamtv.beans.Video;
import com.dream.dreamtv.details.DetailViewExampleActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * TODO: Javadoc
 */
public class DialogChooseLanguageFragment extends GuidedStepFragment {

    private static final int ACTION_ID_POSITIVE = 1;
    private static final int ACTION_ID_NEGATIVE = ACTION_ID_POSITIVE + 1;
    private static final int ACTION_ID_CONFIRM = 1;
    private static final int ACTION_ID_PAYMENT_METHOD = ACTION_ID_CONFIRM + 1;
    private static final int ACTION_ID_NEW_PAYMENT = ACTION_ID_PAYMENT_METHOD + 1;

    //    protected static ArrayList<String> sCards = new ArrayList();
    protected static int sSelectedLanguageIndex = -1;
    protected static int sSelectedLanguage = -1;
    private Bundle mArgs;
    private Video mSelectedVideo;

//    static {
//        sCards.add("Spanish");
//        sCards.add("English");
//        sCards.add("Polish");
//    }

    public static DialogChooseLanguageFragment newInstance(Video video) {
        DialogChooseLanguageFragment fragment = new DialogChooseLanguageFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("Video", video);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectedVideo = getArguments().getParcelable("Video");
    }

    @NonNull
    @Override
    public Guidance onCreateGuidance(Bundle savedInstanceState) {
        Guidance guidance = new Guidance(getString(R.string.dialog_choose_language_title),
                getString(R.string.dialog_example_description),
                "", null);
        return guidance;
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        GuidedAction action = new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_CONFIRM)
                .title("Start")
                .description(R.string.edit_subtitle)
                .editable(false)
                .build();
        action.setEnabled(false);
        actions.add(action);
        List<GuidedAction> subActions = new ArrayList();
        action = new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_PAYMENT_METHOD)
                .title(R.string.subtitle_languages)
                .editTitle("")
                .description(R.string.dialog_choose_language_title)
                .subActions(subActions)
                .build();
        actions.add(action);
    }

    @Override
    public void onResume() {
        super.onResume();
        GuidedAction payment = findActionById(ACTION_ID_PAYMENT_METHOD);

        List<GuidedAction> paymentSubActions = payment.getSubActions();
        paymentSubActions.clear();
//        for (int i = 0; i < sCards.size(); i++) {
//            paymentSubActions.add(new GuidedAction.Builder(getActivity())
//                    .title(sCards.get(i))
//                    .description("")
//                    .checkSetId(GuidedAction.DEFAULT_CHECK_SET_ID)
//                    .build()
//            );
//        }
        for (Language language : mSelectedVideo.languages) {
            paymentSubActions.add(new GuidedAction.Builder(getActivity())
                    .title(language.name)
                    .description("")
                    .checkSetId(GuidedAction.DEFAULT_CHECK_SET_ID)
                    .build()
            );
        }
        paymentSubActions.add(new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_NEW_PAYMENT)
                .title("Add New Card")
                .description("")
                .editable(false)
                .build()
        );
        if (sSelectedLanguage >= 0 && sSelectedLanguage < mSelectedVideo.languages.size()) {
            payment.setDescription(mSelectedVideo.languages.get(sSelectedLanguageIndex).name);
            findActionById(ACTION_ID_CONFIRM).setEnabled(true);
        } else
            findActionById(ACTION_ID_CONFIRM).setEnabled(false);
        notifyActionChanged(findActionPositionById(ACTION_ID_CONFIRM));
    }

    @Override
    public boolean onSubGuidedActionClicked(GuidedAction action) {

        if (action.isChecked()) {
            String language = action.getTitle().toString();
            sSelectedLanguageIndex = mSelectedVideo.getLanguageListIndex(language);
            if ((sSelectedLanguage = sSelectedLanguageIndex) != -1 && mSelectedVideo.languages.get(sSelectedLanguageIndex).published) {
                findActionById(ACTION_ID_PAYMENT_METHOD).setDescription(language);
                notifyActionChanged(findActionPositionById(ACTION_ID_PAYMENT_METHOD));
                findActionById(ACTION_ID_CONFIRM).setEnabled(true);
                notifyActionChanged(findActionPositionById(ACTION_ID_CONFIRM));
            }else{
                Toast.makeText(getActivity(), "Subtitle Not available - Not yet published!!", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } else {
//            FragmentManager fm = getFragmentManager();
//            GuidedStepFragment fragment = new WizardNewPaymentStepFragment();
//            fragment.setArguments(getArguments());
//            add(fm, fragment);
            Toast.makeText(getActivity(), "New Step", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (ACTION_ID_CONFIRM == action.getId()) {
//            Intent intent = new Intent(getActivity(), DetailViewExampleActivity.class);
//            getActivity().startActivity(intent);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedCodeLanguage", mSelectedVideo.languages.get(sSelectedLanguageIndex).code);
            getActivity().setResult(Activity.RESULT_OK, resultIntent);
            getActivity().finish();
        }
    }
}
