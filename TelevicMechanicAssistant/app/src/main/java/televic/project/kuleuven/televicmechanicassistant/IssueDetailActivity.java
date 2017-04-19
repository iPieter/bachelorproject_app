package televic.project.kuleuven.televicmechanicassistant;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.issue.IssueAsset;
import model.user.User;

public class IssueDetailActivity extends AppCompatActivity {

    private static final String LOG = "ISSUE_DETAIL";
    private static final int REQUEST_TAKE_PHOTO = 1;

    private String mCurrentPhotoPath;
    private IssueAssetListAdapter mListAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageButton buttonSend = (ImageButton) findViewById( R.id.button_send );
        buttonSend.setOnClickListener( new View.OnClickListener() {
            public void onClick( View v ) {
                sendIssueAsset();
            }
        } );

        ImageButton buttonCamera = (ImageButton) findViewById( R.id.button_camera );
        buttonCamera.setOnClickListener( new View.OnClickListener() {
            public void onClick( View v ) {
                takePicture();
            }
        } );

        mListAdapter = new IssueAssetListAdapter( getApplicationContext() );
        ListView listView = (ListView) findViewById( R.id.issue_asset_link );
        listView.setAdapter( mListAdapter );

        User user = new User();
        user.setName( "TEST" );

        List<IssueAsset> assets = new ArrayList<>( );
        IssueAsset asset = new IssueAsset();
        asset.setId( 0 );
        asset.setDescr( "test0" );
        asset.setTime( new Date( ) );
        asset.setUser( user );

        IssueAsset asset1 = new IssueAsset();
        asset1.setId( 1 );
        asset1.setDescr( "test1" );
        asset1.setTime( new Date( ) );
        asset1.setUser( user );

        IssueAsset asset2 = new IssueAsset();
        asset2.setId( 2 );
        asset2.setDescr( "test2" );
        asset2.setTime( new Date( ) );
        asset2.setUser( user );

        assets.add( asset );
        assets.add( asset1 );
        assets.add( asset2 );

        mListAdapter.updateView( assets );
    }

    public void onResume()
    {
        super.onResume();

        /*
        if( mCurrentPhotoPath != null ) {
            Log.i( LOG, "path:" + mCurrentPhotoPath );
            File imgFile = new  File( mCurrentPhotoPath );
            Log.i( LOG, "path2:" + imgFile.exists() + ":" + imgFile.getAbsolutePath() );
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ImageView myImage = (ImageView) findViewById(R.id.image_preview);
                myImage.setImageBitmap(myBitmap);
            }
        }
        */
    }

    public void sendIssueAsset() {
        String url = "http://10.108.0.132:8080/DWPProject-0.0.1-SNAPSHOT/rest/assets/issue";
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest( Request.Method.POST, url, new Response.Listener<NetworkResponse >() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    String status = result.getString("status");
                    String message = result.getString("message");

                    Log.i( LOG, status );
                    Log.i( LOG, message );

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mCurrentPhotoPath = null;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mCurrentPhotoPath = null;

                Context context = getApplicationContext();
                CharSequence text = "De boodschap kon niet verzonden worden, controleer u internetverbinding.";
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
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
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
                params.put( "api_token", "gh659gjhvdyudo973823tt9gvjf7i6ric75r76" );
                params.put( "desc", (( EditText)findViewById( R.id.textfield_issueasset )).getText().toString() );
                params.put( "userID", "1" );
                params.put( "issueID", "1" );
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                if( mCurrentPhotoPath != null ) {
                    File file = new File( mCurrentPhotoPath );
                    if( file.isFile() ) {
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
                        Log.i( LOG, "SENDING TEXT + PICTURE" );
                    } else {
                        Log.i( LOG, "SENDING ONLY TEXT" );
                        params.put("file", new DataPart("file_cover.jpg", new byte[0], "image/jpeg"));
                    }
                }else
                {
                    params.put("file", new DataPart("file_cover.jpg", new byte[0], "image/jpeg"));
                    Log.i( LOG, "PICTURE PATH: " + mCurrentPhotoPath );
                }
                return params;
            }
        };

        RESTSingleton.getInstance(getApplicationContext()).addToRequestQueue(multipartRequest);
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
                Log.e( "ISSUE_DETAIL", ex.getMessage() );
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

    private File createImageFile() throws IOException
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir( Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

}
