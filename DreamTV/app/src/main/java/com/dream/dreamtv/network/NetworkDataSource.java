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
import com.dream.dreamtv.db.entity.TaskEntity;
import com.dream.dreamtv.model.ErrorReason;
import com.dream.dreamtv.model.JsonResponseBaseBean;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.SubtitleResponse;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.model.TaskResponse;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.model.UserTask;
import com.dream.dreamtv.model.VideoTests;
import com.dream.dreamtv.ui.Main.MainFragment;
import com.dream.dreamtv.ui.Settings.SettingsFragment;
import com.dream.dreamtv.utils.AppExecutors;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.JsonUtils;
import com.dream.dreamtv.utils.SharedPreferenceUtils;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static com.dream.dreamtv.utils.JsonUtils.getJsonResponse;

public class NetworkDataSource {

    private static final String TAG = NetworkDataSource.class.getSimpleName();

    private static final String PARAM_PAGE = "page";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_VIDEO_ID = "video_id";
    private static final String PARAM_LANG_CODE = "language_code";
    private static final String PARAM_VERSION = "version";
    private static final String URL_BASE = "http://www.dreamproject.pjwstk.edu.pl/api/";     // Facu Produccion
    private static final int TIMEOUT_MS = 60000; //60 segundos

    // For Singleton instantiation
    private static NetworkDataSource INSTANCE;

    private final Context mContext;
    private final AppExecutors mExecutors;
    // Volley requestQueue
    private RequestQueue mRequestQueue;
    private MutableLiveData<Resource<TaskEntity[]>> responseFromFetchAllTasks;
    private MutableLiveData<Resource<TaskEntity[]>> responseFromFetchContinueTasks;
    private MutableLiveData<Resource<TaskEntity[]>> responseFromFetchTestTasks;
    private MutableLiveData<Resource<TaskEntity[]>> responseFromFetchFinishedTasks;
    private MutableLiveData<Resource<TaskEntity[]>> responseFromFetchMyListTasks;
    private MutableLiveData<Resource<User>> responseFromUserUpdate;
    private MutableLiveData<Resource<SubtitleResponse>> responseFromFetchSubtitle;
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
                Log.d(TAG, "TOKEN: " + string);
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
        fetchTasksByCategory(Constants.TASKS_ALL, responseFromFetchAllTasks, 1);
        fetchTasksByCategory(Constants.TASKS_CONTINUE, responseFromFetchContinueTasks);
        fetchTasksByCategory(Constants.TASKS_MY_LIST, responseFromFetchMyListTasks);
        fetchTasksByCategory(Constants.TASKS_FINISHED, responseFromFetchFinishedTasks);


        String testingMode = getApplication().getTestingMode();
        if (testingMode.equals(mContext.getString(R.string.text_yes_option))) //TODO dejar esto aca? o moverlo
            fetchTasksByCategory(Constants.TASKS_TEST, responseFromFetchTestTasks);


        fetchReasons();
        fetchVideoTestsDetails();


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
        params.put("email", email);
        params.put("password", password);
        Uri loginUri = Uri.parse(URL_BASE.concat(Urls.LOGIN.value)).buildUpon().build();


