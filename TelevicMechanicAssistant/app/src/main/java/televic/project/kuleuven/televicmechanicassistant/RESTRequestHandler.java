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

    public void sendParallelRequest(int currentUserId) {
        fetchIssueData(currentUserId);
        fetchWorkplaceData(currentUserId);
    }

    public void setParserTask(JSONParserTask task) {
        parserTask = task;
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

    static String testStringIssue = "[\n" +
            "    {\n" +
            "        \"id\": 1,\n" +
            "        \"descr\": \"Dit is een testprobleem\",\n" +
            "        \"status\": \"ASSIGNED\",\n" +
            "        \"assets\": [\n" +
            "            {\n" +
            "                \"id\": 1,\n" +
            "                \"descr\": \"TEST\",\n" +
            "                \"time\": 1492594863000,\n" +
            "                \"location\": \"\",\n" +
            "                \"user\": {\n" +
            "                    \"id\": 1,\n" +
            "                    \"name\": \"John Doe\",\n" +
            "                    \"email\": \"john0@test.be\",\n" +
            "                    \"role\": \"MECHANIC\",\n" +
            "                    \"imageHash\": \"qwertyui\",\n" +
            "                    \"lastPrettyLogin\": \"4 uur geleden\"\n" +
            "                }\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": 2,\n" +
            "                \"descr\": \"TEST\",\n" +
            "                \"time\": 1492594935000,\n" +
            "                \"location\": \"\",\n" +
            "                \"user\": {\n" +
            "                    \"id\": 1,\n" +
            "                    \"name\": \"John Doe\",\n" +
            "                    \"email\": \"john0@test.be\",\n" +
            "                    \"role\": \"MECHANIC\",\n" +
            "                    \"imageHash\": \"qwertyui\",\n" +
            "                    \"lastPrettyLogin\": \"4 uur geleden\"\n" +
            "                }\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": 3,\n" +
            "                \"descr\": \"TEST\",\n" +
            "                \"time\": 1492595093000,\n" +
            "                \"location\": \"\",\n" +
            "                \"user\": {\n" +
            "                    \"id\": 1,\n" +
            "                    \"name\": \"John Doe\",\n" +
            "                    \"email\": \"john0@test.be\",\n" +
            "                    \"role\": \"MECHANIC\",\n" +
            "                    \"imageHash\": \"qwertyui\",\n" +
            "                    \"lastPrettyLogin\": \"4 uur geleden\"\n" +
            "                }\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": 4,\n" +
            "                \"descr\": \"TEST\",\n" +
            "                \"time\": 1492595264000,\n" +
            "                \"location\": \"\",\n" +
            "                \"user\": {\n" +
            "                    \"id\": 1,\n" +
            "                    \"name\": \"John Doe\",\n" +
            "                    \"email\": \"john0@test.be\",\n" +
            "                    \"role\": \"MECHANIC\",\n" +
            "                    \"imageHash\": \"qwertyui\",\n" +
            "                    \"lastPrettyLogin\": \"4 uur geleden\"\n" +
            "                }\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": 5,\n" +
            "                \"descr\": \"TEST\",\n" +
            "                \"time\": 1492595277000,\n" +
            "                \"location\": \"C:\\\\Users\\\\Gebruiker/project_televic/issue_assets/2017_47_19_11_47_57_1.png\",\n" +
            "                \"user\": {\n" +
            "                    \"id\": 1,\n" +
            "                    \"name\": \"John Doe\",\n" +
            "                    \"email\": \"john0@test.be\",\n" +
            "                    \"role\": \"MECHANIC\",\n" +
            "                    \"imageHash\": \"qwertyui\",\n" +
            "                    \"lastPrettyLogin\": \"4 uur geleden\"\n" +
            "                }\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": 6,\n" +
            "                \"descr\": \"TEST\",\n" +
            "                \"time\": 1492595354000,\n" +
            "                \"location\": \"\",\n" +
            "                \"user\": {\n" +
            "                    \"id\": 1,\n" +
            "                    \"name\": \"John Doe\",\n" +
            "                    \"email\": \"john0@test.be\",\n" +
            "                    \"role\": \"MECHANIC\",\n" +
            "                    \"imageHash\": \"qwertyui\",\n" +
            "                    \"lastPrettyLogin\": \"4 uur geleden\"\n" +
            "                }\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": 7,\n" +
            "                \"descr\": \"Beschrijving\",\n" +
            "                \"time\": 1492596160000,\n" +
            "                \"location\": \"C:\\\\Users\\\\Gebruiker/project_televic/issue_assets/2017_02_19_12_02_40_1.png\",\n" +
            "                \"user\": {\n" +
            "                    \"id\": 1,\n" +
            "                    \"name\": \"John Doe\",\n" +
            "                    \"email\": \"john0@test.be\",\n" +
            "                    \"role\": \"MECHANIC\",\n" +
            "                    \"imageHash\": \"qwertyui\",\n" +
            "                    \"lastPrettyLogin\": \"4 uur geleden\"\n" +
            "                }\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": 8,\n" +
            "                \"descr\": \"idbsbsjsvsv\",\n" +
            "                \"time\": 1492596189000,\n" +
            "                \"location\": \"\",\n" +
            "                \"user\": {\n" +
            "                    \"id\": 1,\n" +
            "                    \"name\": \"John Doe\",\n" +
            "                    \"email\": \"john0@test.be\",\n" +
            "                    \"role\": \"MECHANIC\",\n" +
            "                    \"imageHash\": \"qwertyui\",\n" +
            "                    \"lastPrettyLogin\": \"4 uur geleden\"\n" +
            "                }\n" +
            "            }\n" +
            "        ],\n" +
            "        \"mechanic\": {\n" +
            "            \"id\": 1,\n" +
            "            \"name\": \"John Doe\",\n" +
            "            \"email\": \"john0@test.be\",\n" +
            "            \"role\": \"MECHANIC\",\n" +
            "            \"imageHash\": \"qwertyui\",\n" +
            "            \"lastPrettyLogin\": \"4 uur geleden\"\n" +
            "        },\n" +
            "        \"operator\": {\n" +
            "            \"id\": 7,\n" +
            "            \"name\": \"John Doe\",\n" +
            "            \"email\": \"john6@test.be\",\n" +
            "            \"role\": \"OPERATOR\",\n" +
            "            \"imageHash\": \"qwertyui\",\n" +
            "            \"lastPrettyLogin\": \"3 uur geleden\"\n" +
            "        },\n" +
            "        \"data\": {\n" +
            "            \"id\": 1,\n" +
            "            \"track\": \"BrusselZ-GentSP\",\n" +
            "            \"traincoach\": {\n" +
            "                \"id\": 24,\n" +
            "                \"constructor\": \"Bombardier\",\n" +
            "                \"name\": \"65201\",\n" +
            "                \"type\": \"M7\",\n" +
            "                \"needsReview\": true\n" +
            "            },\n" +
            "            \"date\": 1492590501000,\n" +
            "            \"location\": \"C:\\\\Users\\\\Gebruiker\\\\project_televic\\\\matlab_files/GentSP_Bombardier_M7_65201_BrusselZ-GentSP.json\"\n" +
            "        },\n" +
            "        \"assignedTime\": 1492590691000,\n" +
            "        \"inProgressTime\": 1492590691000,\n" +
            "        \"closedTime\": 1492590691000,\n" +
            "        \"gpsLat\": 51.1936,\n" +
            "        \"gpsLon\": 5.3286\n" +
            "    }\n" +
            "]";

    static String testStringWorkplace = "[\n" +
            "  {\n" +
            "    \"mechanics\": [\n" +
            "      {\n" +
            "        \"email\": \"john0@test.be\",\n" +
            "        \"id\": 41,\n" +
            "        \"imageHash\": \"qwertyui\",\n" +
            "        \"name\": \"John Doe\",\n" +
            "        \"role\": \"MECHANIC\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"id\": 1,\n" +
            "    \"name\": \"Gent-Sint-Pieters\",\n" +
            "    \"traincoaches\": [\n" +
            "      {\n" +
            "        \"id\": 7,\n" +
            "        \"needsReview\": true,\n" +
            "        \"constructor\": \"BOMBARDIER\",\n" +
            "        \"name\": \"78558\",\n" +
            "        \"type\": \"M7\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": 8,\n" +
            "        \"needsReview\": true,\n" +
            "        \"constructor\": \"BOMBARDIER\",\n" +
            "        \"name\": \"78559\",\n" +
            "        \"type\": \"M7\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"mechanics\": [\n" +
            "      {\n" +
            "        \"email\": \"john5@test.be\",\n" +
            "        \"id\": 46,\n" +
            "        \"imageHash\": \"qwertyui\",\n" +
            "        \"name\": \"John Doe\",\n" +
            "        \"role\": \"MECHANIC\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"id\": 2,\n" +
            "    \"name\": \"Oostende\",\n" +
            "    \"traincoaches\": [\n" +
            "      {\n" +
            "        \"id\": 9,\n" +
            "        \"needsReview\": true,\n" +
            "        \"constructor\": \"BOMBARDIER\",\n" +
            "        \"name\": \"78560\",\n" +
            "        \"type\": \"M7\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"mechanics\": [\n" +
            "    ],\n" +
            "    \"id\": 3,\n" +
            "    \"name\": \"Brussel\",\n" +
            "    \"traincoaches\": [\n" +
            "      {\n" +
            "        \"id\": 6,\n" +
            "        \"needsReview\": false,\n" +
            "        \"constructor\": \"Bombardier\",\n" +
            "        \"name\": \"123912\",\n" +
            "        \"type\": \"M7\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"mechanics\": [\n" +
            "    ],\n" +
            "    \"id\": 4,\n" +
            "    \"name\": \"Luik\",\n" +
            "    \"traincoaches\": [\n" +
            "      {\n" +
            "        \"id\": 24,\n" +
            "        \"needsReview\": true,\n" +
            "        \"constructor\": \"Bombardier\",\n" +
            "        \"name\": \"15963\",\n" +
            "        \"type\": \"M7\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"mechanics\": [\n" +
            "    ],\n" +
            "    \"id\": 5,\n" +
            "    \"name\": \"Flobecq\",\n" +
            "    \"traincoaches\": [\n" +
            "      {\n" +
            "        \"id\": 11,\n" +
            "        \"needsReview\": true,\n" +
            "        \"constructor\": \"Bombardier\",\n" +
            "        \"name\": \"15921\",\n" +
            "        \"type\": \"M8\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "]";
    static String empty = "[]";
}
