package com.dream.dreamtv.ui.Search;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
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

import com.dream.dreamtv.BuildConfig;
import com.dream.dreamtv.R;
import com.dream.dreamtv.model.Card;
import com.dream.dreamtv.model.Resource;
import com.dream.dreamtv.model.Task;
import com.dream.dreamtv.presenter.SideInfoPresenter.SideInfoCardPresenter;
import com.dream.dreamtv.ui.VideoDetails.VideoDetailsActivity;
import com.dream.dreamtv.utils.InjectorUtils;
import com.dream.dreamtv.utils.LoadingDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.dream.dreamtv.utils.Constants.INTENT_CATEGORY;
import static com.dream.dreamtv.utils.Constants.INTENT_TASK;

/*
 * This class demonstrates how to do in-app search
 */
public class SearchFragment extends SearchSupportFragment
        implements SearchSupportFragment.SearchResultProvider {
    private static final String TAG = "SearchFragment";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final boolean FINISH_ON_RECOGNIZER_CANCELED = true;
    private static final int REQUEST_SPEECH = 0x00000010;

    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private String mQuery;
    private boolean mResultsFound = false;
    private SearchViewModel mViewModel;
    private LiveData<Resource<Task[]>> searchLiveData;
    private LoadingDialog loadingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        SearchViewModelFactory factory = InjectorUtils.provideSearchViewModelFactory(Objects.requireNonNull(getActivity()));
        mViewModel = ViewModelProviders.of(this, factory).get(SearchViewModel.class);

        setSearchResultProvider(this);
        instantiateLoading();

        setOnItemViewClickedListener(new ItemViewClickedListener());
        if (DEBUG) {
            Log.d(TAG, "User is initiating a search. Do we have RECORD_AUDIO permission? " +
                    hasPermission(Manifest.permission.RECORD_AUDIO));
        }
        if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
            if (DEBUG) {
                Log.d(TAG, "Does not have RECORD_AUDIO, using SpeechRecognitionCallback");
            }
            // SpeechRecognitionCallback is not required and if not provided recognition will be
            // handled using internal speech recognizer, in which case you must have RECORD_AUDIO
            // permission
//            startRecognition();
        } else if (DEBUG) {
            Log.d(TAG, "We DO have RECORD_AUDIO");
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
                        if (DEBUG) Log.v(TAG, "Voice search canceled");
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
        if (DEBUG) Log.i(TAG, String.format("Search text changed: %s", newQuery));
        loadQuery(newQuery);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (DEBUG) Log.i(TAG, String.format("Search text submitted: %s", query));
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
        loadingDialog.show();
    }

    private void search(String query) {
        searchLiveData = mViewModel.search(query);
        searchLiveData.removeObservers(getViewLifecycleOwner());
        searchLiveData.observe(getViewLifecycleOwner(), tasksListResource -> {
            if (tasksListResource.status.equals(Resource.Status.LOADING))
                showLoading();
            else if (tasksListResource.status.equals(Resource.Status.SUCCESS)) {
                loadVideos(tasksListResource.data);


                if (DEBUG) Log.d(TAG, "task response");
                dismissLoading();
            } else if (tasksListResource.status.equals(Resource.Status.ERROR)) {
                //TODO do something
                if (tasksListResource.message != null)
                    if (DEBUG) Log.d(TAG, tasksListResource.message);
                    else if (DEBUG) Log.d(TAG, "Status ERROR");

                dismissLoading();
            }

        });

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
        ArrayObjectAdapter adapter = new ArrayObjectAdapter(new SideInfoCardPresenter(getActivity()));
        List<Card> cards = new ArrayList<>();

        if (results != null) {
            for (Task task : results) {
                cards.add(new Card(task));
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

    public final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Card) {
                Card value = (Card) item;
                Task task = value.getTask();

                Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                intent.putExtra(INTENT_TASK, task);
                intent.putExtra(INTENT_CATEGORY, value.getCategory());

                startActivity(intent);

            } else
                Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();

        }
    }
}
