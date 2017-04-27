package televic.project.kuleuven.televicmechanicassistant.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * The IssueContract class defines all constants used for the database.
 * Also static helper methods for URI's are implemented here.
 * Created by Matthias on 18/04/2017.
 */

public class IssueContract {
    private static final String LOG_TAG = IssueContract.class.getSimpleName();

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
    public static final String PATH_TRAINCOACH = "traincoach";
    public static final String PATH_WITH_IMG = "with_img";

    /**
     * TABLE 1: Inner class that defines the table contents of the Issue table
     */
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
        public static final String COLUMN_TRAINCOACH_NAME = "traincoach_name";
        public static final String COLUMN_TRAINCOACH_ID = "traincoach_id"; //Needed to link REST request to eachother


        /**
         * Method to help build a URI for a given id
         *
         * @param id
         * @return the URI
         */
        public static Uri buildIssueUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * Method to receive the id from the URI
         *
         * @param uri
         * @return id -1 if no id found
         */
        public static int getIssueIdFromUri(Uri uri) {
            String idString = uri.getQueryParameter(_ID);
            if (idString != null && idString.length() > 0) {
                return Integer.parseInt(idString);
            }
            return -1;
        }
    }

    /**
     * TABLE 2: Inner class that defines the table contents of the IssueAsset table.
     * Each IssueAsset has a IssueId, to link the IssueAsset table to the Issue Table.
     */
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
        //Overwriting _ID BaseColumn, because of _ID column collision with Issue-table at JOIN
        public static final String _ID = "issue_asset_id";
        public static final String COLUMN_DESCRIPTION = "asset_description";
        public static final String COLUMN_IMAGE_PRESENT = "image_location";
        public static final String COLUMN_IMAGE_BLOB = "image_blob";
        public static final String COLUMN_POST_TIME = "post_time";
        public static final String COLUMN_USER_NAME = "user_name";
        public static final String COLUMN_USER_EMAIL = "user_email";
        public static final String COLUMN_ISSUE_ID = "issue_id"; //Foreign key to Issue Table

        /**
         * Method to build a URI for an IssueAsset and append the id on the end.
         *
         * @param id
         * @return the URI
         */
        public static Uri buildIssueAssetUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * Method to build a URI, with appended path and with appended id
         *
         * @param id
         * @return the URI
         */
        public static Uri buildIssueAssetWithImgUri(long id) {
            Uri uri = IssueContract.IssueAssetEntry.CONTENT_URI
                    .buildUpon().appendPath(IssueContract.PATH_WITH_IMG).build();
            return ContentUris.withAppendedId(uri, id);
        }

        /**
         * Returns the id from the URI
         *
         * @param uri
         * @return id -1 if no id found
         */
        public static int getIssueIdFromUri(Uri uri) {
            String idString = uri.getPathSegments().get(1);
            Log.v(LOG_TAG, "idString in getIssueIdFromUri=" + idString);
            if (idString != null && idString.length() > 0) {
                return Integer.parseInt(idString);
            }
            return -1;
        }

        /**
         * Return the id from the URI
         *
         * @param uri
         * @return id -1 if no id found
         */
        public static int getIssueIdFromImgUri(Uri uri) {
            String idString = uri.getPathSegments().get(2);

            if (idString != null && idString.length() > 0) {
                return Integer.parseInt(idString);
            }
            return -1;
        }
    }

    /**
     * TABLE 3: Inner class that defines the table contents of the Traincoach table.
     * This table is linked to the Issue table by the unique Traincoach ID. This way we can
     * retrieve the workplace info for the Issue.
     */
    public static final class TraincoachEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAINCOACH).build();

        //Cursor: Zero Or more items
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAINCOACH;
        //Cursor: One Item
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAINCOACH;

        // Table name
        public static final String TABLE_NAME = "traincoach";

        //Columns
        public static final String _ID = "traincoach_id";
        public static final String COLUMN_WORKPLACE_ID = "workplace_id";
        public static final String COLUMN_WORKPLACE_NAME = "workplace_name";

        /**
         * Returns a URI with the id appended as parameter.
         *
         * @param id
         * @return the URI with appended id
         */
        public static Uri buildTraincoachUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
