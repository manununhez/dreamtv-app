package com.manuelnunhez.dreamtv.ui.search;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
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

import com.manuelnunhez.dreamtv.R;
import com.manuelnunhez.dreamtv.ViewModelFactory;
import com.manuelnunhez.dreamtv.data.model.Card;
import com.manuelnunhez.dreamtv.data.model.Task;
import com.manuelnunhez.dreamtv.data.model.Resource;
import com.manuelnunhez.dreamtv.data.model.Resource.Status;
import com.manuelnunhez.dreamtv.di.InjectorUtils;
import com.manuelnunhez.dreamtv.presenter.CardPresenterSelector;
import com.manuelnunhez.dreamtv.ui.videoDetails.VideoDetailsActivity;
import com.manuelnunhez.dreamtv.utils.LoadingDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_KEY_QUERY;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_KEY_TASK_CATEGORY_SELECTED;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_KEY_TASK_SELECTED;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_SEARCH;
import static com.manuelnunhez.dreamtv.utils.Constants.FIREBASE_LOG_EVENT_TASK_SELECTED;
import static com.manuelnunhez.dreamtv.utils.Constants.INTENT_CATEGORY;
import static com.manuelnunhez.dreamtv.utils.Constants.INTENT_TASK;
import static com.manuelnunhez.dreamtv.utils.Constants.STATUS_ERROR;

/*
 * This class demonstrates how to do in-app search
 */
public class SearchFragment extends SearchSupportFragment
        implements SearchSupportFragment.SearchResultProvider {

    private static final boolean FINISH_ON_RECOGNIZER_CANCELED = true;
    private static final int REQUEST_SPEECH = 0x00000010;
    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private SearchViewModel mViewModel;
    private LoadingDialog loadingDialog;
    private FirebaseAnalytics mFirebaseAnalytics;
    private LiveData<Resource<Task[]>> searchLiveData;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        ViewModelFactory factory = InjectorUtils.provideViewModelFactory(requireActivity());
        mViewModel = ViewModelProviders.of(this, factory).get(SearchViewModel.class);

        setSearchResultProvider(this);
        instantiateLoading();

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireActivity());

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

        search();
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
                        requireView().findViewById(R.id.lb_search_bar_speech_orb).requestFocus();
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

    boolean hasResults() {
        return mRowsAdapter.size() > 0 &&
                (hasFoundDataInResults());
    }

    private boolean hasPermission(final String permission) {
        final Context context = requireActivity();
        return PackageManager.PERMISSION_GRANTED == context.getPackageManager().checkPermission(
                permission, context.getPackageName());
    }

    private void loadQuery(String query) {
        if (!TextUtils.isEmpty(query) && !query.equals("nil")) {
            mViewModel.doQueryAndSearch(query);

            firebaseLoginEvents(query);
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
        if (!requireActivity().isFinishing())
            loadingDialog.show();
    }

    private void search() {
        searchLiveData = mViewModel.search();
        searchLiveData.removeObservers(getViewLifecycleOwner());
        searchLiveData.observe(getViewLifecycleOwner(), tasksListResource -> {
            Status status = tasksListResource.status;
            Task[] data = tasksListResource.data;
            String message = tasksListResource.message;

            if (status.equals(Status.LOADING))
                showLoading();
            else if (status.equals(Status.SUCCESS)) {
                loadVideos(data);


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

    private boolean hasFoundDataInResults(){
        return (mViewModel.resultsFound().getValue() == null) ? false : (mViewModel.resultsFound().getValue());
    }

    private void loadVideos(Task[] results) {

        int mTitleRes;
        if (hasFoundDataInResults())
            mTitleRes = R.string.search_results;
        else
            mTitleRes = R.string.no_search_results;

        HeaderItem header = new HeaderItem(getString(mTitleRes, mViewModel.query.getValue()));
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

    void focusOnSearch() {
        requireView().findViewById(R.id.lb_search_bar).requestFocus();
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

    private void goToVideoDetails(Card card, Task task) {
        Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
        intent.putExtra(INTENT_TASK, task);
        intent.putExtra(INTENT_CATEGORY, card.getCategory());

        startActivity(intent);

        firebaseLoginEvents(card.getTitle(), task.getTaskId());
    }

    public final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Card) {
                Card card = (Card) item;
                Task task = card.getTask();

                goToVideoDetails(card, task);
            } else
                Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
        }
    }
}
