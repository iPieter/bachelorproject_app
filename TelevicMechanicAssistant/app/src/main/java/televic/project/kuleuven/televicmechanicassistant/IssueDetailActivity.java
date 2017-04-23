package televic.project.kuleuven.televicmechanicassistant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import model.issue.IssueAsset;
import televic.project.kuleuven.televicmechanicassistant.data.IssueContract;

public class IssueDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = IssueDetailActivity.class.getSimpleName();

    private static final String url = RESTSingleton.BASE_URL + "/assets/issue";
    private static final int REQUEST_TAKE_PHOTO = 1;

    //Members
    private String mCurrentPhotoPath;
    private IssueAssetListAdapter mListAdapter;
    private HashMap<Integer, Bitmap> mImageMap;
    private int mCurrentUserId; //TODO init

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
            IssueContract.IssueAssetEntry.COLUMN_IMAGE_LOCATION,
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //INIT current user
        mCurrentUserId = Utility.getLocalUserId(this);
        
        //INIT buttons
        Button button = new Button(this);
        button.setText("Grafieken");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToGraphActivity();
            }
        });
        toolbar.addView(button);

        ImageButton buttonSend = (ImageButton) findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                postIssueAsset();
            }
        });

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
    }

    private void goToGraphActivity(int data_id) {
        Intent intent = new Intent(this, GraphActivity.class);
        intent.putExtra("psdid", "1"); //TODO give data id!
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

    public void fetchIssueAssetImage() {
        ImageRequest request = new ImageRequest(url + "/7", new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                removeLoadingProgress();
                //TODO link image to tupple: in hashmap bijhouden OF imagenaam, die wordt opgeslaan
                asset.setBitmap(response);
                mListAdapter.updateView(assets);
            }
        }, 350, 350, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                removeLoadingProgress();

                Context context = getApplicationContext();
                CharSequence text = "De afbeeldingen konden niet geladen worden, probeer later opnieuw.";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                mListAdapter.updateView(assets);
            }
        });
        RESTSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    public void postIssueAsset() {

        sendingDialog.show();

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {

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
                params.put("api_token", "gh659gjhvdyudo973823tt9gvjf7i6ric75r76");
                params.put("desc", ((EditText) findViewById(R.id.textfield_issueasset)).getText().toString());
                params.put("userID", "1");
                params.put("issueID", "1");
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
