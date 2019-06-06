package com.dream.dreamtv.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dream.dreamtv.DreamTVApp;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.ErrorReason;
import com.dream.dreamtv.model.JsonResponseBaseBean;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.model.TasksList;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.UserTaskError;
import com.dream.dreamtv.model.VideoTests;
import com.dream.dreamtv.ui.Main.MainFragment;
import com.dream.dreamtv.ui.Settings.SettingsFragment;
import com.dream.dreamtv.utils.AppExecutors;
import com.dream.dreamtv.utils.JsonUtils;
import com.dream.dreamtv.utils.SharedPreferenceUtils;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static com.dream.dreamtv.utils.Constants.PARAM_AUDIO_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.PARAM_AUDIO_LANGUAGE_CONFIG;
import static com.dream.dreamtv.utils.Constants.PARAM_COMPLETED;
import static com.dream.dreamtv.utils.Constants.PARAM_EMAIL;
import static com.dream.dreamtv.utils.Constants.PARAM_INTERFACE_LANGUAGE;
import static com.dream.dreamtv.utils.Constants.PARAM_INTERFACE_MODE;
import static com.dream.dreamtv.utils.Constants.PARAM_LANG_CODE;
import static com.dream.dreamtv.utils.Constants.PARAM_PAGE;
import static com.dream.dreamtv.utils.Constants.PARAM_PASSWORD;
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
import static com.dream.dreamtv.utils.Constants.TASKS_ALL_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_CONTINUE_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_FINISHED_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_MY_LIST_CAT;
import static com.dream.dreamtv.utils.Constants.TASKS_TEST_CAT;
import static com.dream.dreamtv.utils.JsonUtils.getJsonResponse;

public class NetworkDataSource {

    private static final String TAG = NetworkDataSource.class.getSimpleName();

    private static final String URL_BASE = "http://www.dreamproject.pjwstk.edu.pl/api/";     // Facu Produccion
    private static final int TIMEOUT_MS = 60000; //60 segundos


    // For Singleton instantiation
    private static NetworkDataSource INSTANCE;

    private final Context mContext;
    private final AppExecutors mExecutors;
    // Volley requestQueue
    private RequestQueue mRequestQueue;
    private MutableLiveData<Resource<TasksList>> responseFromFetchAllTasks;
    private MutableLiveData<Resource<TasksList>> responseFromFetchContinueTasks;
    private MutableLiveData<Resource<TasksList>> responseFromFetchTestTasks;
    private MutableLiveData<Resource<TasksList>> responseFromFetchFinishedTasks;
    private MutableLiveData<Resource<TasksList>> responseFromFetchMyListTasks;
    private MutableLiveData<Resource<Boolean>> responseFromAddToListTasks;
    private MutableLiveData<Resource<Boolean>> responseFromRemoveFromListTasks;
    private MutableLiveData<Resource<User>> responseFromUserUpdate;
    private MutableLiveData<Resource<SubtitleResponse>> responseFromFetchSubtitle;
    private MutableLiveData<Resource<UserTask>> responseFromFetchUserTask;
    private MutableLiveData<Resource<UserTask>> responseFromCreateUserTask;
    private int currentPage = 1;

    private NetworkDataSource(Context context, AppExecutors executors) {
        mContext = context;
        mRequestQueue = getRequestQueue();

        mExecutors = executors;

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

    }

    public static synchronized NetworkDataSource getInstance(Context context, AppExecutors executors) {
        if (INSTANCE == null) {
            synchronized (NetworkDataSource.class) {
                INSTANCE = new NetworkDataSource(context, executors);
                Log.d(TAG, "Made new NetworkDataSource");

            }
        }
        return INSTANCE;
    }


    private DreamTVApp getApplication() {
        return ((DreamTVApp) mContext.getApplicationContext());
    }

