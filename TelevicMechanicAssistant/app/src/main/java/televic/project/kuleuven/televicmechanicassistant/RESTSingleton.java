package televic.project.kuleuven.televicmechanicassistant;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * The RESTSingleton is a singleton that should be created with the ApplicationContext.
 * This way, any request can be handled by Volley through the app's lifecycle.
 * Created by Matthias on 28/03/2017.
 */

public class RESTSingleton{
    private final String LOG_TAG = RESTSingleton.class.getSimpleName();

    public final static String BASE_URL = "http://ec2-54-202-94-106.us-west-2.compute.amazonaws.com:8080/DWPProject/rest";
    public final static String ISSUES_ALL_FOR_USER_PATH = "issues/all_for_user";
    public final static String ISSUES_PATH = "issues";
    public final static String WORKPLACE_PATH = "workplace/get_by_user_id";

    public final static String LOGIN_PATH = "login";
    public final static String ISSUE_ASSET_PATH = "assets/issue";

    private static RESTSingleton mInstance = null;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    private RESTSingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
        Log.v(LOG_TAG, "RESTSingleton intialized: current context = " + mCtx +", appcontext = "+ mCtx.getApplicationContext());
    }

    /**
     * Returning the RESTSingleton instance
     * @param context
     * @return
     */
    public static synchronized RESTSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RESTSingleton(context);
        }
        return mInstance;
    }

    /**
     * return the Volley requestQueue
     * @return
     */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    /**
     * Method to add the request to the requestQueue.
     * @param req
     * @param <T>
     */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
        Log.v(LOG_TAG, "REST request added to requestqueue: " + req.toString());
    }

    /**
     * Returning the imageLoader.
     * @return
     */
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
