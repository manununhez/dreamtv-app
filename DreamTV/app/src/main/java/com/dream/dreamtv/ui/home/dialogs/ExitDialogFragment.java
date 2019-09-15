package com.dream.dreamtv.ui.home.dialogs;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;

import androidx.annotation.NonNull;

import com.dream.dreamtv.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Objects;

import static android.util.TypedValue.applyDimension;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_CANCEL_EXITDIALOG;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_PRESSED_EXIT_EXITDIALOG;


public class ExitDialogFragment extends DialogFragment {

    private OnListener mCallback;
    private RatingBar rtBar;
    private FirebaseAnalytics mFirebaseAnalytics;

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

        viewRoot.setContentView(R.layout.fragment_exit_dialog);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        Button btnExit = viewRoot.findViewById(R.id.btnExit);
        Button btnCancel = viewRoot.findViewById(R.id.btnCancel);


        btnExit.setTextSize((applyDimension(TypedValue.COMPLEX_UNIT_SP, 9,
                getResources().getDisplayMetrics())));
        btnCancel.setTextSize((applyDimension(TypedValue.COMPLEX_UNIT_SP, 9,
                getResources().getDisplayMetrics())));


        btnCancel.setOnClickListener(v -> {
            mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_CANCEL_EXITDIALOG, new Bundle());
            dismiss();
        });
        btnExit.setOnClickListener(v -> {
            mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_PRESSED_EXIT_EXITDIALOG, new Bundle());
            mCallback.exit();
        });


        btnExit.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                btnExit.setTextSize((applyDimension(TypedValue.COMPLEX_UNIT_SP, 10,
                        getResources().getDisplayMetrics())));
            else
                btnExit.setTextSize((applyDimension(TypedValue.COMPLEX_UNIT_SP, 9,
                        getResources().getDisplayMetrics())));
        });

        btnCancel.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                btnCancel.setTextSize((applyDimension(TypedValue.COMPLEX_UNIT_SP, 10,
                        getResources().getDisplayMetrics())));
            else
                btnCancel.setTextSize((applyDimension(TypedValue.COMPLEX_UNIT_SP, 9,
                        getResources().getDisplayMetrics())));
        });

        btnCancel.requestFocus();

        return viewRoot;
    }

    @Override
    public void onResume() {
        Objects.requireNonNull(getDialog().getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        super.onResume();
    }

    // Container Activity must implement this interface
    public interface OnListener {
        void exit();
    }
}
