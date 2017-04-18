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
    static final int ISSUE_ASSET = 300;

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
        matcher.addURI(authority, IssueContract.PATH_ISSUE_ASSET, ISSUE_ASSET);
        
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new IssueDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        //TODO adapt to db
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);
/*
        switch (match) {
            // Student: Uncomment and fill out these two cases
//            case WEATHER_WITH_LOCATION_AND_DATE:
//            case WEATHER_WITH_LOCATION:
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
                */
            return null;
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
