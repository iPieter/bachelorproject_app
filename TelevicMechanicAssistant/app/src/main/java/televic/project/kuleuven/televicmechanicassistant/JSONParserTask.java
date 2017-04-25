package televic.project.kuleuven.televicmechanicassistant;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

import televic.project.kuleuven.televicmechanicassistant.data.IssueContract;
import televic.project.kuleuven.televicmechanicassistant.data.IssueDbHelper;


/**
 * Created by Matthias on 19/04/2017.
 */

public class JSONParserTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = JSONParserTask.class.getSimpleName();
    private Context mContext;
    private Vector<ContentValues> mIssueVector = null;
    private Vector<ContentValues> mIssueAssetVector = null;
    private Vector<ContentValues> mWorkplaceVector = null;

    //for each Issue/IssueAsset in StringResponse
    private final String ID = "id";
    private final String DESCRIPTION = "descr";
    private final String STATUS = "status";
    private final String ASSETS = "assets";
    private final String OPERATOR = "operator";
    private final String DATA = "data";
    private final String ASSIGNED_TIME = "assignedTime";
    private final String IN_PROGRESS_TIME = "inProgressTime";
    private final String CLOSED_TIME = "closedTime";
    private final String USER = "user";
    private final String NAME = "name";
    private final String TRAINCOACH = "traincoach";
    private final String TRAINCOACHES = "traincoaches";
    private final String TYPE = "type";
    private final String TIME = "time";
    private final String EMAIL = "email";
    private final String LOCATION = "location";

    public JSONParserTask(Context context) {
        this.mContext = context;
        this.mIssueVector = new Vector<>();
        this.mIssueAssetVector = new Vector<>();
        this.mWorkplaceVector = new Vector<>();
    }

    @Override
    protected Void doInBackground(String... strings) {
        Log.v(LOG_TAG, "START JSONPARSING background task");

        String issueStringResponse = strings[0];
        String workplaceStringResponse = strings[1];

        parseIssueJSON(issueStringResponse);
        writeIssuesToDatabase();
        writeIssueAssetsToDatabase();

        parseWorkplaceJSON(workplaceStringResponse);
        writeWorkplacesToDatabase();

        Log.v(LOG_TAG, "COMPLETED JSONPARSING background task");
        return null;
    }

    /**
     * Parsing the Issue JSON obtained from REST
     * IssueAsset Parsing is called internally in the IssueParsing
     * => see parseSingleIssue(JSONObject issue)
     * Adding ContentValues to Vector. This vector is used to bulkInsert into DataBase
     *
     * @param jsonResponse String response from Volley
     */
    public void parseIssueJSON(String jsonResponse) {
        Log.d(LOG_TAG, "entering parseIssueJSON");
        try {
            //Will cast Exception if only on item is present
            JSONArray issues = new JSONArray(jsonResponse);

            //Iterate Issues
            for (int issueIndex = 0; issueIndex < issues.length(); issueIndex++) {
                Log.d(LOG_TAG, "Item " + issueIndex + " being parsed ");
                JSONObject issue = issues.getJSONObject(issueIndex);

                ContentValues issueContentValues = parseSingleIssue(issue);
                mIssueVector.add(issueContentValues);
            }
        } catch (JSONException e1) {
            Log.w(LOG_TAG, "Cannot convert Issues to JSONArray: " + jsonResponse);
            //String response to JSONObject (when only 1 item returned)
            try {
                JSONObject issue = new JSONObject(jsonResponse);

                ContentValues issueContentValues = parseSingleIssue(issue);
                mIssueVector.add(issueContentValues);
            } catch (JSONException e2) {
                Log.e(LOG_TAG, "Cannot convert Issues to JSONObject: " + jsonResponse);
            }
        }
        Log.d(LOG_TAG, "Leaving parseIssueJSON");
    }

    /**
     * Fetching contentValues of 1 issue from all issues in Stringresponse
     * Also parsing issueAssets of this single Issue
     *
     * @param issue single issue
     * @return ContentValues of 1 issue from all issues in Stringresponse
     */
    public ContentValues parseSingleIssue(JSONObject issue) {
        //Values needed for Issue Table
        int issue_id;
        String issue_description;
        String issue_status;
        String issue_operator;
        int issue_dataId;
        String assigned_time;
        String in_progress_time;
        String closed_time;
        String traincoach_name;
        String traincoach_type;
        int traincoach_id;
        ContentValues issueContentValues = new ContentValues();

        try {
            issue_id = issue.getInt(ID);
            issue_description = issue.getString(DESCRIPTION);
            issue_status = issue.getString(STATUS);

            parseIssueAssetJSON(issue);

            JSONObject operator = issue.getJSONObject(OPERATOR);
            issue_operator = operator.getString(NAME);

            JSONObject data = issue.getJSONObject(DATA);
            issue_dataId = data.getInt(ID);

            JSONObject traincoach = data.getJSONObject(TRAINCOACH);
            traincoach_id = traincoach.getInt(ID);
            traincoach_name = traincoach.getString(NAME);
            traincoach_type = traincoach.getString(TYPE);

            assigned_time = issue.getString(ASSIGNED_TIME);
            in_progress_time = issue.getString(IN_PROGRESS_TIME);
            closed_time = issue.getString(CLOSED_TIME);

            //Adding all parsed values to the contentValues
            issueContentValues.put(IssueContract.IssueEntry._ID, issue_id);
            issueContentValues.put(IssueContract.IssueEntry.COLUMN_DESCRIPTION, issue_description);
            issueContentValues.put(IssueContract.IssueEntry.COLUMN_STATUS, issue_status);
            issueContentValues.put(IssueContract.IssueEntry.COLUMN_OPERATOR, issue_operator);
            issueContentValues.put(IssueContract.IssueEntry.COLUMN_DATA_ID, issue_dataId);
            issueContentValues.put(IssueContract.IssueEntry.COLUMN_ASSIGNED_TIME, assigned_time);
            issueContentValues.put(IssueContract.IssueEntry.COLUMN_IN_PROGRESS_TIME, in_progress_time);
            issueContentValues.put(IssueContract.IssueEntry.COLUMN_CLOSED_TIME, closed_time);
            issueContentValues.put(IssueContract.IssueEntry.COLUMN_TRAINCOACH_NAME, traincoach_type + " - " + traincoach_name);
            issueContentValues.put(IssueContract.IssueEntry.COLUMN_TRAINCOACH_ID, traincoach_id);

            Log.d(LOG_TAG, "Leaving parseSingleIssue: Fetched ContentValues for IssueID = " + issue_id);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "parseSingleIssue FAILED!");
            e.printStackTrace();
        }

        return issueContentValues;
    }

    /**
     * Parsing the IssueAssets from 1 Issue
     * Adding ContentValues to Vector. This vector is used to bulkInsert into DataBase
     *
     * @param issue the single Issue, is JSONObject
     * @return Vector used to insert Contentvalues in DataBase
     */
    public void parseIssueAssetJSON(JSONObject issue) {
        try {
            JSONArray assets = issue.getJSONArray(ASSETS);

            //Iterate IssueAssets of Issue
            for (int assetIndex = 0; assetIndex < assets.length(); assetIndex++) {
                JSONObject asset = assets.getJSONObject(assetIndex);

                ContentValues issueAssetContentValues = parseSingleAsset(asset, issue);
                mIssueAssetVector.add(issueAssetContentValues);
            }
        } catch (JSONException e1) {
            //When Assets only contains 1 entry
            try {
                JSONObject asset = issue.getJSONObject(ASSETS);

                ContentValues issueAssetContentValues = parseSingleAsset(asset, issue);
                mIssueAssetVector.add(issueAssetContentValues);
            } catch (JSONException e2) {
                Log.e(LOG_TAG, "Cannot convert assets: " + issue.toString());
            }
        }

        Log.d(LOG_TAG, "Leaving parseIssueAssetJSON ");
    }

    /**
     * Fetching ContentValues for a single Asset in all assets in JSON Stringresponse
     *
     * @param asset single asset
     * @param issue the parent Issue of the Asset
     * @return ContentValues of a single Asset
     */
    public ContentValues parseSingleAsset(JSONObject asset, JSONObject issue) {
        //Values needed for IssueAsset Table
        int asset_id;
        String asset_description;
        String post_time;
        String imgLocation;
        String asset_user_name;
        String asset_user_email;
        int asset_issue_id;
        ContentValues issueAssetContentValues = new ContentValues();

        try {
            asset_id = asset.getInt(ID);
            asset_description = asset.getString(DESCRIPTION);
            post_time = asset.getString(TIME);
            imgLocation = asset.getString(LOCATION);

            if (imgLocation.equals("")) {
                imgLocation = "NO_IMG";
            } else {
                imgLocation = "IMG";
            }

            JSONObject user = asset.getJSONObject(USER);
            asset_user_name = user.getString(NAME);
            asset_user_email = user.getString(EMAIL);

            asset_issue_id = issue.getInt(ID);

            //Adding all parsed values to the contentValues
            issueAssetContentValues.put(IssueContract.IssueAssetEntry._ID, asset_id);
            issueAssetContentValues.put(IssueContract.IssueAssetEntry.COLUMN_DESCRIPTION, asset_description);
            issueAssetContentValues.put(IssueContract.IssueAssetEntry.COLUMN_POST_TIME, post_time);
            issueAssetContentValues.put(IssueContract.IssueAssetEntry.COLUMN_IMAGE_PRESENT, imgLocation);
            issueAssetContentValues.put(IssueContract.IssueAssetEntry.COLUMN_USER_NAME, asset_user_name);
            issueAssetContentValues.put(IssueContract.IssueAssetEntry.COLUMN_USER_EMAIL, asset_user_email);
            issueAssetContentValues.put(IssueContract.IssueAssetEntry.COLUMN_ISSUE_ID, asset_issue_id);

            Log.d(LOG_TAG, "Leaving parseSingleAsset: Fetched ContentValues for AssetID = " + asset_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return issueAssetContentValues;
    }

    /**
     * Parsing of a single workplace of all workplaces
     *
     * @param jsonResponse raw REST response in String format
     */
    public void parseWorkplaceJSON(String jsonResponse) {
        Log.d(LOG_TAG, "Entering parseWorkplaceJSON");
        try {
            //Will cast Exception if only on item is present
            JSONArray workplaces = new JSONArray(jsonResponse);

            //Iterate Issues
            for (int workplaceIndex = 0; workplaceIndex < workplaces.length(); workplaceIndex++) {
                Log.d(LOG_TAG, "Workplace " + workplaceIndex + " being parsed ");
                JSONObject workplace = workplaces.getJSONObject(workplaceIndex);

                parseSingleWorkplace(workplace);
            }
        } catch (JSONException e1) {
            Log.w(LOG_TAG, "Cannot convert Workplace to JSONArray: " + jsonResponse);
            //String response to JSONObject (when only 1 item returned)
            try {
                JSONObject workplace = new JSONObject(jsonResponse);
                parseSingleWorkplace(workplace);
            } catch (JSONException e2) {
                Log.e(LOG_TAG, "Cannot convert Workplace to JSONObject: " + jsonResponse);
            }
        }

        Log.d(LOG_TAG, "Leaving parseWorkplaceJSON");
    }

    /**
     * Parsing of a single workplace of all workplaces
     * Also adding contentvalues to mWorkplaceVector Vector, used for bulkInsert into Database
     *
     * @param workplace the workplace JSONObject
     * @return ContentValues of a single workplace
     */
    private void parseSingleWorkplace(JSONObject workplace) {
        int workplace_id;
        String workplace_name;

        try {
            JSONArray traincoaches = workplace.getJSONArray(TRAINCOACHES);

            workplace_id = workplace.getInt(ID);
            workplace_name = workplace.getString(NAME);

            for (int traincoachIndex = 0; traincoachIndex < traincoaches.length(); traincoachIndex++) {
                JSONObject traincoach = traincoaches.getJSONObject(traincoachIndex);

                ContentValues contentValues = parseSingleTraincoach(traincoach, workplace_id, workplace_name);
                mWorkplaceVector.add(contentValues);
            }
        } catch (JSONException e1) {
            try {
                JSONObject traincoach = workplace.getJSONObject(TRAINCOACHES);

                workplace_id = workplace.getInt(ID);
                workplace_name = workplace.getString(NAME);

                ContentValues contentValues = parseSingleTraincoach(traincoach, workplace_id, workplace_name);
                mWorkplaceVector.add(contentValues);
            } catch (JSONException e2) {
                e2.printStackTrace();
                Log.e(LOG_TAG, "Traincoach object could not be parsed to JSONObject or JSONArray in parseSingleWorkplace()");
            }
        }
    }

    /**
     * Parsing of a single traincoach of all traincoaches of a single workplace
     *
     * @param traincoach
     * @param workplace_id
     * @param workplace_name
     * @return
     */
    private ContentValues parseSingleTraincoach(JSONObject traincoach, int workplace_id, String workplace_name) {
        int traincoach_id;
        ContentValues contentValues = new ContentValues();
        try {
            traincoach_id = traincoach.getInt(ID);

            contentValues.put(IssueContract.TraincoachEntry._ID, traincoach_id);
            contentValues.put(IssueContract.TraincoachEntry.COLUMN_WORKPLACE_ID, workplace_id);
            contentValues.put(IssueContract.TraincoachEntry.COLUMN_WORKPLACE_NAME, workplace_name);
            Log.d(LOG_TAG, "Leaving parseSingleTraincoach: Fetched ContentValues for traincoachID = " + traincoach_id);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Cannot parse single traincoach in parseSingleTraincoach()!");
        }

        return contentValues;
    }

    /*--- WRTING TO DATABASE ---*/
    public void writeIssuesToDatabase() {
        Log.v(LOG_TAG, "DATABASE TRANSACTION to Issue-Table STARTED");
        int rowsInserted = 0;
        if (mIssueVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[mIssueVector.size()];
            mIssueVector.toArray(cvArray);
            rowsInserted = mContext.getContentResolver().bulkInsert(IssueContract.IssueEntry.CONTENT_URI, cvArray);
        }

        Log.v(LOG_TAG, "DATABASE TRANSACTION to Issue-Table COMPLETE: " + rowsInserted + " rows inserted!");
    }

    public void writeIssueAssetsToDatabase() {
        Log.v(LOG_TAG, "DATABASE TRANSACTION to IssueAsset-Table STARTED");
        int rowsInserted = 0;
        if (mIssueAssetVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[mIssueAssetVector.size()];
            mIssueAssetVector.toArray(cvArray);
            rowsInserted = mContext.getContentResolver().bulkInsert(IssueContract.IssueAssetEntry.CONTENT_URI, cvArray);
        }

        Log.v(LOG_TAG, "DATABASE TRANSACTION to IssueAsset-Table COMPLETE: " + rowsInserted + " rows inserted!");
    }

    public void writeWorkplacesToDatabase() {
        Log.v(LOG_TAG, "DATABASE TRANSACTION to Traincoach-Table STARTED");
        int rowsInserted = 0;
        if (mWorkplaceVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[mWorkplaceVector.size()];
            mWorkplaceVector.toArray(cvArray);
            rowsInserted = mContext.getContentResolver().bulkInsert(IssueContract.TraincoachEntry.CONTENT_URI, cvArray);
        }

        Log.v(LOG_TAG, "DATABASE TRANSACTION to Traincoach-Table COMPLETE: " + rowsInserted + " rows inserted!");
    }
}
