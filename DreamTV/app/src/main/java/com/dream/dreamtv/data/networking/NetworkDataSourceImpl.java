package com.dream.dreamtv.data.networking;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.VolleyError;
import com.dream.dreamtv.data.model.Category;
import com.dream.dreamtv.data.networking.model.AuthResponse;
import com.dream.dreamtv.data.networking.model.ErrorReason;
import com.dream.dreamtv.data.networking.model.JsonResponseBaseBean;
import com.dream.dreamtv.data.networking.model.Resource;
import com.dream.dreamtv.data.networking.model.SubtitleResponse;
import com.dream.dreamtv.data.networking.model.Task;
import com.dream.dreamtv.data.networking.model.TaskRequest;
import com.dream.dreamtv.data.networking.model.TasksList;
import com.dream.dreamtv.data.networking.model.User;
import com.dream.dreamtv.data.networking.model.UserTask;
import com.dream.dreamtv.data.networking.model.UserTaskError;
import com.dream.dreamtv.data.networking.model.VideoTest;
import com.dream.dreamtv.data.networking.model.VideoTopicSchema;
import com.dream.dreamtv.ui.home.HomeFragment;
import com.dream.dreamtv.utils.AppExecutors;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.android.volley.Request.Method.DELETE;
import static com.android.volley.Request.Method.GET;
import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;
import static com.dream.dreamtv.data.model.Category.Type.ALL;
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
import static com.dream.dreamtv.utils.Constants.PARAM_AUDIO_LANGUAGE_CONFIG;
import static com.dream.dreamtv.utils.Constants.PARAM_CATEGORY;
import static com.dream.dreamtv.utils.Constants.PARAM_COMPLETED;
import static com.dream.dreamtv.utils.Constants.PARAM_EMAIL;
import static com.dream.dreamtv.utils.Constants.PARAM_LANG_CODE;
import static com.dream.dreamtv.utils.Constants.PARAM_PAGE;
import static com.dream.dreamtv.utils.Constants.PARAM_PASSWORD;
import static com.dream.dreamtv.utils.Constants.PARAM_QUERY;
import static com.dream.dreamtv.utils.Constants.PARAM_RATING;
import static com.dream.dreamtv.utils.Constants.PARAM_REASON_CODE;
import static com.dream.dreamtv.utils.Constants.PARAM_SUB_LANGUAGE_CONFIG;
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
    private MutableLiveData<Resource<TasksList>> responseFromFetchAllTasks;
    private MutableLiveData<Resource<TasksList>> responseFromFetchContinueTasks;
    private MutableLiveData<Resource<TasksList>> responseFromFetchTestTasks;
    private MutableLiveData<Resource<TasksList>> responseFromFetchFinishedTasks;
    private MutableLiveData<Resource<TasksList>> responseFromFetchMyListTasks;
    private MutableLiveData<Resource<Task[]>> responseFromSearch;
    private MutableLiveData<Resource<Task[]>> responseFromSearchByKeywordCategory;
    private MutableLiveData<Resource<Boolean>> responseFromAddToListTasks;
    private MutableLiveData<Resource<Boolean>> responseFromRemoveFromListTasks;
    private MutableLiveData<Resource<User>> responseFromFetchUser;
    private MutableLiveData<Resource<User>> responseFromUserUpdate;
    private MutableLiveData<Resource<SubtitleResponse>> responseFromFetchSubtitle;
    private MutableLiveData<Resource<UserTask>> responseFromFetchUserTask;
    private MutableLiveData<Resource<UserTask>> responseFromCreateUserTask;
    private MutableLiveData<Resource<UserTaskError[]>> responseFromErrorsUpdate;
    private MutableLiveData<Resource<VideoTopicSchema[]>> responseFromCategories;
    private MutableLiveData<Resource<ErrorReason[]>> responseFromFetchReasons;
    private MutableLiveData<Resource<VideoTest[]>> responseFromFetchVideoTests;
    private MutableLiveData<Resource<AuthResponse>> responseFromAuth;
    private int currentPage = 1;

    private NetworkDataSourceImpl(Context context, AppExecutors executors, VolleyController volley) {
        mContext = context;
        mVolley = volley;

        mExecutors = executors;

        responseFromAuth = new MutableLiveData<>();
        responseFromFetchVideoTests = new MutableLiveData<>();
        responseFromFetchReasons = new MutableLiveData<>();
        responseFromSearchByKeywordCategory = new MutableLiveData<>();
        responseFromCategories = new MutableLiveData<>();
        responseFromSearch = new MutableLiveData<>();
        responseFromFetchUser = new MutableLiveData<>();
        responseFromUserUpdate = new MutableLiveData<>();
        responseFromFetchSubtitle = new MutableLiveData<>();
        responseFromFetchFinishedTasks = new MutableLiveData<>();
        responseFromFetchMyListTasks = new MutableLiveData<>();
        responseFromFetchTestTasks = new MutableLiveData<>();
        responseFromFetchAllTasks = new MutableLiveData<>();
        responseFromFetchContinueTasks = new MutableLiveData<>();
        responseFromFetchUserTask = new MutableLiveData<>();
        responseFromCreateUserTask = new MutableLiveData<>();
        responseFromAddToListTasks = new MutableLiveData<>();
        responseFromRemoveFromListTasks = new MutableLiveData<>();
        responseFromErrorsUpdate = new MutableLiveData<>();

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


    /******************
     ** NETWORK CALLS **
     *******************/

    public void fetchAllTaskCategory(TaskRequest taskRequest) {
        fetchTasksByCategory(taskRequest, responseFromFetchAllTasks, 1);
    }

    public void fetchContinueTaskCategory(TaskRequest taskRequest) {
        fetchTasksByCategory(taskRequest, responseFromFetchContinueTasks);
    }

    public void fetchMyListTaskCategory(TaskRequest taskRequest) {
        fetchTasksByCategory(taskRequest, responseFromFetchMyListTasks);
    }

    public void fetchFinishedTaskCategory(TaskRequest taskRequest) {
        fetchTasksByCategory(taskRequest, responseFromFetchFinishedTasks);
    }

    public void fetchTestTaskCategory(TaskRequest taskRequest) {
        fetchTasksByCategory(taskRequest, responseFromFetchTestTasks);
    }

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
    @SuppressWarnings("unchecked")
    private void register(final String email, final String password) {
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

                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);

                responseFromFetchUser.postValue(Resource.success(jsonResponse.data)); //post the value to live data

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
     * @param user User
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<User>> updateUser(User user) {
        responseFromUserUpdate.setValue(Resource.loading(null));

        String userUri = getUserURL(user.interfaceMode, user.subLanguage, user.audioLanguage);

        Timber.d("updateUser() Request URL: " + userUri + " Params: interfaceMode=>" + user.interfaceMode
                + "; subLanguage=>" + user.subLanguage
                + "; audioLanguage=>" + user.audioLanguage);


        mExecutors.networkIO().execute(() -> mVolley.requestString(PUT, userUri, null, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d("updateUser() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);

                responseFromUserUpdate.postValue(Resource.success(jsonResponse.data)); //post the value to live data

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

                TypeToken type = new TypeToken<JsonResponseBaseBean<ErrorReason[]>>() {
                };
                JsonResponseBaseBean<ErrorReason[]> jsonResponse = getJsonResponse(response, type);

                responseFromFetchReasons.postValue(Resource.success(jsonResponse.data));

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                responseFromFetchReasons.postValue(Resource.error(error.getMessage(), null));

            }
        }));
    }

    /**
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<VideoTopicSchema[]>> fetchCategories() {
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

                responseFromCategories.postValue(Resource.success(jsonResponse.data));

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

                TypeToken type = new TypeToken<JsonResponseBaseBean<VideoTest[]>>() {
                };
                JsonResponseBaseBean<VideoTest[]> jsonResponse = getJsonResponse(response, type);

                responseFromFetchVideoTests.postValue(Resource.success(jsonResponse.data));

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
     * @param videoId      Video id
     * @param languageCode LanguageCode
     * @param version      version
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<SubtitleResponse>> fetchSubtitle(String videoId, String languageCode, String version) {
        responseFromFetchSubtitle.postValue(Resource.loading(null));

        String subtitleUri = getSubtitleURL(videoId, languageCode, version);

        Timber.d("fetchSubtitle() Request URL: " + subtitleUri + " Params: " + PARAM_VIDEO_ID + "=>" + videoId
                + "; " + PARAM_LANG_CODE + "=>" + languageCode
                + "; " + PARAM_VERSION + "=>" + version);


        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, subtitleUri, null, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d("fetchSubtitle() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<SubtitleResponse>>() {
                };
                JsonResponseBaseBean<SubtitleResponse> jsonResponse = getJsonResponse(response, type);

                responseFromFetchSubtitle.postValue(Resource.success(jsonResponse.data)); //post the value to live data

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
     * fetchTasksByCategory used to get All Tasks
     *
     * @param responseMutable Reference to MutableLiveData
     * @param page            Pagination value
     */
    @SuppressWarnings("unchecked")
    private void fetchTasksByCategory(final TaskRequest taskRequest, final MutableLiveData<Resource<TasksList>> responseMutable, int page) {

        responseMutable.setValue(Resource.loading(null));

        if (page == -1) return;

        String tasksUri = getTasksURL(taskRequest.getCategory(), page, taskRequest.getVideoDuration());


        Timber.d("fetchTasksByCategory() Request URL: " + tasksUri + " Params: " + PARAM_PAGE + "=>" + page
                + "; " + PARAM_TYPE + "=>" + taskRequest.getCategory());


        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, tasksUri, null, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d("fetchTasksByCategory() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<TasksList>>() {
                };
                JsonResponseBaseBean<TasksList> jsonResponse = getJsonResponse(response, type);

                TasksList taskResponse = jsonResponse.data;
                taskResponse.category = ALL;


                responseMutable.postValue(Resource.success(taskResponse)); //post the value to live data


                if (taskResponse.current_page < taskResponse.last_page) //Pagination
                    currentPage++;
                else
                    currentPage = -1;

            }


            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromFetchAllTasks.postValue(Resource.error(error.getMessage(), null));

                Timber.d("all tasks response: %s", error.getMessage());
            }
        }));
    }


    /**
     * fetchTasksByCategory used to get Tasks by categories
     *
     * @param responseMutable Reference to MutableLiveData
     */
    @SuppressWarnings("unchecked")
    private void fetchTasksByCategory(final TaskRequest taskRequest, final MutableLiveData<Resource<TasksList>> responseMutable) {

        responseMutable.setValue(Resource.loading(null));

        Category.Type paramType = taskRequest.getCategory();

        String tasksUri = getTasksURL(taskRequest.getCategory(), taskRequest.getVideoDuration());

        Timber.d("fetchTasksByCategory() Request URL: " + tasksUri + " Params: " + PARAM_TYPE + "=>" + paramType);


        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, tasksUri, null, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d(paramType + "fetchTasksByCategory() Response JSON: " + response);


                TypeToken type = new TypeToken<JsonResponseBaseBean<Task[]>>() {
                };
                JsonResponseBaseBean<Task[]> jsonResponse = getJsonResponse(response, type);

                Task[] tasks = jsonResponse.data;

                TasksList tasksList = new TasksList();
                tasksList.data = tasks;
                tasksList.category = paramType;

                responseMutable.postValue(Resource.success(tasksList)); //post the value to live data

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseMutable.postValue(Resource.error(error.getMessage(), null));

                Timber.d(paramType + "fetchTasksByCategory() Response Error: " + error.getMessage());
            }
        }));
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

                TypeToken type = new TypeToken<JsonResponseBaseBean<Task[]>>() {
                };
                JsonResponseBaseBean<Task[]> jsonResponse = getJsonResponse(response, type);

                responseFromSearchByKeywordCategory.postValue(Resource.success(jsonResponse.data)); //post the value to live data

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

                TypeToken type = new TypeToken<JsonResponseBaseBean<Task[]>>() {
                };
                JsonResponseBaseBean<Task[]> jsonResponse = getJsonResponse(response, type);

                responseFromSearch.postValue(Resource.success(jsonResponse.data)); //post the value to live data

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

                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTask>>() {
                };
                JsonResponseBaseBean<UserTask> jsonResponse = getJsonResponse(response, type);

                responseFromCreateUserTask.postValue(Resource.success(jsonResponse.data)); //post the value to live data

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

    @Override
    public MutableLiveData<Resource<UserTask>> fetchUserTask() {
        return responseFromFetchUserTask;

    }


    /**
     * @param taskId        taskId
     * @param subLanguage   subLanguageConfig
     * @param audioLanguage audioLanguageConfig
     */
    @Override
    public MutableLiveData<Resource<Boolean>> addTaskToList(int taskId, String subLanguage, String audioLanguage) {

        String URL = addTaskToUserListURL();

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(taskId));
        params.put(PARAM_SUB_LANGUAGE_CONFIG, subLanguage);
        params.put(PARAM_AUDIO_LANGUAGE_CONFIG, audioLanguage);

        Timber.d("addTaskToList() Request URL: " + URL + " Params: " + PARAM_TASK_ID + "=>" + taskId
                + "; " + PARAM_SUB_LANGUAGE_CONFIG + "=>" + subLanguage
                + "; " + PARAM_AUDIO_LANGUAGE_CONFIG + "=>" + audioLanguage);


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


    @Override
    @SuppressWarnings("unchecked")
    public void updateUserTask(UserTask userTask) {
        String URL = userTaskURL();

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(userTask.getTaskId()));
        params.put(PARAM_SUB_VERSION, userTask.getSubtitleVersion());
        params.put(PARAM_TIME_WATCHED, String.valueOf(userTask.getTimeWatched()));
        params.put(PARAM_COMPLETED, String.valueOf(userTask.
                getCompleted()));
        params.put(PARAM_RATING, String.valueOf(userTask.getRating()));

        Timber.d("updateUserTask() Request URL: " + URL + " Params: " + PARAM_TASK_ID + "=>" + userTask.getTaskId()
                + "; " + PARAM_SUB_VERSION + " => " + userTask.getSubtitleVersion());


        mExecutors.networkIO().execute(() -> mVolley.requestString(PUT, URL, params, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {

                Timber.d("updateUserTask() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTask>>() {
                };
                JsonResponseBaseBean<UserTask> jsonResponse = getJsonResponse(response, type);

                responseFromFetchUserTask.postValue(Resource.success(jsonResponse.data)); //to update the userTask value in VideoDetailsFragment
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

            }

        }));

    }


    /**
     * @param taskId
     * @param subtitleVersion
     * @param userTaskError
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<UserTaskError[]>> updateErrorReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        String URL = userErrorsURL();

        responseFromErrorsUpdate.postValue(Resource.loading(null));

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(taskId));
        params.put(PARAM_REASON_CODE, userTaskError.getReasonCode());
        params.put(PARAM_SUB_POSITION, String.valueOf(userTaskError.getSubtitlePosition()));
        params.put(PARAM_SUB_VERSION, String.valueOf(subtitleVersion));


        Timber.d("updateErrors() Request URL: " + URL + " Params: " + PARAM_TASK_ID + "=>" + taskId
                + "; " + PARAM_SUB_VERSION + " => " + subtitleVersion
                + "; " + PARAM_SUB_POSITION + " => " + userTaskError.getSubtitlePosition()
                + "; " + PARAM_REASON_CODE + " => " + userTaskError.getReasonCode());


        mExecutors.networkIO().execute(() -> mVolley.requestString(PUT, URL, params, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {

                Timber.d("updateErrors() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTaskError[]>>() {
                };
                JsonResponseBaseBean<UserTaskError[]> jsonResponse = getJsonResponse(response, type);

                responseFromErrorsUpdate.postValue(Resource.success(jsonResponse.data));
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
     * @param taskId
     * @param subtitleVersion
     * @param userTaskError
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<UserTaskError[]>> saveErrorReasons(int taskId, int subtitleVersion, UserTaskError userTaskError) {
        String URL = userErrorsURL();

        responseFromErrorsUpdate.postValue(Resource.loading(null));

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(taskId));
        params.put(PARAM_REASON_CODE, userTaskError.getReasonCode());
        params.put(PARAM_SUB_POSITION, String.valueOf(userTaskError.getSubtitlePosition()));
        params.put(PARAM_SUB_VERSION, String.valueOf(subtitleVersion));


        Timber.d("saveErrors() Request URL: " + URL + " Params: " + PARAM_TASK_ID + "=>" + taskId
                + "; " + PARAM_SUB_VERSION + " => " + subtitleVersion
                + "; " + PARAM_SUB_POSITION + " => " + userTaskError.getSubtitlePosition()
                + "; " + PARAM_REASON_CODE + " => " + userTaskError.getReasonCode());


        mExecutors.networkIO().execute(() -> mVolley.requestString(POST, URL, params, new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Timber.d("saveErrors() Response JSON: %s", response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTaskError[]>>() {
                };
                JsonResponseBaseBean<UserTaskError[]> jsonResponse = getJsonResponse(response, type);

                responseFromErrorsUpdate.postValue(Resource.success(jsonResponse.data));
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
     * @return {@link LiveData} representing the response of the request requestCurrentWeathersByCityIDs()
     */
    public LiveData<Resource<TasksList>> responseFromFetchAllTasks() {
        return responseFromFetchAllTasks;
    }

    /**
     *
     */
    public LiveData<Resource<TasksList>> responseFromFetchContinueTasks() {
        return responseFromFetchContinueTasks;
    }

    /**
     *
     */
    public LiveData<Resource<TasksList>> responseFromFetchTestTasks() {
        return responseFromFetchTestTasks;
    }

    /**
     *
     */
    public LiveData<Resource<TasksList>> responseFromFetchFinishedTasks() {
        return responseFromFetchFinishedTasks;
    }

    /**
     *
     */
    public LiveData<Resource<TasksList>> responseFromFetchMyListTasks() {
        return responseFromFetchMyListTasks;
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

    public MutableLiveData<Resource<AuthResponse>> responseFromAuth() {
        return responseFromAuth;
    }
}
