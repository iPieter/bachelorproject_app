package televic.project.kuleuven.televicmechanicassistant;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import model.app.IssueOverviewRowitem;

/**
 * Created by Matthias on 29/03/2017.
 */

public class OverviewListAdapter extends CursorAdapter {
    private final String LOG_TAG = OverviewListAdapter.class.getSimpleName();

    public OverviewListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_issue_overview, parent, false);
        return view;
    }

    /**
     * Binding the views in the ListView to Rows in the database, using the cursor.
     * @param view The view returned in newView
     * @param context
     * @param cursor Cursor that is used to iterate over Database Rows
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.v(LOG_TAG, "Updating Listitem View");

        //Collect (text)views from layout of list item
        Log.v(LOG_TAG, "Fetching layout items from list");
        TextView item_header_workplace =
                (TextView) view.findViewById(R.id.item_header_workplace);
        TextView item_header_status =
                (TextView) view.findViewById(R.id.item_header_status);
        TextView item_header_traincoach =
                (TextView) view.findViewById(R.id.item_header_traincoach);
        TextView item_content =
                (TextView) view.findViewById(R.id.item_content);

        //Set datafields in the list item
        Log.v(LOG_TAG, "Binding datafields to list item");
        item_header_workplace.setText(cursor.getString(IssueOverviewFragment.COL_ISSUE_WORKPLACE));
        item_header_status.setText(cursor.getString(IssueOverviewFragment.COL_ISSUE_STATUS));
        item_header_traincoach.setText(cursor.getString(IssueOverviewFragment.COL_ISSUE_TRAINCOACH));
        item_content.setText(cursor.getString(IssueOverviewFragment.COL_ISSUE_DESCRIPTION));
        Log.v(LOG_TAG, "List item set!");
    }
}