    //**************************************
     /*        Volley Configuration
     //************************************/

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return mRequestQueue;
    }

    private <T> void addToRequestQueue(Request<T> req, Object tag) {
        // set the default tag if tag is empty
        req.setTag(tag == null ? TAG : tag);
        getRequestQueue().add(req);
        if (req instanceof RequestQueue.RequestFinishedListener)
            getRequestQueue().addRequestFinishedListener((RequestQueue.RequestFinishedListener<Object>) req);
    }


    private void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            Log.d(TAG, "Cancel pending request for: " + tag.toString());
            mRequestQueue.cancelAll(tag);
        }
    }


    private void requestString(int method, final String webserviceUrl, final Map<String, String> params,
                               ResponseListener responseListener) {

        StringRequest stringRequest = new StringRequest(method, webserviceUrl, responseListener, responseListener) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> map = new HashMap<>();
                String string = "Bearer " + SharedPreferenceUtils.getValue(mContext, mContext.getString(R.string.dreamTVApp_token));
//                Log.d(TAG, "TOKEN: " + string);
                map.put("Authorization", string);
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                return params;
            }


        };

        stringRequest.setShouldCache(false);

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Adding String request to request queue
        INSTANCE.addToRequestQueue(stringRequest, webserviceUrl);
    }

    //*****************
    // * NETWORK CALLS
    // ****************/

    /**
     * Data synchronization
     */
    public void syncData() {
        Log.d(TAG, "synchronizing data ...");
        //We start by calling allTasks() -> continueTasks()->MyList()->Reasons()->videoTestsDetails

        updateAllTaskCategory();
        updateContinueTaskCategory();
        updateMyListTaskCategory();
        updateFinishedTaskCategory();
        updateTestTaskCategory();

        fetchReasons();

        fetchVideoTestsDetails();


    }

    public void updateAllTaskCategory() {
        fetchTasksByCategory(TASKS_ALL_CAT, responseFromFetchAllTasks, 1);
    }

    public void updateContinueTaskCategory() {
        fetchTasksByCategory(TASKS_CONTINUE_CAT, responseFromFetchContinueTasks);
    }

    public void updateMyListTaskCategory() {
        fetchTasksByCategory(TASKS_MY_LIST_CAT, responseFromFetchMyListTasks);
    }

    public void updateFinishedTaskCategory() {
        fetchTasksByCategory(TASKS_FINISHED_CAT, responseFromFetchFinishedTasks);
    }

    public void updateTestTaskCategory(){
        fetchTasksByCategory(TASKS_TEST_CAT, responseFromFetchTestTasks);
    }

    /**
     * Login. Used in {@link MainFragment} to implement the ...
     *
     * @param email    Email
     * @param password password
     */
    @SuppressWarnings("unchecked")
    public void login(final String email, final String password) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_EMAIL, email);
        params.put(PARAM_PASSWORD, password);

        Uri loginUri = Uri.parse(URL_BASE.concat(Urls.LOGIN.value)).buildUpon().build();

        Log.d(TAG, "login() Request URL: " + loginUri.toString() + " Paramaters: email=>" + email
                + "; password=>" + password);

        ResponseListener responseListener = new ResponseListener(mContext, false, false, "") {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "login() Response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);


                User user = jsonResponse.data;

                getApplication().setToken(user.token); //updating token

                fetchUserDetails();


            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
                //TODO do something error
//                Log.d(TAG, "login() Error response: " + error.getMessage());
                if (VolleyErrorHelper.getErrorType(error, mContext).equals(mContext.getString(R.string.auth_failed)))
                    register(email, password);

            }
        };


        mExecutors.networkIO().execute(() -> requestString(Method.POST, loginUri.toString(), params, responseListener));

    }

    /**
     * Register. Used in {@link MainFragment} to implement the ...
     *
     * @param email    Email
     * @param password Password
     */
    @SuppressWarnings("unchecked")
    private void register(final String email, final String password) {

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_EMAIL, email);
        params.put(PARAM_PASSWORD, password);
        Uri registerUri = Uri.parse(URL_BASE.concat(Urls.REGISTER.value)).buildUpon().build();

        Log.d(TAG, "register() Request URL: " + registerUri.toString() + " Paramaters: email=>" + email
                + "; password=>" + password);

        ResponseListener responseListener = new ResponseListener(mContext, false, false, "") {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "register() Response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);


                User user = jsonResponse.data;
                getApplication().setToken(user.token); //updating token

                fetchUserDetails();
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                login(email, password); //we try to login again
            }
        };


        mExecutors.networkIO().execute(() -> requestString(Method.POST, registerUri.toString(), params, responseListener));
    }

    /**
     * UserDetails. Used in {@link MainFragment} to get user details
     */
    @SuppressWarnings("unchecked")
    private void fetchUserDetails() {
        Uri userDetailsUri = Uri.parse(URL_BASE.concat(Urls.USER_DETAILS.value)).buildUpon().build();

        Log.d(TAG, "fetchUserDetails() Request URL: " + userDetailsUri.toString());

        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "fetchUserDetails() Response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);


                User user = jsonResponse.data;

                getApplication().setUser(user); //updating token

//                syncData();


//                responseFromUserUpdate.postValue(resourceResponse); //post the value to live data


                syncData();
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
//                responseFromUserUpdate.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "fetchUserDetails() Response Error: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> requestString(Method.GET, userDetailsUri.toString(), null, responseListener));
    }


    /**
     * Used in {@link SettingsFragment}
     *
     * @param user User
     */
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<User>> updateUser(User user) {
        responseFromUserUpdate.setValue(Resource.loading(null));

        Uri userUri = Uri.parse(URL_BASE.concat(Urls.USER.value)).buildUpon()
                .appendQueryParameter(PARAM_INTERFACE_MODE, user.interfaceMode)
                .appendQueryParameter(PARAM_INTERFACE_LANGUAGE, user.interfaceLanguage)
                .appendQueryParameter(PARAM_SUB_LANGUAGE, user.subLanguage)
                .appendQueryParameter(PARAM_AUDIO_LANGUAGE, user.audioLanguage)
                .build();

        Log.d(TAG, "updateUser() Request URL: " + userUri.toString() + " Params: interfaceMode=>" + user.interfaceMode
                + "; interfaceLanguage=>" + user.interfaceLanguage
                + "; subLanguage=>" + user.subLanguage
                + "; audioLanguage=>" + user.audioLanguage);

        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "updateUser() Response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);


                User user = jsonResponse.data;

                getApplication().setUser(user); //updating token


                responseFromUserUpdate.postValue(Resource.success(user)); //post the value to live data


//                syncData();
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromUserUpdate.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "updateUser() Response Error: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> requestString(Method.PUT, userUri.toString(), null, responseListener));

        return responseFromUserUpdate;
    }


    /**
     *
     */
    @SuppressWarnings("unchecked")
    private void fetchReasons() {
        Uri errorsUri = Uri.parse(URL_BASE.concat(Urls.REASON_ERRORS.value)).buildUpon().build();

        Log.d(TAG, "fetchReasons() Request URL: " + errorsUri.toString());


        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "fetchReasons() Response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<ErrorReason[]>>() {
                };
                JsonResponseBaseBean<ErrorReason[]> jsonResponse = getJsonResponse(response, type);


                getApplication().setReasons(jsonResponse.data);

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
            }
        };


        mExecutors.networkIO().execute(() -> requestString(Method.GET, errorsUri.toString(), null, responseListener));
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    private void fetchVideoTestsDetails() {
        Uri videoTestsUri = Uri.parse(URL_BASE.concat(Urls.VIDEO_TESTS.value)).buildUpon().build();

        Log.d(TAG, "fetchVideoTestsDetails() Request URL: " + videoTestsUri.toString());


        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "fetchVideoTestsDetails() Response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<VideoTests[]>>() {
                };
                JsonResponseBaseBean<VideoTests[]> jsonResponse = getJsonResponse(response, type);


                getApplication().setVideoTests(jsonResponse.data);

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);
            }
        };


        mExecutors.networkIO().execute(() -> requestString(Method.GET, videoTestsUri.toString(), null, responseListener));
    }


    /**
     * FetchSubtitle
     *
     * @param videoId      Video id
     * @param languageCode LanguageCode
     * @param version      version
     */
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<SubtitleResponse>> fetchSubtitle(String videoId, String languageCode, int version) {
        responseFromFetchSubtitle.postValue(Resource.loading(null));

        Uri subtitleUri;
        if (version > 0) {
            subtitleUri = Uri.parse(URL_BASE.concat(Urls.SUBTITLE.value)).buildUpon()
                    .appendQueryParameter(PARAM_VIDEO_ID, videoId)
                    .appendQueryParameter(PARAM_LANG_CODE, languageCode)
                    .appendQueryParameter(PARAM_VERSION, String.valueOf(version))
                    .build();
        } else
            subtitleUri = Uri.parse(URL_BASE.concat(Urls.SUBTITLE.value)).buildUpon()
                    .appendQueryParameter(PARAM_VIDEO_ID, videoId)
                    .appendQueryParameter(PARAM_LANG_CODE, languageCode)
                    .build();

        Log.d(TAG, "fetchSubtitle() Request URL: " + subtitleUri.toString() + " Params: " + PARAM_VIDEO_ID + "=>" + videoId
                + "; " + PARAM_LANG_CODE + "=>" + languageCode
                + "; " + PARAM_VERSION + "=>" + version);


        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
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


        mExecutors.networkIO().execute(() -> requestString(Method.GET, subtitleUri.toString(), null, responseListener));

        return responseFromFetchSubtitle;
    }


    /**
     * fetchTasksByCategory used to get All Tasks
     *
     * @param paramType       Type of category
     * @param responseMutable Reference to MutableLiveData
     * @param page            Pagination value
     */
    @SuppressWarnings("unchecked")
    private void fetchTasksByCategory(final String paramType, final MutableLiveData<Resource<TasksList>> responseMutable, int page) {

        responseMutable.setValue(Resource.loading(null));

        if (page == -1) return;


        Uri tasksUri = Uri.parse(URL_BASE.concat(Urls.TASKS_BY_CATEGORY.value)).buildUpon()
                .appendQueryParameter(PARAM_PAGE, String.valueOf(page))
                .appendQueryParameter(PARAM_TYPE, paramType)
                .build();

        Log.d(TAG, "fetchTasksByCategory() Request URL: " + tasksUri.toString() + " Params: " + PARAM_PAGE + "=>" + page
                + "; " + PARAM_TYPE + "=>" + paramType);


        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<TasksList>>() {
                };
                JsonResponseBaseBean<TasksList> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, "fetchTasksByCategory() Response JSON: " + response);

                TasksList taskResponse = jsonResponse.data;
                taskResponse.category = TASKS_ALL_CAT;

