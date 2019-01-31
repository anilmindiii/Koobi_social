package com.mualab.org.user.activity.booking;

import android.app.Dialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.adapter.BookingHistoryAdapter;
import com.mualab.org.user.activity.booking.model.BookingHistoryInfo;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BookingHisoryActivity extends AppCompatActivity {

    private RecyclerView recycler_view;
    ArrayList<BookingHistoryInfo.DataBean> dataBean;
    BookingHistoryAdapter adapter;
    private TextView tv_bookingHistory,tv_booking_scheduled,tv_msg;
    private View iv_bookingHistory,iv_booking_scheduled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_hisory);


        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.my_booking));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        recycler_view = findViewById(R.id.recycler_view);

        tv_msg = findViewById(R.id.tv_msg);
        tv_bookingHistory = findViewById(R.id.tv_bookingHistory);
        tv_booking_scheduled = findViewById(R.id.tv_booking_scheduled);

        iv_bookingHistory = findViewById(R.id.iv_bookingHistory);
        iv_booking_scheduled = findViewById(R.id.iv_booking_scheduled);


        dataBean = new ArrayList<>();
        adapter = new BookingHistoryAdapter(this,dataBean);
        recycler_view.setAdapter(adapter);

        tv_bookingHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_bookingHistory.setVisibility(View.VISIBLE);
                iv_booking_scheduled.setVisibility(View.GONE);

                tv_bookingHistory.setTextColor(ContextCompat.getColor(BookingHisoryActivity.this,R.color.colorPrimary));
                tv_booking_scheduled.setTextColor(ContextCompat.getColor(BookingHisoryActivity.this,R.color.gray));

                bookingHistory("Past");
            }
        });

        tv_booking_scheduled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_bookingHistory.setVisibility(View.GONE);
                iv_booking_scheduled.setVisibility(View.VISIBLE);

                tv_bookingHistory.setTextColor(ContextCompat.getColor(BookingHisoryActivity.this,R.color.gray));
                tv_booking_scheduled.setTextColor(ContextCompat.getColor(BookingHisoryActivity.this,R.color.colorPrimary));


                bookingHistory("");
            }
        });

        bookingHistory("Past");

    }

    private void bookingHistory(final String strBookingType) {
        Progress.show(BookingHisoryActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingHisoryActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        bookingHistory(strBookingType);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("type", strBookingType);


        HttpTask task = new HttpTask(new HttpTask.Builder(BookingHisoryActivity.this, "userBookingHistory", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingHisoryActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    dataBean.clear();
                    if (status.equals("success")) {
                        Gson gson = new Gson();
                        BookingHistoryInfo historyInfo = gson.fromJson(response,BookingHistoryInfo.class);
                        dataBean.addAll(historyInfo.data);

                    } else {
                        MyToast.getInstance(BookingHisoryActivity.this).showDasuAlert(message);
                    }

                    if(dataBean.size() == 0 ){
                        tv_msg.setVisibility(View.VISIBLE);
                    }else tv_msg.setVisibility(View.GONE);

                    adapter.notifyDataSetChanged();

                } catch (Exception e) {
                    Progress.hide(BookingHisoryActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingHisoryActivity.this);
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
}
