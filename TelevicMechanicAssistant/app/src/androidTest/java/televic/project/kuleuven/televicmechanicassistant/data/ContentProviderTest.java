package televic.project.kuleuven.televicmechanicassistant.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import java.util.Map;
import java.util.Set;

/**
 * Created by Matthias on 13/05/2017.
 */
public class ContentProviderTest extends ProviderTestCase2<IssueProvider> {

    private MockContentResolver mMockContentResolver;
    private int mIssueId = 3;

    //Constant test values for Issue Table
    private final String TEST_ISSUE_DESCRIPTION = "test description";
    private final String TEST_ISSUE_STATUS = "In Progress";
    private final String TEST_ISSUE_OPERATOR = "Henk";
    private final int TEST_ISSUE_DATA_ID = 9;
    private final String TEST_ISSUE_ASSIGNED_TIME = "1330000";
    private final String TEST_ISSUE_IN_PROGRESS_TIME = "1350000";
    private final String TEST_ISSUE_CLOSED_TIME = "";
    private final String TEST_ISSUE_TRAINCOACH = "M7 - 32145";
    private final int TEST_ISSUE_TRAINCOACH_ID = 5;

    //Constant test values for IssueAsset Table
    //This table is linked to the Issue table with the IssueId column
    private final int TEST_ASSET_ID = 5;
    private final String TEST_ASSET_DESCRIPTION = "asset_description";
    private final String TEST_ASSET_IMAGE_PRESENT = "image_location";
    private final String TEST_ASSET_IMAGE_BLOB = "image_blob";
    private final String TEST_ASSET_POST_TIME = "post_time";
    private final String TEST_ASSET_USER_NAME = "user_name";
    private final String TEST_ASSET_USER_EMAIL = "user_email@test.be";

    //Constant test values for Traincoach Table
    //This table is linked to the IssueTable
    // on the _ID column with the IssueTable.TRAINCOACH_ID column
    public final int TEST_TRAINCOACH_ID = TEST_ISSUE_TRAINCOACH_ID;
    public final int TEST_TRAINCOACH_WORKPLACE_ID = 5;
    public final String TEST_TRAINCOACH_WORKPLACE_NAME = "GENT SP";

