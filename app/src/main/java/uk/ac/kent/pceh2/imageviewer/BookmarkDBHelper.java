package uk.ac.kent.pceh2.imageviewer;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Phoebe on 20/12/2017.
 */
public class BookmarkDBHelper extends SQLiteOpenHelper {



    public BookmarkDBHelper(Context context) {
        super(context, "bookmarksDB", null  , 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //creates database

        db.execSQL("CREATE  TABLE bookmarks " +
                "(bookmarkId INTEGER PRIMARY KEY AUTOINCREMENT, id CHAR(120), imageResource CHAR(120), description CHAR(2220), owner_name CHAR(120), owner_id CHAR(120), owner CHAR(120), url_l CHAR(1200),  url_o CHAR(1200), title CHAR(1200),url_m CHAR(1200));");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(String id, int imageResource, String description, String owner_name,String owner, String url_l, String url_o,String title, String url_m) {
        //inserts data into database

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("imageResource", imageResource);
        values.put("description", description);
        values.put("owner_name", owner_name);
        values.put("owner", owner);
        values.put("url_l", url_l);
        values.put("url_o", url_o);
        values.put("url_m", url_m);
        values.put("title", title);
        db.insert("bookmarks", null, values);

        db.close();

    }

    public void remove(int itemId) {
        //removes data from database

        SQLiteDatabase db = this.getWritableDatabase();

        String condition = "bookmarkId = " + itemId;
        db.delete("bookmarks", condition, null);

    }

}
