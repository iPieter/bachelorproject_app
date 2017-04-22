package televic.project.kuleuven.televicmechanicassistant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GraphActivity extends AppCompatActivity
{
    private static final String url = "http://192.168.1.4:8080/DWPProject-0.0.1-SNAPSHOT/rest/processed_data";
    private static final String LOG_TAG = "GRAPH_ACTIVITY";
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_graph );

        loadingDialog = new ProgressDialog( this );
        loadingDialog.setTitle( "Laden.." );
        loadingDialog.setMessage( "De boodschappen worden geladen" );
        loadingDialog.setCancelable( false );
        loadingDialog.show();

        Intent intent = getIntent();
        String id = intent.getStringExtra( "psdid" );

        JsonObjectRequest request = new JsonObjectRequest( Request.Method.GET, url + "/" + id, null, new Response.Listener< JSONObject >()
        {
            @Override
            public void onResponse( JSONObject response )
            {
                loadingDialog.dismiss();

                Log.i( LOG_TAG, response.toString() );

                try
                {
                    JSONArray yaw = response.getJSONArray( "yaw" );

                    GraphView graphYaw = (GraphView) findViewById(R.id.graph_yaw );
                    LineGraphSeries<DataPoint > seriesYaw = new LineGraphSeries<>();

                    for( int i = 0; i < yaw.length(); i++ )
                        seriesYaw.appendData( new DataPoint(  i, yaw.getDouble( i ) ), false, yaw.length() );

                    graphYaw.addSeries(seriesYaw);

                    JSONArray roll = response.getJSONArray( "roll" );

                    GraphView graphRoll = (GraphView) findViewById(R.id.graph_roll );
                    LineGraphSeries<DataPoint > seriesRoll = new LineGraphSeries<>();

                    for( int i = 0; i < roll.length(); i++ )
                        seriesRoll.appendData( new DataPoint(  i, roll.getDouble( i ) ), false, roll.length() );

                    graphRoll.addSeries(seriesRoll);

                } catch ( JSONException e )
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse( VolleyError error )
            {
                loadingDialog.dismiss();

                Context context = getApplicationContext();
                CharSequence text = "Er was een probleem tijdens het ophalen van de grafieken." +
                                    " Test u internetverbinding en probeer opnieuw.";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        } );

        RESTSingleton.getInstance( getApplicationContext() ).addToRequestQueue( request );
    }
}
