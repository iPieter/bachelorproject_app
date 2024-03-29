package televic.project.kuleuven.televicmechanicassistant;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * OverviewListAdapter binds database rows to listItems in the ListView.
 * This is specifically used by the IssueOverviewFragment
 * Created by Matthias on 29/03/2017.
 */

public class OverviewListAdapter extends CursorAdapter {
    private final String LOG_TAG = OverviewListAdapter.class.getSimpleName();

    public OverviewListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        Log.v(LOG_TAG, "OverviewListAdapter Constructor called!");

    }

    /**
     * Called when a new View in the List is created
     *
     * @param context
     * @param cursor
     * @param parent
     * @return the view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.v(LOG_TAG, "newView");
        View view = LayoutInflater.from(context).inflate(R.layout.item_issue_overview, parent, false);
        return view;
    }

    /**
     * Binding the views in the ListView to Rows in the database, using the cursor.
     *
     * @param view    The view returned in newView
     * @param context
     * @param cursor  Cursor that is used to iterate over Database Rows
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
        item_header_status.setText(convertIssueStatus(cursor.getString(IssueOverviewFragment.COL_ISSUE_STATUS)));
        item_header_traincoach.setText(cursor.getString(IssueOverviewFragment.COL_ISSUE_TRAINCOACH));
        item_content.setText(cursor.getString(IssueOverviewFragment.COL_ISSUE_DESCRIPTION));
        Log.v(LOG_TAG, "List item set!");
    }

    /**
     * Making the status user friendly
     *
     * @param status
     * @return
     */
    public String convertIssueStatus(String status) {
        switch (status) {
            case "CREATED":
                return "Aangemaakt";
            case "ASSIGNED":
                return "Toegewezen";
            case "IN_PROGRESS":
                return "In behandeling";
            case "CLOSED":
                return "Gesloten";
            default:
                return "";
        }
    }
}
