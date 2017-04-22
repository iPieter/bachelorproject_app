package televic.project.kuleuven.televicmechanicassistant;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

import java.util.concurrent.CountDownLatch;

import televic.project.kuleuven.televicmechanicassistant.data.IssueContract;


//MAIN LAUNCH Activity
public class IssueOverviewFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = IssueOverviewFragment.class.getSimpleName();
    private boolean DEBUG_MODE = true;

    //TODO init currentUserId @login!!!
    private int mCurrentUserId;

    //Passing to other activity
    public static String INTENT_ISSUE_ID = "issue_id_value987564321";

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_issue_overview, container, false);

        //INIT
        mCurrentUserId = 1; //TODO

        //Setting up adapter
        ListView listView = (ListView) rootView.findViewById(android.R.id.list);
        mOverviewListAdapter = new OverviewListAdapter(getActivity(), null, 0);
        listView.setAdapter(mOverviewListAdapter);
        Log.v(LOG_TAG,"Adapter is set to listview in onCreateView");

        // When item is clicked, a IssueDetailActivity will be started
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.v(LOG_TAG,"Item Clicked!");
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    Log.v(LOG_TAG,"Creating intent");
                    Intent intent = new Intent(getActivity(), IssueDetailActivity.class)
                            .putExtra(INTENT_ISSUE_ID, cursor.getInt(COL_ISSUE_ID));
                    //.putExtra(CURRENT_MECHANIC_ID,mCurrentMechanic);
                    //TODO in response in DetailActivity: int intValue = mIntent.getIntExtra(INTENT_ISSUE_ID, 0);
                    startActivity(intent);
                }
            }
        });

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this.getContext());
        progressBar.setId(R.id.progressbar_loading);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        listView.setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup rootGroup = (ViewGroup) rootView.findViewById(android.R.id.content);
        rootGroup.addView(progressBar);
        Log.v(LOG_TAG, "Progressbar is set!");

        //Activating Back-End
        handleOverviewData();

        Log.v(LOG_TAG, "onCreateView Ended");
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(OVERVIEW_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
        Log.v(LOG_TAG,"Activity created and initLoader");
    }

    public void setEmptyText(String text) {
        TextView textView = (TextView) getView().findViewById(android.R.id.empty);
        textView.setText(text);
    }

    public void removeProgressBar() {
        Log.v(LOG_TAG, "Trying to remove the progressbar");
        try {
            ListView listView = (ListView) getListView().findViewById(android.R.id.list);
            ProgressBar progressBar = (ProgressBar) listView.findViewById(R.id.progressbar_loading);
            listView.removeViewInLayout(progressBar);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }
    }

    /**
     * Calling the backend in RESTRequestHandler and starts AsyncTask JSONParserTask
     * to parse the REST response and write the parsed data to the database.
     */
    public void handleOverviewData() {
        //INIT
        JSONParserTask jsonParserTask = new JSONParserTask(getActivity());
        CountDownLatch mCountDownLatch = new CountDownLatch(RESTRequestHandler.REQUEST_COUNT);
        RESTRequestHandler mRestRequestHandler = new RESTRequestHandler(
                this.getActivity().getApplicationContext(),
                mCountDownLatch);

        //Calling backend
        if (mCurrentUserId >= 0) {
            if (DEBUG_MODE) {
                mRestRequestHandler.setIssueStringResponse(RESTRequestHandler.testStringIssue);
                mRestRequestHandler.setWorkplaceStringResponse(RESTRequestHandler.testStringWorkplace);
            } else {
                mRestRequestHandler.sendParallelRequest(mCurrentUserId);
                try {
                    mCountDownLatch.await(); //await until all parallel requests have a response
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //The order of these parameters is obligatory
            jsonParserTask.execute(
                    mRestRequestHandler.getIssueStringResponse(),
                    mRestRequestHandler.getWorkplaceStringResponse());
        } else {
            Log.e(LOG_TAG, "Current user id < 0");
        }
    }

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

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.v(LOG_TAG, "Loader onLoadFinished");
        mOverviewListAdapter.swapCursor(cursor);
        Log.v(LOG_TAG, "Loader cursor swapped, cursorCount = "+cursor.getCount());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.v(LOG_TAG, "Loader onLoaderReset");
        mOverviewListAdapter.swapCursor(null);
    }
}