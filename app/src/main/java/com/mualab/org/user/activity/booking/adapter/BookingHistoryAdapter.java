package com.mualab.org.user.activity.booking.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingDetailsActivity;
import com.mualab.org.user.activity.booking.BookingHisoryActivity;
import com.mualab.org.user.activity.booking.model.BookingHistoryInfo;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by mindiii on 29/1/19.
 */

public class BookingHistoryAdapter  extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {
    Context mContext;
    ArrayList<BookingHistoryInfo.DataBean> dataBean;

    public BookingHistoryAdapter(Context mContext, ArrayList<BookingHistoryInfo.DataBean> dataBean) {
        this.mContext = mContext;
        this.dataBean = dataBean;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_histroy,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingHistoryInfo.DataBean bean = dataBean.get(position);


        if(bean.bookingInfo.size() > 1){
            holder.newText.setVisibility(View.VISIBLE);
            holder.tvServicesnew.setText(bean.bookingInfo.get(1).artistServiceName+"");
        }else holder.newText.setVisibility(View.GONE);

        holder.tvServices.setText(bean.bookingInfo.get(0).artistServiceName+"");

        holder.tvArtistName.setText(bean.artistDetail.get(0).userName+"");
        holder.tv_price.setText("£"+bean.totalPrice+"");


        holder.tvDateTime.setText(bean.bookingDate+", "+bean.bookingTime);

        if (bean.artistDetail.get(0).profileImage!= null && !bean.artistDetail.get(0).profileImage.equals("")){
            Picasso.with(mContext).load(bean.artistDetail.get(0).profileImage).placeholder(R.drawable.default_placeholder).fit().into(holder.ivProfile);
        }else {
            holder.ivProfile.setImageDrawable(mContext.getResources().getDrawable(R.drawable.default_placeholder));
        }


        if(bean.bookStatus.equals("0")){
            holder.tv_status.setText("Pending");
        }else holder.tv_status.setText("Confirmed");

        if(bean.bookStatus.equals("0")){
            holder.tv_status.setText(R.string.pending);
            holder.tv_status.setTextColor(ContextCompat.getColor(mContext,R.color.main_orange_color));
        }else  if(bean.bookStatus.equals("1")){
            holder.tv_status.setText(R.string.confirm);
            holder.tv_status.setTextColor(ContextCompat.getColor(mContext,R.color.main_green_color));
        }else if(bean.bookStatus.equals("2")){
            holder.tv_status.setText("Cancelled");
            holder.tv_status.setTextColor(ContextCompat.getColor(mContext,R.color.red));
        }
        else if(bean.bookStatus.equals("3")){
            holder.tv_status.setText("Completed");
            holder.tv_status.setTextColor(ContextCompat.getColor(mContext,R.color.main_green_color));
        }
        else if(bean.bookStatus.equals("5")){
            holder.tv_status.setText("In progress");
            holder.tv_status.setTextColor(ContextCompat.getColor(mContext,R.color.main_green_color));
        }

    }

    @Override
    public int getItemCount() {
        return dataBean.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView ivProfile;
        TextView tvArtistName,tv_status,tv_price,tvServices,tvServicesnew,tvDateTime;
        RelativeLayout newText;

        public ViewHolder(View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvArtistName = itemView.findViewById(R.id.tvArtistName);
            tv_status = itemView.findViewById(R.id.tv_status);
            tv_price = itemView.findViewById(R.id.tv_price);
            tvServices = itemView.findViewById(R.id.tvServices);
            newText = itemView.findViewById(R.id.newText);
            tvServicesnew = itemView.findViewById(R.id.tvServicesnew);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(mContext, BookingDetailsActivity.class);
            intent.putExtra("bookingId",dataBean.get(getAdapterPosition())._id);
            mContext.startActivity(intent);
        }
    }
}
