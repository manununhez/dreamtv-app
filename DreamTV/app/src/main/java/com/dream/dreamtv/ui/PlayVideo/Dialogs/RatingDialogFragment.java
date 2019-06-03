package com.dream.dreamtv.ui.PlayVideo.Dialogs;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;

import androidx.annotation.NonNull;

import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.UserTaskError;

import java.util.ArrayList;
import java.util.Objects;

import static com.dream.dreamtv.utils.Constants.ARG_SELECTED_TASK;
import static com.dream.dreamtv.utils.Constants.ARG_SUBTITLE;
import static com.dream.dreamtv.utils.Constants.ARG_SUBTITLE_ORIGINAL_POSITION;
import static com.dream.dreamtv.utils.Constants.ARG_USER_TASK_ERROR;
import static com.dream.dreamtv.utils.Constants.ARG_USER_TASK_ERROR_COMPLETE;


public class RatingDialogFragment extends DialogFragment {
    private static final String TAG = RatingDialogFragment.class.getSimpleName();


    private OnListener mCallback;
    private int selectedRating;
    private Dialog viewRoot;
    private Button btnSaveAndExit;
    private RatingBar rtBar;


    // Container Activity must implement this interface
    public interface OnListener {
        void setRating(int rating);
    }


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
        viewRoot = new Dialog(getActivity(), R.style.DefaultDialogFragmentStyle);
        viewRoot.setContentView(R.layout.fragment_rating_dialog);

        btnSaveAndExit = viewRoot.findViewById(R.id.btnSaveAndExit);
        rtBar = viewRoot.findViewById(R.id.rtBar);


        btnSaveAndExit.setOnClickListener(v -> {
            dismiss();
        });

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
