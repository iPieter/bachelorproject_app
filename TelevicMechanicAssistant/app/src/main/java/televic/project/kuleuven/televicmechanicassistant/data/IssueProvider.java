package televic.project.kuleuven.televicmechanicassistant.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * IssueProvider extends from ContentProvider. This IssueProvider is accessed to
 * to manage the database. It uses a IssueDbHelper to manage the database.
 * Queries,updates,deletions and inserts are handled by the IssueProvider.
 * Created by Matthias on 18/04/2017.
 */

public class IssueProvider extends ContentProvider {
    private static final String LOG_TAG = IssueProvider.class.getSimpleName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private IssueDbHelper mOpenHelper;

    //The Codes for the UriMatcher, corresponding with a certain path
    static final int ISSUE = 100; //contains traincoach table for workplace info: see SQLiteQueryBuilder for JOIN!
    static final int ISSUE_WITH_ID = 101;
    static final int ISSUE_ASSET = 300;
    static final int ISSUE_ASSET_WITH_ISSUE_ID = 301;
    static final int ISSUE_ASSET_WITH_ISSUE_ID_AND_IMG = 302;
    static final int TRAINCOACH = 400;

    //SQLiteQueryBuilder JOINS used for QUERIES (NOT for inserts,deletes,updates)
    private static final SQLiteQueryBuilder sIssueAssetWorkplaceByIssueQueryBuilder;
    private static final SQLiteQueryBuilder sWorkplaceByIssueQueryBuilder;

    //Init of the SQLiteQueryBuilder JOINS
    static {
        Log.v(LOG_TAG, "Creating SQLiteQueryBuilder!");
        sIssueAssetWorkplaceByIssueQueryBuilder = new SQLiteQueryBuilder();
        sWorkplaceByIssueQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //issue INNER JOIN issue_asset ON issue_asset.issue_id = issue._id
        //      INNER JOIN traincoach ON issue.traincoach_id = traincoach._id
        sIssueAssetWorkplaceByIssueQueryBuilder.setTables(
                IssueContract.IssueEntry.TABLE_NAME
                        + " INNER JOIN " +
                        IssueContract.IssueAssetEntry.TABLE_NAME +
                        " ON " + IssueContract.IssueAssetEntry.TABLE_NAME +
                        "." + IssueContract.IssueAssetEntry.COLUMN_ISSUE_ID +
                        " = " + IssueContract.IssueEntry.TABLE_NAME +
                        "." + IssueContract.IssueEntry._ID
                        + " INNER JOIN " +
                        IssueContract.TraincoachEntry.TABLE_NAME +
                        " ON " + IssueContract.IssueEntry.TABLE_NAME +
                        "." + IssueContract.IssueEntry.COLUMN_TRAINCOACH_ID +
                        " = " + IssueContract.TraincoachEntry.TABLE_NAME +
                        "." + IssueContract.TraincoachEntry._ID
        );

        //issue INNER JOIN traincoach ON issue.traincoach_id = traincoach._id
        sWorkplaceByIssueQueryBuilder.setTables(
                IssueContract.IssueEntry.TABLE_NAME
                        + " INNER JOIN " +
                        IssueContract.TraincoachEntry.TABLE_NAME +
                        " ON " + IssueContract.IssueEntry.TABLE_NAME +
                        "." + IssueContract.IssueEntry.COLUMN_TRAINCOACH_ID +
                        " = " + IssueContract.TraincoachEntry.TABLE_NAME +
                        "." + IssueContract.TraincoachEntry._ID
        );
        Log.v(LOG_TAG, "SQLiteQueryBuilders builded!");
    }

    /**
     * Creation of a UriMatcher. This UriMatcher is used to match the URI sent to the IssueProvider
     * to the corresponding code to access the correct query,updated,delete or insert method.
     *
     * @return UriMatcher
     */
    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = IssueContract.CONTENT_AUTHORITY;

