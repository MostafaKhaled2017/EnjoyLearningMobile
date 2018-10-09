package com.mk.playAndLearn.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.PostInDetailsActivity;
import com.mk.playAndLearn.model.Post;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.MyHolder>{

    ArrayList<Post> list;
    Context context;

    public PostsAdapter(ArrayList<Post> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.post_item,parent,false);
        MyHolder myHolder = new MyHolder(view);

        return myHolder;
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        final Post post = list.get(position);
        if(post.getContent() != null)
            holder.content.setText(post.getContent());
        if(post.getDate() != null)
            holder.date.setText(post.getDate());
        if(post.getWriter() != null && !post.getWriter().equals(""))
            holder.name.setText(post.getWriter());
        if(post.getImage() != null && !post.getImage().equals(""))
            Picasso.with(context).load(post.getImage()).placeholder(R.drawable.picasso_placeholder).into(holder.imageView);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostInDetailsActivity.class);
                intent.putExtra("content", post.getContent());
                intent.putExtra("date", post.getDate());
                intent.putExtra("name", post.getWriter());
                intent.putExtra("image", post.getImage());
                if(post.getId() != null)
                    intent.putExtra("id", post.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {

        int arr = 0;

        try{
            if(list.size()==0) {
                arr = 0;
            }
            else{
                arr=list.size();
            }
        }catch (Exception e){}

        return arr;

    }

    class MyHolder extends RecyclerView.ViewHolder{
        TextView content, date, name;
        ImageView imageView;
        CardView cardView;


        public MyHolder(View itemView) {
            super(itemView);
            content =  itemView.findViewById(R.id.postContentInDetails);
            date = itemView.findViewById(R.id.postDateInDetails);
            name = itemView.findViewById(R.id.postUserNameInDetails);
            imageView = itemView.findViewById(R.id.postImageInDetails);
            cardView = itemView.findViewById(R.id.card_view_of_posts);
        }
    }

}
