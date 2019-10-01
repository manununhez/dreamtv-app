package com.dream.dreamtv.data.networking;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.android.volley.VolleyError;
import com.dream.dreamtv.data.model.Category;
import com.dream.dreamtv.data.model.ErrorReason;
import com.dream.dreamtv.data.model.Subtitle;
import com.dream.dreamtv.data.model.Subtitle.SubtitleText;
import com.dream.dreamtv.data.model.Task;
import com.dream.dreamtv.data.model.TasksList;
import com.dream.dreamtv.data.model.User;
import com.dream.dreamtv.data.model.UserTask;
import com.dream.dreamtv.data.model.UserTaskError;
import com.dream.dreamtv.data.model.Video;
import com.dream.dreamtv.data.model.VideoDuration;
import com.dream.dreamtv.data.model.VideoTest;
import com.dream.dreamtv.data.model.VideoTopic;
import com.dream.dreamtv.data.networking.model.AuthResponse;
import com.dream.dreamtv.data.networking.model.ErrorReasonSchema;
import com.dream.dreamtv.data.networking.model.JsonResponseBaseBean;
import com.dream.dreamtv.data.networking.model.Resource;
import com.dream.dreamtv.data.networking.model.SubtitleSchema;
import com.dream.dreamtv.data.networking.model.TaskSchema;
import com.dream.dreamtv.data.networking.model.TasksListSchema;
import com.dream.dreamtv.data.networking.model.UserSchema;
import com.dream.dreamtv.data.networking.model.UserTaskErrorSchema;
import com.dream.dreamtv.data.networking.model.UserTaskSchema;
import com.dream.dreamtv.data.networking.model.VideoSchema;
import com.dream.dreamtv.data.networking.model.VideoTestSchema;
import com.dream.dreamtv.data.networking.model.VideoTopicSchema;
import com.dream.dreamtv.ui.home.HomeFragment;
import com.dream.dreamtv.utils.AppExecutors;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static com.android.volley.Request.Method.DELETE;
import static com.android.volley.Request.Method.GET;
import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;
import static com.dream.dreamtv.data.networking.NetworkUtils.addTaskToUserListURL;
import static com.dream.dreamtv.data.networking.NetworkUtils.getCategoriesURL;
import static com.dream.dreamtv.data.networking.NetworkUtils.getErrorReasonsURL;
import static com.dream.dreamtv.data.networking.NetworkUtils.getRegisterURL;
import static com.dream.dreamtv.data.networking.NetworkUtils.getSubtitleURL;
import static com.dream.dreamtv.data.networking.NetworkUtils.getTasksURL;
import static com.dream.dreamtv.data.networking.NetworkUtils.getUserURL;
import static com.dream.dreamtv.data.networking.NetworkUtils.getVideoTestsURL;
import static com.dream.dreamtv.data.networking.NetworkUtils.removeTaskFromUserListURL;
import static com.dream.dreamtv.data.networking.NetworkUtils.searchByCategoryURL;
import static com.dream.dreamtv.data.networking.NetworkUtils.searchURL;
import static com.dream.dreamtv.data.networking.NetworkUtils.userErrorsURL;
import static com.dream.dreamtv.data.networking.NetworkUtils.userTaskURL;
import static com.dream.dreamtv.data.networking.model.SubtitleSchema.SubtitleTextSchema;
import static com.dream.dreamtv.utils.Constants.PARAM_AUDIO_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.PARAM_CATEGORY;
import static com.dream.dreamtv.utils.Constants.PARAM_COMPLETED;
import static com.dream.dreamtv.utils.Constants.PARAM_EMAIL;
import static com.dream.dreamtv.utils.Constants.PARAM_LANG_CODE;
import static com.dream.dreamtv.utils.Constants.PARAM_PASSWORD;
import static com.dream.dreamtv.utils.Constants.PARAM_QUERY;
import static com.dream.dreamtv.utils.Constants.PARAM_RATING;
import static com.dream.dreamtv.utils.Constants.PARAM_REASON_CODE;
import static com.dream.dreamtv.utils.Constants.PARAM_SUB_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.PARAM_SUB_POSITION;
import static com.dream.dreamtv.utils.Constants.PARAM_SUB_VERSION;
import static com.dream.dreamtv.utils.Constants.PARAM_TASK_ID;
import static com.dream.dreamtv.utils.Constants.PARAM_TIME_WATCHED;
import static com.dream.dreamtv.utils.Constants.PARAM_TYPE;
import static com.dream.dreamtv.utils.Constants.PARAM_VERSION;
import static com.dream.dreamtv.utils.Constants.PARAM_VIDEO_ID;
import static com.dream.dreamtv.utils.JsonUtils.getJsonResponse;