//                Task[] taskEntities = new Task[taskResponse.data.length];

//                for (int i = 0; i < taskResponse.data.length; i++) {
//                    taskEntities[i] = taskResponse.data[i].getEntity(TASKS_ALL_CAT);
//                }


                responseMutable.postValue(Resource.success(taskResponse)); //post the value to live data


                if (taskResponse.current_page < taskResponse.last_page) //Pagination
                    currentPage++;
                else
                    currentPage = -1;

//                fetchTasksByCategory(paramType, responseMutable, currentPage);
            }


            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromFetchAllTasks.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "all tasks response: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> requestString(Method.GET, tasksUri.toString(), null, responseListener));
    }

    /**
     * fetchTasksByCategory used to get Tasks by categories
     *
     * @param paramType       Type of category
     * @param responseMutable Reference to MutableLiveData
     */
    @SuppressWarnings("unchecked")
    private void fetchTasksByCategory(final String paramType, final MutableLiveData<Resource<TasksList>> responseMutable) {

        responseMutable.setValue(Resource.loading(null));

        Uri tasksUri = Uri.parse(URL_BASE.concat(Urls.TASKS_BY_CATEGORY.value)).buildUpon()
                .appendQueryParameter(PARAM_TYPE, paramType)
                .build();

        Log.d(TAG, "fetchTasksByCategory() Request URL: " + tasksUri.toString() + " Params: " + PARAM_TYPE + "=>" + paramType);

        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
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

//                if (taskResponse.length > 0) {//If the response data if not empty
//                Task[] taskEntities = new Task[taskResponse.length];
//
//                for (int i = 0; i < taskResponse.length; i++) {
//                    taskEntities[i] = taskResponse[i].getEntity(paramType);
//                }


                responseMutable.postValue(Resource.success(tasksList)); //post the value to live data
//                }

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseMutable.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, paramType + "fetchTasksByCategory() Response Error: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> requestString(Method.GET, tasksUri.toString(), null, responseListener));
    }


    /**
     * @param taskId
     * @param mSubtitleVersion
     */
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<UserTask>> createUserTask(int taskId, int mSubtitleVersion) {
        responseFromCreateUserTask.postValue(Resource.loading(null));

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(taskId));
        params.put(PARAM_SUB_VERSION, String.valueOf(mSubtitleVersion));

        Uri userTaskUri = Uri.parse(URL_BASE.concat(Urls.USER_TASKS.value)).buildUpon().build();

        Log.d(TAG, "createUserTask() Request URL: " + userTaskUri.toString() + " Params: " + PARAM_TASK_ID + "=>" + taskId
                + "; " + PARAM_SUB_VERSION + " => " + mSubtitleVersion);


        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
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


        mExecutors.networkIO().execute(() -> requestString(Method.POST, userTaskUri.toString(), params, responseListener));

        return responseFromCreateUserTask;
    }

    public MutableLiveData<Resource<UserTask>> fetchUserTask() {
        return responseFromFetchUserTask;

    }
