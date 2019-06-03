package com.dream.dreamtv.common;

import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.UserTaskError;

import java.util.ArrayList;

public interface IReasonsDialogListener {
    void showReasonDialogPopUp(long subtitlePosition, UserTask userTask);
    void showReasonDialogPopUp(long subtitlePosition, UserTask userTask,
                               ArrayList<UserTaskError> userTaskErrors);
    void controlReasonDialogPopUp();
}
