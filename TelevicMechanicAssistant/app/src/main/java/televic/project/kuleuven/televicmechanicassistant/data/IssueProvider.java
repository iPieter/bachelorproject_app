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
 * Created by Matthias on 18/04/2017.
 */

public class IssueProvider extends ContentProvider {
    private static final String LOG_TAG = IssueProvider.class.getSimpleName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private IssueDbHelper mOpenHelper;

    //The Codes for the UriMatcher, corresponding with a certain path
    static final int ISSUE = 100;
    static final int ISSUE_WITH_ID = 101;
    static final int ISSUE_ASSET = 300;
    static final int ISSUE_ASSET_WITH_ISSUE_ID = 301;
    static final int TRAINCOACH = 400;

    private static final SQLiteQueryBuilder sIssueAssetWorkplaceByIssueQueryBuilder;

    static {
        Log.v(LOG_TAG, "Creating SQLiteQueryBuilder!");
        sIssueAssetWorkplaceByIssueQueryBuilder = new SQLiteQueryBuilder();

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
        Log.v(LOG_TAG, "SQLiteQueryBuilder builded!");
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = IssueContract.CONTENT_AUTHORITY;

        // For each new URI path, create corresponding code.
        matcher.addURI(authority, IssueContract.PATH_ISSUE, ISSUE);
        matcher.addURI(authority, IssueContract.PATH_ISSUE + "/*", ISSUE_WITH_ID);
        matcher.addURI(authority, IssueContract.PATH_ISSUE_ASSET, ISSUE_ASSET);
        matcher.addURI(authority, IssueContract.PATH_ISSUE_ASSET + "/*", ISSUE_ASSET_WITH_ISSUE_ID);
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

    @Override
    public boolean onCreate() {
        Log.v(LOG_TAG, "entered onCreate");
        mOpenHelper = new IssueDbHelper(getContext());
        Log.v(LOG_TAG, "leaving onCreate: IssueProvider created!");
        return true;
    }

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
                Log.v(LOG_TAG, "QUERY: case ISSUE with Uri = " + uri +", projection = "+ projection
                +",selection="+selection+",selectionArgs="+selectionArgs+",sortOrder="+sortOrder);

                retCursor = sIssueAssetWorkplaceByIssueQueryBuilder.query(mOpenHelper.getReadableDatabase(),
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

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        //DEBUG: countRowsInAllTables();
        Log.v(LOG_TAG, "QUERY cursor #rows = " + retCursor.getCount());
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /**
     * DEBUG PURPOSES
     * TODO DELETE
     */
    public void countRowsInAllTables(){
        String[] testprojection={IssueContract.IssueEntry._ID};
        Cursor test1 = mOpenHelper.getReadableDatabase().query(
                IssueContract.IssueEntry.TABLE_NAME,
                testprojection,
                null,
                null,
                null,
                null,
                null
        );
        Log.v(LOG_TAG,"QUERY TESTCURSOR: IssueTable #ROWS="+test1.getCount());

        String[] testprojection2={IssueContract.IssueAssetEntry._ID};
        Cursor test2 = mOpenHelper.getReadableDatabase().query(
                IssueContract.IssueAssetEntry.TABLE_NAME,
                testprojection2,
                null,
                null,
                null,
                null,
                null
        );
        Log.v(LOG_TAG,"QUERY TESTCURSOR: IssueAssetTable #ROWS="+test2.getCount());

        String[] testprojection3={IssueContract.TraincoachEntry._ID};
        Cursor test3 = mOpenHelper.getReadableDatabase().query(
                IssueContract.TraincoachEntry.TABLE_NAME,
                testprojection3,
                null,
                null,
                null,
                null,
                null
        );
        Log.v(LOG_TAG,"QUERY TESTCURSOR: TraincoachTable #ROWS="+test3.getCount());

        //SELECT _id FROM issue INNER JOIN issue_asset ON issue_asset.issue_id = issue._id INNER JOIN traincoach ON issue.traincoach_id = traincoach._id
        String[] testprojection4={IssueContract.IssueEntry._ID};
        Cursor test4 = sIssueAssetWorkplaceByIssueQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                testprojection4,
                null,
                null,
                null,
                null,
                null
        );
        Log.v(LOG_TAG,"QUERY TESTCURSOR: JOINED TABLES #ROWS="+test4.getCount());
    }

    //Setup of query to fetch an Issue in Issue Table with specified parameter _ID
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

    //Setup of query to ask all IssueAssets for certain parameter IssueId
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
        return rowsDeleted;
    }

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
            case ISSUE_ASSET:
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
        return rowsUpdated;
    }
}
