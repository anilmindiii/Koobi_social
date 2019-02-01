package com.mualab.org.user.activity.booking.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.model.BookingConfirmInfo;
import com.mualab.org.user.activity.booking.model.BookingListInfo;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mindiii on 1/2/19.
 */

public class NewBookingHistoryAdapter extends RecyclerView.Adapter<NewBookingHistoryAdapter.ViewHolder>{

    Context mContext;
    BookingListInfo bookingListInfo;

    public NewBookingHistoryAdapter(Context mContext, BookingListInfo bookingListInfo) {
        this.mContext = mContext;
        this.bookingListInfo = bookingListInfo;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_details_booking,parent,false);
        return new NewBookingHistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        BookingListInfo.DataBean.BookingInfoBean bean = bookingListInfo.data.bookingInfo.get(position);

        if (!bean.staffImage.isEmpty() && !bean.staffImage.equals("")) {
            Picasso.with(mContext).load(bean.staffImage).placeholder(R.drawable.default_placeholder).
                    fit().into(holder.iv_profile);
        }else {
            holder.iv_profile.setImageResource(R.drawable.default_placeholder);
        }


        bean.bookingDate = Helper.formateDateFromstring("yyyy-MM-dd","dd/MM/yyyy",bean.bookingDate);
        holder.tv_date_time.setText(bean.bookingDate + ", " + bean.startTime);
        holder.tv_service_name.setText(bean.artistServiceName);
        holder.tv_name.setText(bean.staffName);
        holder.tv_price.setText("£"+bean.bookingPrice);


        /*........................................................*/
        try {
            if (bookingListInfo.data.bookStatus.equals("1") || bookingListInfo.data.bookStatus.equals("5")) {
                for (BookingListInfo.DataBean.BookingInfoBean tempBookBean : bookingListInfo.data.bookingInfo) {
                    if (tempBookBean.status != 0) {
                        holder.rlStatus.setVisibility(View.VISIBLE);
                        holder.tvStatus.setText(getBookingStatus(holder, bookingListInfo.data.bookingInfo.get(position)));
                        break;
                    }
                }
            }
            else{
                holder.rlStatus.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
            Log.d("key",ignored.toString());
        }


    }


    private String getBookingStatus(ViewHolder holder, BookingListInfo.DataBean.BookingInfoBean mainBean) {
        String tempStatus = "";
        if (bookingListInfo.data.bookStatus.equals("1") || bookingListInfo.data.bookStatus.equals("3")
                || bookingListInfo.data.bookStatus.equals("5")) {

            switch (mainBean.status) {
                case 0:
                    tempStatus = "Confirm";
                    holder.tvStatus.setTextColor(holder.tvStatus.getContext().getResources().getColor(R.color.main_green_color));
                    break;

                case 1:
                    tempStatus = "On the way";
                    holder.tvStatus.setTextColor(holder.tvStatus.getContext().getResources().getColor(R.color.main_green_color));
                    break;

                case 2:
                    tempStatus = "On going";
                    holder.tvStatus.setTextColor(holder.tvStatus.getContext().getResources().getColor(R.color.colorPrimary));
                    break;

                case 3:
                case 5:
                    tempStatus = "Service end";
                    holder.tvStatus.setTextColor(holder.tvStatus.getContext().getResources().getColor(R.color.main_green_color));
                    break;

                default:
                    tempStatus = "Service end";
                    holder.tvStatus.setTextColor(holder.tvStatus.getContext().getResources().getColor(R.color.main_green_color));
                    break;
            }
        }

        return tempStatus;
    }

    @Override
    public int getItemCount() {
        return bookingListInfo.data.bookingInfo.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name,tv_service_name,tv_date_time,tv_edit,tv_delete,tv_price,tvStatus;
        ImageView iv_profile,iv_service_arrow,iv_price_arrow,iv_time_date_arrow;
        LinearLayout rlStatus;

        public ViewHolder(View itemView) {
            super(itemView);

            iv_profile = itemView.findViewById(R.id.iv_profile);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_service_name = itemView.findViewById(R.id.tv_service_name);
            tv_date_time = itemView.findViewById(R.id.tv_date_time);
            tv_edit = itemView.findViewById(R.id.tv_edit);
            tv_delete = itemView.findViewById(R.id.tv_delete);
            tv_price = itemView.findViewById(R.id.tv_price);

            iv_service_arrow = itemView.findViewById(R.id.iv_service_arrow);
            iv_price_arrow = itemView.findViewById(R.id.iv_price_arrow);
            iv_time_date_arrow = itemView.findViewById(R.id.iv_time_date_arrow);

            tvStatus = itemView.findViewById(R.id.tvStatus);
            rlStatus = itemView.findViewById(R.id.rlStatus);
        }
    }
}
