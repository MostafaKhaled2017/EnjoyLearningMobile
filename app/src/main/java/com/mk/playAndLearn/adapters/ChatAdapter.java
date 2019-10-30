package com.mk.playAndLearn.adapters;

/**
 * Created by VMac on 17/11/16.
 */

import android.os.Build;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Message;

import java.util.ArrayList;



public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private int SELF = 100;
    private ArrayList<Message> messageArrayList;


    public ChatAdapter(ArrayList<Message> messageArrayList) {
        this.messageArrayList=messageArrayList;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        // view type is to identify where to render the chat message
        // left or right
        if (viewType == SELF) {
            // self message
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_self, parent, false);
        } else {
            // WatBot message
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_watson, parent, false);
        }


        return new ViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageArrayList.get(position);
        if (message.getId()!=null && message.getId().equals("1")) {
            return SELF;
        }

        return position;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Message message = messageArrayList.get(position);
        message.setMessage(message.getMessage());
        //((ViewHolder) holder).message.setText(message.getMessage());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((ViewHolder) holder).message.setText(Html.fromHtml(message.getMessage(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            ((ViewHolder) holder).message.setText(Html.fromHtml(message.getMessage()));
        }
        Log.v("LoggingMessage", "message content is : " + message.getMessage());
        }

    @Override
    public int getItemCount() {
            return messageArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView message;

        ViewHolder(View view) {
            super(view);
            message = (TextView) itemView.findViewById(R.id.message);
            message.setMovementMethod(LinkMovementMethod.getInstance());

            //TODO: Uncomment this if you want to use a custom Font
            /*String customFont = "Montserrat-Regular.ttf";
            Typeface typeface = Typeface.createFromAsset(itemView.getContext().getAssets(), customFont);
            message.setTypeface(typeface);*/

        }
    }
}