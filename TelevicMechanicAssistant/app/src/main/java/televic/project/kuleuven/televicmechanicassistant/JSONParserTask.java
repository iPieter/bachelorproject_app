package televic.project.kuleuven.televicmechanicassistant;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Vector;

import televic.project.kuleuven.televicmechanicassistant.data.IssueContract;


/**
 * Created by Matthias on 19/04/2017.
 */

public class JSONParserTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = JSONParserTask.class.getSimpleName();

    //TestStrings For REST
    //TODO DELETE STRINGS
    static final String testString1 = "[]";

    public JSONParserTask() {
    }

    @Override
    protected Void doInBackground(String... strings) {
        String issueStringResponse = strings[0];
        String workplaceStringResponse = strings[1];

        parseIssueJSON(issueStringResponse);
        parseWorkplaceJSON(workplaceStringResponse);

        return null;
    }

    public static final String COLUMN_WORKPLACE_ID = "workplace_id";
    public static final String COLUMN_WORKPLACE_NAME = "workplace_name";

    public void parseIssueJSON(String jsonResponse) {
        //The names of the REST JSON attributes

        //for each Issue
        final String ID = "id";
        final String DESCRIPTION = "descr";
        final String STATUS = "status";
        final String ASSETS = "assets";
        final String MECHANIC = "mechanic";
        final String OPERATOR = "operator";
        final String DATA = "data";
        final String ASSIGNED_TIME = "assignedTime";
        final String IN_PROGRESS_TIME = "inProgressTime";
        final String CLOSED_TIME = "closedTime";
        final String USER = "user";
        final String NAME = "name";
        final String TRAINCOACH = "traincoach";
        final String TYPE="type";

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

        //Values needed for IssueAsset Table
        String asset_description;
        String post_time;
        String asset_user_name;
        String asset_user_email;
        int asset_issue_id;

        Vector<ContentValues> issueVector = null;
        Vector<ContentValues> issueAssetVector = null;

        try {
            //Will cast Exception if only on item is present
            JSONArray issues = new JSONArray(jsonResponse);
            issueVector = new Vector<ContentValues>();
            issueAssetVector = new Vector<ContentValues>();

            //Iterate Issues
            for (int issueIndex = 0; issueIndex < issues.length(); issueIndex++) {
                Log.v(LOG_TAG, "Item " + issueIndex + " being parsed ");
                JSONObject issue = issues.getJSONObject(issueIndex);

                issue_id = issue.getInt(ID);
                issue_description = issue.getString(DESCRIPTION);
                issue_status = issue.getString(STATUS);

                //Iterate IssueAssets of Issue
                try{
                    JSONArray assets = issue.getJSONArray(ASSETS);
                    for(int assetIndex=0;assetIndex<assets.length();assetIndex++){

                    }
                }catch(JSONException e1){
                    //When Assets only contains 1 entry
                    try {
                        JSONObject asset = issue.getJSONObject(ASSETS);
                    }catch(JSONException e2){
                        Log.e(LOG_TAG, "Cannot convert assets: "+jsonResponse);
                    }
                }

                JSONObject operator = issue.getJSONObject(OPERATOR);
                issue_operator = operator.getString(NAME);

                JSONObject data = issue.getJSONObject(DATA);
                issue_dataId= data.getInt(ID);

                JSONObject traincoach = data.getJSONObject(TRAINCOACH);
                traincoach_id = traincoach.getInt(ID);
                traincoach_name=traincoach.getString(NAME);
                traincoach_type=traincoach.getString(TYPE);

                assigned_time = issue.getString(ASSIGNED_TIME);
                in_progress_time = issue.getString(IN_PROGRESS_TIME);
                closed_time = issue.getString(CLOSED_TIME);

                //Adding all parsed values to the contentValues
                ContentValues issueContentValues = new ContentValues();

                issueContentValues.put(IssueContract.IssueEntry._ID,issue_id);
                issueContentValues.put(IssueContract.IssueEntry.COLUMN_DESCRIPTION,issue_description);
                issueContentValues.put(IssueContract.IssueEntry.COLUMN_STATUS,issue_status);
                issueContentValues.put(IssueContract.IssueEntry.COLUMN_OPERATOR,issue_operator);
                issueContentValues.put(IssueContract.IssueEntry.COLUMN_DATA_ID,issue_dataId);
                issueContentValues.put(IssueContract.IssueEntry.COLUMN_ASSIGNED_TIME,assigned_time);
                issueContentValues.put(IssueContract.IssueEntry.COLUMN_IN_PROGRESS_TIME,in_progress_time);
                issueContentValues.put(IssueContract.IssueEntry.COLUMN_CLOSED_TIME,closed_time);
                issueContentValues.put(IssueContract.IssueEntry.COLUMN_TRAINCOACH_NAME,traincoach_type +" - "+traincoach_name);
                issueContentValues.put(IssueContract.IssueEntry.COLUMN_TRAINCOACH_ID,traincoach_id);
            }
        } catch (JSONException e) {
            Log.w(LOG_TAG, "Cannot convert Issues to JSONArray: " + jsonResponse);
            //String response to JSONObject (when only 1 item returned)
            try{

            }catch (JSONException e) {
                Log.e(LOG_TAG, "Cannot convert Issues to JSONObject: " + jsonResponse);
            }
        }

        if (issueVector != null) {
            Log.v(LOG_TAG, "JSON PARSED");
        } else {
            Log.e(LOG_TAG, "JSON PARSING FAILED");
        }
    }

    public void parseWorkplaceJSON(String jsonResponse) {

    }
}