        // For each new URI path, create corresponding code.
        matcher.addURI(authority, IssueContract.PATH_ISSUE, ISSUE);
        matcher.addURI(authority, IssueContract.PATH_ISSUE + "/*", ISSUE_WITH_ID);
        matcher.addURI(authority, IssueContract.PATH_ISSUE_ASSET, ISSUE_ASSET);
        matcher.addURI(authority, IssueContract.PATH_ISSUE_ASSET + "/#", ISSUE_ASSET_WITH_ISSUE_ID);
        matcher.addURI(authority, IssueContract.PATH_ISSUE_ASSET + "/"
                + IssueContract.PATH_WITH_IMG + "/#", ISSUE_ASSET_WITH_ISSUE_ID_AND_IMG);
        matcher.addURI(authority, IssueContract.PATH_TRAINCOACH, TRAINCOACH);

        Log.v(LOG_TAG, "UriMatcher initialized");
        return matcher;
    }

    //Issue._ID = ?
    private static final String sIssueByIdSelection =
            IssueContract.IssueEntry.TABLE_NAME +
                    "." + IssueContract.IssueEntry._ID + " = ? ";

    //IssueAsset.issueId = ?
    private static final String sIssueAssetByIssueSelection =
            IssueContract.IssueAssetEntry.TABLE_NAME +
                    "." + IssueContract.IssueAssetEntry.COLUMN_ISSUE_ID + " = ? ";

    //IssueAsset.COLUMN_IMAGE_PRESENT = ?
    private static final String sIssueAssetByIssueAndImgSelection =
            IssueContract.IssueAssetEntry.TABLE_NAME +
                    "." + IssueContract.IssueAssetEntry.COLUMN_IMAGE_PRESENT + " = ? AND " +
                    IssueContract.IssueAssetEntry.TABLE_NAME +
                    "." + IssueContract.IssueAssetEntry.COLUMN_ISSUE_ID + " = ? ";

    /**
     * We create a new SQLiteOpenHelper to create the database if it didn't exist yet.
     * Or upgrade the database if it's version was incremented.
     */
    @Override
    public boolean onCreate() {
        Log.d(LOG_TAG, "entered onCreate");
        mOpenHelper = new IssueDbHelper(getContext());

        return true;
    }

    /**
     * Method to query the database
     *
     * @param uri           defines which query to execute
     * @param projection    Which columns to return in the cursor
     * @param selection     Where clause
     * @param selectionArgs Where clause parameters
     * @param sortOrder     The sorting Order of the returned rows
     * @return a cursor with the resulting rows
     */
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Log.v(LOG_TAG, "QUERY ATTEMPT");
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "issue"
            case ISSUE: {
                Log.v(LOG_TAG, "QUERY ISSUE");
                //get All Issues With Workplace
                retCursor = sWorkplaceByIssueQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "issue/*"
            case ISSUE_WITH_ID: {
                Log.v(LOG_TAG, "QUERY ISSUE_WITH_ID");
                retCursor = getIssueById(uri, projection, sortOrder);
                break;
            }
            // "issue_asset/*"
            case ISSUE_ASSET_WITH_ISSUE_ID: {
                Log.v(LOG_TAG, "QUERY ISSUE_ASSET_WITH_ISSUE_ID");
                retCursor = getIssueAssetByIssueId(uri, projection, sortOrder);
                break;
            }

            // "issue_asset/with_img/*"
            case ISSUE_ASSET_WITH_ISSUE_ID_AND_IMG: {
                Log.v(LOG_TAG, "QUERY ISSUE_ASSET_WITH_ISSUE_ID_AND_IMG");
                retCursor = getIssueAssetByIssueIdAndImg(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Log.v(LOG_TAG, "QUERY cursor #rows = " + retCursor.getCount());
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /**
     * Setup of query to fetch an Issue in Issue Table with specified parameter _ID
     *
     * @param uri
     * @param projection
     * @param sortOrder
     * @return cursor with result of query
     */
    private Cursor getIssueById(Uri uri, String[] projection, String sortOrder) {
        int issueId = IssueContract.IssueEntry.getIssueIdFromUri(uri);

        String selection = sIssueByIdSelection;
        String[] selectionArgs = new String[]{Integer.toString(issueId)};

        return sIssueAssetWorkplaceByIssueQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /**
     * Setup of query to ask all IssueAssets for certain parameter IssueId
     *
     * @param uri
     * @param projection
     * @param sortOrder
     * @return cursor with result of query
     */
    private Cursor getIssueAssetByIssueId(Uri uri, String[] projection, String sortOrder) {
        int issueId = IssueContract.IssueAssetEntry.getIssueIdFromUri(uri);

        String selection = sIssueAssetByIssueSelection;
        String[] selectionArgs = new String[]{Integer.toString(issueId)};

        return sIssueAssetWorkplaceByIssueQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /**
     * Setup of query to ask all IssueAssets for certain parameter IssueId
     * and check if IMAGE_LOCATION="IMG" (when Issue contains an image on server)
     */
    private Cursor getIssueAssetByIssueIdAndImg(Uri uri, String[] projection, String sortOrder) {
        int issueId = IssueContract.IssueAssetEntry.getIssueIdFromImgUri(uri);
        Log.v(LOG_TAG, "IssueId to query getIssueAssetByIssueIdAndImg = " + issueId);

        String selection = sIssueAssetByIssueAndImgSelection;
        String[] selectionArgs = new String[]{"IMG", Integer.toString(issueId)};

        return sIssueAssetWorkplaceByIssueQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /**
     * Get the Type of the URI
     *
     * @param uri
     * @return String representing the type
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ISSUE:
                return IssueContract.IssueEntry.CONTENT_TYPE;
            case ISSUE_WITH_ID:
                return IssueContract.IssueEntry.CONTENT_ITEM_TYPE;
            case ISSUE_ASSET:
                return IssueContract.IssueAssetEntry.CONTENT_TYPE;
            case ISSUE_ASSET_WITH_ISSUE_ID:
                return IssueContract.IssueAssetEntry.CONTENT_TYPE;
            case TRAINCOACH:
                return IssueContract.TraincoachEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Method to insert in the database
     *
     * @param uri defines on which table to execute insert
     * @param contentValues the values to insert in corresponding column
     * @return URI on which the insert is executed
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ISSUE: {
                long _id = db.insert(IssueContract.IssueEntry.TABLE_NAME, null, contentValues);
                if (_id > 0)
                    returnUri = IssueContract.IssueEntry.buildIssueUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ISSUE_ASSET: {
                long _id = db.insert(IssueContract.IssueAssetEntry.TABLE_NAME, null, contentValues);
                if (_id > 0)
                    returnUri = IssueContract.IssueAssetEntry.buildIssueAssetUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAINCOACH: {
                long _id = db.insert(IssueContract.TraincoachEntry.TABLE_NAME, null, contentValues);
                if (_id > 0)
                    returnUri = IssueContract.TraincoachEntry.buildTraincoachUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    /**
     *
     * @param uri Which table to insert in
     * @param values array of contentValues with corresponding Column
     * @return amount if inserts
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Log.v(LOG_TAG, "entering bulkInsert");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount;

        switch (match) {
            case ISSUE:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(IssueContract.IssueEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case ISSUE_ASSET:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(IssueContract.IssueAssetEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case TRAINCOACH:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(IssueContract.TraincoachEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    /**
     * Method to delete rows from tables in database.
     * @param uri which table to delete in
     * @param selection where clause
     * @param selectionArgs arguments of where clause
     * @return amount of rows deleted
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case ISSUE:
                rowsDeleted = db.delete(
                        IssueContract.IssueEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ISSUE_ASSET:
                rowsDeleted = db.delete(
                        IssueContract.IssueAssetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAINCOACH:
                rowsDeleted = db.delete(
                        IssueContract.TraincoachEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        Log.v(LOG_TAG, "ROWS DELETED = " + rowsDeleted);
        return rowsDeleted;
    }

    /**
     * Method to update rows in the database.
     * @param uri which table to update in
     * @param contentValues contentValues with corresponding Columns to update
     * @param selection where clause
     * @param selectionArgs arguments in where clause
     * @return amount of rows updated
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ISSUE:
                rowsUpdated = db.update(
                        IssueContract.IssueEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case ISSUE_WITH_ID:
                rowsUpdated = db.update(
                        IssueContract.IssueEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case ISSUE_ASSET:
                rowsUpdated = db.update(
                        IssueContract.IssueAssetEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case ISSUE_ASSET_WITH_ISSUE_ID:
                rowsUpdated = db.update(
                        IssueContract.IssueAssetEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case TRAINCOACH:
                rowsUpdated = db.update(
                        IssueContract.TraincoachEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        Log.v(LOG_TAG, rowsUpdated + " rows updated!");
        return rowsUpdated;
    }
}
