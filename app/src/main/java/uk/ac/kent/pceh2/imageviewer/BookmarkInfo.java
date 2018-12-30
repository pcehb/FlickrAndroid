package uk.ac.kent.pceh2.imageviewer;

/**
 * Created by Phoebe on 20/12/2017.
 */

public class BookmarkInfo {

    int bookmarkId;
    String title;
    int imageResource;
    String id;
    String description;
    String owner_name;
    String url_m;
    String url_l;
    String url_o;
    String owner;

    public BookmarkInfo(int bookmarkId, String id, int imageResource, String description, String owner_name, String owner, String url_l, String url_o,String title, String url_m) {
        this.bookmarkId = bookmarkId;
        this.title = title;
        this.imageResource = imageResource;
        this.id = id;
        this.description = description;
        this.owner_name = owner_name;
        this.url_m = url_m;
        this.url_l = url_l;
        this.url_o = url_o;
        this.owner = owner;
    }
}
