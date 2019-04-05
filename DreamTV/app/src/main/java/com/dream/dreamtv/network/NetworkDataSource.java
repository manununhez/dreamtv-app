package com.dream.dreamtv.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
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
import com.dream.dreamtv.db.entity.UserEntity;
import com.dream.dreamtv.model.JsonResponseBaseBean;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.model.TaskResponse;
import com.dream.dreamtv.model.User;
import com.dream.dreamtv.ui.Main.MainFragment;
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
    private final Context context;
    // Volley requestQueue
    private RequestQueue mRequestQueue;
    private MutableLiveData<Resource<UserEntity>> responseFromUserUpdate;
    private MutableLiveData<Resource<TaskEntity[]>> responseFromTasks;
    private MutableLiveData<Resource<TaskEntity[]>> responseFromContinueTasks;
    private MutableLiveData<Resource<String>> responseFromSyncData;
    private int currentPage = 1;

    private NetworkDataSource(Context context) {
        this.context = context.getApplicationContext();
        mRequestQueue = getRequestQueue();

        responseFromUserUpdate = new MutableLiveData<>();
        responseFromTasks = new MutableLiveData<>();
        responseFromContinueTasks = new MutableLiveData<>();
        responseFromSyncData = new MutableLiveData<>();

    }

    public static synchronized NetworkDataSource getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (NetworkDataSource.class) {
                INSTANCE = new NetworkDataSource(context);
                Log.d(TAG, "Made new NetworkDataSource");

            }
        }
        return INSTANCE;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(context);
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

//    /**
//     *
//     * @param context
//     * @param url
//     * @param urlParams
//     * @param listener
//     * @param tag
//     * @return
//     */
//    public static MySingletonVolley get(
//            final Context context,
//            @NonNull Urls url, final Map<String, String> urlParams,
//            ResponseListener listener, Object tag) {
//
//        StringBuilder urlParamsString = new StringBuilder();
//        if (urlParams != null && urlParams.entrySet().size() >= 1) {
//            int i = 0;
//            for (Map.Entry<String, String> entry : urlParams.entrySet()) {
//
//                String key = entry.getKey();
//                String value = entry.getValue();
//
//                if (i == 0)
//                    urlParamsString = new StringBuilder("?" + key + "=" + value);
//                else
//                    urlParamsString.append("&").append(key).append("=").append(value);
//
//                i++;
//
//            }
//        }
//
////        String URL_BASE = ((DreamTVApp) ((Activity)context).getApplication()).getBaseURL();
//        String uri = URL_BASE + url.value + "/" + urlParamsString;
//
//        Log.d(TAG,"Url: " + uri);
//
//        MySingletonVolley volley = MySingletonVolley.getInstance(context);
//
//        StringRequest request = new StringRequest(Method.GET, uri, listener, listener) {
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }
//
//
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> map = new HashMap<>();
////                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                String string = SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                Log.d(TAG,"TOKEN: " + string);
//                map.put("Authorization", string);
//                return map;
//            }
//
//
//        };
//
//        request.setShouldCache(false);
//
//        request.setRetryPolicy(new DefaultRetryPolicy(
//                TIMEOUT_MS,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//        ));
//
//        if (tag != null)
//            volley.addToRequestQueue(request, tag);
//        else
//            volley.addToRequestQueue(request, context);
//
//        return volley;
//    }

//
//    /**
//     *
//     * @param context
//     * @param url
//     * @param action
//     * @param json
//     * @param listener
//     * @param tag
//     * @return
//     */
//    public static MySingletonVolley post(
//            final Context context,
//            @NonNull Urls url, Actions action,
//            final String json, ResponseListener listener, Object tag) {
//
//        String uri = URL_BASE + url.value;
//
//        if (action != null)
//            uri += "/" + action.value;
//
//        Log.d(TAG,"Url: " + uri);
//
//        MySingletonVolley volley = MySingletonVolley.getInstance(context);
//
//        StringRequest request = new StringRequest(Method.POST, uri, listener, listener) {
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }
//
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                return super.getParams();
//            }
//
//
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> map = new HashMap<>();
////                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                String string = SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                Log.d(TAG,"TOKEN: " + string);
//                map.put("Authorization", string);
//                return map;
//            }
//        };
//
//        request.setShouldCache(false);
//
//        request.setRetryPolicy(new DefaultRetryPolicy(
//                TIMEOUT_MS,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//        ));
//
//        if (tag != null)
//            volley.addToRequestQueue(request, tag);
//        else
//            volley.addToRequestQueue(request, context);
//
//        return volley;
//    }