public class NetworkDataSourceImpl implements NetworkDataSource {

    // For Singleton instantiation
    private static NetworkDataSourceImpl INSTANCE;

    private final Context mContext;
    private final AppExecutors mExecutors;
    private final VolleyController mVolley;
    // VolleyController requestQueue
    private MutableLiveData<Resource<TasksList[]>> responseFromFetchTasks;
    private MutableLiveData<Resource<Task[]>> responseFromSearch;
    private MutableLiveData<Resource<Task[]>> responseFromSearchByKeywordCategory;
    private MutableLiveData<Resource<Boolean>> responseFromAddToListTasks;
    private MutableLiveData<Resource<Boolean>> responseFromRemoveFromListTasks;
    private MutableLiveData<Resource<User>> responseFromFetchUser;
    private MutableLiveData<Resource<User>> responseFromUserUpdate;
    private MutableLiveData<Resource<Subtitle>> responseFromFetchSubtitle;
    private MutableLiveData<Resource<UserTask>> responseFromFetchUserTask;
    private MutableLiveData<Resource<UserTask>> responseFromCreateUserTask;
    private MutableLiveData<Resource<UserTaskError[]>> responseFromErrorsUpdate;
    private MutableLiveData<Resource<VideoTopic[]>> responseFromCategories;
    private MutableLiveData<Resource<ErrorReason[]>> responseFromFetchReasons;
    private MutableLiveData<Resource<VideoTest[]>> responseFromFetchVideoTests;
    private MutableLiveData<Resource<AuthResponse>> responseFromAuth;

    private NetworkDataSourceImpl(Context context, AppExecutors executors, VolleyController volley) {
        mContext = context;
        mVolley = volley;

        mExecutors = executors;

        responseFromAuth = new MutableLiveData<>();
        responseFromFetchVideoTests = new MutableLiveData<>();
        responseFromFetchReasons = new MutableLiveData<>();
        responseFromSearchByKeywordCategory = new MutableLiveData<>();
        responseFromSearch = new MutableLiveData<>();
        responseFromFetchUser = new MutableLiveData<>();
        responseFromUserUpdate = new MutableLiveData<>();
        responseFromFetchSubtitle = new MutableLiveData<>();
        responseFromFetchTasks = new MutableLiveData<>();
        responseFromFetchUserTask = new MutableLiveData<>();
        responseFromCreateUserTask = new MutableLiveData<>();
        responseFromAddToListTasks = new MutableLiveData<>();
        responseFromRemoveFromListTasks = new MutableLiveData<>();
        responseFromErrorsUpdate = new MutableLiveData<>();
        responseFromCategories = new MutableLiveData<>();

    }

    public static synchronized NetworkDataSourceImpl getInstance(Context context, AppExecutors executors, VolleyController volley) {
        if (INSTANCE == null) {
            synchronized (NetworkDataSourceImpl.class) {
                INSTANCE = new NetworkDataSourceImpl(context, executors, volley);
                Timber.d("Made new NetworkDataSourceImpl");

            }
        }
        return INSTANCE;
    }


    //****************
    //** NETWORK CALLS **
    //*******************/


    /**
     * Login. Used in {@link HomeFragment} to implement the ...
     *
     * @param email    Email
     * @param password password
     */
    @Override
    @SuppressWarnings("unchecked")
    public void login(final String email, final String password) {
        String URL = NetworkUtils.getLoginURL();

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_EMAIL, email);
        params.put(PARAM_PASSWORD, password);


        Timber.d("login() Request URL: " + URL + " Paramaters: email=>" + email
                + "; password=>" + password);

        responseFromAuth.setValue(Resource.loading(null));

