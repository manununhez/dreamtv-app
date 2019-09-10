package com.dream.dreamtv.data.networking;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.VolleyError;
import com.dream.dreamtv.data.model.Category;
import com.dream.dreamtv.data.model.VideoDuration;
import com.dream.dreamtv.data.model.api.AuthResponse;
import com.dream.dreamtv.data.model.api.ErrorReason;
import com.dream.dreamtv.data.model.api.JsonResponseBaseBean;
import com.dream.dreamtv.data.model.api.Resource;
import com.dream.dreamtv.data.model.api.SubtitleResponse;
import com.dream.dreamtv.data.model.api.Task;
import com.dream.dreamtv.data.model.api.TaskRequest;
import com.dream.dreamtv.data.model.api.TasksList;
import com.dream.dreamtv.data.model.api.User;
import com.dream.dreamtv.data.model.api.UserTask;
import com.dream.dreamtv.data.model.api.UserTaskError;
import com.dream.dreamtv.data.model.api.VideoTest;
import com.dream.dreamtv.data.model.api.VideoTopic;
import com.dream.dreamtv.ui.home.HomeFragment;
import com.dream.dreamtv.utils.AppExecutors;
import com.dream.dreamtv.utils.JsonUtils;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

import static com.android.volley.Request.Method.DELETE;
import static com.android.volley.Request.Method.GET;
import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;
import static com.dream.dreamtv.data.model.Category.Type.ALL;
import static com.dream.dreamtv.data.networking.NetworkDataSource.Urls.CATEGORIES;
import static com.dream.dreamtv.data.networking.NetworkDataSource.Urls.LOGIN;
import static com.dream.dreamtv.data.networking.NetworkDataSource.Urls.REASON_ERRORS;
import static com.dream.dreamtv.data.networking.NetworkDataSource.Urls.REGISTER;
import static com.dream.dreamtv.data.networking.NetworkDataSource.Urls.SUBTITLE;
import static com.dream.dreamtv.data.networking.NetworkDataSource.Urls.TASKS_BY_CATEGORY;
import static com.dream.dreamtv.data.networking.NetworkDataSource.Urls.TASKS_BY_KEYWORD_CATEGORIES;
import static com.dream.dreamtv.data.networking.NetworkDataSource.Urls.TASKS_SEARCH;
import static com.dream.dreamtv.data.networking.NetworkDataSource.Urls.USER;
import static com.dream.dreamtv.data.networking.NetworkDataSource.Urls.USER_DETAILS;
import static com.dream.dreamtv.data.networking.NetworkDataSource.Urls.USER_ERRORS;
import static com.dream.dreamtv.data.networking.NetworkDataSource.Urls.USER_TASKS;
import static com.dream.dreamtv.data.networking.NetworkDataSource.Urls.USER_TASK_MY_LIST;
import static com.dream.dreamtv.data.networking.NetworkDataSource.Urls.VIDEO_TESTS;
import static com.dream.dreamtv.utils.Constants.BASE_URL;
import static com.dream.dreamtv.utils.Constants.PARAM_AUDIO_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.PARAM_AUDIO_LANGUAGE_CONFIG;
import static com.dream.dreamtv.utils.Constants.PARAM_CATEGORY;
import static com.dream.dreamtv.utils.Constants.PARAM_COMPLETED;
import static com.dream.dreamtv.utils.Constants.PARAM_EMAIL;
import static com.dream.dreamtv.utils.Constants.PARAM_INTERFACE_MODE;
import static com.dream.dreamtv.utils.Constants.PARAM_LANG_CODE;
import static com.dream.dreamtv.utils.Constants.PARAM_MAX_VIDEO_DURATION;
import static com.dream.dreamtv.utils.Constants.PARAM_MIN_VIDEO_DURATION;
import static com.dream.dreamtv.utils.Constants.PARAM_PAGE;
import static com.dream.dreamtv.utils.Constants.PARAM_PASSWORD;
import static com.dream.dreamtv.utils.Constants.PARAM_QUERY;
import static com.dream.dreamtv.utils.Constants.PARAM_RATING;
import static com.dream.dreamtv.utils.Constants.PARAM_REASON_CODE;
import static com.dream.dreamtv.utils.Constants.PARAM_SUB_LANGUAGE;
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

    private static final String TAG = NetworkDataSourceImpl.class.getSimpleName();


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
    private MutableLiveData<Resource<VideoTopic[]>> responseFromCategories;
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
        //TODO si se decide hacer sincrono, solo existiria una variable que agrupe los tasks y que luego insertaria en BD todo de una sola vez
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
                Log.d(TAG, "Made new NetworkDataSourceImpl");

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
        String URL = getUri(LOGIN);

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_EMAIL, email);
        params.put(PARAM_PASSWORD, password);


        Log.d(TAG, "login() Request URL: " + URL + " Paramaters: email=>" + email
                + "; password=>" + password);

        responseFromAuth.setValue(Resource.loading(null));

        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "login() Response JSON: " + response);

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
//                Log.d(TAG, "login() Error response: " + error.getMessage());
//                if (VolleyErrorHelper.getErrorType(error, mContext).equals(mContext.getString(R.string.auth_failed)))
                register(email, password);

            }
        };


        mExecutors.networkIO().execute(() -> mVolley.requestString(POST, URL, params, responseListener));

    }

    /**
     * Register. Used in {@link HomeFragment} to implement the ...
     *
     * @param email    Email
     * @param password Password
     */
    @SuppressWarnings("unchecked")
    private void register(final String email, final String password) {
        String URL = getUri(REGISTER);

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_EMAIL, email);
        params.put(PARAM_PASSWORD, password);

        Log.d(TAG, "register() Request URL: " + URL + " Paramaters: email=>" + email
                + "; password=>" + password);

        responseFromAuth.setValue(Resource.loading(null));

        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "register() Response JSON: " + response);

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
        };


        mExecutors.networkIO().execute(() -> mVolley.requestString(POST, URL, params, responseListener));
    }

    /**
     * UserDetails. Used in {@link HomeFragment} to get user details
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fetchUserDetails() {
        String URL = getUri(USER_DETAILS);

        responseFromFetchUser.setValue(Resource.loading(null));

        Log.d(TAG, "fetchUserDetails() Request URL: " + URL);

        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "fetchUserDetails() Response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);


                User user = jsonResponse.data;

//                getApplication().setUser(user); //updating token

//                syncData();


                responseFromFetchUser.postValue(Resource.success(user)); //post the value to live data

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromFetchUser.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "fetchUserDetails() Response Error: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, URL, null, responseListener));

//        ret urn responseFromFetchUser;
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

        Uri userUri = Uri.parse(BASE_URL.concat(USER.value)).buildUpon()
                .appendQueryParameter(PARAM_INTERFACE_MODE, user.interfaceMode)
                .appendQueryParameter(PARAM_SUB_LANGUAGE, user.subLanguage)
                .appendQueryParameter(PARAM_AUDIO_LANGUAGE, user.audioLanguage)
                .build();

        Log.d(TAG, "updateUser() Request URL: " + userUri.toString() + " Params: interfaceMode=>" + user.interfaceMode
                + "; subLanguage=>" + user.subLanguage
                + "; audioLanguage=>" + user.audioLanguage);

        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "updateUser() Response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);


//                getApplication().setUser(user); //updating token


                responseFromUserUpdate.postValue(Resource.success(jsonResponse.data)); //post the value to live data

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromUserUpdate.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "updateUser() Response Error: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> mVolley.requestString(PUT, userUri.toString(), null, responseListener));

        return responseFromUserUpdate;
    }


    /**
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fetchReasons() {
        String URL = getUri(REASON_ERRORS);

        Log.d(TAG, "fetchReasons() Request URL: " + URL);

        responseFromFetchReasons.setValue(Resource.loading(null));

        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "fetchReasons() Response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<ErrorReason[]>>() {
                };
                JsonResponseBaseBean<ErrorReason[]> jsonResponse = getJsonResponse(response, type);


//                getApplication().setReasons(jsonResponse.data);

                responseFromFetchReasons.postValue(Resource.success(jsonResponse.data));


            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                responseFromFetchReasons.postValue(Resource.error(error.getMessage(), null));

            }
        };


        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, URL, null, responseListener));
    }

    /**
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<VideoTopic[]>> fetchCategories() {
        responseFromCategories.setValue(Resource.loading(null));

        Uri categoriesUri = Uri.parse(BASE_URL.concat(CATEGORIES.value)).buildUpon()
                .build();

        Log.d(TAG, "fetchCategories() Request URL: " + categoriesUri.toString());


        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "fetchCategories() Response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<VideoTopic[]>>() {
                };
                JsonResponseBaseBean<VideoTopic[]> jsonResponse = getJsonResponse(response, type);

                VideoTopic[] categories = jsonResponse.data;

                responseFromCategories.postValue(Resource.success(categories));


            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromCategories.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "fetchCategories() Response Error: " + error.getMessage());
            }
        };


        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, categoriesUri.toString(), null, responseListener));

        return responseFromCategories;
    }

    /**
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fetchVideoTestsDetails() {
        String URL = getUri(VIDEO_TESTS);

        Log.d(TAG, "fetchVideoTestsDetails() Request URL: " + URL);

        responseFromFetchVideoTests.setValue(Resource.loading(null));

        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "fetchVideoTestsDetails() Response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<VideoTest[]>>() {
                };
                JsonResponseBaseBean<VideoTest[]> jsonResponse = getJsonResponse(response, type);


//                getApplication().setVideoTests(jsonResponse.data);

                responseFromFetchVideoTests.postValue(Resource.success(jsonResponse.data));

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                responseFromFetchVideoTests.postValue(Resource.error(error.getMessage(), null));

            }
        };


        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, URL, null, responseListener));
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

        Uri subtitleUri;
//        if (version > 0) {
        subtitleUri = Uri.parse(BASE_URL.concat(SUBTITLE.value)).buildUpon()
                .appendQueryParameter(PARAM_VIDEO_ID, videoId)
                .appendQueryParameter(PARAM_LANG_CODE, languageCode)
                .appendQueryParameter(PARAM_VERSION, version)
                .build();

        Log.d(TAG, "fetchSubtitle() Request URL: " + subtitleUri.toString() + " Params: " + PARAM_VIDEO_ID + "=>" + videoId
                + "; " + PARAM_LANG_CODE + "=>" + languageCode
                + "; " + PARAM_VERSION + "=>" + version);

        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "fetchSubtitle() Response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<SubtitleResponse>>() {
                };
                JsonResponseBaseBean<SubtitleResponse> jsonResponse = JsonUtils.getJsonResponse(response, type);

                SubtitleResponse subtitleResponse = jsonResponse.data;
                responseFromFetchSubtitle.postValue(Resource.success(subtitleResponse)); //post the value to live data

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromFetchSubtitle.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "fetchSubtitle() Response Error: " + error.getMessage());
            }
        };


        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, subtitleUri.toString(), null, responseListener));

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

        Uri tasksUri = taskUrlFormatter(BASE_URL.concat(TASKS_BY_CATEGORY.value), taskRequest.getCategory(), page, taskRequest.getVideoDuration());


        Log.d(TAG, "fetchTasksByCategory() Request URL: " + tasksUri.toString() + " Params: " + PARAM_PAGE + "=>" + page
                + "; " + PARAM_TYPE + "=>" + taskRequest.getCategory());


        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<TasksList>>() {
                };
                JsonResponseBaseBean<TasksList> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, "fetchTasksByCategory() Response JSON: " + response);

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

                Log.d(TAG, "all tasks response: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, tasksUri.toString(), null, responseListener));
    }


    private Uri taskUrlFormatter(String uriString, Category.Type paramType, VideoDuration videoDuration) {

        Uri uri;

        if (videoDuration.getMinDuration() > 0 && videoDuration.getMaxDuration() > 0)
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .appendQueryParameter(PARAM_MIN_VIDEO_DURATION, String.valueOf(videoDuration.getMinDuration()))
                    .appendQueryParameter(PARAM_MAX_VIDEO_DURATION, String.valueOf(videoDuration.getMaxDuration()))
                    .build();

        else if (videoDuration.getMinDuration() > 0)
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .appendQueryParameter(PARAM_MIN_VIDEO_DURATION, String.valueOf(videoDuration.getMinDuration()))
                    .build();

        else if (videoDuration.getMaxDuration() > 0)
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .appendQueryParameter(PARAM_MAX_VIDEO_DURATION, String.valueOf(videoDuration.getMaxDuration()))
                    .build();

        else
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .build();


        return uri;
    }

    private Uri taskUrlFormatter(String uriString, Category.Type paramType, int page, VideoDuration videoDuration) {

        Uri uri;

        if (videoDuration.getMinDuration() > 0 && videoDuration.getMaxDuration() > 0)
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .appendQueryParameter(PARAM_PAGE, String.valueOf(page))
                    .appendQueryParameter(PARAM_MIN_VIDEO_DURATION, String.valueOf(videoDuration.getMinDuration()))
                    .appendQueryParameter(PARAM_MAX_VIDEO_DURATION, String.valueOf(videoDuration.getMaxDuration()))
                    .build();

        else if (videoDuration.getMinDuration() > 0)
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .appendQueryParameter(PARAM_PAGE, String.valueOf(page))
                    .appendQueryParameter(PARAM_MIN_VIDEO_DURATION, String.valueOf(videoDuration.getMinDuration()))
                    .build();

        else if (videoDuration.getMaxDuration() > 0)
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .appendQueryParameter(PARAM_PAGE, String.valueOf(page))
                    .appendQueryParameter(PARAM_MAX_VIDEO_DURATION, String.valueOf(videoDuration.getMaxDuration()))
                    .build();

        else
            uri = Uri.parse(uriString).buildUpon()
                    .appendQueryParameter(PARAM_TYPE, paramType.value)
                    .appendQueryParameter(PARAM_PAGE, String.valueOf(page))
                    .build();

        return uri;
    }

    /**
     * @param category category
     * @return responseFromSearchByKeywordCategory
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<Task[]>> searchByKeywordCategory(String category) {

        responseFromSearchByKeywordCategory.setValue(Resource.loading(null));

        Uri searchUri = Uri.parse(BASE_URL.concat(TASKS_BY_KEYWORD_CATEGORIES.value)).buildUpon()
                .appendQueryParameter(PARAM_CATEGORY, category)
                .build();

        Log.d(TAG, "searchByKeywordCategory() Request URL: " + searchUri.toString()
                + " Params: " + PARAM_CATEGORY + "=>" + category);

        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<Task[]>>() {
                };
                JsonResponseBaseBean<Task[]> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, "searchByKeywordCategory() Response JSON: " + response);

                Task[] tasks = jsonResponse.data;


                responseFromSearchByKeywordCategory.postValue(Resource.success(tasks)); //post the value to live data

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromSearchByKeywordCategory.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "searchByKeywordCategory() Response Error: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, searchUri.toString(), null, responseListener));

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

        Uri searchUri = Uri.parse(BASE_URL.concat(TASKS_SEARCH.value)).buildUpon()
                .appendQueryParameter(PARAM_QUERY, query)
                .build();

        Log.d(TAG, "search() Request URL: " + searchUri.toString() + " Params: " + PARAM_QUERY + "=>" + query);

        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<Task[]>>() {
                };
                JsonResponseBaseBean<Task[]> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, "search() Response JSON: " + response);

                Task[] tasks = jsonResponse.data;

                responseFromSearch.postValue(Resource.success(tasks)); //post the value to live data

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromSearch.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "search() Response Error: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, searchUri.toString(), null, responseListener));

        return responseFromSearch;
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

        Uri tasksUri = taskUrlFormatter(BASE_URL.concat(TASKS_BY_CATEGORY.value), taskRequest.getCategory(), taskRequest.getVideoDuration());

        Log.d(TAG, "fetchTasksByCategory() Request URL: " + tasksUri.toString() + " Params: " + PARAM_TYPE + "=>" + paramType);

        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<Task[]>>() {
                };
                JsonResponseBaseBean<Task[]> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, paramType + "fetchTasksByCategory() Response JSON: " + response);

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

                Log.d(TAG, paramType + "fetchTasksByCategory() Response Error: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> mVolley.requestString(GET, tasksUri.toString(), null, responseListener));
    }


    /**
     * @param taskId           taskId
     * @param mSubtitleVersion mSubtitleVersion
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<UserTask>> createUserTask(int taskId, int mSubtitleVersion) {
        String URL = getUri(USER_TASKS);

        responseFromCreateUserTask.postValue(Resource.loading(null));

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(taskId));
        params.put(PARAM_SUB_VERSION, String.valueOf(mSubtitleVersion));


        Log.d(TAG, "createUserTask() Request URL: " + URL + " Params: " + PARAM_TASK_ID + "=>" + taskId
                + "; " + PARAM_SUB_VERSION + " => " + mSubtitleVersion);


        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "createUserTask() Response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTask>>() {
                };
                JsonResponseBaseBean<UserTask> jsonResponse = getJsonResponse(response, type);

                UserTask userTasks = jsonResponse.data;

                responseFromCreateUserTask.postValue(Resource.success(userTasks)); //post the value to live data

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromCreateUserTask.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "createUserTask() Response Error: " + error.getMessage());
            }
        };


        mExecutors.networkIO().execute(() -> mVolley.requestString(POST, URL, params, responseListener));

        return responseFromCreateUserTask;
    }

    @Override
    public MutableLiveData<Resource<UserTask>> fetchUserTask() {
        return responseFromFetchUserTask;

    }


    /**
     * @param taskId              taskId
     * @param subLanguageConfig   subLanguageConfig
     * @param audioLanguageConfig audioLanguageConfig
     */
    @Override
    public MutableLiveData<Resource<Boolean>> addTaskToList(int taskId, String subLanguageConfig, String audioLanguageConfig) {

        String URL = getUri(USER_TASK_MY_LIST);

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(taskId));
        params.put(PARAM_SUB_LANGUAGE_CONFIG, subLanguageConfig);
        params.put(PARAM_AUDIO_LANGUAGE_CONFIG, audioLanguageConfig);

        Log.d(TAG, "addTaskToList() Request URL: " + URL + " Params: " + PARAM_TASK_ID + "=>" + taskId
                + "; " + PARAM_SUB_LANGUAGE_CONFIG + "=>" + subLanguageConfig
                + "; " + PARAM_AUDIO_LANGUAGE_CONFIG + "=>" + audioLanguageConfig);

        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {

                Log.d(TAG, "addTaskToList() Response JSON: " + response);


                responseFromAddToListTasks.postValue(Resource.success(true));


            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                responseFromAddToListTasks.postValue(Resource.error(error.getMessage(), null));
            }
        };

        mExecutors.networkIO().execute(() -> mVolley.requestString(POST, URL, params, responseListener));

        return responseFromAddToListTasks;

    }

    /**
     * @param taskId taskId
     */
    @Override
    public MutableLiveData<Resource<Boolean>> removeTaskFromList(int taskId) {
        Uri removeFromListUri = Uri.parse(BASE_URL.concat(USER_TASK_MY_LIST.value)).buildUpon()
                .appendQueryParameter(PARAM_TASK_ID, String.valueOf(taskId))
                .build();

        Log.d(TAG, "removeTaskFromList() Request URL: " + removeFromListUri.toString() + " Params: " + PARAM_TASK_ID + "=>" + taskId);

        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {

                Log.d(TAG, "removeTaskFromList() Response JSON: " + response);


                responseFromRemoveFromListTasks.postValue(Resource.success(true));


            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                responseFromRemoveFromListTasks.postValue(Resource.error(error.getMessage(), null));

            }

        };

        mExecutors.networkIO().execute(() -> mVolley.requestString(DELETE, removeFromListUri.toString(), null, responseListener));

        return responseFromRemoveFromListTasks;

    }

    private String getUri(Urls resource) {
        return String.valueOf(Uri.parse(BASE_URL.concat(resource.value)).buildUpon().build());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateUserTask(UserTask userTask) {
        String URL = getUri(USER_TASKS);

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(userTask.getTaskId()));
        params.put(PARAM_SUB_VERSION, userTask.getSubtitleVersion());
        params.put(PARAM_TIME_WATCHED, String.valueOf(userTask.getTimeWatched()));
        params.put(PARAM_COMPLETED, String.valueOf(userTask.
                getCompleted()));
        params.put(PARAM_RATING, String.valueOf(userTask.getRating()));

        Log.d(TAG, "updateUserTask() Request URL: " + URL + " Params: " + PARAM_TASK_ID + "=>" + userTask.getTaskId()
                + "; " + PARAM_SUB_VERSION + " => " + userTask.getSubtitleVersion());

        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {

                Log.d(TAG, "updateUserTask() Response JSON: " + response);


                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTask>>() {
                };
                JsonResponseBaseBean<UserTask> jsonResponse = getJsonResponse(response, type);

                UserTask userTasks = jsonResponse.data;

                responseFromFetchUserTask.postValue(Resource.success(userTasks)); //to update the userTask value in VideoDetailsFragment


            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

            }

        };

        mExecutors.networkIO().execute(() -> mVolley.requestString(PUT, URL, params, responseListener));

    }


    /**
     * @param userTaskError userTaskError
     */
    @Override
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<UserTaskError[]>> errorsUpdate(int taskId, int subtitleVersion, UserTaskError userTaskError, boolean saveError) {
        String URL = getUri(USER_ERRORS);

        responseFromErrorsUpdate.postValue(Resource.loading(null));

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(taskId));
        params.put(PARAM_REASON_CODE, userTaskError.getReasonCode());
        params.put(PARAM_SUB_POSITION, String.valueOf(userTaskError.getSubtitlePosition()));
        params.put(PARAM_SUB_VERSION, String.valueOf(subtitleVersion));


        if (saveError)
            Log.d(TAG, "saveErrors() Request URL: " + URL + " Params: " + PARAM_TASK_ID + "=>" + taskId
                    + "; " + PARAM_SUB_VERSION + " => " + subtitleVersion
                    + "; " + PARAM_SUB_POSITION + " => " + userTaskError.getSubtitlePosition()
                    + "; " + PARAM_REASON_CODE + " => " + userTaskError.getReasonCode());
        else
            Log.d(TAG, "updateErrors() Request URL: " + URL + " Params: " + PARAM_TASK_ID + "=>" + taskId
                    + "; " + PARAM_SUB_VERSION + " => " + subtitleVersion
                    + "; " + PARAM_SUB_POSITION + " => " + userTaskError.getSubtitlePosition()
                    + "; " + PARAM_REASON_CODE + " => " + userTaskError.getReasonCode());


        ResponseListener responseListener = new ResponseListener(mContext) {
            @Override
            protected void processResponse(String response) {


                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTaskError[]>>() {
                };
                JsonResponseBaseBean<UserTaskError[]> jsonResponse = getJsonResponse(response, type);

                if (saveError)
                    Log.d(TAG, "saveErrors() Response JSON: " + response);
                else
                    Log.d(TAG, "updateErrors() Response JSON: " + response);


                UserTaskError[] userTaskErrors = jsonResponse.data;

                responseFromErrorsUpdate.postValue(Resource.success(userTaskErrors));


            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                responseFromErrorsUpdate.postValue(Resource.error(error.getMessage(), null));

            }

        };

        if (saveError)
            mExecutors.networkIO().execute(() -> mVolley.requestString(POST, URL, params, responseListener));
        else //update Error
            mExecutors.networkIO().execute(() -> mVolley.requestString(PUT, URL, params, responseListener));


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
