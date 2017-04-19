package televic.project.kuleuven.televicmechanicassistant;

import android.os.AsyncTask;

import java.util.Observable;

/**
 * Created by Matthias on 19/04/2017.
 */

public class JSONParserTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = JSONParserTask.class.getSimpleName();

    //TestStrings For REST
    //TODO DELETE STRINGS
    static final String testString1 = "[]";

    public JSONParserTask() {
    }

    @Override
    protected Void doInBackground(String... strings) {
        String issueStringResponse = strings[0];
        String workplaceStringResponse = strings[1];

        parseIssueJSON(issueStringResponse);
        parseWorkplaceJSON(workplaceStringResponse);

        return null;
    }

    public void parseIssueJSON(String jsonResponse) {


    }

    public void parseWorkplaceJSON(String jsonResponse) {

    }
}
