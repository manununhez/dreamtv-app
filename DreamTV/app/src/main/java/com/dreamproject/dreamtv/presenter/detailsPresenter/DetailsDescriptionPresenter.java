package com.dreamproject.dreamtv.presenter.detailsPresenter;

import android.content.Context;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.dreamproject.dreamtv.R;
import com.dreamproject.dreamtv.data.model.Task;
import com.dreamproject.dreamtv.utils.TimeUtils;


public class DetailsDescriptionPresenter extends CustomAbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        Task task = ((Task) item);

        Context context = viewHolder.view.getContext();

        if (task.getVideo() != null) {
            String timeFormatted = TimeUtils.getTimeFormatMinSecs(task.getVideo().getVideoDurationInMs());

            viewHolder.getTitle().setText(task.getVideoTitleTranslated());
            viewHolder.getSubtitle().setText(context.getString(R.string.title_video_details, task.getVideo().project,
                    task.getVideo().audioLanguage, task.getSubLanguage(), timeFormatted));
            viewHolder.getBody().setText(task.getVideoDescriptionTranslated());
        }
    }
}
