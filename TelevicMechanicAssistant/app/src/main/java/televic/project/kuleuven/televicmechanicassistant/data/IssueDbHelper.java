package televic.project.kuleuven.televicmechanicassistant.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Matthias on 18/04/2017.
 * Helps managing the Issue Database
 */


public class IssueDbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = IssueDbHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 3;
    static final String DATABASE_NAME = "bachelorproject.db";

    public IssueDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.v(LOG_TAG, "Constructor: created IssueDbHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.v(LOG_TAG, "entered onCreate");

        //Create Issue Table
        final String SQL_CREATE_ISSUE_TABLE = "CREATE TABLE " +
                IssueContract.IssueEntry.TABLE_NAME + " (" +
                IssueContract.IssueEntry._ID + " INTEGER PRIMARY KEY," +
                IssueContract.IssueEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                IssueContract.IssueEntry.COLUMN_STATUS + " TEXT NOT NULL, " +
                IssueContract.IssueEntry.COLUMN_OPERATOR + " TEXT NOT NULL, " +
                IssueContract.IssueEntry.COLUMN_DATA_ID + " INTEGER NOT NULL, " +
                IssueContract.IssueEntry.COLUMN_ASSIGNED_TIME + " DATETIME NOT NULL, " +
                IssueContract.IssueEntry.COLUMN_IN_PROGRESS_TIME + " DATETIME, " +
                IssueContract.IssueEntry.COLUMN_CLOSED_TIME + " DATETIME, " +
                IssueContract.IssueEntry.COLUMN_TRAINCOACH_NAME + " TEXT NOT NULL, " +
                IssueContract.IssueEntry.COLUMN_TRAINCOACH_ID + " INTEGER NOT NULL " +
                " );";

        //Create IssueAsset Table
        final String SQL_CREATE_ISSUE_ASSET_TABLE = "CREATE TABLE " +
                IssueContract.IssueAssetEntry.TABLE_NAME + " (" +
                IssueContract.IssueAssetEntry._ID + " INTEGER PRIMARY KEY," +
                IssueContract.IssueAssetEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                IssueContract.IssueAssetEntry.COLUMN_IMAGE_LOCATION + " TEXT, " +
                IssueContract.IssueAssetEntry.COLUMN_POST_TIME + " DATETIME NOT NULL, " +
                IssueContract.IssueAssetEntry.COLUMN_USER_NAME + " TEXT NOT NULL, " +
                IssueContract.IssueAssetEntry.COLUMN_USER_EMAIL + " TEXT NOT NULL, " +
                IssueContract.IssueAssetEntry.COLUMN_ISSUE_ID + " INTEGER NOT NULL, " +

                " FOREIGN KEY (" + IssueContract.IssueAssetEntry.COLUMN_ISSUE_ID +
                ") REFERENCES " + IssueContract.IssueEntry.TABLE_NAME +
                " (" + IssueContract.IssueEntry._ID + ") " +

                " );";

        //Create Traincoach Table
        final String SQL_CREATE_TRAINCOACH_TABLE = "CREATE TABLE " +
                IssueContract.TraincoachEntry.TABLE_NAME + " (" +
                IssueContract.TraincoachEntry._ID + " INTEGER PRIMARY KEY," +
                IssueContract.TraincoachEntry.COLUMN_WORKPLACE_ID + " INTEGER NOT NULL, " +
                IssueContract.TraincoachEntry.COLUMN_WORKPLACE_NAME + " TEXT NOT NULL " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_ISSUE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_ISSUE_ASSET_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAINCOACH_TABLE);

        Log.v(LOG_TAG, "Leaving onCreate: SQL CREATE TABLES executed");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //Database only serves as cache, so onUpgrade drop table
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + IssueContract.IssueEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + IssueContract.IssueAssetEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + IssueContract.TraincoachEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
