package com.manuelnunhez.dreamtv.presenter.sideInfoPresenter;

import android.content.Context;
import android.view.ViewGroup;

import com.manuelnunhez.dreamtv.data.model.Card;

import androidx.leanback.widget.BaseCardView;
import androidx.leanback.widget.Presenter;


/**
 * This abstract, generic class will create and manage the
 * ViewHolder and will provide typed Presenter callbacks such that you do not have to perform casts
 * on your own.
 *
 * @param <T> View type for the card.
 */
public abstract class AbstractCardPresenter<T extends BaseCardView> extends Presenter {

    private static final String TAG = "AbstractCardPresenter";
    private final Context mContext;

    /**
     * @param context The current context.
     */
    public AbstractCardPresenter(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        T cardView = onCreateView();
        return new ViewHolder(cardView);
    }

    @Override
    public final void onBindViewHolder(ViewHolder viewHolder, Object item) {
        Card card = (Card) item;
        onBindViewHolder(card, (T) viewHolder.view);
    }

    @Override
    public final void onUnbindViewHolder(ViewHolder viewHolder) {
        onUnbindViewHolder();
    }

    public void onUnbindViewHolder() {
        // Nothing to clean up. Override if necessary.
    }

    /**
     * Invoked when a new view is created.
     *
     * @return Returns the newly created view.
     */
    protected abstract T onCreateView();

    /**
     * Implement this method to update your card's view with the data bound to it.
     *
     * @param card     The model containing the data for the card.
     * @param cardView The view the card is bound to.
     */
    public abstract void onBindViewHolder(Card card, T cardView);

}
