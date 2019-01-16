package com.mualab.org.user.activity.booking.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.model.ServiceInfoBooking;

import java.util.ArrayList;

/**
 * Created by mindiii on 16/1/19.
 */

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {

    Context mContext;
    ArrayList<ServiceInfoBooking.StaffInfoBean.StaffHoursBean> timeSlotList;

    public TimeSlotAdapter(Context mContext, ArrayList<ServiceInfoBooking.StaffInfoBean.StaffHoursBean> timeSlotList) {
        this.mContext = mContext;
        this.timeSlotList = timeSlotList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return timeSlotList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
