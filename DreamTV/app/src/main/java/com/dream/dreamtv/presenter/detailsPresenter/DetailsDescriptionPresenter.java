package com.dream.dreamtv.presenter.detailsPresenter;

import android.content.Context;

import com.dream.dreamtv.R;
import com.dream.dreamtv.data.networking.model.Task;
import com.dream.dreamtv.utils.TimeUtils;


public class DetailsDescriptionPresenter extends CustomAbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        Task task = ((Task) item);

        Context context = viewHolder.view.getContext();

        if (task.video != null) {
            String timeFormatted = TimeUtils.getTimeFormatMinSecs(task.video.getVideoDurationInMs());

            viewHolder.getTitle().setText(task.videoTitleTranslated);
            viewHolder.getSubtitle().setText(context.getString(R.string.title_video_details, task.video.project,
                    task.video.primaryAudioLanguageCode, task.subLanguage, timeFormatted));
            viewHolder.getBody().setText(task.videoDescriptionTranslated);
        }
    }
}
