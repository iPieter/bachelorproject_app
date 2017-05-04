package televic.project.kuleuven.televicmechanicassistant;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import android.widget.TextView;

import televic.project.kuleuven.televicmechanicassistant.data.IssueContract;


/**
 * This fragment shows a list of all assigned Issues to the user.
 */
public class IssueOverviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = IssueOverviewFragment.class.getSimpleName();
    private int mCurrentUserId;

    //UI Components
    private View mProgressView;
    private ListView mListView;
    private TextView mEmptyListTextView;

    //Tag for Extra to pass with intent to other activity
    public static String INTENT_ISSUE_ID = "issue_id_value987564321";
    public static String INTENT_DATA_ID = "data_id_value987564321";

    //The adapter used to populate the listview
    private OverviewListAdapter mOverviewListAdapter;

    //Id of the loader
    private static final int OVERVIEW_LOADER = 0;

    //Values in Database needed in this activity
    private static final String[] OVERVIEW_COLUMNS = {
            IssueContract.IssueEntry.TABLE_NAME + "." + IssueContract.IssueEntry._ID,
            IssueContract.IssueEntry.COLUMN_STATUS,
            IssueContract.IssueEntry.COLUMN_DESCRIPTION,
            IssueContract.IssueEntry.COLUMN_ASSIGNED_TIME,
            IssueContract.IssueEntry.COLUMN_IN_PROGRESS_TIME,
            IssueContract.IssueEntry.COLUMN_TRAINCOACH_NAME,
            IssueContract.IssueEntry.COLUMN_OPERATOR,
            IssueContract.TraincoachEntry.COLUMN_WORKPLACE_NAME,
            IssueContract.IssueEntry.COLUMN_DATA_ID
    };

    //Depends on OVERVIEW_COLUMNS, if OVERVIEW_COLUMNS changes, so must these indexes!
    static final int COL_ISSUE_ID = 0;
    static final int COL_ISSUE_STATUS = 1;
    static final int COL_ISSUE_DESCRIPTION = 2;
    static final int COL_ISSUE_ASSIGNED_TIME = 3;
    static final int COL_ISSUE_IN_PROGRESS_TIME = 4;
    static final int COL_ISSUE_TRAINCOACH = 5;
    static final int COL_ISSUE_OPERATOR = 6;
    static final int COL_ISSUE_WORKPLACE = 7;
    static final int COL_ISSUE_DATA_ID = 8;

    /**
     * Setup on creation of fragment
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.v(LOG_TAG, "onCreate method ended");
    }

    /**
     * The action_refresh must be handled within the fragment,
     * because the fragment handles the handleOverviewData() for the back-end.
     * Other options in the menu are handled in the super Activity.
     *
     * @param item the selected item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            handleOverviewData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialization of all attributes of the class
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_issue_overview, container, false);

        //INIT
        mCurrentUserId = Utility.getLocalUserId(getActivity());
        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mProgressView = rootView.findViewById(R.id.overviewlist_progress);
        mEmptyListTextView = (TextView) rootView.findViewById(android.R.id.empty);

        //Show progressbar until backend is handled
        showProgress(true);

        //Setting up adapter
        mOverviewListAdapter = new OverviewListAdapter(getActivity(), null, 0);
        mListView.setAdapter(mOverviewListAdapter);
        Log.v(LOG_TAG, "Adapter is set to listview in onCreateView");

        // When item is clicked, a IssueDetailActivity will be started
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.v(LOG_TAG, "Item Clicked!");
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    Log.v(LOG_TAG, "Creating intent");
                    Intent intent = new Intent(getActivity(), IssueDetailActivity.class)
                            .putExtra(INTENT_ISSUE_ID, cursor.getInt(COL_ISSUE_ID))
                            .putExtra(INTENT_DATA_ID, cursor.getInt(COL_ISSUE_DATA_ID));
                    startActivity(intent);
                }
            }
        });

        //Activating Back-End
        handleOverviewData();

        Log.v(LOG_TAG, "onCreateView Ended");
        return rootView;
    }

    /**
     * When the activity is created, the loader must be initialized.
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(OVERVIEW_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
        Log.v(LOG_TAG, "Activity created and initLoader");
    }

    /**
     * Text showed when there are no Issues assigned to the user and
     * hides the list.
     * @param show
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showListEmptyText(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mListView.setVisibility(show ? View.GONE : View.VISIBLE);
        mListView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mListView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mEmptyListTextView.setVisibility(show? View.VISIBLE : View.GONE);
        mEmptyListTextView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Shows the progress UI and hides the list
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mListView.setVisibility(show ? View.GONE : View.VISIBLE);
        mListView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mListView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Calling the backend in RESTRequestHandler and starts AsyncTask JSONParserTask
     * to parse the REST response and write the parsed data to the database.
     */
    public void handleOverviewData() {
        //INIT
        JSONParserTask jsonParserTask = new JSONParserTask(getActivity());
        RESTRequestHandler mRestRequestHandler = new RESTRequestHandler(
                this.getActivity().getApplicationContext(), jsonParserTask);

        //Calling backend
        if (Utility.isUserIdValid(mCurrentUserId)) {
            mRestRequestHandler.sendParallelRequest(mCurrentUserId);
        } else {
            Log.e(LOG_TAG, "Current user id is not valid!");
        }
    }

    /**
     * Called when loader is initializing
     *
     * @param i
     * @param bundle
     * @return CursorLoader
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(LOG_TAG, "Creating CursorLoader");

        // Sort order =  Ascending, by Assigned Time
        String sortOrder = IssueContract.IssueEntry.COLUMN_ASSIGNED_TIME + " ASC";
        Uri allIssues = IssueContract.IssueEntry.CONTENT_URI;
        Log.v(LOG_TAG, "CURSORLOADER URI: " + allIssues);

        return new CursorLoader(getActivity(),
                allIssues,
                OVERVIEW_COLUMNS,
                null,
                null,
                sortOrder);
    }

    /**
     * Called when loader is finished.
     * Show a message when no tasks are loaded.
     *
     * @param cursorLoader
     * @param cursor
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.v(LOG_TAG, "Loader onLoadFinished");
        mOverviewListAdapter.swapCursor(cursor);

        //Hide progressbar
        showProgress(false);

        //When no tasks assigned, display message
        if (cursor.getCount() == 0) {
            showListEmptyText(true);
        } else {
            showListEmptyText(false);
        }
        Log.v(LOG_TAG, "Loader cursor swapped, cursorCount = " + cursor.getCount());
    }

    /**
     * Called when loader resets
     *
     * @param cursorLoader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.v(LOG_TAG, "Loader onLoaderReset");
        mOverviewListAdapter.swapCursor(null);
        showProgress(true);
    }
}