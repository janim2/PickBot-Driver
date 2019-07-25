package com.pickbot.pickbotdriver.historyRecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.pickbot.pickbotdriver.R;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder>{
    ArrayList<HistoryObject> itemList;
    Context context;

    public class ViewHolder extends RecyclerView.ViewHolder{
        View view;

        public ViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    public HistoryAdapter(ArrayList<HistoryObject> itemList,Context context){
        this.itemList  = itemList;
        this.context  = context;
    }



    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history,parent,false);
        ViewHolder vh = new ViewHolder(layoutView);
        return vh;
    }

    @Override
    public void onBindViewHolder(HistoryAdapter.ViewHolder holder, int position) {
        final TextView rideId = holder.view.findViewById(R.id.rideId);
        TextView timestamp = holder.view.findViewById(R.id.timestamp);
        rideId.setText(itemList.get(position).getRideId());
        timestamp.setText(itemList.get(position).getTime());

        rideId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(v.getContext(), Fake_Single_history.class);
//                Bundle b = new Bundle();
//                b.putString("rideId",rideId.getText().toString());
//                intent.putExtras(b);
//                v.getContext().startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {

        return itemList.size();
    }
}
