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

    /*
    private LayoutInflater mLayoutInflater;
    private List<IssueOverviewRowitem> mDataList = new ArrayList<>();

    public OverviewListAdapter(Context context) {
        Log.v(LOG_TAG, "Init OverviewListAdapter");
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int i) {
        return mDataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    //Inlezen van ListItem
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.v(LOG_TAG, "Updating Listitem View");


        //Check for avoiding Null pointer exception
        LinearLayout itemView;
        if (convertView == null) {
            itemView = (LinearLayout) mLayoutInflater
                    .inflate(R.layout.item_issue_overview, parent, false);
        } else {
            itemView = (LinearLayout) convertView;
        }

        //Collect (text)views from layout of list item
        Log.v(LOG_TAG, "Fetching layout items from list");
        TextView item_header_workplace =
                (TextView) itemView.findViewById(R.id.item_header_workplace);
        TextView item_header_status =
                (TextView) itemView.findViewById(R.id.item_header_status);
        TextView item_header_traincoach =
                (TextView) itemView.findViewById(R.id.item_header_traincoach);
        TextView item_content =
                (TextView) itemView.findViewById(R.id.item_content);
        /*
        //Set datafields in the list item
        Log.v(LOG_TAG, "Setting datafields to list item");
        item_header_workplace.setText(mDataList.get(position).getWorkplace());
        item_header_status.setText(mDataList.get(position).getStatus());
        item_header_traincoach.setText(mDataList.get(position).getTraincoach());
        item_content.setText(mDataList.get(position).getDescription());
        Log.v(LOG_TAG, "List item set!");

        return itemView;
    }

    public void updateView(List<IssueOverviewRowitem> dataList) {
        mDataList = dataList;
        notifyDataSetChanged();
    }*/

    public OverviewListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_issue_overview, parent, false);
        return view;
    }

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

        /* TODO bind to values cursor.toString(COLUMN IN MAIN_ACTIVITY)
        //Set datafields in the list item
        Log.v(LOG_TAG, "Setting datafields to list item");
        item_header_workplace.setText(mDataList.get(position).getWorkplace());
        item_header_status.setText(mDataList.get(position).getStatus());
        item_header_traincoach.setText(mDataList.get(position).getTraincoach());
        item_content.setText(mDataList.get(position).getDescription());
        Log.v(LOG_TAG, "List item set!");
        */
    }
}
