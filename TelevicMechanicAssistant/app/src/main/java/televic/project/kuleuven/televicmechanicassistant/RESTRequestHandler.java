package televic.project.kuleuven.televicmechanicassistant;

import android.content.Context;
import java.util.concurrent.CountDownLatch;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by Matthias on 19/04/2017.
 */

public class RESTRequestHandler {
    private final String LOG_TAG = RESTRequestHandler.class.getSimpleName();

    //CODES for the observer to know which one is executed
    public static final int REQUEST_COUNT = 2; //How many requests

    private Context mContext;
    private CountDownLatch mCountDownLatch;
    private String issueStringResponse;
    private String workplaceStringResponse;

    public RESTRequestHandler(Context mContext, CountDownLatch countDownLatch) {
        this.mContext = mContext;
        this.issueStringResponse = null;
        this.workplaceStringResponse = null;
        this.mCountDownLatch = countDownLatch;
    }

    public void sendParallelRequest(int currentUserId) {
        fetchIssueData(currentUserId);
        fetchWorkplaceData(currentUserId);
    }

    /**
     * REST REQUEST 1
     *
     * @param mCurrentUserId
     */
    //REST request for Issue data
    private void fetchIssueData(int mCurrentUserId) {
        Log.v(LOG_TAG, "Entered fetchIssueData");

        try {
            //Rest Request URL
            String url = RESTSingleton.BASE_URL + "/" +
                    RESTSingleton.ISSUES_PATH + "/" + mCurrentUserId;

            //Creating JsonStringRequest for REST call
            //We do not know if getting a JSONArray or JSONObject, So we use the StringRequest
            StringRequest jsonStringRequest = new StringRequest
                    (Request.Method.GET, url, new Response.Listener<String>() {

                        public void onResponse(String response) {
                            VolleyLog.v(LOG_TAG, "JSONObject response received from REST:" + response);
                            issueStringResponse = response;
                            mCountDownLatch.notify();
                        }
                    }, new Response.ErrorListener() {

                        public void onErrorResponse(VolleyError error) {
                            error.fillInStackTrace();
                            VolleyLog.e("Error in RESTSingleton request:" + error.networkResponse);
                        }
                    });

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
     * REST REQUEST 2
     *
     * @param mCurrentUserId
     */
    //REST request for Issue data
    private void fetchWorkplaceData(int mCurrentUserId) {
        Log.v(LOG_TAG, "Entered fetchWorkplaceData");

        try {
            //Rest Request URL
            String url = RESTSingleton.BASE_URL + "/" +
                    RESTSingleton.WORKPLACE_PATH + "/" + mCurrentUserId;


            //Creating JsonStringRequest for REST call
            //We do not know if getting a JSONArray or JSONObject, So we use the StringRequest
            StringRequest jsonStringRequest = new StringRequest
                    (Request.Method.GET, url, new Response.Listener<String>() {

                        public void onResponse(String response) {
                            VolleyLog.v(LOG_TAG, "JSONObject response received from REST:" + response);
                            workplaceStringResponse = response;
                            mCountDownLatch.notify();
                        }
                    }, new Response.ErrorListener() {

                        public void onErrorResponse(VolleyError error) {
                            error.fillInStackTrace();
                            VolleyLog.e("Error in RESTSingleton request:" + error.networkResponse);
                        }
                    });

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
