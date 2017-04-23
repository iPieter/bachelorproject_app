package televic.project.kuleuven.televicmechanicassistant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

/**
 * Created by Matthias on 23/04/2017.
 */

public class Utility {
    private static final String LOG_TAG = Utility.class.getSimpleName();

    //Adapt the DEBUG_MODE here
    public static final boolean DEBUG_MODE = false;
    public static final boolean DEBUG_SKIP_LOGIN = false; //TODO DELETE THIS

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

        SharedPreferences.Editor editor = pref.edit();
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

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(USER_ID_TAG, user_id);
        editor.putString(USER_NAME_TAG, user_name);
        editor.apply();
        Log.v(LOG_TAG, "SHARED_PREF: user info saved: id=" + user_id + ", name=" + user_name);
    }

    /**
     * Check if the userId is valid.
     * @param id the userid
     * @return true if id is valid
     */
    public static boolean isUserIdValid(int id){
        Log.v(LOG_TAG,"Validating user_id="+id);
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
                Log.v(LOG_TAG, "Redirecting to Login");
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
            }
        }
    }
}
