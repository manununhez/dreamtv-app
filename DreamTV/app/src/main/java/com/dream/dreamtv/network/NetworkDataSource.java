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
import com.dream.dreamtv.model.JsonResponseBaseBean;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.model.TaskResponse;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.ui.Main.MainFragment;
import com.dream.dreamtv.utils.AppExecutors;
import com.dream.dreamtv.utils.Constants;
import com.dream.dreamtv.utils.SharedPreferenceUtils;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static com.dream.dreamtv.utils.JsonUtils.getJsonResponse;

public class NetworkDataSource {
    //AllTasks
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_TYPE = "type";
    private static final String TAG = NetworkDataSource.class.getSimpleName();
    private static final String URL_BASE = "http://www.dreamproject.pjwstk.edu.pl/api/";     // Facu Produccion
    private static final int TIMEOUT_MS = 60000; //60 segundos
    public static int MAX_RETRIES = 3;
    // For Singleton instantiation
    private static NetworkDataSource INSTANCE;
    private final Context mContext;
    private final AppExecutors mExecutors;
    // Volley requestQueue
    private RequestQueue mRequestQueue;
    private MutableLiveData<Resource<TaskEntity[]>> responseFromAllTasks;
    private MutableLiveData<Resource<TaskEntity[]>> responseFromContinueTasks;
    private MutableLiveData<Resource<TaskEntity[]>> responseFromTestTasks;
    private MutableLiveData<Resource<TaskEntity[]>> responseFromFinishedTasks;
    private MutableLiveData<Resource<TaskEntity[]>> responseFromMyListTasks;
    private MutableLiveData<Resource<User>> responseFromUserUpdate;
    private int currentPage = 1;

    private NetworkDataSource(Context context, AppExecutors executors) {
        mContext = context;
        mRequestQueue = getRequestQueue();

        //TODO si se decide hacer sincrono, solo existiria una variable que agrupe los tasks y que luego insertaria en BD todo de una sola vez
        responseFromFinishedTasks = new MutableLiveData<>();
        responseFromMyListTasks = new MutableLiveData<>();
        responseFromTestTasks = new MutableLiveData<>();
        responseFromUserUpdate = new MutableLiveData<>();
        responseFromAllTasks = new MutableLiveData<>();
        responseFromContinueTasks = new MutableLiveData<>();

        mExecutors = executors;


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


    //**************************************
     /*        Volley Configuration
     //************************************/

    private <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
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

//            @Override
//            public String getBodyContentType() {
//                return "application/x-www-form-urlencoded";
//            }

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

    /**
     * Get the current weather of city by name. Used in {@link MainFragment}
     * to implement the search.
     *
     * @param {@link String} location with the name of the city.
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
                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, "login() response JSON: " + response);

                User user = jsonResponse.data;

                ((DreamTVApp) mContext.getApplicationContext()).setToken(user.token); //updating token

                userDetails();


            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
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


        mExecutors.networkIO().execute(() -> {
            requestString(Method.POST, loginUri.toString(), params, responseListener);
        });

    }

    /**
     * @param email
     * @param password
     */
    @SuppressWarnings("unchecked")
    public void register(final String email, final String password) {

        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
        Uri registerUri = Uri.parse(URL_BASE.concat(Urls.REGISTER.value)).buildUpon().build();

        ResponseListener responseListener = new ResponseListener(mContext, false, false, "") {
            @Override
            protected void processResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, "register() response JSON: " + response);

                User user = jsonResponse.data;
                ((DreamTVApp) mContext.getApplicationContext()).setToken(user.token); //updating token

                userDetails();
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                login(email, password); //we try to login again
            }
        };


        mExecutors.networkIO().execute(() -> {
            requestString(Method.POST, registerUri.toString(), params, responseListener);
        });
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public void userDetails() {
        Uri userDetailsUri = Uri.parse(URL_BASE.concat(Urls.USER_DETAILS.value)).buildUpon().build();

        ResponseListener responseListener = new ResponseListener(mContext, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, "userDetails Response JSON: " + response);

                Resource<User> resourceResponse;
                User user = jsonResponse.data;
                resourceResponse = Resource.success(user);
                responseFromUserUpdate.postValue(resourceResponse); //post the value to live data

                ((DreamTVApp) mContext.getApplicationContext()).setUser(user); //updating token

//                syncData();
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);

