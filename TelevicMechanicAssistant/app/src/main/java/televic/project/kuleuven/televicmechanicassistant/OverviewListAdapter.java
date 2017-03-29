package televic.project.kuleuven.televicmechanicassistant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthias on 29/03/2017.
 */

public class OverviewListAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    private List<String[]> mDataList = new ArrayList<>();

    public OverviewListAdapter(Context context) {
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
        //Check for avoiding Null pointer exception
        LinearLayout itemView;
        if (convertView == null) {
            itemView = (LinearLayout) mLayoutInflater
                    .inflate(R.layout.item_issue_overview, parent, false);
        } else {
            itemView = (LinearLayout) convertView;
        }

        //Collect (text)views from layout of list item
        TextView item_header_workplace =
                (TextView) itemView.findViewById(R.id.item_header_workplace);
        TextView item_header_status =
                (TextView) itemView.findViewById(R.id.item_header_status);
        TextView item_header_traincoach =
                (TextView) itemView.findViewById(R.id.item_header_traincoach);
        TextView item_content =
                (TextView) itemView.findViewById(R.id.item_content);

        //Set datafields in the list item
        int counter = 0;
        item_header_workplace.setText(mDataList.get(position)[counter++]);
        item_header_status.setText(mDataList.get(position)[counter++]);
        item_header_traincoach.setText(mDataList.get(position)[counter++]);
        item_content.setText(mDataList.get(position)[counter++]);

        return itemView;
    }

    public void updateView(List<String[]> dataList) {
        mDataList = dataList;
        notifyDataSetChanged();
    }
}
