package com.mualab.org.user.activity.booking;

import android.app.Dialog;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.adapter.ConfirmServiceAdapter;
import com.mualab.org.user.activity.booking.adapter.NewBookingHistoryAdapter;
import com.mualab.org.user.activity.booking.model.BookingConfirmInfo;
import com.mualab.org.user.activity.booking.model.BookingHistoryInfo;
import com.mualab.org.user.activity.booking.model.BookingListInfo;
import com.mualab.org.user.activity.booking.model.TrackInfo;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingDetailsActivity extends AppCompatActivity {

    private NewBookingHistoryAdapter adapter;
    private RecyclerView rcv_service;
    private ImageView iv_profile_artist;
    private TextView tv_artist_name,tv_address,booking_status,tv_payment_method,tv_new_amount,tv_amount,
            tv_voucher_code,tv_discounted_price,tv_call_type;
    private FrameLayout ly_amount;
    private RelativeLayout ly_voucher_code,ly_map_direction;
    BookingListInfo historyInfo;
    private int bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);


        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.appoinment));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        rcv_service = findViewById(R.id.rcv_service);
        iv_profile_artist = findViewById(R.id.iv_profile_artist);
        tv_artist_name = findViewById(R.id.tv_artist_name);
        tv_address = findViewById(R.id.tv_address);
        booking_status = findViewById(R.id.booking_status);
        tv_payment_method = findViewById(R.id.tv_payment_method);
        tv_new_amount = findViewById(R.id.tv_new_amount);
        tv_amount = findViewById(R.id.tv_amount);
        ly_amount = findViewById(R.id.ly_amount);
        ly_voucher_code = findViewById(R.id.ly_voucher_code);
        ly_map_direction = findViewById(R.id.ly_map_direction);
        tv_voucher_code = findViewById(R.id.tv_voucher_code);
        tv_discounted_price = findViewById(R.id.tv_discounted_price);
        tv_call_type = findViewById(R.id.tv_call_type);


        if (getIntent().getIntExtra("bookingId", 0) != 0) {
            bookingId = getIntent().getIntExtra("bookingId", 0);
            getDetailsBookService(getIntent().getIntExtra("bookingId", 0));
        }




        historyInfo = new BookingListInfo();

        ly_map_direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<TrackInfo> mapBeanArrayList = new ArrayList<>();

                if(historyInfo.data != null){

                    TrackInfo trackUser = new TrackInfo();
                    trackUser.address = historyInfo.data.location;
                    trackUser.latitude = historyInfo.data.latitude;
                    trackUser.longitude = historyInfo.data.longitude;
                    if(historyInfo.data.userDetail.get(0).profileImage.equals("")){
                        trackUser.profileImage = "https://image.shutterstock.com/image-vector/male-default-placeholder-avatar-profile-260nw-387516193.jpg";

                    }else trackUser.profileImage = historyInfo.data.userDetail.get(0).profileImage;


                    TrackInfo trackArtist = new TrackInfo();
                    trackArtist.address =  "";
                    trackArtist.latitude =  historyInfo.data.bookingInfo.get(0).trackingLatitude;
                    trackArtist.longitude =  historyInfo.data.bookingInfo.get(0).trackingLongitude;
                    trackArtist.staffName =  historyInfo.data.bookingInfo.get(0).staffName;
                    trackArtist.bookingDate =  historyInfo.data.bookingInfo.get(0).bookingDate;
                    trackArtist.startTime =  historyInfo.data.bookingInfo.get(0).startTime;
                    trackArtist.artistServiceName =  historyInfo.data.bookingInfo.get(0).artistServiceName;

                    if(historyInfo.data.bookingInfo.get(0).staffImage.equals("")){
                        trackArtist.profileImage = "https://image.shutterstock.com/image-vector/male-default-placeholder-avatar-profile-260nw-387516193.jpg";
                    }else trackArtist.profileImage = historyInfo.data.bookingInfo.get(0).staffImage;


                    Intent intent = new Intent(BookingDetailsActivity.this,TrackingActivity.class);
                    intent.putExtra("trackUser",trackUser);
                    intent.putExtra("trackArtist",trackArtist);
                    intent.putExtra("bookingId",bookingId);
                    startActivity(intent);
                }


            }
        });
    }

    private void getDetailsBookService(final int bookingId) {
        Progress.show(BookingDetailsActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingDetailsActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        getDetailsBookService(bookingId);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("bookingId", String.valueOf(bookingId));


        HttpTask task = new HttpTask(new HttpTask.Builder(BookingDetailsActivity.this, "bookingDetail", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingDetailsActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equals("success")) {
                        Gson gson = new Gson();
                        historyInfo = gson.fromJson(response, BookingListInfo.class);
                        adapter = new NewBookingHistoryAdapter(BookingDetailsActivity.this, historyInfo);
                        rcv_service.setAdapter(adapter);

                        setData(historyInfo);
                        adapter.notifyDataSetChanged();

                    } else {
                        MyToast.getInstance(BookingDetailsActivity.this).showDasuAlert(message);
                    }


                } catch (Exception e) {
                    Progress.hide(BookingDetailsActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingDetailsActivity.this);
                try {
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        })
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(this.getClass().getName());
    }

    void setData(BookingListInfo historyInfo){

        if (historyInfo.data.bookingType == 2) {
            //bookingType = "2";
            tv_call_type.setText("Out Call");
        } else {
            //bookingType = "1";
            tv_call_type.setText("In Call");
        }


        if (!historyInfo.data.artistDetail.get(0).profileImage.isEmpty() && !historyInfo.data.artistDetail.get(0).profileImage.equals("")) {
            Picasso.with(BookingDetailsActivity.this).load(historyInfo.data.artistDetail.get(0).profileImage).placeholder(R.drawable.default_placeholder).
                    fit().into(iv_profile_artist);
        }else {
            iv_profile_artist.setImageResource(R.drawable.default_placeholder);
        }

        tv_artist_name.setText(historyInfo.data.artistDetail.get(0).userName+"");
        tv_address.setText(historyInfo.data.location);



        if(historyInfo.data.bookStatus.equals("0")){
            booking_status.setText(R.string.pending);
            ly_map_direction.setVisibility(View.GONE);
            booking_status.setTextColor(ContextCompat.getColor(BookingDetailsActivity.this,R.color.main_orange_color));
        }else  if(historyInfo.data.bookStatus.equals("1")){
            booking_status.setText(R.string.confirm);
            ly_map_direction.setVisibility(View.VISIBLE);
            booking_status.setTextColor(ContextCompat.getColor(BookingDetailsActivity.this,R.color.main_green_color));
        }else if(historyInfo.data.bookStatus.equals("2")){
            booking_status.setText("Cancelled");
            booking_status.setTextColor(ContextCompat.getColor(BookingDetailsActivity.this,R.color.red));
        }
        else if(historyInfo.data.bookStatus.equals("3")){
            booking_status.setText("Completed");
            booking_status.setTextColor(ContextCompat.getColor(BookingDetailsActivity.this,R.color.main_green_color));
        }
        else if(historyInfo.data.bookStatus.equals("5")){
            booking_status.setText("In progress");
            booking_status.setTextColor(ContextCompat.getColor(BookingDetailsActivity.this,R.color.main_green_color));
        }


        if(historyInfo.data.paymentStatus == 2){
            tv_payment_method.setText(R.string.cash);
        }else tv_payment_method.setText(R.string.online);

        if(historyInfo.data.discountPrice.equals("")){
            ly_amount.setVisibility(View.GONE);
            tv_new_amount.setText("£"+historyInfo.data.totalPrice+"");
        }else {
            ly_amount.setVisibility(View.VISIBLE);
            tv_amount.setText("£"+historyInfo.data.totalPrice+"");
            tv_new_amount.setText(getString(R.string.pond_symbol)+historyInfo.data.discountPrice+"");
        }

        if(historyInfo.data.voucher.voucherCode == null){
            ly_voucher_code.setVisibility(View.GONE);
        }else {
            ly_voucher_code.setVisibility(View.VISIBLE);
            tv_voucher_code.setText(historyInfo.data.voucher.voucherCode);
            tv_discounted_price.setText(historyInfo.data.voucher.amount);
        }
    }
}
