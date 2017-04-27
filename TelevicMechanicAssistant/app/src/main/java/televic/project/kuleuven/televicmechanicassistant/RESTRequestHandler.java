package televic.project.kuleuven.televicmechanicassistant;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

/**
 * This class handles the REST request of the IssueOverviewActivity.
 * The queried data is meant for the database, that serves as cache for the app.
 * Created by Matthias on 19/04/2017.
 */

public class RESTRequestHandler {
    private final String LOG_TAG = RESTRequestHandler.class.getSimpleName();

    //CODES for the observer to know which one is executed
    public static final int REQUEST_COUNT = 2; //How many requests

    private Context mContext;
    private String issueStringResponse;
    private String workplaceStringResponse;
    private JSONParserTask parserTask;
    private int received = 0;

    public RESTRequestHandler(Context mContext, JSONParserTask task) {
        this.mContext = mContext;
        this.issueStringResponse = null;
        this.workplaceStringResponse = null;
        this.parserTask = task;
        received = 0;
    }

    /**
     * Method to send all request for the IssueOverviewFragment
     * @param currentUserId
     */
    public void sendParallelRequest(int currentUserId) {
        fetchIssueData(currentUserId);
        fetchWorkplaceData(currentUserId);
    }

    /**
     * REST REQUEST 1: Fetching the Issue data
     *
     * @param mCurrentUserId
     */
    //REST request for Issue data
    private void fetchIssueData(int mCurrentUserId) {
        Log.v(LOG_TAG, "Entered fetchIssueData");

        try {
            //Rest Request URL
            String url = RESTSingleton.BASE_URL + "/" +
                    RESTSingleton.ISSUES_ALL_FOR_USER_PATH + "/" + mCurrentUserId;

            //Creating JsonStringRequest for REST call
            //We do not know if getting a JSONArray or JSONObject, So we use the StringRequest
            StringRequest jsonStringRequest = new StringRequest
                    (Request.Method.GET, url, new Response.Listener<String>() {

                        public void onResponse(String response) {
                            Log.i(LOG_TAG, "RECEIVED ISSUES");
                            VolleyLog.v(LOG_TAG, "JSONObject response received from REST:" + response);
                            issueStringResponse = response;
                            received++;
                            if (received >= REQUEST_COUNT) {
                                parserTask.execute(
                                        getIssueStringResponse(),
                                        getWorkplaceStringResponse());
                            }
                        }
                    }, new Response.ErrorListener() {

                        public void onErrorResponse(VolleyError error) {
                            error.fillInStackTrace();
                            VolleyLog.e("Error in RESTSingleton request:" + error.networkResponse);

                            Utility.redirectIfUnauthorized(mContext, error);
                        }
                    }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Authorization", "Bearer " + Utility.getLocalToken(mContext));

                    return params;
                }
            };

            //Singleton handles call to REST
            Log.v(LOG_TAG, "Calling RESTSingleton with context:" + mContext);
            RESTSingleton.getInstance(mContext).addToRequestQueue(jsonStringRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Failed REST issue fetch");
        }
        Log.v(LOG_TAG, "Leaving fetchIssueData");
    }

    /**
     * REST REQUEST 2: Fetching the workplace data
     *
     * @param mCurrentUserId
     */
    //REST request for Issue data
    private void fetchWorkplaceData(int mCurrentUserId) {
        Log.v(LOG_TAG, "Entered fetchWorkplaceData");

        try {
            //Rest Request URL
            String url = RESTSingleton.BASE_URL + "/" +
                    RESTSingleton.WORKPLACE_PATH + "/" + Utility.getLocalUserId(mContext);

            //Creating JsonStringRequest for REST call
            //We do not know if getting a JSONArray or JSONObject, So we use the StringRequest
            StringRequest jsonStringRequest = new StringRequest
                    (Request.Method.GET, url, new Response.Listener<String>() {

                        public void onResponse(String response) {
                            Log.i(LOG_TAG, "RECEIVED WORKPLACES");
                            VolleyLog.v(LOG_TAG, "JSONObject response received from REST:" + response);
                            workplaceStringResponse = response;
                            received++;
                            if (received >= REQUEST_COUNT) {
                                parserTask.execute(
                                        getIssueStringResponse(),
                                        getWorkplaceStringResponse());
                            }
                        }
                    }, new Response.ErrorListener() {

                        public void onErrorResponse(VolleyError error) {
                            error.fillInStackTrace();
                            VolleyLog.e("Error in RESTSingleton request:" + error.networkResponse);

                            Utility.redirectIfUnauthorized(mContext, error);
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Authorization", "Bearer " + Utility.getLocalToken(mContext));

                    return params;
                }
            };

            //Singleton handles call to REST
            Log.v(LOG_TAG, "Calling RESTSingleton with context:" + mContext);

            RESTSingleton.getInstance(mContext).addToRequestQueue(jsonStringRequest);


        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Failed REST workplace fetch");
        }
        Log.v(LOG_TAG, "Leaving fetchWorkplaceData");
    }

    //GETTERS&SETTERS
    public String getIssueStringResponse() {
        return issueStringResponse;
    }

    public void setIssueStringResponse(String issueStringResponse) {
        this.issueStringResponse = issueStringResponse;
    }

    public String getWorkplaceStringResponse() {
        return workplaceStringResponse;
    }

    public void setWorkplaceStringResponse(String workplaceStringResponse) {
        this.workplaceStringResponse = workplaceStringResponse;
    }
}
