package uk.ac.kent.pceh2.imageviewer;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

/**
 * Created by Phoebe on 20/12/2017.
 */


public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {

    public ArrayList<BookmarkInfo> bookmarkList = new ArrayList<BookmarkInfo>();

    public Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView imageOwner;
        public TextView imageTitle;
        public TextView imageDescription;
        public NetworkImageView ownerImage;
        public NetworkImageView mainImage;
        ImageButton imageDelete;


        public ViewHolder(View itemView) {
            super(itemView);
            imageTitle = (TextView) itemView.findViewById(R.id.imageTitle);

            imageOwner = (TextView) itemView.findViewById(R.id.imageOwnerName);
            imageDescription = (TextView) itemView.findViewById(R.id.imageDescription);

            mainImage = (NetworkImageView) itemView.findViewById(R.id.mainImage);
            ownerImage = (NetworkImageView) itemView.findViewById(R.id.ownerImage);
            imageDelete = (ImageButton) itemView.findViewById(R.id.imageDelete);

            imageDelete.setClickable(true);
            imageDelete.setFocusable(true);
            // Remove items when they are selected
            imageDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = getAdapterPosition();
                    final BookmarkInfo bookmark = bookmarkList.get(position);

                    // Create dialog to confirm
                    AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(context);
                    dlgBuilder.setMessage("Delete Bookmark?");

                    dlgBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // If the user confirms deletion, use the dbHelper to remove item
                            BookmarkDBHelper dbHelper = new BookmarkDBHelper(context);

                            // Remove from database
                            dbHelper.remove(bookmark.bookmarkId);
                            // Remove from arraylist
                            bookmarkList.remove(position);
                            // Remove from recyclerview
                            notifyItemRemoved(position);

                            Toast.makeText(context, "Bookmark removed", Toast.LENGTH_LONG).show();

                        }
                    });
                    dlgBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog dlg = dlgBuilder.create();
                    dlg.show();
                }
            });
        }
    }


    public BookmarkAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.cell_image_fave_card, parent, false);

        BookmarkAdapter.ViewHolder vh = new BookmarkAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(BookmarkAdapter.ViewHolder holder, int position) {
        if (position < bookmarkList.size()) {
            BookmarkInfo imageInformation = bookmarkList.get(position);

            holder.imageTitle.setText(imageInformation.title);
            holder.imageOwner.setText(imageInformation.owner_name);
            holder.imageDescription.setText(imageInformation.description);
            holder.ownerImage.setImageUrl(imageInformation.owner, NetworkMgr.getInstance(context).imageLoader);
            holder.mainImage.setImageUrl(imageInformation.url_m, NetworkMgr.getInstance(context).imageLoader);
        }
    }

    @Override
    public int getItemCount() {
        return bookmarkList.size();
    }

}
