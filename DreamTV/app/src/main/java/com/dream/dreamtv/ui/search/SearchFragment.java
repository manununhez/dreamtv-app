package com.dream.dreamtv.ui.search;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.leanback.app.SearchSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.ObjectAdapter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.dream.dreamtv.R;
import com.dream.dreamtv.ViewModelFactory;
import com.dream.dreamtv.data.model.Card;
import com.dream.dreamtv.data.networking.model.Resource;
import com.dream.dreamtv.data.networking.model.Resource.Status;
import com.dream.dreamtv.data.networking.model.Task;
import com.dream.dreamtv.di.InjectorUtils;
import com.dream.dreamtv.presenter.CardPresenterSelector;
import com.dream.dreamtv.ui.videoDetails.VideoDetailsActivity;
import com.dream.dreamtv.utils.LoadingDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_QUERY;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_TASK_CATEGORY_SELECTED;
import static com.dream.dreamtv.utils.Constants.FIREBASE_KEY_TASK_SELECTED;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_SEARCH;
import static com.dream.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_TASK_SELECTED;
import static com.dream.dreamtv.utils.Constants.INTENT_CATEGORY;
import static com.dream.dreamtv.utils.Constants.INTENT_TASK;
import static com.dream.dreamtv.utils.Constants.STATUS_ERROR;

/*
 * This class demonstrates how to do in-app search
 */
public class SearchFragment extends SearchSupportFragment
        implements SearchSupportFragment.SearchResultProvider {
    private static final boolean FINISH_ON_RECOGNIZER_CANCELED = true;
    private static final int REQUEST_SPEECH = 0x00000010;
    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private String mQuery;
    private boolean mResultsFound = false;
    private SearchViewModel mViewModel;
    private LoadingDialog loadingDialog;
    private FirebaseAnalytics mFirebaseAnalytics;
    private LiveData<Resource<Task[]>> searchLiveData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        ViewModelFactory factory = InjectorUtils.provideViewModelFactory(Objects.requireNonNull(getActivity()));
        mViewModel = ViewModelProviders.of(this, factory).get(SearchViewModel.class);

        setSearchResultProvider(this);
        instantiateLoading();

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        setOnItemViewClickedListener(new ItemViewClickedListener());
        Timber.d("User is initiating a search. Do we have RECORD_AUDIO permission? %s", hasPermission(Manifest.permission.RECORD_AUDIO));

        if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
            Timber.d("Does not have RECORD_AUDIO, using SpeechRecognitionCallback");
            // SpeechRecognitionCallback is not required and if not provided recognition will be
            // handled using internal speech recognizer, in which case you must have RECORD_AUDIO
            // permission
//            startRecognition();
        } else {
            Timber.d("We DO have RECORD_AUDIO");
        }
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SPEECH) {
            if (resultCode == Activity.RESULT_OK) {
                setSearchQuery(data, true);
            } else {// If recognizer is canceled or failed, keep focus on the search orb
                if (FINISH_ON_RECOGNIZER_CANCELED) {
                    if (!hasResults()) {
                        Timber.v("Voice search canceled");
                        getView().findViewById(R.id.lb_search_bar_speech_orb).requestFocus();
                    }
                }
            }
        }
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return mRowsAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        Timber.i("Search text changed: %s", newQuery);
        loadQuery(newQuery);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Timber.i("Search text submitted: %s", query);
        loadQuery(query);
        return true;
    }

    public boolean hasResults() {
        return mRowsAdapter.size() > 0 && mResultsFound;
    }

    private boolean hasPermission(final String permission) {
        final Context context = getActivity();
        return PackageManager.PERMISSION_GRANTED == context.getPackageManager().checkPermission(
                permission, context.getPackageName());
    }

    private void loadQuery(String query) {
        if (!TextUtils.isEmpty(query) && !query.equals("nil")) {
            mQuery = query;
            search(query);
        }
    }

    private void instantiateLoading() {
        loadingDialog = new LoadingDialog(getActivity(), getString(R.string.title_loading_retrieve_tasks));
        loadingDialog.setCanceledOnTouchOutside(false);
    }

    private void dismissLoading() {
        loadingDialog.dismiss();
    }

    private void showLoading() {
        if (!Objects.requireNonNull(getActivity()).isFinishing())
            loadingDialog.show();
    }

    private void search(String query) {
        searchLiveData = mViewModel.search(query);
        searchLiveData.removeObservers(getViewLifecycleOwner());
        searchLiveData.observe(getViewLifecycleOwner(), tasksListResource -> {
            Status status = tasksListResource.status;
            Task[] data = tasksListResource.data;
            String message = tasksListResource.message;

            if (status.equals(Status.LOADING))
                showLoading();
            else if (status.equals(Status.SUCCESS)) {
                loadVideos(data);

                firebaseLoginEvents(query);

                Timber.d("task response");
                dismissLoading();
            } else if (status.equals(Status.ERROR)) {
                Timber.d(message != null ? message : STATUS_ERROR);

                dismissLoading();
            }

        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (searchLiveData != null)
            searchLiveData.removeObservers(getViewLifecycleOwner());
    }

    private void loadVideos(Task[] results) {
        int titleRes;
        if (results != null && results.length > 0) {
            mResultsFound = true;
            titleRes = R.string.search_results;
        } else {
            mResultsFound = false;
            titleRes = R.string.no_search_results;
        }

        HeaderItem header = new HeaderItem(getString(titleRes, mQuery));
        ArrayObjectAdapter adapter = new ArrayObjectAdapter(new CardPresenterSelector(getActivity()));
        List<Card> cards = new ArrayList<>();

        if (results != null) {
            for (Task task : results) {
                cards.add(new Card(task, Card.Type.SIDE_INFO));
            }
        }

        adapter.addAll(0, cards);
        mRowsAdapter.clear();
        ListRow row = new ListRow(header, adapter);
        mRowsAdapter.add(row);
    }

    public void focusOnSearch() {
        getView().findViewById(R.id.lb_search_bar).requestFocus();
    }

    private void firebaseLoginEvents(String category, int taskId) {
        Bundle bundle = new Bundle();

        bundle.putString(FIREBASE_KEY_TASK_CATEGORY_SELECTED, category);
        bundle.putInt(FIREBASE_KEY_TASK_SELECTED, taskId);
        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_TASK_SELECTED, bundle);
    }

    private void firebaseLoginEvents(String value) {
        Bundle bundle = new Bundle();

        bundle.putString(FIREBASE_KEY_QUERY, value);
        mFirebaseAnalytics.logEvent(FIREBASE_LOG_EVENT_SEARCH, bundle);
    }

    public final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Card) {
                Card card = (Card) item;
                Task task = card.getTask();

                Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                intent.putExtra(INTENT_TASK, task);
                intent.putExtra(INTENT_CATEGORY, card.getCategory());

                startActivity(intent);

                firebaseLoginEvents(card.getTitle(), task.taskId);

            } else
                Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();

        }
    }
}
