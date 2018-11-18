package com.dream.dreamtv.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dream.dreamtv.R;
import com.dream.dreamtv.beans.Subtitle;
import com.dream.dreamtv.utils.Constants;

import java.util.List;
import java.util.Objects;

/**
 * Created by manuel on 9/11/17.
 */

public class MySubtitleAdapter extends ArrayAdapter<Subtitle> {
    private final Context context;
    private final List<Subtitle> values;
    private final Integer currentSubtitlePosition;
    private final String userInterfaceMode;

    static class ViewHolder {
        TextView tvText;
        TextView tvTime;
    }

    public MySubtitleAdapter(Context context, List<Subtitle> values, int currentSubtitlePosition, String userInterfaceMode) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
        this.currentSubtitlePosition = currentSubtitlePosition;
        this.userInterfaceMode = userInterfaceMode;
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
            holder.tvText = convertView.findViewById(R.id.tvText);
            holder.tvTime = convertView.findViewById(R.id.tvTime);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }


        if (position == (currentSubtitlePosition - 1)) {
            holder.tvText.setBackgroundColor(context.getResources().getColor(R.color.blue, null));
            holder.tvText.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9, context.getResources().getDisplayMetrics()));
            holder.tvText.setTypeface(holder.tvText.getTypeface(), Typeface.NORMAL);
        } else {
//            android:textSize="14sp"
//            android:textStyle="italic"
            holder.tvText.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8, context.getResources().getDisplayMetrics()));
            holder.tvText.setTypeface(holder.tvText.getTypeface(), Typeface.ITALIC);
            holder.tvText.setBackgroundColor(context.getResources().getColor(R.color.black_opaque, null));
        }

        holder.tvText.setText(Html.fromHtml(values.get(position).text));

        if (userInterfaceMode.equals(Constants.ADVANCED_INTERFACE_MODE)) { //Advanced MODE
            holder.tvTime.setText(videoCurrentReadVeloc(values.get(position).text, (values.get(position).end - values.get(position).start)));

        }
        return convertView;
    }

    private String videoCurrentReadVeloc(String text, long millis){
        if(millis > 1000) //to avoid division by zero
            return text.length()/ (millis/1000) + " chars/s";
        else
            return "0 chars/s";


    }
}
