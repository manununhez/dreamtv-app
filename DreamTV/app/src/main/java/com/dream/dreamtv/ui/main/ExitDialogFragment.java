package com.dream.dreamtv.ui.main;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;

import androidx.annotation.NonNull;

import com.dream.dreamtv.R;

import java.util.Objects;

import static android.util.TypedValue.applyDimension;


public class ExitDialogFragment extends DialogFragment {
    private static final String TAG = ExitDialogFragment.class.getSimpleName();


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

        viewRoot.setContentView(R.layout.fragment_exit_dialog);

        Button btnExit = viewRoot.findViewById(R.id.btnExit);
        Button btnCancel = viewRoot.findViewById(R.id.btnCancel);


        btnExit.setTextSize((applyDimension(TypedValue.COMPLEX_UNIT_SP, 9,
                getResources().getDisplayMetrics())));
        btnCancel.setTextSize((applyDimension(TypedValue.COMPLEX_UNIT_SP, 9,
                getResources().getDisplayMetrics())));


        btnCancel.setOnClickListener(v -> dismiss());
        btnExit.setOnClickListener(v -> mCallback.exit());


        btnExit.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus)
                btnExit.setTextSize((applyDimension(TypedValue.COMPLEX_UNIT_SP, 10,
                        getResources().getDisplayMetrics())));
            else
                btnExit.setTextSize((applyDimension(TypedValue.COMPLEX_UNIT_SP, 9,
                        getResources().getDisplayMetrics())));
        });

        btnCancel.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus)
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
