package televic.project.kuleuven.televicmechanicassistant;

import android.os.AsyncTask;

import java.util.Observable;

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

    public void parseIssueJSON(String jsonResponse) {
        //The names of the REST JSON attributes
        final String WORKPLACE = "workplace";
        final String STATUS = "status";
        final String TRAINCOACH = "traincoach";
        final String DESCR = "descr";

        //Each element in the list represents a listItem/row in the ListView
        //Each String-column respectivly represents the attribute from that listItem
        List<IssueOverviewRowitem> result = null;
        //String response to JSONArray
        try {
            JSONArray listitems = new JSONArray(response);
            result = new ArrayList<>(listitems.length());

            //Parsing JSON to result
            IssueOverviewRowitem oneItemData;
            for (int listItemIndex = 0; listItemIndex < listitems.length(); listItemIndex++) {
                Log.v(LOG_TAG, "Item " + listItemIndex + " being parsed ");
                oneItemData = new IssueOverviewRowitem();
                JSONObject oneItemJSON = listitems.getJSONObject(listItemIndex);

                //Parsing data in fixed sequential order
                oneItemData.setWorkplace(oneItemJSON.getString(WORKPLACE));
                oneItemData.setStatus(oneItemJSON.getString(STATUS));
                oneItemData.setTraincoach(oneItemJSON.getString(TRAINCOACH));
                oneItemData.setDescription(oneItemJSON.getString(DESCR));

                //One ListItem filled with data, added to the list of ListItems
                result.add(oneItemData);
                Log.v(LOG_TAG, "Item " + listItemIndex + " result:\n"
                        + "String[0]=" + result.get(listItemIndex).getWorkplace() + "\n"
                        + "String[1]=" + result.get(listItemIndex).getStatus() + "\n"
                        + "String[2]=" + result.get(listItemIndex).getTraincoach() + "\n"
                        + "String[3]=" + result.get(listItemIndex).getDescription());
            }
        } catch (JSONException e) {
            Log.w(LOG_TAG, "Cannot convert to JSONArray: " + response);
        }

        if(result==null) {
            //String response to JSONObject (when only 1 item returned)
            try {
                JSONObject oneItemJSON = new JSONObject(response);
                result = new ArrayList<>();

                IssueOverviewRowitem oneItemData;
                Log.v(LOG_TAG, "Item " + 0 + " being parsed ");
                oneItemData = new IssueOverviewRowitem();

                //Parsing data in fixed sequential order
                oneItemData.setWorkplace(oneItemJSON.getString(WORKPLACE));
                oneItemData.setStatus(oneItemJSON.getString(STATUS));
                oneItemData.setTraincoach(oneItemJSON.getString(TRAINCOACH));
                oneItemData.setDescription(oneItemJSON.getString(DESCR));

                //One ListItem filled with data, added to the list of ListItems
                result.add(oneItemData);
                Log.v(LOG_TAG, "Item " + 0 + " result:\n"
                        + "String[0]=" + result.get(0).getWorkplace() + "\n"
                        + "String[1]=" + result.get(0).getStatus() + "\n"
                        + "String[2]=" + result.get(0).getTraincoach() + "\n"
                        + "String[3]=" + result.get(0).getDescription());

            } catch (JSONException e) {
                Log.w(LOG_TAG, "Cannot convert to JSONObject: " + response);
            }
        }

        if (result != null) {
            listItems = result;
            Log.v(LOG_TAG, "JSON PARSED");
        } else {
            Log.e(LOG_TAG, "JSON PARSING FAILED");
            listItems = new ArrayList<>();
        }

        return new ArrayList<IssueOverviewRowitem>();
        //return result

    }

    public void parseWorkplaceJSON(String jsonResponse) {

    }
}
