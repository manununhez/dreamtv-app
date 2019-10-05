package com.dream.dreamtv.ui.playVideo.dialogs;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;

import androidx.annotation.NonNull;

import com.dream.dreamtv.R;

import java.util.Objects;


public class RatingDialogFragment extends DialogFragment {
    private static final String TAG = RatingDialogFragment.class.getSimpleName();


    // Container Activity must implement this interface
    public interface OnListener {
        void setRating(int rating);
    }

    private OnListener mCallback;
    private RatingBar rtBar;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnListener) {
            mCallback = (OnListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //do nothing. We disable onBackPressed
        Dialog viewRoot = new Dialog(getActivity(), R.style.DefaultDialogFragmentStyle) {
            @Override
            public void onBackPressed() {
                //do nothing. We disable onBackPressed
            }
        };

        viewRoot.setContentView(R.layout.fragment_rating_dialog);

        Button btnSaveAndExit = viewRoot.findViewById(R.id.btnSaveAndExit);
        rtBar = viewRoot.findViewById(R.id.rtBar);

        //set rating bar value min to 1
        rtBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (rating < 1.0f)
                ratingBar.setRating(1.0f);
        });

        rtBar.requestFocus();

        btnSaveAndExit.setOnClickListener(v -> dismiss());

        return viewRoot;
    }

    @Override
    public void onResume() {
        Objects.requireNonNull(getDialog().getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        super.onResume();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        mCallback.setRating((int) rtBar.getRating());
    }

}
