package com.dream.dreamtv.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.beans.Subtitle;
import com.dream.dreamtv.beans.User;
import com.dream.dreamtv.utils.Constants;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by manuel on 9/11/17.
 */

public class MySubtitleAdapter extends ArrayAdapter<Subtitle> {
    private final Context context;
    private final List<Subtitle> values;
    private Integer currentSubtitlePosition;
    private String userInterfaceMode;

    public MySubtitleAdapter(Context context, List<Subtitle> values, int currentSubtitlePosition, String userInterfaceMode) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
        this.currentSubtitlePosition = currentSubtitlePosition;
        this.userInterfaceMode = userInterfaceMode;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.subtitle_layout, parent, false);
        TextView tvText = (TextView) rowView.findViewById(R.id.tvText);
        TextView tvTime = (TextView) rowView.findViewById(R.id.tvTime);

        if (position == (currentSubtitlePosition - 1)) {
            tvText.setBackgroundColor(context.getResources().getColor(R.color.blue, null));
            tvText.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9, context.getResources().getDisplayMetrics()));
            tvText.setTypeface(tvText.getTypeface(), Typeface.NORMAL);
        } else {
//            android:textSize="14sp"
//            android:textStyle="italic"
            tvText.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8, context.getResources().getDisplayMetrics()));
            tvText.setTypeface(tvText.getTypeface(), Typeface.ITALIC);
            tvText.setBackgroundColor(context.getResources().getColor(R.color.black_opaque, null));
        }

        tvText.setText(Html.fromHtml(values.get(position).text));

        if (userInterfaceMode.equals(Constants.ADVANCED_INTERFACE_MODE)) { //Advanced MODE
            tvTime.setText(videoCurrentReadVeloc(values.get(position).text, (values.get(position).end - values.get(position).start)));
//            tvTime.setText(videoCurrentTimeFormat(values.get(position).start) + " - "
//                    + videoCurrentTimeFormat(values.get(position).end));
        }
        return rowView;
    }

    private String videoCurrentTimeFormat(long millis) {
        String hms = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));

        return hms;
    }

    private String videoCurrentReadVeloc(String text, long millis){
        return text.length()/ (millis/1000) + " char/seg";
    }
}