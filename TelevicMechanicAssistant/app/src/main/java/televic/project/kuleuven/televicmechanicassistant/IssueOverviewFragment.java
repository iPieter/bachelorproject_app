package televic.project.kuleuven.televicmechanicassistant;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

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
        View rootView = inflater.inflate(R.layout.fragment_issue_overview, container, false);

        //Setting up adapter
        mOverviewListAdapter = new OverviewListAdapter(this.getContext());
        setListAdapter(mOverviewListAdapter);


        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this.getContext());
        progressBar.setId(R.id.progressbar_loading);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        ListView listView = (ListView) rootView.findViewById(android.R.id.list);
        listView.setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup rootGroup = (ViewGroup) rootView.findViewById(android.R.id.content);
        rootGroup.addView(progressBar);
        Log.v(LOG_TAG, "Progressbar is set!");

        //Calling backend (default= active issues called)
        fetchIssueData(RESTSingleton.ACTIVE_ISSUES_PARAM);

        Log.v(LOG_TAG, "onCreateView Ended");
        return rootView;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this.getActivity(), IssueDetailActivity.class);
        //TODO Extract from clicked view
        startActivity(intent);
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
            String url = "http://10.108.0.132:8088";

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
                            String testString1 = "[]";

                            String testString2 = "{\n" +
                                    "    \"workplace\" : \"test\",\n" +
                                    "    \"status\": \"ASSIGNED\",\n" +
                                    "    \"traincoach\": \"MATTREIN - WAGONTJE\",\n" +
                                    "    \"descr\": \"Er is een trillingkje.\"\n" +
                                    "    },\n";

                            String testString3 = "[{\n" +
                                    "    \"workplace\" : \"test\",\n" +
                                    "    \"status\": \"ASSIGNED\",\n" +
                                    "    \"traincoach\": \"MATTREIN - WAGONTJE\",\n" +
                                    "    \"descr\": \"Er is een trillingkje.\"\n" +
                                    "    },\n" +
                                    "{\n" +
                                    "    \"workplace\" : \"test2\",\n" +
                                    "    \"status\": \"ASSIGNED2\",\n" +
                                    "    \"traincoach\": \"MATTREIN - WAGONTJE2\",\n" +
                                    "    \"descr\": \"Er is een trillingkje2.\"  \n" +
                                    "}]";

                            try {
                                JSONParser parser = new JSONParser();
                                parser.execute(testString3);
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

    public void setEmptyText(String text) {
        TextView textView = (TextView) getView().findViewById(android.R.id.empty);
        textView.setText(text);
    }

    public void removeProgressBar(){
        Log.v(LOG_TAG,"Trying to remove the progressbar");
        try {
            ListView listView = (ListView) getListView().findViewById(android.R.id.list);
            ProgressBar progressBar = (ProgressBar) listView.findViewById(R.id.progressbar_loading);
            listView.removeViewInLayout(progressBar);
        }catch (Exception e){
            Log.e(LOG_TAG,e.toString());
        }
    }

    public class JSONParser extends AsyncTask<String, Void, List<IssueOverviewRowitem>> {

        @Override
        protected List<IssueOverviewRowitem> doInBackground(String... jsonArrays) {
            String response = jsonArrays[0];
            Log.v(LOG_TAG, "Entering JSONParser doInBackground Task. ROOT JSONARRAY=" + response);

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
                Log.v(LOG_TAG, "JSON PARSED");
            } else {
                Log.e(LOG_TAG, "JSON PARSING FAILED");
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<IssueOverviewRowitem> result) {
            super.onPostExecute(result);
            if (result.size() == 0) {
                setEmptyText(getString(R.string.item_issue_overview_dataempty));
                removeProgressBar();
            }
            mOverviewListAdapter.updateView(result);
        }
    }
}