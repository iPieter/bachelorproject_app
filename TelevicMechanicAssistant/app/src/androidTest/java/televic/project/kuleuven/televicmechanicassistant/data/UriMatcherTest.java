package televic.project.kuleuven.televicmechanicassistant.data;

import android.content.UriMatcher;
import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import static org.junit.Assert.*;

/**
 * Created by Matthias on 13/05/2017.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class UriMatcherTest {

    //Fake data
    private final static int FAKE_ISSUE_ID = 5;
    private final static int FAKE_IMG_ID = 7;

    //Test URI's
    private final static Uri TEST_URI_ISSUE = IssueContract.IssueEntry.CONTENT_URI;
    private final static Uri TEST_URI_ISSUE_WITH_ID =
            IssueContract.IssueEntry.buildIssueUri(FAKE_ISSUE_ID);
    private final static Uri TEST_URI_ISSUE_ASSET = IssueContract.IssueAssetEntry.CONTENT_URI;
    private final static Uri TEST_URI_ISSUE_ASSET_WITH_ISSUE_ID =
            IssueContract.IssueAssetEntry.buildIssueAssetUri(FAKE_ISSUE_ID);
    private final static Uri TEST_URI_ISSUE_ASSET_WITH_ISSUE_ID_AND_IMG =
            IssueContract.IssueAssetEntry.buildIssueAssetWithImgUri(FAKE_IMG_ID);
    private final static Uri TEST_TRAINCOACH = IssueContract.TraincoachEntry.CONTENT_URI;

    public UriMatcherTest(){
    }

    /**
     * Testing if the UriMatchers matches the Uri to the correct corresponding code.
     */
    @Test
    public void testUriMatcher() {
        UriMatcher uriMatcherTester = IssueProvider.buildUriMatcher();

        assertEquals("ERROR: urimatcher doesn't match correct code for TEST_URI_ISSUE",
                IssueProvider.ISSUE,
                uriMatcherTester.match(TEST_URI_ISSUE)
        );
        assertEquals("ERROR: urimatcher doesn't match correct code for TEST_URI_ISSUE_WITH_ID",
                IssueProvider.ISSUE_WITH_ID,
                uriMatcherTester.match(TEST_URI_ISSUE_WITH_ID)
        );
        assertEquals("ERROR: urimatcher doesn't match correct code for TEST_URI_ISSUE_ASSET",
                IssueProvider.ISSUE_ASSET,
                uriMatcherTester.match(TEST_URI_ISSUE_ASSET)
        );
        assertEquals("ERROR: urimatcher doesn't match correct code for TEST_URI_ISSUE_ASSET_WITH_ISSUE_ID",
                IssueProvider.ISSUE_ASSET_WITH_ISSUE_ID,
                uriMatcherTester.match(TEST_URI_ISSUE_ASSET_WITH_ISSUE_ID)
        );
        assertEquals("ERROR: urimatcher doesn't match correct code for TEST_URI_ISSUE_ASSET_WITH_ISSUE_ID_AND_IMG",
                IssueProvider.ISSUE_ASSET_WITH_ISSUE_ID_AND_IMG,
                uriMatcherTester.match(TEST_URI_ISSUE_ASSET_WITH_ISSUE_ID_AND_IMG)
        );
        assertEquals("ERROR: urimatcher doesn't match correct code for TEST_TRAINCOACH",
                IssueProvider.TRAINCOACH,
                uriMatcherTester.match(TEST_TRAINCOACH)
        );
    }
}
