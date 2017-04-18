package televic.project.kuleuven.televicmechanicassistant.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Matthias on 18/04/2017.
 */

public class IssueProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private IssueDbHelper mOpenHelper;

    //The Codes for the UriMatcher, corresponding with a certain path
    static final int ISSUE = 100;
    static final int ISSUE_WITH_ID = 101;
    static final int ISSUE_ASSET_WITH_ISSUE_ID = 300;

    private static final SQLiteQueryBuilder sIssueAssetByIssueQueryBuilder;

    static{
        sIssueAssetByIssueQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //issue INNER JOIN issue_asset ON issue_asset.issue_id = issue._id
        sIssueAssetByIssueQueryBuilder.setTables(
                IssueContract.IssueEntry.TABLE_NAME + " INNER JOIN " +
                        IssueContract.IssueAssetEntry.TABLE_NAME +
                        " ON " + IssueContract.IssueAssetEntry.TABLE_NAME +
                        "." + IssueContract.IssueAssetEntry.COLUMN_ISSUE_ID +
                        " = " + IssueContract.IssueEntry.TABLE_NAME +
                        "." + IssueContract.IssueEntry._ID);
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = IssueContract.CONTENT_AUTHORITY;

        // For each new URI path, create corresponding code.
        matcher.addURI(authority, IssueContract.PATH_ISSUE, ISSUE);
        matcher.addURI(authority, IssueContract.PATH_ISSUE + "/*", ISSUE_WITH_ID);
        matcher.addURI(authority, IssueContract.PATH_ISSUE_ASSET+ "/*", ISSUE_ASSET_WITH_ISSUE_ID);

        return matcher;
    }

    //Issue._ID = ?
    private static final String sIssueByIdSelection =
            IssueContract.IssueEntry.TABLE_NAME+
                    "." + IssueContract.IssueEntry._ID + " = ? ";

    //IssueAsset.issueId = ?
    private static final String sIssueAssetByIssueSelection =
            IssueContract.IssueAssetEntry.TABLE_NAME+
                    "." + IssueContract.IssueAssetEntry.COLUMN_ISSUE_ID + " = ? ";

    @Override
    public boolean onCreate() {
        mOpenHelper = new IssueDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "issue"
            case ISSUE:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        IssueContract.IssueEntry.TABLE_NAME,
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
                retCursor = getIssueById(uri, projection, sortOrder);
                break;
            }
            // "issue_asset/*"
            case ISSUE_ASSET_WITH_ISSUE_ID: {
                retCursor = getIssueAssetByIssueId(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    //Setup of query to fetch an Issue in Issue Table with specified parameter _ID
    private Cursor getIssueById(Uri uri, String[] projection, String sortOrder) {
        int issueId = IssueContract.IssueEntry.getIssueIdFromUri(uri);

        String selection = sIssueByIdSelection;
        String[] selectionArgs = new String[]{Integer.toString(issueId)};

        return sIssueAssetByIssueQueryBuilder.query(mOpenHelper.getReadableDatabase(),
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

        return sIssueAssetByIssueQueryBuilder.query(mOpenHelper.getReadableDatabase(),
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
            case ISSUE_ASSET_WITH_ISSUE_ID:
                return IssueContract.IssueAssetEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
