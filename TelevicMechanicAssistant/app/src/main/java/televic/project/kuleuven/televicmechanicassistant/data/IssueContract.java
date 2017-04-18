package televic.project.kuleuven.televicmechanicassistant.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Matthias on 18/04/2017.
 */

public class IssueContract {
    /*
    How URI looks like:
    content://<Base_url>/<Table>/<Query>?<AdditionalQuery>
    Example (ask for issue with id=69 and specific date):
    content://televic.project.kuleuven.televicmechanicassistant/issue/69?DATE=080430
    */
    public static final String CONTENT_AUTHORITY = "televic.project.kuleuven.televicmechanicassistant";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ISSUE = "issue";
    public static final String PATH_ISSUE_ASSET = "issue_asset";


    /* TABLE 1: Inner class that defines the table contents of the Issue table */
    public static final class IssueEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ISSUE).build();

        //Cursor: Zero Or more items
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ISSUE;
        //Cursor: One Item
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ISSUE;

        // Table name
        public static final String TABLE_NAME = "issue";

        //Columns
        public static final String COLUMN_DESCRIPTION = "issue_description";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_OPERATOR = "operator";
        public static final String COLUMN_DATA_ID = "data_id";
        public static final String COLUMN_ASSIGNED_TIME = "assigned_time";
        public static final String COLUMN_IN_PROGRESS_TIME = "in_progress_time";
        public static final String COLUMN_CLOSED_TIME = "closed_time";

        public static Uri buildIssueUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static int getIssueIdFromUri(Uri uri){
            String idString = uri.getQueryParameter(_ID);
            if(idString!=null && idString.length()>0){
                return Integer.parseInt(idString);
            }
            return -1;
        }
    }

    /* TABLE 2: Inner class that defines the table contents of the IssueAsset table */
    //ELKE ASSET HEEFT ISSUE_ID
    public static final class IssueAssetEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ISSUE_ASSET).build();

        //Cursor: Zero Or more items
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ISSUE_ASSET;
        //Cursor: One Item
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ISSUE_ASSET;

        // Table name
        public static final String TABLE_NAME = "issue_asset";

        //Columns
        public static final String COLUMN_DESCRIPTION = "asset_description";
        public static final String COLUMN_IMAGE_LOCATION = "image_location";
        public static final String COLUMN_POST_TIME = "post_time";
        public static final String COLUMN_USER_NAME = "user_name";
        public static final String COLUMN_USER_EMAIL = "user_email";
        public static final String COLUMN_ISSUE_ID = "issue_id"; //Foreign key to Issue Table

        public static Uri buildIssueAssetUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static int getIssueIdFromUri(Uri uri){
            String idString = uri.getQueryParameter(_ID);
            if(idString!=null && idString.length()>0){
                return Integer.parseInt(idString);
            }
            return -1;
        }
    }
}
