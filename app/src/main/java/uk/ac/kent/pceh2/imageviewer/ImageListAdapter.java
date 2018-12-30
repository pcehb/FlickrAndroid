package uk.ac.kent.pceh2.imageviewer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;


/**
 * Created by pceh2 on 10/11/2017.
 */

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder> {


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView imageOwner;
        public TextView imageTitle;
        public TextView imageDescription;
        public NetworkImageView ownerImage;
        public NetworkImageView mainImage;

        View.OnClickListener imageClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) { //goes to details page when image clicked on

                int position = ViewHolder.this.getLayoutPosition();

                Intent intent = new Intent(view.getContext(), DetailsActivity.class);

                intent.putExtra("PHOTO_POSITION", position);
                NetworkMgr.getInstance(context).imageList = imageList;

                context.startActivity(intent);
            }
        };

        public ViewHolder(View itemView) {
            super(itemView);
            imageTitle = (TextView) itemView.findViewById(R.id.imageTitle);
            imageOwner = (TextView) itemView.findViewById(R.id.imageOwnerName);
            imageDescription = (TextView) itemView.findViewById(R.id.imageDescription);
            mainImage = (NetworkImageView) itemView.findViewById(R.id.mainImage);
            ownerImage = (NetworkImageView) itemView.findViewById(R.id.ownerImage);
            mainImage.setOnClickListener(imageClick);
        }

    }


    public ArrayList<ImageInfo> imageList = new ArrayList<ImageInfo>();

    public Context context;

    public ImageListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.cell_image_card, parent, false);

        ImageListAdapter.ViewHolder vh = new ImageListAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ImageListAdapter.ViewHolder holder, int position) { //sets information as the text
        ImageInfo imageInformation = imageList.get(position);

        holder.imageTitle.setText(imageInformation.title);
        holder.imageOwner.setText(imageInformation.owner_name);
        holder.imageDescription.setText(imageInformation.description);

        holder.ownerImage.setImageUrl(imageInformation.owner, NetworkMgr.getInstance(context).imageLoader);
        holder.mainImage.setImageUrl(imageInformation.url_m, NetworkMgr.getInstance(context).imageLoader);

    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

}