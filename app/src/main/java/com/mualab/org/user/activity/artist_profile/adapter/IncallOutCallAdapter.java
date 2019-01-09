package com.mualab.org.user.activity.artist_profile.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistServicesActivity;
import com.mualab.org.user.activity.artist_profile.model.Services;

import java.util.ArrayList;

/**
 * Created by mindiii on 9/1/19.
 */

public class IncallOutCallAdapter extends RecyclerView.Adapter<IncallOutCallAdapter.ViewHolder> {
    Context  mContext;
    ArrayList<Services.ArtistServicesBean.SubServiesBean.ArtistservicesBean> inCallList;

    public IncallOutCallAdapter(Context mContext, ArrayList<Services.ArtistServicesBean.SubServiesBean.ArtistservicesBean> inCallList) {
        this.mContext = mContext;
        this.inCallList = inCallList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incall_outcall,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.tv_inoutcall_service.setText(inCallList.get(position).title+"");
    }

    @Override
    public int getItemCount() {
        return inCallList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_inoutcall_service;

        public ViewHolder(View itemView) {
            super(itemView);

            tv_inoutcall_service = itemView.findViewById(R.id.tv_inoutcall_service);
        }
    }
}