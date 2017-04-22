package televic.project.kuleuven.televicmechanicassistant;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;

public class IssueOverviewActivity extends AppCompatActivity {
    private final String LOG_TAG = IssueOverviewActivity.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_overview);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.overview_container, new IssueOverviewFragment())
                    .commit();
            Log.v(LOG_TAG, "Fragment transaction ended");
        }

        RESTSingleton.getInstance(getApplicationContext());
        Log.v(LOG_TAG, "Added application context in main_overview activity");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_overview, menu);
        return true;
    }

    //See Fragment for implementation
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //TODO create settings
            //startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
