package com.dream.dreamtv.ui.playVideo.dialogs;

import android.content.Context;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dream.dreamtv.R;
import com.dream.dreamtv.data.model.Subtitle.SubtitleText;
import com.dream.dreamtv.data.model.UserTaskError;

import java.util.List;
import java.util.Objects;

import static android.util.TypedValue.applyDimension;

public class MySubtitleAdapter extends ArrayAdapter<SubtitleText> {

    private final Context context;
    private final List<SubtitleText> values;
    private final Integer currentSubtitlePosition;
    private UserTaskError[] userTaskErrors;

    MySubtitleAdapter(Context context, List<SubtitleText> values, int currentSubtitlePosition,
                      UserTaskError[] userTaskErrors) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
        this.currentSubtitlePosition = currentSubtitlePosition;
        this.userTaskErrors = userTaskErrors;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder;
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = Objects.requireNonNull(mInflater).inflate(R.layout.subtitle_layout, null);
            holder = new ViewHolder();
            holder.tvSubtitle = convertView.findViewById(R.id.tvSubtitle);
            holder.tvSubtitleError = convertView.findViewById(R.id.tvSubtitleError);
            holder.tvSubtitleSelected = convertView.findViewById(R.id.tvSubtitleSelected);
            holder.tvTime = convertView.findViewById(R.id.tvTime);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        if (position == currentSubtitlePosition - 1) {
            if (isPositionError(userTaskErrors, position)) {
                holder.tvSubtitle.setVisibility(View.GONE);
                holder.tvSubtitleSelected.setVisibility(View.GONE);
                holder.tvSubtitleError.setVisibility(View.VISIBLE);
                holder.tvSubtitleError.setTextSize(applyDimension(TypedValue.COMPLEX_UNIT_SP, 9,
                        context.getResources().getDisplayMetrics()));
                holder.tvSubtitleError.setText(Html.fromHtml(values.get(position).getText()));
            } else {
                holder.tvSubtitle.setVisibility(View.GONE);
                holder.tvSubtitleError.setVisibility(View.GONE);
                holder.tvSubtitleSelected.setVisibility(View.VISIBLE);
                holder.tvSubtitleSelected.setTextSize(applyDimension(TypedValue.COMPLEX_UNIT_SP, 9,
                        context.getResources().getDisplayMetrics()));
                holder.tvSubtitleSelected.setText(Html.fromHtml(values.get(position).getText()));
            }
        } else {
            if (isPositionError(userTaskErrors, position)) {
                holder.tvSubtitle.setVisibility(View.GONE);
                holder.tvSubtitleSelected.setVisibility(View.GONE);
                holder.tvSubtitleError.setVisibility(View.VISIBLE);
                holder.tvSubtitleError.setTextSize(applyDimension(TypedValue.COMPLEX_UNIT_SP, 8,
                        context.getResources().getDisplayMetrics()));
                holder.tvSubtitleError.setText(Html.fromHtml(values.get(position).getText()));
            } else {
                holder.tvSubtitleSelected.setVisibility(View.GONE);
                holder.tvSubtitleError.setVisibility(View.GONE);
                holder.tvSubtitle.setVisibility(View.VISIBLE);
                holder.tvSubtitle.setTextSize(applyDimension(TypedValue.COMPLEX_UNIT_SP, 8,
                        context.getResources().getDisplayMetrics()));
                holder.tvSubtitle.setText(Html.fromHtml(values.get(position).getText()));
            }

        }

        return convertView;
    }

//        private String videoCurrentReadVeloc(String text, long millis) {
//            if (millis > 1000) //to avoid division by zero
//                return text.length() / (millis / 1000) + " " + CHARS_S;
//            else
//                return "0 " + CHARS_S;
//        }

    private boolean isPositionError(UserTaskError[] userTaskErrorsList, int position) {
        //userTaskErrorsList is ordered by subtitle_position asc
        for (UserTaskError userTaskError : userTaskErrorsList) {
            if ((userTaskError.getSubtitlePosition() - 1) > position)
                return false;
            else if ((userTaskError.getSubtitlePosition() - 1) == position)
                return true;
        }

        return false;
    }

    class ViewHolder {
        TextView tvSubtitle;
        TextView tvSubtitleError;
        TextView tvSubtitleSelected;
        TextView tvTime;
    }
}


