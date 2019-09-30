package com.dream.dreamtv.data.networking;

import androidx.lifecycle.MutableLiveData;

import com.dream.dreamtv.data.model.Category;
import com.dream.dreamtv.data.model.Subtitle;
import com.dream.dreamtv.data.model.Task;
import com.dream.dreamtv.data.model.TasksList;
import com.dream.dreamtv.data.model.User;
import com.dream.dreamtv.data.model.UserTask;
import com.dream.dreamtv.data.model.UserTaskError;
import com.dream.dreamtv.data.model.VideoDuration;
import com.dream.dreamtv.data.model.VideoTopic;
import com.dream.dreamtv.data.networking.model.Resource;

public interface NetworkDataSource {
    void login(String email, String password);

    void register(String email, String password);

    void fetchUserDetails();

    void fetchReasons();

    void fetchVideoTestsDetails();

    void updateUserTask(UserTask userTask);

    MutableLiveData<Resource<TasksList[]>> fetchTasks(Category.Type category, VideoDuration videoDuration);

    MutableLiveData<Resource<VideoTopic[]>> fetchCategories();

    MutableLiveData<Resource<User>> updateUser(User user);

    MutableLiveData<Resource<Subtitle>> fetchSubtitle(String videoId,
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
