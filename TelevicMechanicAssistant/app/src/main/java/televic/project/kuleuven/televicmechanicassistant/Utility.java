package televic.project.kuleuven.televicmechanicassistant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Class for Utility methods. This are methods that are useful to many activities.
 * Created by Matthias on 23/04/2017.
 */

public class Utility {
    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static final String SHARED_PREF = "main_shared_pref";
    public static final int PRIVATE_MODE = 0;

    //Values in SharedPref
    public static final String TOKEN_TAG = "token_login";
    public static final String USER_ID_TAG = "user_id";
    public static final String USER_NAME_TAG = "user_name";

    //ErrorCodes Http
    public static final int UNAUTHORIZED = 401;


    /**
     * Fetching locally stored TOKEN in sharedPreferences.
     *
     * @return null if no TOKEN present
     */
    public static String getLocalToken(Context context) {
        SharedPreferences pref = context.getApplicationContext()
                .getSharedPreferences(SHARED_PREF, PRIVATE_MODE);

        //Returning null if TOKEN_TAG not present
        String token = pref.getString(TOKEN_TAG, null);

        return token;
    }

    /**
     * Fetching locally stored USER_ID in sharedPreferences.
     *
     * @return null if no USER_ID present
     */
    public static int getLocalUserId(Context context) {
        SharedPreferences pref = context.getApplicationContext()
                .getSharedPreferences(SHARED_PREF, PRIVATE_MODE);

        //Returning null if USER_ID_TAG not present
        int id = pref.getInt(USER_ID_TAG, -1);

        return id;
    }

    /**
     * Fetching locally stored USER_NAME in sharedPreferences.
     *
     * @return null if no USER_NAME present
     */
    public static String getLocalUserName(Context context) {
        SharedPreferences pref = context.getApplicationContext()
                .getSharedPreferences(SHARED_PREF, PRIVATE_MODE);

        //Returning null if USER_NAME_TAG not present
        String name = pref.getString(USER_NAME_TAG, null);

        return name;
    }

    /**
     * Putting TOKEN in sharedPreferences.
     * Overwrites if TOKEN_TAG already present.
     */
    public static void putLocalToken(Context context, String token) {
        SharedPreferences pref = context.getApplicationContext()
                .getSharedPreferences(SHARED_PREF, PRIVATE_MODE);

        Editor editor = pref.edit();
        editor.putString(TOKEN_TAG, token);
        editor.apply();
        Log.v(LOG_TAG, "SHARED_PREF: local token saved: " + token);
    }

    /**
     * Putting user info in sharedPreferences.
     * Overwrites if already present.
     */
    public static void putLocalUserInfo(Context context, int user_id, String user_name) {
        SharedPreferences pref = context.getApplicationContext()
                .getSharedPreferences(SHARED_PREF, PRIVATE_MODE);

        Editor editor = pref.edit();
        editor.putInt(USER_ID_TAG, user_id);
        editor.putString(USER_NAME_TAG, user_name);
        editor.apply();
        Log.v(LOG_TAG, "SHARED_PREF: user info saved: id=" + user_id + ", name=" + user_name);
    }

    /**
     * Removing locally stored TOKEN in sharedPreferences, if present.
     */
    public static void removeLocalToken(Context context) {
        SharedPreferences pref = context.getApplicationContext()
                .getSharedPreferences(SHARED_PREF, PRIVATE_MODE);

        Editor editor = pref.edit();
        editor.remove(TOKEN_TAG);
        editor.apply();
        Log.v(LOG_TAG,"Token removed!");
    }

    /**
     * Check if the userId is valid.
     *
     * @param id the userid
     * @return true if id is valid
     */
    public static boolean isUserIdValid(int id) {
        Log.v(LOG_TAG, "Validating user_id=" + id);
        return id >= 0;
    }

    /**
     * Must be called in every REST call, because the app uses tokens to check if authorized.
     * If current user is unauthorized, the user gets redirected to the login.
     * In the login a user can receive a new token by correctly logging in.
     *
     * @param error the VolleyError to be handled.
     */
    public static void redirectIfUnauthorized(Context context, VolleyError error) {
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            if (networkResponse.statusCode == Utility.UNAUTHORIZED) {
                //Redirect to login
                redirectToLogin(context);
            }
        }
    }

    /**
     * The user gets redirected to the login screen.
     * In all cases: if a token is present, it must be deleted.
     * @param context
     */
    public static void redirectToLogin(Context context){
        //Delete token if present
        removeLocalToken(context);

        Log.v(LOG_TAG, "Redirecting to Login");
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    /**
     * Method to convert a Bitmap to a Byte array.
     * @param bitmap
     * @return the Byte array
     */
    public static byte[] toByteArray(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;

        Log.v(LOG_TAG,"toByteArray");

        //Compress the bitmap and write to the ByteArrayOutputStream
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, baos);

        return baos.toByteArray();
    }

    /**
     * Method to convert a Byte array to a Bitmap
     * @param byteArray
     * @return the Bitmap
     */
    public static Bitmap toBitmap(byte[] byteArray){
        ByteArrayInputStream imageStream = new ByteArrayInputStream(byteArray);

        return BitmapFactory.decodeStream(imageStream);
    }

    /**
     * Method to convert picture in external storage to a byte array
     * @param path
     * @return byte array of image, if it is present. null if no image present on path.
     */
    public static byte[] getBytesFromPicture(String path){
        Log.v(LOG_TAG,"Getting bytes from picture");
        byte[] blob=null;
        if (path != null) {
            File file = new File(path);

            //Security check
            if (file.isFile()) {
                Bitmap bitmap = convertToCompressedBitmap(path);
                blob = toByteArray(bitmap);
            }
        }
        return blob;
    }

    /**
     * Method to convert a file from the selected path to a bitmap
     * @param filePath
     * @return
     */
    public static Bitmap convertToCompressedBitmap(String filePath){
        Log.v(LOG_TAG,"Compressing bitmap");
        Bitmap bitmap= BitmapFactory.decodeFile(filePath);
        return Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), 350, true);
    }
}
