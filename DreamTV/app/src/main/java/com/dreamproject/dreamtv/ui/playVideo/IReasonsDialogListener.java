package com.dreamproject.dreamtv.ui.playVideo;


import com.dreamproject.dreamtv.data.model.UserTask;
import com.dreamproject.dreamtv.data.model.UserTaskError;

import java.util.ArrayList;

public interface IReasonsDialogListener {

    void showReasonDialogPopUp(long subtitlePosition,
                               UserTask userTask,
                               ArrayList<UserTaskError> userTaskErrors);
}
