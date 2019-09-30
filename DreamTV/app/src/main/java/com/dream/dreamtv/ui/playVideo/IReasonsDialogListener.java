package com.dream.dreamtv.ui.playVideo;


import com.dream.dreamtv.data.model.UserTask;
import com.dream.dreamtv.data.model.UserTaskError;

import java.util.ArrayList;

public interface IReasonsDialogListener {
    void showReasonDialogPopUp(long subtitlePosition, UserTask userTask);
    void showReasonDialogPopUp(long subtitlePosition, UserTask userTask,
                               ArrayList<UserTaskError> userTaskErrors);
    void controlReasonDialogPopUp();
}
