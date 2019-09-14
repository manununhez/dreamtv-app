package com.dream.dreamtv.data.networking;

import androidx.lifecycle.MutableLiveData;

import com.dream.dreamtv.data.networking.model.Resource;
import com.dream.dreamtv.data.networking.model.SubtitleResponse;
import com.dream.dreamtv.data.networking.model.Task;
import com.dream.dreamtv.data.model.User;
import com.dream.dreamtv.data.networking.model.UserTask;
import com.dream.dreamtv.data.networking.model.UserTaskError;
import com.dream.dreamtv.data.networking.model.VideoTopicSchema;

public interface NetworkDataSource {
    void login(String email, String password);

    void fetchUserDetails();

    void fetchReasons();

    void fetchVideoTestsDetails();

    void updateUserTask(UserTask userTask);

    MutableLiveData<Resource<User>> updateUser(User user);

    MutableLiveData<Resource<VideoTopicSchema[]>> fetchCategories();

    MutableLiveData<Resource<SubtitleResponse>> fetchSubtitle(String videoId,
                                                              String languageCode, String version);

    MutableLiveData<Resource<Task[]>> searchByKeywordCategory(String category);

    MutableLiveData<Resource<Task[]>> search(String query);

    MutableLiveData<Resource<UserTask>> createUserTask(int taskId, int mSubtitleVersion);

    MutableLiveData<Resource<UserTask>> fetchUserTask();

    MutableLiveData<Resource<Boolean>> addTaskToList(int taskId, String subLanguageConfig, String audioLanguageConfig);

    MutableLiveData<Resource<Boolean>> removeTaskFromList(int taskId);

    MutableLiveData<Resource<UserTaskError[]>> updateErrorReasons(int taskId, int subtitleVersion,
                                                            UserTaskError userTaskError);

    MutableLiveData<Resource<UserTaskError[]>> saveErrorReasons(int taskId, int subtitleVersion,
                                                            UserTaskError userTaskError);

}