//
//    /**
//     *
//     * @param context
//     * @param url
//     * @param action
//     * @param json
//     * @param listener
//     * @param tag
//     * @return
//     */
//    public static MySingletonVolley put(
//            final Context context,
//            @NonNull Urls url, Actions action,
//            final String json, ResponseListener listener, Object tag) {
//
////        String URL_BASE = ((DreamTVApp) ((Activity)context).getApplication()).getBaseURL();
//        String uri = URL_BASE + url.value;
//
//        if (action != null)
//            uri += "/" + action.value;
//
//        Log.d(TAG,"Url: " + uri);
//
//        MySingletonVolley volley = MySingletonVolley.getInstance(context);
//
//        StringRequest request = new StringRequest(Method.PUT, uri, listener, listener) {
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }
//
//            @Override
//            public byte[] getBody() {
//                return json.getBytes();
//            }
//
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> map = new HashMap<>();
////                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                String string = SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                Log.d(TAG,"TOKEN: " + string);
//                map.put("Authorization", string);
//                return map;
//            }
//        };
//
//        request.setShouldCache(false);
//
//        request.setRetryPolicy(new DefaultRetryPolicy(
//                TIMEOUT_MS,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//        ));
//
//        if (tag != null)
//            volley.addToRequestQueue(request, tag);
//        else
//            volley.addToRequestQueue(request, context);
//
//        return volley;
//    }
//
//    /**
//     *
//     * @param context
//     * @param url
//     * @param urlParams
//     * @param listener
//     * @param tag
//     * @return
//     */
//    public static MySingletonVolley delete(
//            final Context context,
//            @NonNull Urls url, final Map<String, String> urlParams,
//            ResponseListener listener, Object tag) {
//
//        StringBuilder urlParamsString = new StringBuilder();
//        if (urlParams != null && urlParams.entrySet().size() >= 1) {
//            int i = 0;
//            for (Map.Entry<String, String> entry : urlParams.entrySet()) {
//
//                String key = entry.getKey();
//                String value = entry.getValue();
//
//                if (i == 0)
//                    urlParamsString = new StringBuilder("?" + key + "=" + value);
//                else
//                    urlParamsString.append("&").append(key).append("=").append(value);
//
//                i++;
//
//            }
//        }
//
////        String URL_BASE = ((DreamTVApp) ((Activity)context).getApplication()).getBaseURL();
//        String uri = URL_BASE + url.value + "/" + urlParamsString;
//
//        Log.d(TAG,"Url: " + uri);
//
//        MySingletonVolley volley = MySingletonVolley.getInstance(context);
//
//        StringRequest request = new StringRequest(Method.DELETE, uri, listener, listener) {
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }
//
//
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> map = new HashMap<>();
////                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                String string = SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
//                Log.d(TAG,"TOKEN: " + string);
//                map.put("Authorization", string);
//                return map;
//            }
//
//
//        };
//
//        request.setShouldCache(false);
//
//        request.setRetryPolicy(new DefaultRetryPolicy(
//                TIMEOUT_MS,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//        ));
//
//        if (tag != null)
//            volley.addToRequestQueue(request, tag);
//        else
//            volley.addToRequestQueue(request, context);
//
//        return volley;
//    }


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
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                String string = "Bearer " + SharedPreferenceUtils.getValue(context, context.getString(R.string.dreamTVApp_token));
                Log.d(TAG, "TOKEN: " + string);
                map.put("Authorization", string);
                return map;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
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


        ResponseListener responseListener = new ResponseListener(context, false, false, "") {
            @Override
            protected void processResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, "login() response JSON: " + response);

                User user = jsonResponse.data;

                ((DreamTVApp) context.getApplicationContext()).setToken(user.token); //updating token

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
                if (VolleyErrorHelper.getErrorType(error, context).equals(context.getString(R.string.auth_failed)))
                    register(email, password);

            }
        };

        requestString(Method.POST, loginUri.toString(), params, responseListener);

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

        ResponseListener responseListener = new ResponseListener(context, false, false, "") {
            @Override
            protected void processResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, "register() response JSON: " + response);

                User user = jsonResponse.data;
                ((DreamTVApp) context.getApplicationContext()).setToken(user.token); //updating token

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

        requestString(Method.POST, registerUri.toString(), params, responseListener);

    }


    public void userDetails() {
        Uri userDetailsUri = Uri.parse(URL_BASE.concat(Urls.USER_DETAILS.value)).buildUpon().build();

        ResponseListener responseListener = new ResponseListener(context, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<User>>() {
                };
                JsonResponseBaseBean<User> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, "userDetails Response JSON: " + response);

                Resource<UserEntity> resourceResponse = null;
                User user = jsonResponse.data;
                resourceResponse = Resource.success(user.getEntity());
                responseFromUserUpdate.postValue(resourceResponse); //post the value to live data

                syncData();
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
                responseFromUserUpdate.postValue(Resource.<UserEntity>error(error.getMessage(), null));

                Log.d(TAG, "userDetails response: " + error.getMessage());
            }
        };

        requestString(Method.GET, userDetailsUri.toString(), null, responseListener);
    }

    public void syncData() {
        Log.d(TAG, "synchronizing data ...");
        allTasks(1);
    }

    private void allTasks(int page) {

        if (page == -1) {
            continueTasks();
            return;
        } //other tasks


        Uri tasksUri = Uri.parse(URL_BASE.concat(Urls.TASKS.value)).buildUpon()
                .appendQueryParameter(PARAM_PAGE, String.valueOf(page))
                .appendQueryParameter(PARAM_TYPE, Constants.TASKS_ALL)
                .build();

        ResponseListener responseListener = new ResponseListener(context, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<TaskResponse>>() {
                };
                JsonResponseBaseBean<TaskResponse> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, "allTasks() Response JSON: " + response);

                Resource<TaskEntity[]> resourceResponse = null;
                TaskResponse taskResponse = jsonResponse.data;

                TaskEntity[] taskEntities = new TaskEntity[taskResponse.data.length];

                for (int i = 0; i < taskResponse.data.length; i++) {
                    taskEntities[i] = taskResponse.data[i].getEntity(Constants.TASKS_ALL);
                }


                resourceResponse = Resource.success(taskEntities);
                responseFromTasks.postValue(resourceResponse); //post the value to live data


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
                responseFromTasks.postValue(Resource.<TaskEntity[]>error(error.getMessage(), null));

                Log.d(TAG, "all tasks response: " + error.getMessage());
            }
        };

        requestString(Method.GET, tasksUri.toString(), null, responseListener);
    }

    private void continueTasks() {

//        responseFromContinueTasks.postValue(Resource.loading(new TaskEntity[0]));

        Uri tasksUri = Uri.parse(URL_BASE.concat(Urls.TASKS.value)).buildUpon()
                .appendQueryParameter(PARAM_TYPE, Constants.TASKS_CONTINUE)
                .build();

        ResponseListener responseListener = new ResponseListener(context, false,
                false, "") {
            @Override
            protected void processResponse(String response) {
                TypeToken type = new TypeToken<JsonResponseBaseBean<Task[]>>() {
                };
                JsonResponseBaseBean<Task[]> jsonResponse = getJsonResponse(response, type);

                Log.d(TAG, "continueTasks() Response JSON: " + response);

                Resource<TaskEntity[]> resourceResponse = null;
                Task[] taskResponse = jsonResponse.data;

//                if (taskResponse != null) {
                    TaskEntity[] taskEntities = new TaskEntity[taskResponse.length];

                    for (int i = 0; i < taskResponse.length; i++) {
                        taskEntities[i] = taskResponse[i].getEntity(Constants.TASKS_CONTINUE);
                    }


                    resourceResponse = Resource.success(taskEntities);
                    responseFromContinueTasks.postValue(resourceResponse); //post the value to live data
//                }

                responseFromSyncData.setValue(Resource.success("Completed"));

            }

            @Override
            public void processError(JsonResponseBaseBean jsonResponse) {
                super.processError(jsonResponse);

                Log.d(TAG, "continue tasks response: " + jsonResponse.message, null);
            }

            @Override
            public void processError(VolleyError error) {
                super.processError(error);

                //TODO do something error
                responseFromContinueTasks.postValue(Resource.<TaskEntity[]>error(error.getMessage(), null));

                Log.d(TAG, "continue tasks response: " + error.getMessage());
            }
        };

        requestString(Method.GET, tasksUri.toString(), null, responseListener);
    }