//    /**
//     * @param taskId
//     */
//    @SuppressWarnings("unchecked")
//    public MutableLiveData<Resource<UserTask>> fetchUserTask(int taskId) {
//        responseFromFetchUserTask.postValue(Resource.loading(null));
//
//        Uri taskErrorsUri = Uri.parse(URL_BASE.concat(Urls.USER_TASKS_WITH_ERRORS.value)).buildUpon()
//                .appendQueryParameter(PARAM_TASK_ID, String.valueOf(taskId))
//                .build();
//
//        Log.d(TAG, "fetchUserTask() Request URL: " + taskErrorsUri.toString() + " Params: " + PARAM_TASK_ID + "=>" + taskId);
//
//
//        ResponseListener responseListener = new ResponseListener(mContext, false,
//                false, "") {
//            @Override
//            protected void processResponse(String response) {
//                Log.d(TAG, "fetchUserTask() Response JSON: " + response);
//
//                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTask>>() {
//                };
//                JsonResponseBaseBean<UserTask> jsonResponse = getJsonResponse(response, type);
//
//                UserTask userTasks = jsonResponse.data;
//
//                responseFromFetchUserTask.postValue(Resource.success(userTasks)); //post the value to live data
//
//            }
//
//            @Override
//            public void processError(VolleyError error) {
//                super.processError(error);
//
//                //TODO do something error
//                responseFromFetchUserTask.postValue(Resource.error(error.getMessage(), null));
//
//                Log.d(TAG, "fetchUserTask() Response Error: " + error.getMessage());
//            }
//        };
//
//
//        mExecutors.networkIO().execute(() -> requestString(Method.GET, taskErrorsUri.toString(), null, responseListener));
//
//        return responseFromFetchUserTask;
//    }

    /**
     * @param taskId
     * @param subLanguageConfig
     * @param audioLanguageConfig
     */
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<Boolean>> addTaskToList(int taskId, String subLanguageConfig, String audioLanguageConfig) {

        Uri addToListUri = Uri.parse(URL_BASE.concat(Urls.USER_TASK_MY_LIST.value)).buildUpon().build();


        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(taskId));
        params.put(PARAM_SUB_LANGUAGE_CONFIG, subLanguageConfig);
        params.put(PARAM_AUDIO_LANGUAGE_CONFIG, audioLanguageConfig);

        Log.d(TAG, "addTaskToList() Request URL: " + addToListUri.toString() + " Params: " + PARAM_TASK_ID + "=>" + taskId
                + "; " + PARAM_SUB_LANGUAGE_CONFIG + "=>" + subLanguageConfig
                + "; " + PARAM_AUDIO_LANGUAGE_CONFIG + "=>" + audioLanguageConfig);

        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
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

        mExecutors.networkIO().execute(() -> requestString(Method.POST, addToListUri.toString(), params, responseListener));

        return responseFromAddToListTasks;

    }

    /**
     * @param taskId
     */
    @SuppressWarnings("unchecked")
    public MutableLiveData<Resource<Boolean>> removeTaskFromList(int taskId) {
        Uri removeFromListUri = Uri.parse(URL_BASE.concat(Urls.USER_TASK_MY_LIST.value)).buildUpon()
                .appendQueryParameter(PARAM_TASK_ID, String.valueOf(taskId))
                .build();

        Log.d(TAG, "removeTaskFromList() Request URL: " + removeFromListUri.toString() + " Params: " + PARAM_TASK_ID + "=>" + taskId);

        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
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

        mExecutors.networkIO().execute(() -> requestString(Method.DELETE, removeFromListUri.toString(), null, responseListener));

        return responseFromRemoveFromListTasks;

    }

    public void updateUserTask(UserTask userTask) {

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(userTask.getTaskId()));
        params.put(PARAM_SUB_VERSION, userTask.getSubtitleVersion());
        params.put(PARAM_TIME_WATCHED, String.valueOf(userTask.getTimeWatched()));
        params.put(PARAM_COMPLETED, String.valueOf(userTask.getCompleted()));
        params.put(PARAM_RATING, String.valueOf(userTask.getRating()));

        Uri userTaskUri = Uri.parse(URL_BASE.concat(Urls.USER_TASKS.value)).buildUpon().build();

        Log.d(TAG, "updateUserTask() Request URL: " + userTaskUri.toString() + " Params: " + PARAM_TASK_ID + "=>" + userTask.getTaskId()
                + "; " + PARAM_SUB_VERSION + " => " + userTask.getSubtitleVersion());

        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
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

        mExecutors.networkIO().execute(() -> requestString(Method.PUT, userTaskUri.toString(), params, responseListener));

    }


    public void saveErrors(UserTaskError userTaskError) {

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TASK_ID, String.valueOf(userTaskError.getTaskId()));
        params.put(PARAM_REASON_CODE, userTaskError.getReasonCode());
        params.put(PARAM_SUB_POSITION, String.valueOf(userTaskError.getSubtitlePosition()));
        params.put(PARAM_SUB_VERSION, String.valueOf(userTaskError.getSubtitleVersion()));

        Uri errorsUri = Uri.parse(URL_BASE.concat(Urls.USER_ERRORS.value)).buildUpon().build();

        Log.d(TAG, "saveErrors() Request URL: " + errorsUri.toString() + " Params: " + PARAM_TASK_ID + "=>" + userTaskError.getTaskId()
                + "; " + PARAM_SUB_VERSION + " => " + userTaskError.getSubtitleVersion()
                + "; " + PARAM_SUB_POSITION + " => " + userTaskError.getSubtitlePosition()
                + "; " + PARAM_REASON_CODE + " => " + userTaskError.getReasonCode());


        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
            @Override
            protected void processResponse(String response) {

                Log.d(TAG, "saveErrors() Response JSON: " + response);


            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

            }

        };

        mExecutors.networkIO().execute(() -> requestString(Method.POST, errorsUri.toString(), params, responseListener));