                Log.d(TAG, "userDetails response: " + jsonResponse.message, null);
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromUserUpdate.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "userDetails response: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> {
            requestString(Method.GET, userDetailsUri.toString(), null, responseListener);
        });
    }


    /**
     * @param user
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
                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, "userDetailsUpdate Response JSON: " + response);

                Resource<User> resourceResponse;
                User user = jsonResponse.data;
                resourceResponse = Resource.success(user);
                responseFromUserUpdate.postValue(resourceResponse); //post the value to live data

                ((DreamTVApp) mContext.getApplicationContext()).setUser(user); //updating token

//                syncData();
            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);

                Log.d(TAG, "userDetails response: " + jsonResponse.message, null);
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromUserUpdate.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "userDetails response: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> {
            requestString(Method.PUT, userUri.toString(), params, responseListener);
        });
    }

    public void syncData() {
        Log.d(TAG, "synchronizing data ...");
        //We start by calling allTasks() -> continueTasks()->MyList()->Reasons()->videoTestsDetails
        allTasks(1);
        taskByCategory(Constants.TASKS_CONTINUE, responseFromContinueTasks);
        taskByCategory(Constants.TASKS_MY_LIST, responseFromMyListTasks);
        taskByCategory(Constants.TASKS_FINISHED, responseFromFinishedTasks);


        String testingMode = ((DreamTVApp) mContext.getApplicationContext()).getTestingMode();
        if (testingMode.equals(mContext.getString(R.string.text_yes_option))) //TODO dejar esto aca? o moverlo
            taskByCategory(Constants.TASKS_TEST, responseFromTestTasks);


    }

    private void reasons() {
        //TODO completar
    }

    /**
     * @param page
     */
    @SuppressWarnings("unchecked")
    private void allTasks(int page) {

        if (page == -1) return;


        Uri tasksUri = Uri.parse(URL_BASE.concat(Urls.TASKS.value)).buildUpon()
                .appendQueryParameter(PARAM_PAGE, String.valueOf(page))
                .appendQueryParameter(PARAM_TYPE, Constants.TASKS_ALL)
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
                responseFromAllTasks.postValue(resourceResponse); //post the value to live data


                if (taskResponse.current_page < taskResponse.last_page) //Pagination
                    currentPage++;
                else
                    currentPage = -1;

                allTasks(currentPage);
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
                responseFromAllTasks.postValue(Resource.error(error.getMessage(), null));

                Log.d(TAG, "all tasks response: " + error.getMessage());
            }
        };

        mExecutors.networkIO().execute(() -> {
            requestString(Method.GET, tasksUri.toString(), null, responseListener);
        });
    }

    /**
     * @param paramType
     * @param responseMutable
     */
    @SuppressWarnings("unchecked")
    private void taskByCategory(final String paramType, final MutableLiveData<Resource<TaskEntity[]>> responseMutable) {

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

//
//    private void getReasons() {
//        ResponseListener responseListener = new ResponseListener(getActivity(), false, true, getString(R.string.title_loading_retrieve_options)) {
//
//            @Override
//            public void processResponse(String response) {
//                Gson gson = new Gson();
//                Log.d(TAG, response);
//
//                TypeToken type = new TypeToken<JsonResponseBaseBean<List<ErrorReason>>>() {
//                };
//                JsonResponseBaseBean<List<ErrorReason>> jsonResponse = getJsonResponse(response, type);
//
//                ((DreamTVApp) getActivity().getApplication()).setReasons(jsonResponse.data);
//
//                Log.d(TAG, jsonResponse.data.toString());
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
////        NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.REASONS, null, responseListener, this);
//
//    }

    //    private void getVideoTests(final Video selectedVideo) {
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true,
//                getString(R.string.title_loading_retrieve_user_tasks)) {
//
//            @Override
//            public void processResponse(String response) {
//                Log.d(TAG, response);
//
//                TypeToken type = new TypeToken<JsonResponseBaseBean<VideoTests[]>>() {
//                };
//                JsonResponseBaseBean<VideoTests[]> jsonResponse = JsonUtils.getJsonResponse(response, type);
//
//                VideoTests[] videoTests = jsonResponse.data;
//                Log.d(TAG, Arrays.toString(videoTests));
//
//                int videoTestIndex = 0;
//                for (int i = 0; i < videoTests.length; i++) {
//                    if (videoTests[i].video_id.equals(selectedVideo.video_id)) {
//                        videoTestIndex = i;
//                        break;
//                    }
//                }
//
//                getSubtitleJson(selectedVideo, videoTests[videoTestIndex].version);
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
//       // NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.VIDEO_TESTS, null, responseListener, this);
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
    public LiveData<Resource<TaskEntity[]>> responseFromTasks() {
        return responseFromAllTasks;
    }


    public LiveData<Resource<TaskEntity[]>> responseFromContinueTasks() {
        return responseFromContinueTasks;
    }

    public LiveData<Resource<TaskEntity[]>> responseFromTestTasks() {
        return responseFromTestTasks;
    }

    public LiveData<Resource<TaskEntity[]>> responseFromFinishedTasks() {
        return responseFromFinishedTasks;
    }

    public LiveData<Resource<TaskEntity[]>> responseFromMyListTasks() {
        return responseFromMyListTasks;
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

        USER("user");


        final String value;

        Urls(String value) {
            this.value = value;
        }
    }

}
