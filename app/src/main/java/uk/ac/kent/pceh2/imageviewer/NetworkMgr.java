package uk.ac.kent.pceh2.imageviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

/**
 * Created by pceh2 on 17/11/2017.
 */

public class NetworkMgr {
    private Context context;
    private static NetworkMgr instance;
    public RequestQueue requestQueue;
    ArrayList<ImageInfo> imageList;
    ArrayList<CommentInfo> commentList;
    public ArrayList<BookmarkInfo> bookmarkList;

    public ImageLoader imageLoader;

    private ImageLoader.ImageCache imageCache = new ImageLoader.ImageCache() {
        private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20);

        @Override
        public Bitmap getBitmap(String url) {
            return cache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            cache.put(url, bitmap);
        }
    };



    public NetworkMgr(Context context) {
        this.context = context;

        requestQueue = Volley.newRequestQueue(context.getApplicationContext());

        commentList = new ArrayList<CommentInfo>();

        imageList = new ArrayList<ImageInfo>();

        imageLoader = new ImageLoader(requestQueue, imageCache);
    }

    public static NetworkMgr getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkMgr(context.getApplicationContext());
        }
        return instance;
    }
}