        mExecutors.networkIO().execute(() -> mVolley.requestString(POST, URL, params, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d("login() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<AuthResponse>>() {
                };
                JsonResponseBaseBean<AuthResponse> jsonResponse = getJsonResponse(response, type);

                responseFromAuth.postValue(Resource.success(jsonResponse.data));

                fetchUserDetails();
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                //TODO do something error
//                Timber.d( "login() Error response: " + error.getMessage());
//                if (VolleyErrorHelper.getErrorType(error, mContext).equals(mContext.getString(R.string.auth_failed)))
                register(email, password);

            }
        }));
    }

    /**
     * Register. Used in {@link HomeFragment} to implement the ...
     *
     * @param email    Email
     * @param password Password
     */
    @Override
    @SuppressWarnings("unchecked")
    public void register(final String email, final String password) {
        String URL = getRegisterURL();

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_EMAIL, email);
        params.put(PARAM_PASSWORD, password);

        Timber.d("register() Request URL: " + URL + " Paramaters: email=>" + email
                + "; password=>" + password);

        responseFromAuth.setValue(Resource.loading(null));

        mExecutors.networkIO().execute(() -> mVolley.requestString(POST, URL, params, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d("register() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<AuthResponse>>() {
                };
                JsonResponseBaseBean<AuthResponse> jsonResponse = getJsonResponse(response, type);

                responseFromAuth.postValue(Resource.success(jsonResponse.data));

                fetchUserDetails();
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                login(email, password); //we try to login again
            }
        }));
    }

    /**
     * UserDetails. Used in {@link HomeFragment} to get user details
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fetchUserDetails() {
        String URL = NetworkUtils.getUserDetailsURL();

        responseFromFetchUser.setValue(Resource.loading(null));

        Timber.d("fetchUserDetails() Request URL: %s", URL);


        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, URL, null, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d("fetchUserDetails() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<UserSchema>>() {
                };
                JsonResponseBaseBean<UserSchema> jsonResponse = getJsonResponse(response, type);

                UserSchema userSchema = jsonResponse.data;
                User user = getUserFromSchema(userSchema);

                responseFromFetchUser.postValue(Resource.success(user)); //post the value to live data

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromFetchUser.postValue(Resource.error(error.getMessage(), null));

                Timber.d("fetchUserDetails() Response Error: %s", error.getMessage());
            }
        }));

    }


    /**
     * Used in {@link HomeFragment}
     *
     * @param user user
     * @return MutableLiveData<Resource < User>>
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<User>> updateUser(User user) {
        responseFromUserUpdate.setValue(Resource.loading(null));

        String userUri = getUserURL(user.getInterfaceMode(), user.getSubLanguage(), user.getAudioLanguage());

        Timber.d("updateUser() Request URL: " + userUri + " Params: interfaceMode=>" + user.getInterfaceMode()
                + "; subLanguage=>" + user.getSubLanguage()
                + "; audioLanguage=>" + user.getAudioLanguage());


        mExecutors.networkIO().execute(() -> mVolley.requestString(PUT, userUri, null, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d("updateUser() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<UserSchema>>() {
                };
                JsonResponseBaseBean<UserSchema> jsonResponse = getJsonResponse(response, type);

                UserSchema userSchema = jsonResponse.data;
                User user = getUserFromSchema(userSchema);

                responseFromUserUpdate.postValue(Resource.success(user)); //post the value to live data

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromUserUpdate.postValue(Resource.error(error.getMessage(), null));

                Timber.d("updateUser() Response Error: %s", error.getMessage());
            }
        }));

        return responseFromUserUpdate;
    }


    /**
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fetchReasons() {
        String URL = getErrorReasonsURL();

        Timber.d("fetchReasons() Request URL: %s", URL);

        responseFromFetchReasons.setValue(Resource.loading(null));


        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, URL, null, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d("fetchReasons() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<ErrorReasonSchema[]>>() {
                };
                JsonResponseBaseBean<ErrorReasonSchema[]> jsonResponse = getJsonResponse(response, type);

                ErrorReason[] errorReasons = getReasonsFromSchema(jsonResponse.data);

                responseFromFetchReasons.postValue(Resource.success(errorReasons));

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                responseFromFetchReasons.postValue(Resource.error(error.getMessage(), null));

            }
        }));
    }

    /**
     * @return MutableLiveData<Resource < VideoTopicSchema [ ]>>
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<VideoTopic[]>> fetchCategories() {
        responseFromCategories.setValue(Resource.loading(null));

        String categoriesUri = getCategoriesURL();

        Timber.d("fetchCategories() Request URL: %s", categoriesUri);


        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, categoriesUri, null, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d("fetchCategories() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<VideoTopicSchema[]>>() {
                };
                JsonResponseBaseBean<VideoTopicSchema[]> jsonResponse = getJsonResponse(response, type);

                VideoTopic[] videoTopics = getTopicFromSchema(jsonResponse.data);

                responseFromCategories.postValue(Resource.success(videoTopics));

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromCategories.postValue(Resource.error(error.getMessage(), null));

                Timber.d("fetchCategories() Response Error: %s", error.getMessage());
            }
        }));

        return responseFromCategories;

    }

    /**
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fetchVideoTestsDetails() {
        String URL = getVideoTestsURL();

        Timber.d("fetchVideoTestsDetails() Request URL: %s", URL);

        responseFromFetchVideoTests.setValue(Resource.loading(null));


        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, URL, null, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d("fetchVideoTestsDetails() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<VideoTestSchema[]>>() {
                };
                JsonResponseBaseBean<VideoTestSchema[]> jsonResponse = getJsonResponse(response, type);

                VideoTest[] videoTests = getVideoTestFromSchema(jsonResponse.data);
                responseFromFetchVideoTests.postValue(Resource.success(videoTests));

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                responseFromFetchVideoTests.postValue(Resource.error(error.getMessage(), null));

            }
        }));
    }


    /**
     * FetchSubtitle
     *
     * @param videoId      videoId
     * @param languageCode languageCode
     * @param version      version
     * @return MutableLiveData<Resource < SubtitleResponse>>
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<Subtitle>> fetchSubtitle(String videoId, String languageCode, String version) {
        responseFromFetchSubtitle.postValue(Resource.loading(null));

        String subtitleUri = getSubtitleURL(videoId, languageCode, version);

        Timber.d("fetchSubtitle() Request URL: " + subtitleUri + " Params: " + PARAM_VIDEO_ID + "=>" + videoId
                + "; " + PARAM_LANG_CODE + "=>" + languageCode
                + "; " + PARAM_VERSION + "=>" + version);


        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, subtitleUri, null, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d("fetchSubtitle() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<SubtitleSchema>>() {
                };
                JsonResponseBaseBean<SubtitleSchema> jsonResponse = getJsonResponse(response, type);

                Subtitle subtitle = getSubtitleFromSchema(jsonResponse.data);

                responseFromFetchSubtitle.postValue(Resource.success(subtitle)); //post the value to live data

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromFetchSubtitle.postValue(Resource.error(error.getMessage(), null));

                Timber.d("fetchSubtitle() Response Error: %s", error.getMessage());
            }
        }));

        return responseFromFetchSubtitle;
    }


    /**
     * fetchTasks used to get Tasks by categories
     *
     * @param category      category
     * @param videoDuration videoDuration
     * @return MutableLiveData<Resource < TasksList [ ]>>
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<TasksList[]>> fetchTasks(Category.Type category, VideoDuration videoDuration) {


        responseFromFetchTasks.setValue(Resource.loading(null));

        String tasksUri = getTasksURL(category, videoDuration);

        Timber.d("fetchTasks() Request URL: " + tasksUri + " Params: " + PARAM_TYPE + "=>" + category);


        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, tasksUri, null, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d(category + "fetchTasks() Response JSON: " + response);


                TypeToken type = new TypeToken<JsonResponseBaseBean<TasksListSchema[]>>() {
                };
                JsonResponseBaseBean<TasksListSchema[]> jsonResponse = getJsonResponse(response, type);

                TasksList[] tasksList = getTasksListFromSchema(jsonResponse.data);

                responseFromFetchTasks.postValue(Resource.success(tasksList)); //post the value to live data

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromFetchTasks.postValue(Resource.error(error.getMessage(), null));

                Timber.d(category + "fetchTasks() Response Error: " + error.getMessage());
            }
        }));

        return responseFromFetchTasks;
    }

    /**
     * @param category category
     * @return responseFromSearchByKeywordCategory
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<Task[]>> searchByKeywordCategory(String category) {

        responseFromSearchByKeywordCategory.setValue(Resource.loading(null));

        String searchUri = searchByCategoryURL(category);

        Timber.d("searchByKeywordCategory() Request URL: " + searchUri
                + " Params: " + PARAM_CATEGORY + "=>" + category);


        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, searchUri, null, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d("searchByKeywordCategory() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<TaskSchema[]>>() {
                };
                JsonResponseBaseBean<TaskSchema[]> jsonResponse = getJsonResponse(response, type);

                Task[] tasks = getTasksFromSchema(jsonResponse.data);

                responseFromSearchByKeywordCategory.postValue(Resource.success(tasks)); //post the value to live data

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromSearchByKeywordCategory.postValue(Resource.error(error.getMessage(), null));

                Timber.d("searchByKeywordCategory() Response Error: %s", error.getMessage());
            }
        }));

        return responseFromSearchByKeywordCategory;
    }

    /**
     * @param query query
     * @return responseFromSearch
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<Task[]>> search(String query) {

        responseFromSearch.setValue(Resource.loading(null));

        String searchUri = searchURL(query);

        Timber.d("search() Request URL: " + searchUri + " Params: " + PARAM_QUERY + "=>" + query);


        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, searchUri, null, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {

                Timber.d("search() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<TaskSchema[]>>() {
                };
                JsonResponseBaseBean<TaskSchema[]> jsonResponse = getJsonResponse(response, type);

                Task[] tasks = getTasksFromSchema(jsonResponse.data);

                responseFromSearch.postValue(Resource.success(tasks)); //post the value to live data

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromSearch.postValue(Resource.error(error.getMessage(), null));

                Timber.d("search() Response Error: %s", error.getMessage());
            }
        }));

        return responseFromSearch;
    }

    /**
     * @param taskId           taskId
     * @param mSubtitleVersion mSubtitleVersion
     * @return MutableLiveData<Resource < UserTask>>
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<UserTask>> createUserTask(int taskId, int mSubtitleVersion) {
        String URL = userTaskURL();

        responseFromCreateUserTask.postValue(Resource.loading(null));

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(taskId));
        params.put(PARAM_SUB_VERSION, String.valueOf(mSubtitleVersion));


        Timber.d("createUserTask() Request URL: " + URL + " Params: " + PARAM_TASK_ID + "=>" + taskId
                + "; " + PARAM_SUB_VERSION + " => " + mSubtitleVersion);


        mExecutors.networkIO().execute(() -> mVolley.requestString(POST, URL, params, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d("createUserTask() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTaskSchema>>() {
                };
                JsonResponseBaseBean<UserTaskSchema> jsonResponse = getJsonResponse(response, type);

                UserTask userTask = getUserTaskFromSchema(jsonResponse.data);

                responseFromCreateUserTask.postValue(Resource.success(userTask)); //post the value to live data

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromCreateUserTask.postValue(Resource.error(error.getMessage(), null));

                Timber.d("createUserTask() Response Error: %s", error.getMessage());
            }
        }));

        return responseFromCreateUserTask;
    }


    /**
     * @return MutableLiveData<Resource < UserTask>>
     */
    @Override
    public MutableLiveData<Resource<UserTask>> fetchUserTask() {
        return responseFromFetchUserTask;

    }

    /**
     * @param taskId        taskId
     * @param subLanguage   subLanguage
     * @param audioLanguage audioLanguage
     * @return MutableLiveData<Resource < Boolean>>
     */
    @Override
    public MutableLiveData<Resource<Boolean>> addTaskToList(int taskId, String subLanguage, String audioLanguage) {

        String URL = addTaskToUserListURL();

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(taskId));
        params.put(PARAM_SUB_LANGUAGE, subLanguage);
        params.put(PARAM_AUDIO_LANGUAGE, audioLanguage);

        Timber.d("addTaskToList() Request URL: " + URL + " Params: " + PARAM_TASK_ID + "=>" + taskId
                + "; " + PARAM_SUB_LANGUAGE + "=>" + subLanguage
                + "; " + PARAM_AUDIO_LANGUAGE + "=>" + audioLanguage);


        mExecutors.networkIO().execute(() -> mVolley.requestString(POST, URL, params, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {

                Timber.d("addTaskToList() Response JSON: %s", response);

                responseFromAddToListTasks.postValue(Resource.success(true));
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                responseFromAddToListTasks.postValue(Resource.error(error.getMessage(), null));
            }
        }));

        return responseFromAddToListTasks;

    }

    /**
     * @param taskId taskId
     * @return MutableLiveData<Resource < Boolean>>
     */
    @Override
    public MutableLiveData<Resource<Boolean>> removeTaskFromList(int taskId) {
        String removeFromListUri = removeTaskFromUserListURL(taskId);

        Timber.d("removeTaskFromList() Request URL: " + removeFromListUri + " Params: " + PARAM_TASK_ID + "=>" + taskId);


        mExecutors.networkIO().execute(() -> mVolley.requestString(DELETE, removeFromListUri, null, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {

                Timber.d("removeTaskFromList() Response JSON: %s", response);


                responseFromRemoveFromListTasks.postValue(Resource.success(true));
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                responseFromRemoveFromListTasks.postValue(Resource.error(error.getMessage(), null));

            }

        }));

        return responseFromRemoveFromListTasks;

    }

    /**
     * @param userTask userTask
     */
    @Override
    @SuppressWarnings("unchecked")
    public void updateUserTask(UserTask userTask) {
        String URL = userTaskURL();

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(userTask.getTaskId()));
        params.put(PARAM_SUB_VERSION, userTask.getSubVersion());
        params.put(PARAM_TIME_WATCHED, String.valueOf(userTask.getTimeWatched()));
        params.put(PARAM_COMPLETED, String.valueOf(userTask.
                getCompleted()));
        params.put(PARAM_RATING, String.valueOf(userTask.getRating()));

        Timber.d("updateUserTask() Request URL: " + URL + " Params: " + PARAM_TASK_ID + "=>" + userTask.getTaskId()
                + "; " + PARAM_SUB_VERSION + " => " + userTask.getSubVersion());


        mExecutors.networkIO().execute(() -> mVolley.requestString(PUT, URL, params, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {

                Timber.d("updateUserTask() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTaskSchema>>() {
                };
                JsonResponseBaseBean<UserTaskSchema> jsonResponse = getJsonResponse(response, type);

                UserTask userTask = getUserTaskFromSchema(jsonResponse.data);

                responseFromFetchUserTask.postValue(Resource.success(userTask)); //to update the userTask value in VideoDetailsFragment
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

            }

        }));

    }

    /**
     * @param taskId          taskId
     * @param subtitleVersion subVersion
     * @param userTaskError   userTaskError
     * @return MutableLiveData<Resource < UserTaskError [ ]>>
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<UserTaskError[]>> updateErrorReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        String URL = userErrorsURL();

        responseFromErrorsUpdate.postValue(Resource.loading(null));

        String errorReasonList = getErrorReasonListSchemaFromModel(userTaskError.getErrorReasonList());

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(taskId));
        params.put(PARAM_REASON_CODE, errorReasonList);
        params.put(PARAM_SUB_POSITION, String.valueOf(userTaskError.getSubtitlePosition()));
        params.put(PARAM_SUB_VERSION, String.valueOf(subtitleVersion));


        Timber.d("updateErrors() Request URL: " + URL + " Params: " + PARAM_TASK_ID + "=>" + taskId
                + "; " + PARAM_SUB_VERSION + " => " + subtitleVersion
                + "; " + PARAM_SUB_POSITION + " => " + userTaskError.getSubtitlePosition()
                + "; " + PARAM_REASON_CODE + " => " + errorReasonList);


        mExecutors.networkIO().execute(() -> mVolley.requestString(PUT, URL, params, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {

                Timber.d("updateErrors() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTaskErrorSchema[]>>() {
                };
                JsonResponseBaseBean<UserTaskErrorSchema[]> jsonResponse = getJsonResponse(response, type);

                UserTaskError[] userTaskErrors = getUserTaskErrorsFromSchema(jsonResponse.data);

                responseFromErrorsUpdate.postValue(Resource.success(userTaskErrors));
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                responseFromErrorsUpdate.postValue(Resource.error(error.getMessage(), null));
            }

        }));

        return responseFromErrorsUpdate;
    }


    /**
     * @param taskId          taskId
     * @param subtitleVersion subVersion
     * @param userTaskError   userTaskError
     * @return MutableLiveData<Resource < UserTaskError [ ]>>
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<UserTaskError[]>> saveErrorReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        String URL = userErrorsURL();

        responseFromErrorsUpdate.postValue(Resource.loading(null));

        String errorReasonList = getErrorReasonListSchemaFromModel(userTaskError.getErrorReasonList());

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(taskId));
        params.put(PARAM_REASON_CODE, errorReasonList);
        params.put(PARAM_SUB_POSITION, String.valueOf(userTaskError.getSubtitlePosition()));
        params.put(PARAM_SUB_VERSION, String.valueOf(subtitleVersion));


        Timber.d("saveErrors() Request URL: " + URL + " Params: " + PARAM_TASK_ID + "=>" + taskId
                + "; " + PARAM_SUB_VERSION + " => " + subtitleVersion
                + "; " + PARAM_SUB_POSITION + " => " + userTaskError.getSubtitlePosition()
                + "; " + PARAM_REASON_CODE + " => " + errorReasonList);


        mExecutors.networkIO().execute(() -> mVolley.requestString(POST, URL, params, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d("saveErrors() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTaskErrorSchema[]>>() {
                };
                JsonResponseBaseBean<UserTaskErrorSchema[]> jsonResponse = getJsonResponse(response, type);

                UserTaskError[] userTaskErrors = getUserTaskErrorsFromSchema(jsonResponse.data);


                responseFromErrorsUpdate.postValue(Resource.success(userTaskErrors));
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                responseFromErrorsUpdate.postValue(Resource.error(error.getMessage(), null));

            }

        }));

        return responseFromErrorsUpdate;
    }


    public MutableLiveData<Resource<User>> responseFromFetchUserDetails() {
        return responseFromFetchUser;
    }

    public MutableLiveData<Resource<User>> responseFromUserDetailsUpdate() {
        return responseFromUserUpdate;
    }

    public MutableLiveData<Resource<ErrorReason[]>> responseFromFetchReasons() {
        return responseFromFetchReasons;
    }

    public MutableLiveData<Resource<VideoTest[]>> responseFromVideoTests() {
        return responseFromFetchVideoTests;
    }

    public MutableLiveData<Resource<TasksList[]>> responseFromFetchTasks() {
        return responseFromFetchTasks;
    }

    public MutableLiveData<Resource<AuthResponse>> responseFromAuth() {
        return responseFromAuth;
    }


    /*******************
     * DATA MAPPER
     *******************/

    //*******SCHEMA TO Model************
    private User getUserFromSchema(UserSchema userSchema) {
        return new User(userSchema.email, userSchema.password, userSchema.subLanguage, userSchema.audioLanguage,
                userSchema.interfaceMode);
    }

    private VideoTopic[] getTopicFromSchema(VideoTopicSchema[] videoTopicSchemas) {
        VideoTopic[] videoTopics = new VideoTopic[videoTopicSchemas.length];

        for (int i = 0; i < videoTopicSchemas.length; i++) {
            videoTopics[i] = new VideoTopic(videoTopicSchemas[i].imageName, videoTopicSchemas[i].language,
                    videoTopicSchemas[i].name);
        }

        return videoTopics;
    }

    private TasksList[] getTasksListFromSchema(TasksListSchema[] tasksListSchemas) {
        TasksList[] tasksLists = new TasksList[tasksListSchemas.length];
        for (int i = 0; i < tasksListSchemas.length; i++) {
            tasksLists[i] = new TasksList(getTasksFromSchema(tasksListSchemas[i].tasks),
                    new Category(tasksListSchemas[i].category.orderIndex,
                            tasksListSchemas[i].category.name, tasksListSchemas[i].category.visible));
        }

        return tasksLists;
    }

    private ErrorReason[] getReasonsFromSchema(ErrorReasonSchema[] errorReasonSchemas) {
        ErrorReason[] errorReasons = new ErrorReason[errorReasonSchemas.length];

        for (int i = 0; i < errorReasonSchemas.length; i++) {
            errorReasons[i] = new ErrorReason(errorReasonSchemas[i].reasonCode, errorReasonSchemas[i].name,
                    errorReasonSchemas[i].description, errorReasonSchemas[i].language);
        }

        return errorReasons;
    }

    private Task[] getTasksFromSchema(TaskSchema[] taskSchemas) {
        Task[] tasks = new Task[taskSchemas.length];

        for (int i = 0; i < taskSchemas.length; i++) {
            tasks[i] = new Task(taskSchemas[i].taskId, taskSchemas[i].videoId, taskSchemas[i].videoTitleTranslated,
                    taskSchemas[i].videoDescriptionTranslated, taskSchemas[i].subLanguage, taskSchemas[i].type, getVideoFromSchema(taskSchemas[i].video),
                    getUserTasksFromSchema(taskSchemas[i].userTasks));
        }
        return tasks;
    }

    private UserTask[] getUserTasksFromSchema(UserTaskSchema[] userTaskSchemas) {
        UserTask[] userTasks = new UserTask[userTaskSchemas.length];
        for (int i = 0; i < userTaskSchemas.length; i++) {
            userTasks[i] = getUserTaskFromSchema(userTaskSchemas[i]);
        }

        return userTasks;
    }

    private UserTask getUserTaskFromSchema(UserTaskSchema userTaskSchema) {
        return new UserTask(userTaskSchema.id, userTaskSchema.userId, userTaskSchema.taskId, userTaskSchema.completed,
                userTaskSchema.rating, userTaskSchema.timeWatched, userTaskSchema.subVersion, getUserTaskErrorsFromSchema(userTaskSchema.userTaskErrorList));
    }

    private UserTaskError[] getUserTaskErrorsFromSchema(UserTaskErrorSchema[] userTaskErrorSchemas) {
        UserTaskError[] userTaskErrors = new UserTaskError[userTaskErrorSchemas.length];

        for (int i = 0; i < userTaskErrorSchemas.length; i++) {
            userTaskErrors[i] = new UserTaskError(userTaskErrorSchemas[i].reasonCode,
                    userTaskErrorSchemas[i].subtitlePosition, userTaskErrorSchemas[i].comment);
        }

        return userTaskErrors;
    }

    private Video getVideoFromSchema(VideoSchema videoSchema) {
        return new Video(videoSchema.videoId, videoSchema.audioLanguage, videoSchema.speakerName,
                videoSchema.title, videoSchema.description, videoSchema.duration, videoSchema.thumbnail,
                videoSchema.team, videoSchema.project, videoSchema.videoUrl);
    }

    private Subtitle getSubtitleFromSchema(SubtitleSchema subtitleSchema) {
        return new Subtitle(subtitleSchema.versionNumber, getSubtitleTextFromSchema(subtitleSchema.subtitles), subtitleSchema.subFormat, subtitleSchema.videoTitleTranslated,
                subtitleSchema.videoDescriptionTranslated, subtitleSchema.videoTitleOriginal, subtitleSchema.videoDescriptionOriginal);
    }

    private List<SubtitleText> getSubtitleTextFromSchema(List<SubtitleTextSchema> subtitleTextSchema) {
        List<SubtitleText> subtitleTextList = new ArrayList<>();
        for (SubtitleTextSchema textSchema : subtitleTextSchema) {
            subtitleTextList.add(new SubtitleText(textSchema.getText(), textSchema.getPosition(),
                    textSchema.getStart(), textSchema.getEnd()));
        }
        return subtitleTextList;
    }

    private VideoTest[] getVideoTestFromSchema(VideoTestSchema[] data) {
        VideoTest[] videoTests = new VideoTest[data.length];
        for (int i = 0; i < data.length; i++) {
            videoTests[i] = new VideoTest(data[i].id, data[i].videoId, data[i].subVersion,
                    data[i].subLanguage);
        }
        return videoTests;
    }


    private String getErrorReasonListSchemaFromModel(List<ErrorReason> errorReasonList) {
        List<ErrorReasonSchema> errorReasonSchemas = new ArrayList<>();
        for (ErrorReason errorReason : errorReasonList) {
            errorReasonSchemas.add(new ErrorReasonSchema(errorReason.getReasonCode(), errorReason.getName(),
                    errorReason.getDescription(),
                    errorReason.getLanguage()));
        }
        return new Gson().toJson(errorReasonSchemas);
    }

}
