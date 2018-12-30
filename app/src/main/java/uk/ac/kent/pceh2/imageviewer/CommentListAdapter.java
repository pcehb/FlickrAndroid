package uk.ac.kent.pceh2.imageviewer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Phoebe on 21/12/2017.
 */

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView commentOwner;
        public TextView comment;


        public ViewHolder(View itemView) {
            super(itemView);
            comment = (TextView) itemView.findViewById(R.id.comment);
            commentOwner = (TextView) itemView.findViewById(R.id.commentOwner);
        }
    }


    public ArrayList<CommentInfo> commentList = new ArrayList<CommentInfo>();

    public Context context;

    public CommentListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public CommentListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_layout, parent, false);

        CommentListAdapter.ViewHolder vh = new CommentListAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(CommentListAdapter.ViewHolder holder, int position) {
        CommentInfo imageInformation = commentList.get(position);

        holder.comment.setText(imageInformation._content);
        holder.commentOwner.setText(imageInformation.realname);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

}