//    private void getAllTasks() {
//        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put(PARAM_PAGE, FIRST_PAGE);
//
//        //testing mode
//        String mode = ((DreamTVApp) getActivity().getApplication()).getTestingMode();
//        if (mode == null || mode.equals(getString(R.string.text_no_option)))
//            urlParams.put(PARAM_TYPE, Constants.TASKS_ALL);
//        else if (mode.equals(getString(R.string.text_yes_option)))
//            urlParams.put(PARAM_TYPE, Constants.TASKS_TEST);
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true,
//                getString(R.string.title_loading_retrieve_user_tasks)) {
//
//            @Override
//            public void processResponse(String response) {
//                Gson gson = new Gson();
//                Log.d(TAG, response);
//
//                TypeToken type = new TypeToken<JsonResponseBaseBean<TaskResponse>>() {
//                };
//                JsonResponseBaseBean<TaskResponse> jsonResponse = getJsonResponse(response, type);
//                TaskResponse taskResponse = jsonResponse.data;
//
//                Log.d(TAG, taskResponse.toString());
//
//                if (taskResponse.data.size() > 0)
//                    loadVideos(taskResponse, Constants.CHECK_NEW_TASKS_CATEGORY);
//
//                getUserToContinueTasks(FIRST_PAGE);
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
//
////        NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.TASKS, urlParams, responseListener, this);
//
//    }

    //