        ResponseListener responseListener = new ResponseListener(mContext, false, false, "") {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "login() response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);


                User user = jsonResponse.data;

                getApplication().setToken(user.token); //updating token

                userDetails();


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
        params.put("email", email);
        params.put("password", password);
        Uri registerUri = Uri.parse(URL_BASE.concat(Urls.REGISTER.value)).buildUpon().build();

        ResponseListener responseListener = new ResponseListener(mContext, false, false, "") {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "register() response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);


                User user = jsonResponse.data;
                getApplication().setToken(user.token); //updating token

                userDetails();
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
    private void userDetails() {
        Uri userDetailsUri = Uri.parse(URL_BASE.concat(Urls.USER_DETAILS.value)).buildUpon().build();

        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "userDetails Response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);


                Resource<User> resourceResponse;
                User user = jsonResponse.data;
                resourceResponse = Resource.success(user);
                responseFromUserUpdate.postValue(resourceResponse); //post the value to live data

                getApplication().setUser(user); //updating token

//                syncData();
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromUserUpdate.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "userDetails response: " + error.getMessage());
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
    public void userDetailsUpdate(User user) {
        Map<String, String> params = new HashMap<>();
        params.put("interface_mode", user.interface_mode);
        params.put("interface_language", user.interface_language);
        params.put("sub_language", user.sub_language);
        params.put("audio_language", user.audio_language);

        Uri userUri = Uri.parse(URL_BASE.concat(Urls.USER.value)).buildUpon().build();

        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "userDetailsUpdate Response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);


                Resource<User> resourceResponse;
                User user = jsonResponse.data;
                resourceResponse = Resource.success(user);
                responseFromUserUpdate.postValue(resourceResponse); //post the value to live data

                getApplication().setUser(user); //updating token

//                syncData();
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromUserUpdate.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "userDetails response: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> requestString(Method.PUT, userUri.toString(), params, responseListener));
    }


    /**
     *
     */
    @SuppressWarnings("unchecked")
    private void fetchReasons() {
        Uri errorsUri = Uri.parse(URL_BASE.concat(Urls.REASON_ERRORS.value)).buildUpon().build();

        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "fetchReasons() response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<List<ErrorReason>>>() {
                };
                JsonResponseBaseBean<List<ErrorReason>> jsonResponse = getJsonResponse(response, type);


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

        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "fetchVideoTestsDetails() response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<List<VideoTests>>>() {
                };
                JsonResponseBaseBean<List<VideoTests>> jsonResponse = getJsonResponse(response, type);


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
     * @param videoId Video id
     * @param languageCode LanguageCode
     * @param version version
     */
    @SuppressWarnings("unchecked")
    public void fetchSubtitle(String videoId, String languageCode, int version) {

        Uri subtitleUri = Uri.parse(URL_BASE.concat(Urls.SUBTITLE.value)).buildUpon()
                .appendQueryParameter(PARAM_VIDEO_ID, videoId)
                .appendQueryParameter(PARAM_LANG_CODE, languageCode)
                .appendQueryParameter(PARAM_VERSION, String.valueOf(version))
                .build();

        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                Log.d(TAG, "fetchSubtitle() response JSON: " + response);

                TypeToken type = new TypeToken<JsonResponseBaseBean<SubtitleResponse>>() {
                };
                JsonResponseBaseBean<SubtitleResponse> jsonResponse = JsonUtils.getJsonResponse(response, type);


                Resource<SubtitleResponse> resourceResponse;
                SubtitleResponse subtitleResponse = jsonResponse.data;
                resourceResponse = Resource.success(subtitleResponse);
                responseFromFetchSubtitle.postValue(resourceResponse); //post the value to live data

            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromFetchSubtitle.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "fetchSubtitle() Error response: " + error.getMessage());
            }
        };


        mExecutors.networkIO().execute(() -> requestString(Method.GET, subtitleUri.toString(), null, responseListener));
    }



    /**
     * fetchTasksByCategory used to get All Tasks
     *
     * @param paramType       Type of category
     * @param responseMutable Reference to MutableLiveData
     * @param page            Pagination value
     */
    @SuppressWarnings("unchecked")
    private void fetchTasksByCategory(final String paramType, final MutableLiveData<Resource<TaskEntity[]>> responseMutable, int page) {

        if (page == -1) return;


        Uri tasksUri = Uri.parse(URL_BASE.concat(Urls.TASKS.value)).buildUpon()
                .appendQueryParameter(PARAM_PAGE, String.valueOf(page))
                .appendQueryParameter(PARAM_TYPE, paramType)
                .build();

        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<TaskResponse>>() {
                };
                JsonResponseBaseBean<TaskResponse> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, "allTasks() Response JSON: " + response);

                Resource<TaskEntity[]> resourceResponse;
                TaskResponse taskResponse = jsonResponse.data;

                TaskEntity[] taskEntities = new TaskEntity[taskResponse.data.length];

                for (int i = 0; i < taskResponse.data.length; i++) {
                    taskEntities[i] = taskResponse.data[i].getEntity(Constants.TASKS_ALL);
                }


                resourceResponse = Resource.success(taskEntities);
                responseMutable.postValue(resourceResponse); //post the value to live data


                if (taskResponse.current_page < taskResponse.last_page) //Pagination
                    currentPage++;
                else
                    currentPage = -1;

                fetchTasksByCategory(paramType, responseMutable, currentPage);
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);

                Log.d(TAG, "all tasks response: " + jsonResponse.message, null);
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromFetchAllTasks.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "all tasks response: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> {
            requestString(Method.GET, tasksUri.toString(), null, responseListener);
        });
    }

    /**
     * fetchTasksByCategory used to get Tasks by categories
     *
     * @param paramType       Type of category
     * @param responseMutable Reference to MutableLiveData
     */
    @SuppressWarnings("unchecked")
    private void fetchTasksByCategory(final String paramType, final MutableLiveData<Resource<TaskEntity[]>> responseMutable) {

        Uri tasksUri = Uri.parse(URL_BASE.concat(Urls.TASKS.value)).buildUpon()
                .appendQueryParameter(PARAM_TYPE, paramType)
                .build();

        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<Task[]>>() {
                };
                JsonResponseBaseBean<Task[]> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, paramType + "Tasks() Response JSON: " + response);

                Resource<TaskEntity[]> resourceResponse;
                Task[] taskResponse = jsonResponse.data;

                if (taskResponse.length > 0) {//If the response data if not empty
                    TaskEntity[] taskEntities = new TaskEntity[taskResponse.length];

                    for (int i = 0; i < taskResponse.length; i++) {
                        taskEntities[i] = taskResponse[i].getEntity(paramType);
                    }


                    resourceResponse = Resource.success(taskEntities);
                    responseMutable.postValue(resourceResponse); //post the value to live data
                }

            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);

                Log.d(TAG, paramType + "Tasks() Response JSON Error: " + jsonResponse.message, null);
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseMutable.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, paramType + "Tasks() Response Volley Error: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> {
            requestString(Method.GET, tasksUri.toString(), null, responseListener);
        });
    }


    private void fetchTaskErrorDetails(){
        //TODO completar
    }

    public void addTaskToList(int task_id, String sub_language_config, String audio_language_config){
        //TODO completar
    }

    public void removeTaskFromList(int taskId){
        //TODO change delete service by taskId
        //TODO completar
    }
