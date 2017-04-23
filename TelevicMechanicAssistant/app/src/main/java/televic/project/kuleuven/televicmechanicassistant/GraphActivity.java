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
    private static final String url = RESTSingleton.BASE_URL + "/processed_data";
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
                    JSONArray roll = response.getJSONArray( "roll" );

                    createGraph( R.id.graph_yaw, yaw );
                    createGraph( R.id.graph_roll, roll );

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

    private void createGraph( int id, JSONArray array ) throws JSONException
    {
        GraphView graphView = (GraphView) findViewById( id );

        DataPoint [] points = new DataPoint[ array.length() ];
        for( int i = 0; i < array.length(); i++ )
            points[ i ] = new DataPoint( i, array.getDouble( i ) );

        LineGraphSeries<DataPoint > seriesRoll = new LineGraphSeries<>( points );

        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(500);
        graphView.getViewport().setMaxX(1000);

        // enable scaling and scrolling
        graphView.getViewport().setScalable(true);
        graphView.getViewport().setScalableY(true);

        graphView.addSeries(seriesRoll);
    }
}
