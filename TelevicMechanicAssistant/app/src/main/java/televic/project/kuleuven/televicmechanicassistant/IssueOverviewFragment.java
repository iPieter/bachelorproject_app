package televic.project.kuleuven.televicmechanicassistant;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthias on 29/03/2017.
 */

public class IssueOverviewFragment extends ListFragment {
    private final String LOG_TAG = IssueOverviewFragment.class.getSimpleName();

    private OverviewListAdapter mOverviewListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        Log.v(LOG_TAG, "onCreate method ended");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView");

        //Setting up adapter
        mOverviewListAdapter = new OverviewListAdapter(this.getContext());
        setListAdapter(mOverviewListAdapter);

        //Calling backend (default= active issues called)
        fetchIssueData(RESTSingleton.ACTIVE_ISSUES_PARAM);

        Log.v(LOG_TAG, "onCreateView Ended");
        return inflater.inflate(R.layout.fragment_issue_overview, container, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        /*
        Intent intent = new Intent(this.getActivity(), IssueDetailActivity.class);
        String traincoachId = "1"; //TODO Extract from clicked view
        intent.putExtra(TRAINCOACH_ID, traincoachId);
        startActivity(intent);
        */
    }

    /**
     * NETWORKING
     **/
    //No need for AsyncTask: volley takes care of networking on networking thread
    protected void fetchIssueData(String issueMode) {
        Log.v(LOG_TAG, "Entering fetchIssueData");
        //Fetching the JSON file from server through REST
        try {
            //String url = RESTSingleton.BASE_URL + RESTSingleton.OVERVIEW_PARAM + issueMode;
            //test on Node.js server:
            String url = "http://192.168.0.213:3000";

            //Creating JsonObjectRequest for REST call

            //Unsure if getting a JSONArray or JSONObject, So we use the StringRequest
            StringRequest jsObjRequest = new StringRequest
                    (Request.Method.GET, url, new Response.Listener<String>() {

                        public void onResponse(String response) {
                            VolleyLog.v(LOG_TAG, "JSONObject response from REST:" + response);
                            JSONParser parser = new JSONParser();
                            parser.execute(response);
                        }
                    }, new Response.ErrorListener() {

                        public void onErrorResponse(VolleyError error) {

                            //TODO TEST BEGIN
                            String testString = "[]";

                            try {
                                JSONParser parser = new JSONParser();
                                parser.execute(testString);
                            } catch (Exception e) {
                                e.fillInStackTrace();
                                Log.e(LOG_TAG, e.toString());
                            }
                            //TODO TEST END

                            error.fillInStackTrace();
                            VolleyLog.e("Error in RESTSingleton request:" + error.networkResponse);
                        }
                    });

            //Singleton handles call to REST
            Log.v(LOG_TAG, "Calling RESTSingleton with context:" + this.getContext().getApplicationContext().toString());
            RESTSingleton.getInstance(this.getContext().getApplicationContext())
                    .addToRequestQueue(jsObjRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Failed REST fetch");
        }
        Log.v(LOG_TAG, "Ending fetchIssueData: REST JSONRequest is now handed to singleton");
    }

    public class JSONParser extends AsyncTask<String, Void, List<String[]>> {

        @Override
        protected List<String[]> doInBackground(String... jsonArrays) {
            String response = jsonArrays[0];
            Log.v(LOG_TAG, "Entering JSONParser doInBackground Task. ROOT JSONARRAY=" + response);

            //The names of the REST JSON attributes
            final int DATA_ITEM_COUNT = 4;                  //WARNING: addapt count to amount of Strings
            final String WORKPLACE = "workplace";
            final String STATUS = "status";
            final String TRAINCOACH = "traincoach";
            final String DESCR = "descr";

            //Each element in the list represents a listItem/row in the ListView
            //Each String-column respectivly represents the attribute from that listItem
            List<String[]> result = null;
            //String response to JSONArray
            try {
                JSONArray listitems = new JSONArray(response);
                result = new ArrayList<>(listitems.length());

                //Parsing JSON to result
                String[] oneItemData;
                for (int listItemIndex = 0; listItemIndex < listitems.length(); listItemIndex++) {
                    Log.v(LOG_TAG, "Item " + listItemIndex + " being parsed ");
                    oneItemData = new String[DATA_ITEM_COUNT];
                    JSONObject oneItemJSON = listitems.getJSONObject(listItemIndex);

                    //Parsing data in fixed sequential order
                    oneItemData[0] = oneItemJSON.getString(WORKPLACE);
                    oneItemData[1] = oneItemJSON.getString(STATUS);
                    oneItemData[2] = oneItemJSON.getString(TRAINCOACH);
                    oneItemData[3] = oneItemJSON.getString(DESCR);

                    //One ListItem filled with data, added to the list of ListItems
                    result.add(oneItemData);
                    Log.v(LOG_TAG, "Item " + listItemIndex + " result:\n"
                            + "String[0]=" + result.get(listItemIndex)[0] + "\n"
                            + "String[1]=" + result.get(listItemIndex)[1] + "\n"
                            + "String[2]=" + result.get(listItemIndex)[2] + "\n"
                            + "String[3]=" + result.get(listItemIndex)[3]);
                }
            } catch (JSONException e) {
                Log.w(LOG_TAG, "Cannot convert to JSONArray: " + response);
            }

            //String response to JSONObject (when only 1 item returned)
            try {
                JSONObject oneItemJSON = new JSONObject(response);
                result = new ArrayList<>();

                String[] oneItemData;
                Log.v(LOG_TAG, "Item " + 0 + " being parsed ");
                oneItemData = new String[DATA_ITEM_COUNT];

                //Parsing data in fixed sequential order
                oneItemData[0] = oneItemJSON.getString(WORKPLACE);
                oneItemData[1] = oneItemJSON.getString(STATUS);
                oneItemData[2] = oneItemJSON.getString(TRAINCOACH);
                oneItemData[3] = oneItemJSON.getString(DESCR);

                //One ListItem filled with data, added to the list of ListItems
                result.add(oneItemData);
                Log.v(LOG_TAG, "Item " + 0 + " result:\n"
                        + "String[0]=" + result.get(0)[0] + "\n"
                        + "String[1]=" + result.get(0)[1] + "\n"
                        + "String[2]=" + result.get(0)[2] + "\n"
                        + "String[3]=" + result.get(0)[3]);

            } catch (JSONException e) {
                Log.w(LOG_TAG, "Cannot convert to JSONObject: " + response);
            }

            if (result != null) {
                Log.v(LOG_TAG, "JSON PARSED");
            } else {
                Log.e(LOG_TAG, "JSON PARSING FAILED");
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<String[]> result) {
            super.onPostExecute(result);
            mOverviewListAdapter.updateView(result);
        }
    }
}