//        UserTask userTask = new UserTask();
//        userTask.comments = voiceInput.getText().toString();
//        userTask.subtitlePosition = currentSubtitlePosition;
//        userTask.subtitleVersion = String.valueOf(subtitle.versionNumber);
//        userTask.taskId = idTask;
//        //Interface mode settings
//        User user = ((DreamTVApp) getActivity().getApplication()).getUser();
//        if (user.interfaceMode.equals(Constants.BEGINNER_INTERFACE_MODE)) { //We add the selected radio button
//            List<Integer> tempList = new ArrayList<>();
//            tempList.add(rgReasons.getCheckedRadioButtonId());
//            userTask.reasonList = tempList.toString();
//        } else //we add the selected checkbox ADVANCED
//            userTask.reasonList = selectedReasons.toString();
//
//        final String jsonRequest = JsonUtils.getJsonRequest(getActivity(), userTask);
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_saving_reasons)) {
//
//            @Override
//            public void processResponse(String response) {
//                Gson gson = new Gson();
//                Log.d(TAG, response);
//
//                dismiss();
//
//            }
//
//            @Override
//            public void processError(VolleyError error) {
//                super.processError(error);
//                Log.d(TAG, error.getMessage());
//            }
//
//            @Override
//            public void processError(JsonResponseBaseBean jsonResponse) {
//                super.processError(jsonResponse);
//                Log.d(TAG, jsonResponse.toString());
//            }
//        };
//
////        NetworkDataSource.post(getActivity(), NetworkDataSource.Urls.USER_TASKS, null, jsonRequest, responseListener, this);
//
    }




    /**
     * Get the current weather of a list of cities ID.
     *
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
     *
     */
    public LiveData<Resource<TasksList>> responseFromFetchTestTasks() {
        return responseFromFetchTestTasks;
    }

    /**
     *
     *
     */
    public LiveData<Resource<TasksList>> responseFromFetchFinishedTasks() {
        return responseFromFetchFinishedTasks;
    }

    /**
     *
     *
     */
    public LiveData<Resource<TasksList>> responseFromFetchMyListTasks() {
        return responseFromFetchMyListTasks;
    }




    /**
     *
     *
     */


    //********************
    //  Network requests
    //********************

    public enum Urls {
        LOGIN("login"),

        REGISTER("register"),

        USER_DETAILS("details"),

        TASKS_BY_CATEGORY("tasks/categories"),

        USER_TASKS("usertasks"),

        USER_TASKS_WITH_ERRORS("usertasks/details"),

        USER_ERRORS("usertask/errors"),

        USER_TASK_MY_LIST("usertask/list"),

        VIDEO_TESTS("videotests"),

        REASON_ERRORS("errors"),

        SUBTITLE("amara/subtitle"),

        USER("user");


        final String value;

        Urls(String value) {
            this.value = value;
        }
    }

}
