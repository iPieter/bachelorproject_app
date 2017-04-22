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
 * Created by Matthias on 28/03/2017.
 */

public class RESTSingleton{
    private final String LOG_TAG = RESTSingleton.class.getSimpleName();

    public final static String BASE_URL = "http://192.168.1.4:8080/DWPProject-0.0.1-SNAPSHOT/rest";
    public final static String ISSUES_PATH = "issues/all_for_user";
    public final static String WORKPLACE_PATH = "workplace/all";

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

    public static synchronized RESTSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RESTSingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
        Log.v(LOG_TAG, "REST request added to requestqueue: " + req.toString());
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