//
//    private void getUserToContinueTasks(String pagina) {
//        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put(PARAM_PAGE, pagina);
//        urlParams.put(PARAM_TYPE, Constants.TASKS_CONTINUE);
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true,
//                getString(R.string.title_loading_retrieve_user_tasks)) {
//
//            @Override
//            public void processResponse(String response) {
//                Gson gson = new Gson();
//                Log.d(TAG, response);
//                TypeToken type = new TypeToken<JsonResponseBaseBean<TaskResponse>>() {
//                };
//                JsonResponseBaseBean<TaskResponse> jsonResponse = getJsonResponse(response, type);
//                TaskResponse taskResponse = jsonResponse.data;
//
//                Log.d(TAG, taskResponse.toString());
//
//                if (taskResponse.data.size() > 0)
//                    loadVideos(taskResponse, Constants.CONTINUE_WATCHING_CATEGORY);
//
//                getUserVideosList(FIRST_PAGE);
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
////        NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.TASKS, urlParams, responseListener, this);
//
//    }
//
//    private void getUserVideosList(String pagina) {
//        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put(PARAM_PAGE, pagina);
//
//        ResponseListener responseListener = new ResponseListener(getActivity(), true, true,
//                getString(R.string.title_loading_retrieve_user_tasks)) {
//
//            @Override
//            public void processResponse(String response) {
//                Gson gson = new Gson();
//                Log.d(TAG, response);
//
//                TypeToken type = new TypeToken<JsonResponseBaseBean<TaskResponse>>() {
//                };
//                JsonResponseBaseBean<TaskResponse> jsonResponse = getJsonResponse(response, type);
//                TaskResponse taskResponse = jsonResponse.data;
//
//                Log.d(TAG, taskResponse.toString());
//
//                if (taskResponse.data.size() > 0)
//                    loadVideos(taskResponse, Constants.MY_LIST_CATEGORY);
//
//                setFootersOptions();
//
//
//                getReasons();
//
//            }
//
//            @Override
//            public void processError(VolleyError error) {
//                super.processError(error);
//                Log.d(TAG, error.getMessage());
//                setFootersOptions(); //the settings section is displayed anyway
//            }
//
//            @Override
//            public void processError(JsonResponseBaseBean jsonResponse) {
//                super.processError(jsonResponse);
//                Log.d(TAG, jsonResponse.toString());
//                setFootersOptions(); //the settings section is displayed anyway
//            }
//        };
//
////        NetworkDataSource.get(getActivity(), NetworkDataSource.Urls.USER_VIDEOS, urlParams, responseListener, this);
//
//    }
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

    /**
     * Get the current weather of a list of cities ID.
     *
     * @return {@link LiveData} representing the response of the request requestCurrentWeathersByCityIDs()
     */
    public LiveData<Resource<UserEntity>> responseFromUserUpdate() {
        return responseFromUserUpdate;
    }


    /**
     * Get the current weather of a list of cities ID.
     *
     * @return {@link LiveData} representing the response of the request requestCurrentWeathersByCityIDs()
     */
    public LiveData<Resource<TaskEntity[]>> responseFromTasks() {
        return responseFromTasks;
    }


    public LiveData<Resource<TaskEntity[]>> responseFromContinueTasks() {
        return responseFromContinueTasks;
    }

    public LiveData<Resource<String>> responseFromSyncData() {
        return responseFromSyncData;
    }


    //********************
    //  Network requests
    //********************

    public enum Urls {
        LOGIN("login"),

        REGISTER("register"),

        USER_DETAILS("details"),

        USER("user"),

        REASONS("reasons"),

        USER_VIDEOS("users/videos"),
        USER_VIDEO_DETAILS("users/video"),

        USER_TASKS("users/task"),

        TASKS("task/categories"),

        LANGUAGES("languages"),

        SUBTITLE("subtitle"),

        VIDEO_TESTS("videotests");


        final String value;

        Urls(String value) {
            this.value = value;
        }
    }


//    public enum Actions {
//        CREATE("create"),
//        UPDATE("update"),
//        DELETE("delete");
//
//        final String value;
//
//        Actions(String value) {
//            this.value = value;
//        }
//    }


}
