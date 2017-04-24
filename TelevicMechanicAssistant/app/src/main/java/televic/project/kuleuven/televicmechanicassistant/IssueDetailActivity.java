package televic.project.kuleuven.televicmechanicassistant;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import televic.project.kuleuven.televicmechanicassistant.data.IssueContract;

public class IssueDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = IssueDetailActivity.class.getSimpleName();

    private static final int REQUEST_TAKE_PHOTO = 1;

    //Members
    private String mCurrentPhotoPath;
    private IssueAssetListAdapter mListAdapter;
    private HashMap<Integer, Bitmap> mImageMap;
    private int mCurrentUserId;
    private int mIssueId;
    private int mDataId;

    //GUI components
    private ProgressDialog sendingDialog;
    private ProgressDialog loadingDialog;

    //Id of the loader
    private static final int DETAIL_LOADER = 1;

    //Values in Database needed in this activity
    private static final String[] DETAIL_COLUMNS = {
            IssueContract.IssueAssetEntry.TABLE_NAME + "." + IssueContract.IssueAssetEntry._ID,
            IssueContract.IssueAssetEntry.COLUMN_DESCRIPTION,
            IssueContract.IssueAssetEntry.COLUMN_POST_TIME,
            IssueContract.IssueAssetEntry.COLUMN_IMAGE_PRESENT,
            IssueContract.IssueAssetEntry.COLUMN_USER_NAME,
            IssueContract.IssueAssetEntry.COLUMN_USER_EMAIL
    };

    //Depends on DETAIL_COLUMNS, if DETAIL_COLUMNS changes, so must these indexes!
    static final int COL_ASSET_ID = 0;
    static final int COL_ASSET_DESCRIPTION = 1;
    static final int COL_ASSET_POST_TIME = 2;
    static final int COL_ASSET_IMAGE = 3;
    static final int COL_ASSET_USER_NAME = 4;
    static final int COL_ASSET_USER_EMAIL = 5;

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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //INIT get values from intent
        mIssueId = getIntent().getIntExtra(IssueOverviewFragment.INTENT_ISSUE_ID, -1);
        mDataId = getIntent().getIntExtra(IssueOverviewFragment.INTENT_DATA_ID, -1);

        //INIT current user
        mCurrentUserId = Utility.getLocalUserId(this);

        //INIT Send-Button
        ImageButton buttonSend = (ImageButton) findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                postIssueAsset();
            }
        });

        //INIT Photo-Button
        ImageButton buttonCamera = (ImageButton) findViewById(R.id.button_camera);
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePicture();
            }
        });

        //INIT mImageMap
        mImageMap = new HashMap<>();

        //INIT adapter
        mListAdapter = new IssueAssetListAdapter(getApplicationContext(), null, 0);
        ListView listView = (ListView) findViewById(R.id.issue_asset_link);
        listView.setAdapter(mListAdapter);

        //INIT loader
        getSupportLoaderManager().initLoader(DETAIL_LOADER, null, this);

        //INIT loading dialog
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setTitle("Laden..");
        loadingDialog.setMessage("De boodschappen worden geladen");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        //INIT sending dialog
        sendingDialog = new ProgressDialog(this);
        sendingDialog.setTitle("Versturen");
        sendingDialog.setMessage("De boodschap wordt verstuurd.");
        sendingDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog

        //Calling Backend
        fetchIssueAssetImages();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_overview, menu);
        getMenuInflater().inflate(R.menu.extra_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //TODO create settings
            //startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_graphs) {
            goToGraphActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToGraphActivity() {
        Intent intent = new Intent(this, GraphActivity.class);
        intent.putExtra(IssueOverviewFragment.INTENT_DATA_ID, mDataId);
        startActivity(intent);
    }

    public void onResume() {
        super.onResume();

        /*
        if( mCurrentPhotoPath != null ) {
            Log.i( LOG_TAG, "path:" + mCurrentPhotoPath );
            File imgFile = new  File( mCurrentPhotoPath );
            Log.i( LOG_TAG, "path2:" + imgFile.exists() + ":" + imgFile.getAbsolutePath() );
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ImageView myImage = (ImageView) findViewById(R.id.image_preview);
                myImage.setImageBitmap(myBitmap);
            }
        }
        */
    }


    /**
     * First, all issueAssets with current issueId and contain an img are queried.
     * If IMAGE_LOCATION equals "IMG", then a REST request for this IssueAssetId is created
     * and added to the RequestQueue. Volley handles all requests.
     * If we get a response from volley, we store the location of the img in the HashMap.
     * We store the IMG as a blob in the database. The cursorLoader will notify the change
     * and call the cursorAdapter to update the ListView.
     */
    public void fetchIssueAssetImages() {
        //Fetching all Images of those issueAssets that have an Image
        Uri assetsWithImgUri = IssueContract.IssueAssetEntry
                .buildIssueAssetWithImgUri(mIssueId);
        Cursor cursor = getContentResolver().query(
                assetsWithImgUri, REST_COLUMNS, null, null, null);

        String baseUrl = RESTSingleton.BASE_URL + "/" + RESTSingleton.ISSUE_ASSET_PATH;
        String url;
        //Iterate over Cursor's rows
        cursor.moveToFirst();
        while (cursor.isAfterLast()) {
            //Only fetch Image if Blob of image is not present in cache.
            if (cursor.getBlob(COL_REST_IMAGE_BLOB).length == 0) {
                //The URL to fetch the image for a certain issueAssetId
                url = baseUrl + "/" + cursor.getInt(COL_ASSET_ID);

                //Create the REST ImageRequest
                ImageRequest request = new ImageRequest(url,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {
                                removeLoadingProgress();
                                //TODO insert blob to IMAGE_BLOB with db UPDATE method
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(IssueContract.IssueAssetEntry.COLUMN_IMAGE_BLOB,
                                        response.byt);
                                getContentResolver().insert(MyBaseColumn.MyTable.CONTENT_URI, values);
                            }
                        }, 350, 350, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565,
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                Log.e(LOG_TAG, "Image fetch FAILED");

                                removeLoadingProgress();
                                Context context = getApplicationContext();
                                CharSequence text = "De afbeeldingen konden niet geladen worden, probeer later opnieuw.";
                                int duration = Toast.LENGTH_LONG;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }
                        });

                //Adding the request to the requestQueue, Volley handles the rest
                RESTSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
            }

            cursor.moveToNext();
        }
    }

    /**
     * The user can upload a new IssueAsset with picture and description to the server.
     * Also locally on the app, the list must be updated.
     */
    public void postIssueAsset() {
        /*
        sendingDialog.show();

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {

                    //TODO INSERT NEW TUPLE IN DB
                    IssueAsset asset = new IssueAsset();
                    if (mCurrentPhotoPath == null)
                        asset.setLocation("");
                    else
                        asset.setLocation("azeaze");
                    asset.setUser(user);
                    asset.setDescr(((EditText) findViewById(R.id.textfield_issueasset)).getText().toString());
                    asset.setTime(new Date());

                    assets.add(asset);
                    mListAdapter.updateView(assets);

                    JSONObject result = new JSONObject(resultResponse);
                    String status = result.getString("status");
                    String message = result.getString("message");

                    Log.i(LOG_TAG, status);
                    Log.i(LOG_TAG, message);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mCurrentPhotoPath = null;
                removeProgress();
                ((EditText) findViewById(R.id.textfield_issueasset)).setText("");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mCurrentPhotoPath = null;
                removeProgress();

                Context context = getApplicationContext();
                CharSequence text = "De boodschap kon niet verzonden worden, controleer of u internetverbinding werkt.";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message + " Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message + " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message + " Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("api_token", Utility.getLocalToken(getApplicationContext()));
                params.put("desc", ((EditText) findViewById(R.id.textfield_issueasset)).getText().toString());
                params.put("userID", String.valueOf(Utility.getLocalUserId(getApplicationContext())));
                params.put("issueID", String.valueOf(mIssueId));
                return params;
            }

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
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
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
        };

        RESTSingleton.getInstance(getApplicationContext()).addToRequestQueue(multipartRequest);
    */
    }


    private void removeProgress() {
        sendingDialog.dismiss();
    }

    private void removeLoadingProgress() {
        loadingDialog.dismiss();
    }

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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "Creating CursorLoader");

        // Sort order =  Ascending, by Posted Time
        String sortOrder = IssueContract.IssueAssetEntry.COLUMN_POST_TIME + " ASC";
        Uri issueAssetsOnIssueId = IssueContract.IssueAssetEntry
                .buildIssueAssetUri(mCurrentUserId);
        Log.v(LOG_TAG, "CURSORLOADER URI: " + issueAssetsOnIssueId);

        return new CursorLoader(this,
                issueAssetsOnIssueId,
                DETAIL_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mListAdapter.swapCursor(cursor);
        Log.v(LOG_TAG, "onLoadFinished: Loader cursor swapped, cursorCount = " + cursor.getCount());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG, "Loader onLoaderReset");
        mListAdapter.swapCursor(null);
    }

    /*
    //TODO delete
    public void staticDataLoading(){
        user = new User();
        user.setName( "Jan Met De Pet" );

        final IssueAsset asset = new IssueAsset();
        asset.setId( 0 );
        asset.setDescr( "De wagon vertoont een afwijking aan zijn roll waarden. Controleer het onderstel vooraan." );
        asset.setTime( new Date( ) );
        asset.setUser( user );
        asset.setLocation( "azerazer" );

        IssueAsset asset1 = new IssueAsset();
        asset1.setId( 1 );
        asset1.setDescr( "Voluptas molestiae quo voluptas ut ut totam quia. Quibusdam amet labore eos perspiciatis delectus doloribus. Ipsa maiores doloremque culpa iste.\n" +
                "Velit rerum inventore quia sunt. Libero dolores rerum eos nulla explicabo voluptas ratione. Rem sit dolorem voluptate culpa perspiciatis omnis et enim. Sequi ab qui qui voluptatem in dolorem. Illum expedita odit libero enim expedita et doloribus.\n" +
                "Vel deleniti et est consequuntur corporis repellendus molestiae consequatur. Sed nostrum est unde aut occaecati illo ut omnis. Perspiciatis optio est at. Doloremque perspiciatis dignissimos maiores assumenda vitae.\n" +
                "Consectetur nesciunt vel excepturi asperiores earum est veritatis. Ducimus et sequi et voluptas aliquid vitae aut. Non dolor quasi non sunt inventore. Occaecati harum fuga est. Nemo et et illo modi est." );
        asset1.setTime( new Date( ) );
        asset1.setUser( user );
        asset1.setLocation( "" );

        IssueAsset asset2 = new IssueAsset();
        asset2.setId( 2 );
        asset2.setDescr( "Test of de remmen nog goed werken." );
        asset2.setTime( new Date( ) );
        asset2.setUser( user );
        asset2.setLocation( "azerazerazr" );

        assets.add( asset );
        assets.add( asset1 );
        assets.add( asset2 );
    }
    */
}
