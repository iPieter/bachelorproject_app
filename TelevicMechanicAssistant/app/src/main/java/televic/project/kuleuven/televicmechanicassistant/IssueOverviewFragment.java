package televic.project.kuleuven.televicmechanicassistant;

import android.content.Intent;
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
    final private String TRAINCOACH_ID = "traincoach_id";
    private OverviewListAdapter mOverviewListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);

        //Setting up adapter
        mOverviewListAdapter =new OverviewListAdapter(this.getContext());
        setListAdapter(mOverviewListAdapter);

        //Calling backend (default= active issues called)
        fetchIssueData(RESTSingleton.ACTIVE_ISSUES_PARAM);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.fragment_issue_overview,container);
        String workplace_name="Gent";

        return root;
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this.getActivity(), IssueDetailActivity.class);
        String traincoachId = "1"; //TODO Extract from clicked view
        intent.putExtra(TRAINCOACH_ID, traincoachId);
        startActivity(intent);
    }

/** NETWORKING **/
    //No need for AsyncTask: volley takes care of networking on networking thread
    protected void fetchIssueData(String issueMode) {

        //Fetching the JSON file from server through REST
        try{
            String url = RESTSingleton.BASE_URL + RESTSingleton.OVERVIEW_PARAM + issueMode;

            //Creating JsonObjectRequest for REST call
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        public void onResponse(JSONObject response) {
                                JSONParser parser=new JSONParser();
                                parser.execute(response);
                        }
                    }, new Response.ErrorListener() {

                        public void onErrorResponse(VolleyError error) {
                            error.fillInStackTrace();
                        }
                    });

            //Singleton handles call to REST
            RESTSingleton.getInstance(getActivity()).addToRequestQueue(jsObjRequest);

        }catch(Exception e){
            e.printStackTrace();
            Log.e(LOG_TAG ,"Failed REST fetch");
        }
    }

    public class JSONParser extends AsyncTask<JSONObject,Void,List<String[]>>{

        @Override
        protected List<String[]> doInBackground(JSONObject... jsonObjects) {
            JSONObject jsonObject=jsonObjects[0];

            //The names of the REST JSON attributes
            final String WORKPLACES="workplaces";
            final String TRAINCOACHES="traincoaches";

            //Each element in the list represents a listItem/row in the ListView
            //Each String-column respectivly represents the attribute from that listItem
            List<String[]> resultString=null;
            try {
                JSONArray workplaces = jsonObject.getJSONArray(WORKPLACES);
                JSONArray traincoaches = jsonObject.getJSONArray(TRAINCOACHES);

                if(workplaces.length() == traincoaches.length()) {
                    resultString = new ArrayList<>(workplaces.length());
                }else throw new JSONException("No equal-size data attributes in JSON");

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
