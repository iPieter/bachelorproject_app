package televic.project.kuleuven.televicmechanicassistant;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.Locale;

/**
 * Created by Anton on 19/04/2017.
 */

public class IssueAssetListAdapter extends CursorAdapter
{
    private final String LOG_TAG = IssueAssetListAdapter.class.getSimpleName();

    public IssueAssetListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        Log.v(LOG_TAG,"IssueAssetListAdapter Constructor called!");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.v(LOG_TAG,"newView");
        View view = LayoutInflater.from(context).inflate(R.layout.item_issue_asset, parent, false);
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
        ImageView imageView = (ImageView) view.findViewById( R.id.issue_asset_image );
        TextView descriptionField = (TextView) view.findViewById( R.id.issue_asset_description );
        TextView authorField = (TextView) view.findViewById( R.id.issue_asset_author );
        TextView dateField = (TextView) view.findViewById( R.id.issue_asset_date );

        //Set datafields in the list item
        Log.v(LOG_TAG, "Binding datafields to list item");
        descriptionField.setText( cursor.getString(IssueDetailActivity.COL_ASSET_DESCRIPTION) );
        authorField.setText( cursor.getString(IssueDetailActivity.COL_ASSET_USER_NAME) );

        PrettyTime prettyTime = new PrettyTime( new Locale( "nl" ) );
        Date date = new Date(Long.valueOf(cursor.getString(IssueDetailActivity.COL_ASSET_POST_TIME)));
        dateField.setText( prettyTime.format( date ) );

        byte[] imgBlob = cursor.getBlob(IssueDetailActivity.COL_ASSET_IMAGE_BLOB);
        if( imgBlob == null ) {
            imageView.setVisibility(View.GONE);
        }
        else {
            Bitmap imageBitmap = Utility.toBitmap(imgBlob);
            imageView.setImageBitmap( imageBitmap );
            imageView.setVisibility( View.VISIBLE );
        }
        Log.v(LOG_TAG, "List item set!");
    }
}
