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
import com.android.volley.toolbox.JsonObjectRequest;

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
    private final String TRAINCOACH_ID = "traincoach_id";

    private OverviewListAdapter mOverviewListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        Log.v(LOG_TAG,"onCreate method ended");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(LOG_TAG,"onCreateView");

        //Setting up adapter
        mOverviewListAdapter =new OverviewListAdapter(this.getContext());
        setListAdapter(mOverviewListAdapter);

        //Calling backend (default= active issues called)
        fetchIssueData(RESTSingleton.ACTIVE_ISSUES_PARAM);

        Log.v(LOG_TAG,"onCreateView Ended");
        return inflater.inflate(R.layout.fragment_issue_overview,container,false);
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

/** NETWORKING **/
    //No need for AsyncTask: volley takes care of networking on networking thread
    protected void fetchIssueData(String issueMode) {
        Log.v(LOG_TAG,"Entering fetchIssueData");
        //Fetching the JSON file from server through REST
        try{
            //String url = RESTSingleton.BASE_URL + RESTSingleton.OVERVIEW_PARAM + issueMode;
            //test on Node.js server:
            String url = "http://192.168.0.213:3000";

            //Creating JsonObjectRequest for REST call
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, url,null, new Response.Listener<JSONObject>() {

                        public void onResponse(JSONObject response) {
                                VolleyLog.v(LOG_TAG,"JSONObject response from REST:"+response.toString());
                                JSONParser parser=new JSONParser();
                                parser.execute(response);
                        }
                    }, new Response.ErrorListener() {

                        public void onErrorResponse(VolleyError error) {
                            //TEST BEGIN
                            String testString="{\n" +
                                    "    \"workplace\" : \"test\",\n" +
                                    "    \"status\": \"ASSIGNED\",\n" +
                                    "    \"traincoache\": \"MATTREIN - WAGONTJE\",\n" +
                                    "    \"descr\": \"Er is een trillingkje.\"\n" +
                                    "}";
                            JSONObject response;
                            try {
                                response = new JSONObject(testString);
                                JSONParser parser=new JSONParser();
                                parser.execute(response);
                            }catch(Exception e){
                                e.fillInStackTrace();
                                Log.e(LOG_TAG,e.toString());
                            }
                            //TEST END

                            error.fillInStackTrace();
                            VolleyLog.e("Error in RESTSingleton request:"+ error.networkResponse);
                        }
                    });

            //Singleton handles call to REST
            Log.v(LOG_TAG ,"Calling RESTSingleton with context:" + this.getContext().getApplicationContext().toString());
            RESTSingleton.getInstance(this.getContext().getApplicationContext())
                    .addToRequestQueue(jsObjRequest);

        }catch(Exception e){
            e.printStackTrace();
            Log.e(LOG_TAG ,"Failed REST fetch");
        }
        Log.v(LOG_TAG,"Ending fetchIssueData: REST JSONRequest is now handed to singleton");
    }

    public class JSONParser extends AsyncTask<JSONObject,Void,List<String[]>>{

        @Override
        protected List<String[]> doInBackground(JSONObject... jsonObjects) {
            JSONObject jsonObject=jsonObjects[0];
            Log.v(LOG_TAG,"Entering JSONParser doInBackground Task");

            //The names of the REST JSON attributes
            final String WORKPLACE="workplace";
            final String STATUS="status";
            final String TRAINCOACH="traincoache";
            final String DESCR="descr";

            //Each element in the list represents a listItem/row in the ListView
            //Each String-column respectivly represents the attribute from that listItem
            List<String[]> resultString=null;
            try {
                JSONArray workplace = jsonObject.getJSONArray(WORKPLACE);
                JSONArray status = jsonObject.getJSONArray(STATUS);
                JSONArray traincoach = jsonObject.getJSONArray(TRAINCOACH);
                JSONArray descr = jsonObject.getJSONArray(DESCR);

                if(workplace.length() == traincoach.length()) {
                    resultString = new ArrayList<>(workplace.length());
                }else throw new JSONException("No equal-size data attributes in JSON");
            Log.v(LOG_TAG,"JSON PARSED!!!"+resultString.toString());
            }catch(JSONException e){
                e.printStackTrace();
                Log.e(LOG_TAG,e.getMessage(), e);
            }
            return resultString;
        }

        @Override
        protected void onPostExecute(List<String[]> result) {
            super.onPostExecute(result);
            mOverviewListAdapter.updateView(result);
        }
    }
}
