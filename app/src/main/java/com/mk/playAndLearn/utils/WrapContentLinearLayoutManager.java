package com.mk.playAndLearn.utils;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

public class WrapContentLinearLayoutManager extends LinearLayoutManager{
    public WrapContentLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Log.e("probe", "meet a IOOBE in RecyclerView");
        }
    }
}

