package televic.project.kuleuven.televicmechanicassistant;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import televic.project.kuleuven.televicmechanicassistant.data.IssueContract;

/**
 * This activity shows a list of IssueAsset, that belong to a issue with a unique IssueId.
 */
public class IssueDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = IssueDetailActivity.class.getSimpleName();

    //Intent Tags
    public static final String INTENT_DATA_ID_GRAPH = "data_id_for_graph";

    //Request codes
    private static final int REQUEST_TAKE_PHOTO = 1;

    //Members
    private String mCurrentPhotoPath;
    private IssueAssetListAdapter mListAdapter;
    private int mCurrentUserId;
    private int mIssueId;
    private int mDataId;

    //GUI components
    private ProgressDialog sendingDialog;
    private TextView mEmptyListTextView;
    private View mDataLoadingProgressView;
    private ListView mListView;

    //Id of the loader
    private static final int DETAIL_LOADER = 1;

    //Values in Database needed in this activity
    private static final String[] DETAIL_COLUMNS = {
            IssueContract.IssueAssetEntry.TABLE_NAME + "." +
                    IssueContract.IssueAssetEntry._ID + " AS _id", //CursorLoader Needs _id column
            IssueContract.IssueAssetEntry.COLUMN_DESCRIPTION,
            IssueContract.IssueAssetEntry.COLUMN_POST_TIME,
            IssueContract.IssueAssetEntry.COLUMN_IMAGE_PRESENT,
            IssueContract.IssueAssetEntry.COLUMN_IMAGE_BLOB,
            IssueContract.IssueAssetEntry.COLUMN_USER_NAME,
            IssueContract.IssueAssetEntry.COLUMN_USER_EMAIL
    };

    //Depends on DETAIL_COLUMNS, if DETAIL_COLUMNS changes, so must these indexes!
    static final int COL_ASSET_ID = 0;
    static final int COL_ASSET_DESCRIPTION = 1;
    static final int COL_ASSET_POST_TIME = 2;
    static final int COL_ASSET_IMAGE_PRESENT = 3;
    static final int COL_ASSET_IMAGE_BLOB = 4;
    static final int COL_ASSET_USER_NAME = 5;
    static final int COL_ASSET_USER_EMAIL = 6;

    /* Values Used for the rest request! */
    //Values in Database needed in this activity
    private static final String[] REST_COLUMNS = {
            IssueContract.IssueAssetEntry.TABLE_NAME + "." + IssueContract.IssueAssetEntry._ID,
            IssueContract.IssueAssetEntry.COLUMN_IMAGE_PRESENT,
            IssueContract.IssueAssetEntry.COLUMN_IMAGE_BLOB
    };

    //Depends on REST_COLUMNS, if REST_COLUMNS changes, so must these indexes!
    static final int COL_REST_ID = 0;
    static final int COL_REST_IMAGE_PRESENT = 1;
    static final int COL_REST_IMAGE_BLOB = 2;

    /**
     * Called when Activity is created. The intent data are initialized and all
     * attributes are initialized.
     *
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Berichten");

        //INIT get values from intent
        mIssueId = getIntent().getIntExtra(IssueOverviewFragment.INTENT_ISSUE_ID, -1);
        mDataId = getIntent().getIntExtra(IssueOverviewFragment.INTENT_DATA_ID, -1);
        Log.v(LOG_TAG, "onCreate IssueDetailActivity, issueId=" + mIssueId + ",dataId=" + mDataId);

        //INIT current user
        mCurrentUserId = Utility.getLocalUserId(this);

        //INIT Send-Button
        ImageButton buttonSend = (ImageButton) findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendIssueAssetPostRequest();
            }
        });

        //INIT Photo-Button
        ImageButton buttonCamera = (ImageButton) findViewById(R.id.button_camera);
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePicture();
            }
        });

        //INIT adapter
        mListAdapter = new IssueAssetListAdapter(getApplicationContext(), null, 0);
        mListView = (ListView) findViewById(R.id.issue_asset_link);
        mListView.setAdapter(mListAdapter);

        //INIT loader
        getSupportLoaderManager().initLoader(DETAIL_LOADER, null, this);

        //INIT sending dialog
        sendingDialog = new ProgressDialog(this);
        sendingDialog.setTitle("Versturen");
        sendingDialog.setMessage("De boodschap wordt verstuurd.");
        sendingDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog

        //INIT attributes
        mDataLoadingProgressView = findViewById(R.id.detaillist_progress);
        mEmptyListTextView = (TextView) findViewById(R.id.detaillist_empty);

        //Show progressbar until backend is handled
        //showProgress(true);

        //Calling Backend
        fetchIssueAssetImages();
    }

    /**
     * Inflating the menu items to the menu bar
     *
     * @param menu
     * @return true if successful
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_detail, menu);
        return true;
    }

    /**
     * Binding actions to the menu items
     *
     * @param item
     * @return true if successful
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_graphs:
                goToGraphActivity();
                break;

            case R.id.action_change_status:
                String newStatus = "IN_PROGRESS";
                changeIssueStatus(newStatus);
                break;

            case R.id.action_logout: {
                Utility.redirectToLogin(this);
                break;
            }
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * To navigate to the GraphActivity
     */
    private void goToGraphActivity() {
        Intent intent = new Intent(this, GraphActivity.class);
        intent.putExtra(INTENT_DATA_ID_GRAPH, mDataId);
        Log.v(LOG_TAG, "Starting Graph with dataid=" + mDataId);
        startActivity(intent);
    }

    /**
     * Change the status from ASSIGNED to IN_PROGRESS
     */
    private void changeIssueStatus(final String status) {
        //Create POST request to server to update status
        //onResponse, update locally

        //The REST url
        String url = RESTSingleton.BASE_URL + "/" +
                RESTSingleton.ISSUES_PATH + "/" + mIssueId + "/" + status;

        //Creating JsonStringRequest for REST call
        //We do not know if getting a JSONArray or JSONObject, So we use the StringRequest
        JsonObjectRequest jsonStringRequest = new JsonObjectRequest
                (Request.Method.PUT, url, null, new Response.Listener<JSONObject>() {

                    public void onResponse(JSONObject response) {
                        Log.v(LOG_TAG, "JSONRESPONSE STATUSCHANGE: " + response.toString());
                        updateStatusInDatabase(status);
                    }
                }, new Response.ErrorListener() {

                    public void onErrorResponse(VolleyError error) {
                        error.fillInStackTrace();
                        VolleyLog.e("Error in RESTSingleton request:" + error.networkResponse);

                        Utility.redirectIfUnauthorized(getApplicationContext(), error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + Utility.getLocalToken(getApplicationContext()));

                return params;
            }
        };

        //Singleton handles call to REST
        Log.v(LOG_TAG, "Calling RESTSingleton with context:" + getApplicationContext());
        RESTSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonStringRequest);
    }

    /**
     * Method that updates the status of a row in the IssueAsset table.
     *
     * @param status
     */
    private void updateStatusInDatabase(String status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(IssueContract.IssueEntry.COLUMN_STATUS, status);

        //WHERE issue.id = id
        String selection = IssueContract.IssueEntry.TABLE_NAME
                + "." + IssueContract.IssueEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(mIssueId)};

        Uri uri = IssueContract.IssueEntry.buildIssueUri(mIssueId);

        //Calling our contentProvider through the contentResolver
        getContentResolver().update(
                uri,
                contentValues,
                selection,
                selectionArgs);
    }

    /**
     * Text showed when there are no Issues assigned to the user and
     * hides the list.
     *
     * @param show
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showListEmptyText(final boolean show) {
        Log.v(LOG_TAG, "LIST EMPTY TEXT show=" + show);
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mListView.setVisibility(show ? View.GONE : View.VISIBLE);
        mListView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mListView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mEmptyListTextView.setVisibility(show ? View.VISIBLE : View.GONE);
        mEmptyListTextView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mDataLoadingProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Shows the progress UI and hides the list
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mListView.setVisibility(show ? View.GONE : View.VISIBLE);
        mListView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mListView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mDataLoadingProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mDataLoadingProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mDataLoadingProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * First, all issueAssets with current issueId and contain an img are queried.
     * If IMAGE_LOCATION equals "IMG", then a REST request for this IssueAssetId is created
     * and added to the RequestQueue. Volley handles all requests.
     * If we get a response from volley, we store the image as a blob in the database.
     * Therefore we update the row with the IssueAssetId corresponding with the image.
     * The cursorLoader will notify the change and call the cursorAdapter to update the ListView.
     */
    public void fetchIssueAssetImages() {
        //Fetching all Images of those issueAssets that have an Image
        Uri assetsWithImgUri = IssueContract.IssueAssetEntry
                .buildIssueAssetWithImgUri(mIssueId);
        Log.i(LOG_TAG, assetsWithImgUri.getPath());
        Cursor cursor = getContentResolver().query(
                assetsWithImgUri, REST_COLUMNS, null, null, null);
        Log.v(LOG_TAG,"fetchIssueAssetImages query resultcount="+cursor.getCount());

        String baseUrl = RESTSingleton.BASE_URL + "/" + RESTSingleton.ISSUE_ASSET_PATH;
        String url;
        //Iterate over Cursor's rows
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            Log.i(LOG_TAG, "TESTING ASSET: " + cursor.getInt(COL_ASSET_ID));
            //Only fetch Image if Blob of image is not present in cache.
            if (cursor.getBlob(COL_REST_IMAGE_BLOB) == null) {

                Log.i(LOG_TAG, "FETCHING FOR ISSUE ASSET: " + cursor.getInt(COL_ASSET_ID));
                //The URL to fetch the image for a certain issueAssetId
                url = baseUrl + "/" + cursor.getInt(COL_ASSET_ID);

                final int ASSET_ID = cursor.getInt(COL_ASSET_ID);

                //Create the REST ImageRequest
                ImageRequest request = new ImageRequest(url,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {
                                Log.i(LOG_TAG, "REST onResponse of image, assetID=" + ASSET_ID);

                                int assetId = ASSET_ID;
                                updateImageInDatabase(response, assetId);
                            }
                        }, 350, 350, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565,
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                Log.e(LOG_TAG, "Image fetch FAILED");

                                Utility.redirectIfUnauthorized(getApplicationContext(), error);

                                Context context = getApplicationContext();
                                CharSequence text = "De afbeeldingen konden niet geladen worden, probeer later opnieuw.";
                                int duration = Toast.LENGTH_LONG;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }
                        }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("Authorization", "Bearer " +
                                Utility.getLocalToken(getApplicationContext()));
                        Log.i(LOG_TAG, "GETTING HEADERS");
                        return params;
                    }
                };

                //Adding the request to the requestQueue, Volley handles the rest
                RESTSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
            }

            cursor.moveToNext();
        }
        Log.i(LOG_TAG, "FINISHED FETCHING ISSUE ASSETS");
    }

    /**
     * For a specified assetId, the row of the IssueAsset gets updated with the
     * queried image as Blob.
     *
     * @param bitmap  the picture
     * @param assetId the IssueAsset to update
     */
    public void updateImageInDatabase(Bitmap bitmap, int assetId) {
        Log.v(LOG_TAG,"updateImageInDatabase from assetId="+assetId+", bitmap="+bitmap);
        ContentValues contentValues = new ContentValues();
        contentValues.put(IssueContract.IssueAssetEntry.COLUMN_IMAGE_BLOB,
                Utility.toByteArray(bitmap));

        //WHERE asset.assetId = id
        String selection = IssueContract.IssueAssetEntry.TABLE_NAME
                + "." + IssueContract.IssueAssetEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(assetId)};

        Uri uri = IssueContract.IssueAssetEntry.buildIssueAssetUri(assetId);

        //Calling our contentProvider through the contentResolver
        getContentResolver().update(
                uri,
                contentValues,
                selection,
                selectionArgs);
    }

    /**
     * When the user has sent a postRequest to make a new IssueAsset, the server will respond
     * and provide the given id for the IssueAsset
     *
     * @param response the JSONObject returne from the REST request
     */
    public void insertNewIssueAssetInDatabase(JSONObject response) {
        //transfer local file to database, if a local image is present.
        byte[] blob = Utility.getBytesFromPicture(mCurrentPhotoPath);

        ContentValues contentValues = JSONParserTask.parseSingleAsset(response, blob, mIssueId);

        Uri uri = IssueContract.IssueAssetEntry.CONTENT_URI;

        //Calling our contentProvider through the contentResolver
        getContentResolver().insert(uri, contentValues);
    }

    /**
     * The user can upload a new IssueAsset with picture and description to the server.
     * Also locally on the app, the list must be updated.
     */
    public void sendIssueAssetPostRequest() {
        showSendingProgressDialog(true);

        String url = RESTSingleton.BASE_URL + "/" + RESTSingleton.ISSUE_ASSET_PATH;

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String responseString = new String(response.data);
                try {
                    Log.i(LOG_TAG, "TOTAL JSON RESULT: " + responseString);

                    //Parsing the response
                    JSONObject responseJsObj = new JSONObject(responseString);

                    //Insert into database
                    insertNewIssueAssetInDatabase(responseJsObj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mCurrentPhotoPath = null;
                showSendingProgressDialog(false);

                //Clearing the message inputbar
                ((EditText) findViewById(R.id.textfield_issueasset)).setText("");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mCurrentPhotoPath = null;
                showSendingProgressDialog(false);

                Utility.redirectIfUnauthorized(getApplicationContext(), error);

                CharSequence text = "De boodschap kon niet verzonden worden, controleer of u internetverbinding werkt.";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                toast.show();
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " +
                        Utility.getLocalToken(getApplicationContext()));
                return params;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("desc", ((EditText) findViewById(R.id.textfield_issueasset)).getText().toString());
                params.put("userID", String.valueOf(Utility.getLocalUserId(getApplicationContext())));
                params.put("issueID", String.valueOf(mIssueId));
                return params;
            }

            //Overriding the getByteData from the VolleyMultipartRequest
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                if (mCurrentPhotoPath != null) {
                    File file = new File(mCurrentPhotoPath);
                    if (file.isFile()) {
                        int size = (int) file.length();
                        byte[] bytes = new byte[size];
                        try {
                            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                            buf.read(bytes, 0, bytes.length);
                            buf.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        params.put("file", new DataPart("file_cover.jpg", bytes, "image/jpeg"));
                        Log.i(LOG_TAG, "SENDING TEXT + PICTURE");
                    } else {
                        Log.i(LOG_TAG, "SENDING ONLY TEXT");
                        params.put("file", new DataPart("file_cover.jpg", new byte[0], "image/jpeg"));
                    }
                } else {
                    params.put("file", new DataPart("file_cover.jpg", new byte[0], "image/jpeg"));
                    Log.i(LOG_TAG, "PICTURE PATH: " + mCurrentPhotoPath);
                }
                return params;
            }
        };//End of VolleyMultipartRequest

        //Adding the request to the requestQueue
        RESTSingleton.getInstance(getApplicationContext()).addToRequestQueue(multipartRequest);
    }


    /**
     * This ProgressDialog is showed when the user sends in a new IssueAsset text and/or picture
     * to the server.
     * If show is true, then the ProgressDialog will be showed, otherwise it is dismissed.
     *
     * @param show
     */
    private void showSendingProgressDialog(final boolean show) {
        if (show) {
            sendingDialog.show();
        } else {
            sendingDialog.dismiss();
        }
    }

    /**
     * Method to open the Picture App of the Android phone and take a picture with it.
     */
    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("ISSUE_DETAIL", ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Every picture that the user takes, is also available in the photo gallery.
     * This is the method where the imageFile gets created in the DIRECTORY_PICTURES.
     * The picture's name must be unique. Therefore the time when the picture was captured
     * is used to create a file with a unique filename.
     *
     * @return the file where the image is stored in
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Method called when the loader gets created
     *
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "Creating CursorLoader");

        // Sort order =  Ascending, by Posted Time
        String sortOrder = IssueContract.IssueAssetEntry.COLUMN_POST_TIME + " ASC";
        Uri issueAssetsOnIssueId = IssueContract.IssueAssetEntry
                .buildIssueAssetUri(mIssueId);
        Log.v(LOG_TAG, "CURSORLOADER URI FOR QUERIES: " + issueAssetsOnIssueId);

        return new CursorLoader(this,
                issueAssetsOnIssueId,
                DETAIL_COLUMNS,
                null,
                null,
                sortOrder);
    }

    /**
     * Method called when loader is finished
     *
     * @param loader
     * @param cursor
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mListAdapter.swapCursor(cursor);

        //Hide progressbar
        //showProgress(false);

        //When no tasks assigned, display message
        if (cursor.getCount() == 0) {
            showListEmptyText(true);
        } else {
            showListEmptyText(false);
        }

        Log.v(LOG_TAG, "onLoadFinished: Loader cursor swapped, cursorCount = " + cursor.getCount());
    }

    /**
     * Called when loader resets
     *
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG, "Loader onLoaderReset");
        mListAdapter.swapCursor(null);
    }
}
