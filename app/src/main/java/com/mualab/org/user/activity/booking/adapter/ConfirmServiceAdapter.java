package com.mualab.org.user.activity.booking.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.model.BookingConfirmInfo;

import java.util.List;

/**
 * Created by mindiii on 22/1/19.
 */

public class ConfirmServiceAdapter extends RecyclerView.Adapter<ConfirmServiceAdapter.ViewHolder> {

    Context mContext;
    List<BookingConfirmInfo.DataBean> bookingList;

    public ConfirmServiceAdapter(Context mContext,List<BookingConfirmInfo.DataBean> bookingList) {
        this.mContext = mContext;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.confirm_service_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingConfirmInfo.DataBean bean = bookingList.get(position);

       // holder.tv_date_time.setText(bean.);

    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title,tv_service_name,tv_date_time;
        ImageView iv_profile;

        public ViewHolder(View itemView) {
            super(itemView);

            iv_profile = itemView.findViewById(R.id.iv_profile);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_service_name = itemView.findViewById(R.id.tv_service_name);
            tv_date_time = itemView.findViewById(R.id.tv_date_time);
        }
    }
}
