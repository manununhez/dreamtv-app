package com.dream.dreamtv.presenter.DetailsPresenter;

import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.dream.dreamtv.R;

import androidx.leanback.widget.Presenter;


public abstract class CustomAbstractDetailsDescriptionPresenter extends Presenter {
    private static int lb_details_description_body_max_lines = 5;
    private static int lb_details_description_body_min_lines = 3;
    private static int lb_details_description_title_max_lines = 3;

    @Override
    public final CustomAbstractDetailsDescriptionPresenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lb_details_description_custom, parent, false);
        return new CustomAbstractDetailsDescriptionPresenter.ViewHolder(v);
    }

    @Override
    public final void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        CustomAbstractDetailsDescriptionPresenter.ViewHolder vh = (CustomAbstractDetailsDescriptionPresenter.ViewHolder) viewHolder;
        onBindDescription(vh, item);

        boolean hasTitle = true;
        if (TextUtils.isEmpty(vh.mTitle.getText())) {
            vh.mTitle.setVisibility(View.GONE);
            hasTitle = false;
        } else {
            vh.mTitle.setVisibility(View.VISIBLE);
            vh.mTitle.setLineSpacing(vh.mTitleLineSpacing - vh.mTitle.getLineHeight()
                    + vh.mTitle.getLineSpacingExtra(), vh.mTitle.getLineSpacingMultiplier());
            vh.mTitle.setMaxLines(vh.mTitleMaxLines);
        }
        setTopMargin(vh.mTitle, vh.mTitleMargin);

        boolean hasSubtitle = true;
        if (TextUtils.isEmpty(vh.mSubtitle.getText())) {
            vh.mSubtitle.setVisibility(View.GONE);
            hasSubtitle = false;
        } else {
            vh.mSubtitle.setVisibility(View.VISIBLE);
            if (hasTitle) {
                setTopMargin(vh.mSubtitle, vh.mUnderTitleBaselineMargin
                        + vh.mSubtitleFontMetricsInt.ascent - vh.mTitleFontMetricsInt.descent);
            } else {
                setTopMargin(vh.mSubtitle, 0);
            }
        }

        if (TextUtils.isEmpty(vh.mBody.getText())) {
            vh.mBody.setVisibility(View.GONE);
        } else {
            vh.mBody.setVisibility(View.VISIBLE);
            vh.mBody.setLineSpacing(vh.mBodyLineSpacing - vh.mBody.getLineHeight()
                    + vh.mBody.getLineSpacingExtra(), vh.mBody.getLineSpacingMultiplier());

            if (hasSubtitle) {
                setTopMargin(vh.mBody, vh.mUnderSubtitleBaselineMargin
                        + vh.mBodyFontMetricsInt.ascent - vh.mSubtitleFontMetricsInt.descent);
            } else if (hasTitle) {
                setTopMargin(vh.mBody, vh.mUnderTitleBaselineMargin
                        + vh.mBodyFontMetricsInt.ascent - vh.mTitleFontMetricsInt.descent);
            } else {
                setTopMargin(vh.mBody, 0);
            }
        }
    }


    protected abstract void onBindDescription(CustomAbstractDetailsDescriptionPresenter.ViewHolder vh, Object item);

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
    }

    @Override
    public void onViewAttachedToWindow(Presenter.ViewHolder holder) {
        // In case predraw listener was removed in detach, make sure
        // we have the proper layout.
        CustomAbstractDetailsDescriptionPresenter.ViewHolder vh = (CustomAbstractDetailsDescriptionPresenter.ViewHolder) holder;
        vh.addPreDrawListener();
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(Presenter.ViewHolder holder) {
        CustomAbstractDetailsDescriptionPresenter.ViewHolder vh = ( CustomAbstractDetailsDescriptionPresenter.ViewHolder) holder;
        vh.removePreDrawListener();
        super.onViewDetachedFromWindow(holder);
    }

    private void setTopMargin(TextView textView, int topMargin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) textView.getLayoutParams();
        lp.topMargin = topMargin;
        textView.setLayoutParams(lp);
    }

    /**
     * The ViewHolder for the {@link CustomAbstractDetailsDescriptionPresenter}.
     */
    public static class ViewHolder extends Presenter.ViewHolder {
        final TextView mTitle;
        final TextView mSubtitle;
        final TextView mBody;
        final int mTitleMargin;
        final int mUnderTitleBaselineMargin;
        final int mUnderSubtitleBaselineMargin;
        final int mTitleLineSpacing;
        final int mBodyLineSpacing;
        final int mBodyMaxLines;
        final int mBodyMinLines;
        final FontMetricsInt mTitleFontMetricsInt;
        final FontMetricsInt mSubtitleFontMetricsInt;
        final FontMetricsInt mBodyFontMetricsInt;
        final int mTitleMaxLines;
        private ViewTreeObserver.OnPreDrawListener mPreDrawListener;

        public ViewHolder(final View view) {
            super(view);
            mTitle = view.findViewById(R.id.lb_details_description_title);
            mSubtitle = view.findViewById(R.id.lb_details_description_subtitle);
            mBody = view.findViewById(R.id.lb_details_description_body);

            FontMetricsInt titleFontMetricsInt = getFontMetricsInt(mTitle);
            final int titleAscent = view.getResources().getDimensionPixelSize(
                    R.dimen.lb_details_description_title_baseline);
            // Ascent is negative
            mTitleMargin = titleAscent + titleFontMetricsInt.ascent;

            mUnderTitleBaselineMargin = view.getResources().getDimensionPixelSize(
                    R.dimen.lb_details_description_under_title_baseline_margin);
            mUnderSubtitleBaselineMargin = view.getResources().getDimensionPixelSize(
                    R.dimen.lb_details_description_under_subtitle_baseline_margin);

            mTitleLineSpacing = view.getResources().getDimensionPixelSize(
                    R.dimen.lb_details_description_title_line_spacing);
            mBodyLineSpacing = view.getResources().getDimensionPixelSize(
                    R.dimen.lb_details_description_body_line_spacing);

            mBodyMaxLines = lb_details_description_body_max_lines;
            mBodyMinLines = lb_details_description_body_min_lines;
            mTitleMaxLines = lb_details_description_title_max_lines;

            mTitleFontMetricsInt = getFontMetricsInt(mTitle);
            mSubtitleFontMetricsInt = getFontMetricsInt(mSubtitle);
            mBodyFontMetricsInt = getFontMetricsInt(mBody);

            mTitle.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    addPreDrawListener();
                }
            });
        }

        void addPreDrawListener() {
            if (mPreDrawListener != null) {
                return;
            }
            mPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (mSubtitle.getVisibility() == View.VISIBLE
                            && mSubtitle.getTop() > view.getHeight()
                            && mTitle.getLineCount() > 1) {
                        mTitle.setMaxLines(mTitle.getLineCount() - 1);
                        return false;
                    }
                    final int titleLines = mTitle.getLineCount();
                    final int maxLines = titleLines > 1 ? mBodyMinLines : mBodyMaxLines;
                    if (mBody.getMaxLines() == maxLines) {
                        mBody.setMaxLines(maxLines);
                        return false;
                    } else {
                        removePreDrawListener();
                        return true;
                    }
                }
            };
            view.getViewTreeObserver().addOnPreDrawListener(mPreDrawListener);
        }

        void removePreDrawListener() {
            if (mPreDrawListener != null) {
                view.getViewTreeObserver().removeOnPreDrawListener(mPreDrawListener);
                mPreDrawListener = null;
            }
        }

        public TextView getTitle() {
            return mTitle;
        }

        public TextView getSubtitle() {
            return mSubtitle;
        }

        public TextView getBody() {
            return mBody;
        }

        private FontMetricsInt getFontMetricsInt(TextView textView) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextSize(textView.getTextSize());
            paint.setTypeface(textView.getTypeface());
            return paint.getFontMetricsInt();
        }
    }
}
