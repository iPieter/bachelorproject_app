package televic.project.kuleuven.televicmechanicassistant;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.issue.IssueAsset;

/**
 * Created by Anton on 19/04/2017.
 */

public class IssueAssetListAdapter extends BaseAdapter
{
    private final String LOG_TAG = IssueAssetListAdapter.class.getSimpleName();

    private LayoutInflater mLayoutInflater;
    private List<IssueAsset> mDataList = new ArrayList<>();

    public IssueAssetListAdapter( Context context )
    {
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
        return mDataList.size();
    }

    @Override
    public Object getItem( int position )
    {
        return mDataList.get( position );
    }

    @Override
    public long getItemId( int position )
    {
        return mDataList.get( position ).getId();
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        Log.v(LOG_TAG, "Updating Listitem View");

        //Check for avoiding Null pointer exception
        LinearLayout itemView;
        if (convertView == null) {
            itemView = (LinearLayout) mLayoutInflater.inflate(R.layout.item_issue_overview, parent, false);
        } else {
            itemView = (LinearLayout) convertView;
        }
        ImageView issueAssetView = (ImageView) itemView.findViewById( R.id.issue_asset_image );
        TextView descriptionField = (TextView) itemView.findViewById( R.id.issue_asset_description );
        TextView authorField = (TextView) itemView.findViewById( R.id.issue_asset_author );
        TextView dateField = (TextView) itemView.findViewById( R.id.issue_asset_date );

        IssueAsset asset = mDataList.get( position );

        descriptionField.setText( asset.getDescr() );
        authorField.setText( asset.getUser().getName() );
        dateField.setText( asset.getTime().toString() );

        return itemView;
    }
}
