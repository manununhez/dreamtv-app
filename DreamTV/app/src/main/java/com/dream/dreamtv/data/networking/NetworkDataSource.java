package com.dream.dreamtv.data.networking;

import androidx.lifecycle.MutableLiveData;

import com.dream.dreamtv.data.model.api.Resource;
import com.dream.dreamtv.data.model.api.SubtitleResponse;
import com.dream.dreamtv.data.model.api.Task;
import com.dream.dreamtv.data.model.api.User;
import com.dream.dreamtv.data.model.api.UserTask;
import com.dream.dreamtv.data.model.api.UserTaskError;
import com.dream.dreamtv.data.model.api.VideoTopic;

public interface NetworkDataSource {
    void login(String email, String password);

    void fetchUserDetails();

    void fetchReasons();

    void fetchVideoTestsDetails();

    void updateUserTask(UserTask userTask);

    MutableLiveData<Resource<User>> updateUser(User user);

    MutableLiveData<Resource<VideoTopic[]>> fetchCategories();

    MutableLiveData<Resource<SubtitleResponse>> fetchSubtitle(String videoId,
                                                              String languageCode, String version);

    MutableLiveData<Resource<Task[]>> searchByKeywordCategory(String category);

    MutableLiveData<Resource<Task[]>> search(String query);

    MutableLiveData<Resource<UserTask>> createUserTask(int taskId, int mSubtitleVersion);

    MutableLiveData<Resource<UserTask>> fetchUserTask();

    MutableLiveData<Resource<Boolean>> addTaskToList(int taskId, String subLanguageConfig, String audioLanguageConfig);

    MutableLiveData<Resource<Boolean>> removeTaskFromList(int taskId);

    MutableLiveData<Resource<UserTaskError[]>> errorsUpdate(int taskId, int subtitleVersion,
                                                            UserTaskError userTaskError, boolean saveError);

    enum Urls {
        LOGIN("login"),

        REGISTER("register"),

        USER_DETAILS("details"),

        TASKS_BY_KEYWORD_CATEGORIES("tasks/search/category"),

        TASKS_BY_CATEGORY("tasks/categories"),

        TASKS_SEARCH("tasks/search"),

        USER_TASKS("usertasks"),

        USER_ERRORS("usertask/errors"),

        USER_TASK_MY_LIST("usertask/list"),

        VIDEO_TESTS("videotests"),

        CATEGORIES("categories"),

        REASON_ERRORS("errors"),

        SUBTITLE("amara/subtitle"),

        USER("user");


        final String value;

        Urls(String value) {
            this.value = value;
        }
    }
}