    public ContentProviderTest() {
        super(IssueProvider.class, IssueContract.CONTENT_AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
        mMockContentResolver = getMockContentResolver();
    }

    /**
     * Testing a successful attempt to query the database.
     * First we insert one row in each table of the database.
     * For queries all tables are INNER JOIN'ed, so each insert must be linked to retrieve a result.
     * If we receive exactly one row back in the cursor, the query was successful.
     * Thereafter the cursor itself is tested.
     */
    public void testIssueQuery() {

        //Init of values to insert in db
        ContentValues issueContentValues = getIssueTestValues();
        //Inserting the test values in the Issue Table
        Uri issueUri = mMockContentResolver.insert(
                IssueContract.IssueEntry.CONTENT_URI, issueContentValues);
        //Check if inserted properly
        assertEquals("ERROR: Could not insert the row in the Issue Table of database.",
                IssueContract.IssueEntry.buildIssueUri(mIssueId),
                issueUri );

        ContentValues assetContentValues = getIssueAssetTestValues();
        //Inserting the test values in the IssueAsset Table
        Uri assetUri = mMockContentResolver.insert(
                IssueContract.IssueAssetEntry.CONTENT_URI, assetContentValues);
        //Check if inserted properly
        assertEquals("ERROR: Could not insert the row in the IssueAsset Table of database.",
                IssueContract.IssueAssetEntry.buildIssueAssetUri(TEST_TRAINCOACH_ID),
                assetUri );

        ContentValues traincoachContentValues = getTraincoachTestValues();
        //Inserting the test values in the IssueAsset Table
        Uri traincoachUri = mMockContentResolver.insert(
                IssueContract.TraincoachEntry.CONTENT_URI, traincoachContentValues);
        //Check if inserted properly
        assertEquals("ERROR: Could not insert the row in the Traincoach Table of database.",
                IssueContract.TraincoachEntry.buildTraincoachUri(TEST_TRAINCOACH_ID),
                traincoachUri );

        //Testing the basic query on the database
        Cursor cursor = mMockContentResolver
                .query(IssueContract.IssueEntry.CONTENT_URI, null, null, null, null);

        //Check if the one inserted row, does get returned
        assertNotNull(cursor);
        assertTrue("ERROR: Query does not return all rows in Issue Table", cursor.getCount() > 0);

        //Check if the values of the cursor correspond with the inserted values
        validateCursor(cursor, issueContentValues);

        cursor.close();

    }

    public ContentValues getIssueTestValues() {
        ContentValues issueContentValues = new ContentValues();
        issueContentValues.put(IssueContract.IssueEntry._ID, mIssueId);
        issueContentValues.put(IssueContract.IssueEntry.COLUMN_DESCRIPTION, TEST_ISSUE_DESCRIPTION);
        issueContentValues.put(IssueContract.IssueEntry.COLUMN_STATUS, TEST_ISSUE_STATUS);
        issueContentValues.put(IssueContract.IssueEntry.COLUMN_OPERATOR, TEST_ISSUE_OPERATOR);
        issueContentValues.put(IssueContract.IssueEntry.COLUMN_DATA_ID, TEST_ISSUE_DATA_ID);
        issueContentValues.put(IssueContract.IssueEntry.COLUMN_ASSIGNED_TIME, TEST_ISSUE_ASSIGNED_TIME);
        issueContentValues.put(IssueContract.IssueEntry.COLUMN_IN_PROGRESS_TIME, TEST_ISSUE_IN_PROGRESS_TIME);
        issueContentValues.put(IssueContract.IssueEntry.COLUMN_CLOSED_TIME, TEST_ISSUE_CLOSED_TIME);
        issueContentValues.put(IssueContract.IssueEntry.COLUMN_TRAINCOACH_NAME, TEST_ISSUE_TRAINCOACH);
        issueContentValues.put(IssueContract.IssueEntry.COLUMN_TRAINCOACH_ID, TEST_ISSUE_TRAINCOACH_ID);
        return issueContentValues;
    }

    public ContentValues getIssueAssetTestValues() {
        ContentValues issueAssetContentValues = new ContentValues();
        issueAssetContentValues.put(IssueContract.IssueAssetEntry._ID, TEST_ASSET_ID);
        issueAssetContentValues.put(IssueContract.IssueAssetEntry.COLUMN_DESCRIPTION, TEST_ASSET_DESCRIPTION);
        issueAssetContentValues.put(IssueContract.IssueAssetEntry.COLUMN_IMAGE_PRESENT, TEST_ASSET_IMAGE_PRESENT);
        issueAssetContentValues.put(IssueContract.IssueAssetEntry.COLUMN_IMAGE_BLOB, TEST_ASSET_IMAGE_BLOB);
        issueAssetContentValues.put(IssueContract.IssueAssetEntry.COLUMN_POST_TIME, TEST_ASSET_POST_TIME);
        issueAssetContentValues.put(IssueContract.IssueAssetEntry.COLUMN_USER_NAME, TEST_ASSET_USER_NAME);
        issueAssetContentValues.put(IssueContract.IssueAssetEntry.COLUMN_USER_EMAIL, TEST_ASSET_USER_EMAIL);
        issueAssetContentValues.put(IssueContract.IssueAssetEntry.COLUMN_ISSUE_ID, mIssueId);
        return issueAssetContentValues;
    }

    public ContentValues getTraincoachTestValues() {
        ContentValues traincoachContentValues = new ContentValues();
        traincoachContentValues.put(IssueContract.TraincoachEntry._ID, TEST_TRAINCOACH_ID);
        traincoachContentValues.put(IssueContract.TraincoachEntry.COLUMN_WORKPLACE_ID, TEST_TRAINCOACH_WORKPLACE_ID);
        traincoachContentValues.put(IssueContract.TraincoachEntry.COLUMN_WORKPLACE_NAME, TEST_TRAINCOACH_WORKPLACE_NAME);
        return traincoachContentValues;
    }

    public void validateCursor(Cursor cursor, ContentValues contentValues) {
        //Check if columns exist
        Set<Map.Entry<String, Object>> valuesExpected = contentValues.valueSet();
        for (Map.Entry<String, Object> entry : valuesExpected) {
            String columnName = entry.getKey();
            int columnIndex = cursor.getColumnIndex(columnName);

            assertTrue("ERROR:" + entry.getKey() + " column not found in query cursor.",
                    columnIndex > -1);
        }

        cursor.moveToFirst();

        //Check if values are correct
        assertEquals("ERROR: TEST_ISSUE_ID not correctly fetched from database.",
                mIssueId,
                cursor.getInt(0));
        assertEquals("ERROR: TEST_ISSUE_DESCRIPTION not correctly fetched from database.",
                TEST_ISSUE_DESCRIPTION,
                cursor.getString(1));
        assertEquals("ERROR: TEST_ISSUE_STATUS not correctly fetched from database.",
                TEST_ISSUE_STATUS,
                cursor.getString(2));
        assertEquals("ERROR: TEST_ISSUE_OPERATOR not correctly fetched from database.",
                TEST_ISSUE_OPERATOR,
                cursor.getString(3));
        assertEquals("ERROR: TEST_ISSUE_DATA_ID not correctly fetched from database.",
                TEST_ISSUE_DATA_ID,
                cursor.getInt(4));
        assertEquals("ERROR: TEST_ISSUE_ASSIGNED_TIME not correctly fetched from database.",
                TEST_ISSUE_ASSIGNED_TIME,
                cursor.getString(5));
        assertEquals("ERROR: TEST_ISSUE_IN_PROGRESS_TIME not correctly fetched from database.",
                TEST_ISSUE_IN_PROGRESS_TIME,
                cursor.getString(6));
        assertEquals("ERROR: TEST_ISSUE_CLOSED_TIME not correctly fetched from database.",
                TEST_ISSUE_CLOSED_TIME,
                cursor.getString(7));
        assertEquals("ERROR: TEST_ISSUE_TRAINCOACH not correctly fetched from database.",
                TEST_ISSUE_TRAINCOACH,
                cursor.getString(8));
        assertEquals("ERROR: TEST_ISSUE_TRAINCOACH_ID not correctly fetched from database.",
                TEST_ISSUE_TRAINCOACH_ID,
                cursor.getInt(9));
    }

}
