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

import org.ocpsoft.prettytime.PrettyTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        //Check for avoiding Null pointer exception
        LinearLayout itemView;
        if (convertView == null) {
            itemView = (LinearLayout) mLayoutInflater.inflate(R.layout.issue_asset, parent, false);
            Log.i( LOG_TAG, "INFLATING WITH ITEM_ISSUE_OVERVIEW" );
        } else {
            itemView = (LinearLayout) convertView;
        }

        Log.i( LOG_TAG, itemView.toString() );
        Log.i( LOG_TAG, "" + itemView.getId() );

        ImageView issueAssetView = (ImageView) itemView.findViewById( R.id.issue_asset_image );
        TextView descriptionField = (TextView) itemView.findViewById( R.id.issue_asset_description );
        TextView authorField = (TextView) itemView.findViewById( R.id.issue_asset_author );
        TextView dateField = (TextView) itemView.findViewById( R.id.issue_asset_date );

        IssueAsset asset = mDataList.get( position );

        Log.i( LOG_TAG, asset.toString() );

        if( descriptionField == null || authorField == null || dateField == null )
            Log.i( LOG_TAG, "FIELDS ARE NULL" );

        descriptionField.setText( asset.getDescr() );
        authorField.setText( asset.getUser().getName() );

        PrettyTime prettyTime = new PrettyTime( new Locale( "nl" ) );
        dateField.setText( prettyTime.format( asset.getTime() ) );

        if( asset.getBitmap() == null )
            issueAssetView.setVisibility( View.GONE );
        else {
            issueAssetView.setImageBitmap( asset.getBitmap() );
            issueAssetView.setVisibility( View.VISIBLE );
        }
        return itemView;
    }

    public void updateView(List<IssueAsset> dataList) {
        mDataList = dataList;
        notifyDataSetChanged();
    }
}
