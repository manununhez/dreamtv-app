package com.dream.dreamtv.ui;

import com.dream.dreamtv.model.Subtitle;
import com.dream.dreamtv.model.UserTask;

public interface IReasonsDialogListener {
    void showReasonDialogPopUp(long subtitlePosition);
    void showReasonDialogPopUp(long subtitlePosition, UserTask userTask);
    void controlReasonDialogPopUp();
}
