package com.manuelnunhez.dreamtv.ui.playVideo;


import com.manuelnunhez.dreamtv.data.model.UserTask;
import com.manuelnunhez.dreamtv.data.model.UserTaskError;

import java.util.ArrayList;

public interface IReasonsDialogListener {

    void showReasonDialogPopUp(long subtitlePosition,
                               UserTask userTask,
                               ArrayList<UserTaskError> userTaskErrors);
}
