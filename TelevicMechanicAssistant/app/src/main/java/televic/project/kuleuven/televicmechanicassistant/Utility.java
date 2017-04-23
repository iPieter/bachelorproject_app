package televic.project.kuleuven.televicmechanicassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Matthias on 23/04/2017.
 */

public class Utility {
    private static final String LOG_TAG = Utility.class.getSimpleName();

    //Adapt the DEBUG_MODE here
    public static final boolean DEBUG_MODE = false;

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
        Log.v(LOG_TAG,"SHARED_PREF: local token saved: "+token);
    }

    public static void putLocalUserInfo(Context context, int user_id, String user_name) {
        SharedPreferences pref = context.getApplicationContext()
                .getSharedPreferences(SHARED_PREF, PRIVATE_MODE);

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(USER_ID_TAG, user_id);
        editor.putString(USER_NAME_TAG, user_name);
        editor.apply();
        Log.v(LOG_TAG,"SHARED_PREF: user info saved: id="+user_id+", name="+user_name);
    }
}
