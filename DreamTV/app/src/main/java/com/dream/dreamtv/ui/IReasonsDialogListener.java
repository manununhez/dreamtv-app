package com.dream.dreamtv.ui;

import com.dream.dreamtv.model.UserTaskError;

import java.util.ArrayList;

public interface IReasonsDialogListener {
    void showReasonDialogPopUp(long subtitlePosition);
    void showReasonDialogPopUp(long subtitlePosition, ArrayList<UserTaskError> userTask);
    void controlReasonDialogPopUp();
}