//    private void getMyTaskForThisVideo() {
//
//        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put(PARAM_TASK_ID, String.valueOf(userData.mSelectedTask.task_id));
//        urlParams.put(PARAM_TYPE, Constants.TASKS_USER);
//
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true, getString(R.string.title_loading_retrieve_tasks)) {
//
//            @Override
//            public void processResponse(String response) {
////                Gson gson = new Gson();
//                Log.d(TAG, "Tasks -> Mine: " + response);
//                TypeToken type = new TypeToken<JsonResponseBaseBean<UserTask[]>>() {
//                };
//                JsonResponseBaseBean<UserTask[]> jsonResponse = JsonUtils.getJsonResponse(response, type);
//                userData.userTaskList = jsonResponse.data;
//
//                goToPlayVideo();
//
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
////        NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.USER_TASKS, urlParams, responseListener, this);
//
//    }
    /**
     * Get the current weather of a list of cities ID.
     *
     * @return {@link LiveData} representing the response of the request requestCurrentWeathersByCityIDs()
     */
    public LiveData<Resource<User>> responseFromUserUpdate() {
        return responseFromUserUpdate;
    }


    /**
     * Get the current weather of a list of cities ID.
     *
     * @return {@link LiveData} representing the response of the request requestCurrentWeathersByCityIDs()
     */
    public LiveData<Resource<TaskEntity[]>> responseFromFetchAllTasks() {
        return responseFromFetchAllTasks;
    }

    /**
     *
     */
    public LiveData<Resource<TaskEntity[]>> responseFromFetchContinueTasks() {
        return responseFromFetchContinueTasks;
    }

    /**
     *
     *
     */
    public LiveData<Resource<TaskEntity[]>> responseFromFetchTestTasks() {
        return responseFromFetchTestTasks;
    }

    /**
     *
     *
     */
    public LiveData<Resource<TaskEntity[]>> responseFromFetchFinishedTasks() {
        return responseFromFetchFinishedTasks;
    }

    /**
     *
     *
     */
    public LiveData<Resource<TaskEntity[]>> responseFromFetchMyListTasks() {
        return responseFromFetchMyListTasks;
    }


    /**
     *
     *
     */
    public LiveData<Resource<SubtitleResponse>> responseFromFetchSubtitle() {
        return responseFromFetchSubtitle;
    }


    //********************
    //  Network requests
    //********************

    public enum Urls {
        LOGIN("login"),

        REGISTER("register"),

        USER_DETAILS("details"),

        TASKS("tasks"),

        VIDEO_TESTS("resource/videotests"),

        REASON_ERRORS("resource/errors"),

        SUBTITLE("amara/subtitle"),

        USER("user");


        final String value;

        Urls(String value) {
            this.value = value;
        }
    }

}
