package com.mualab.org.user.activity.booking;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.adapter.CustomStringAdapter;
import com.mualab.org.user.activity.artist_profile.adapter.IncallOutCallAdapter;
import com.mualab.org.user.activity.artist_profile.model.Services;
import com.mualab.org.user.activity.booking.adapter.ConfirmServiceAdapter;
import com.mualab.org.user.activity.booking.model.BookingConfirmInfo;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
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

public class BookingConfirmActivity extends AppCompatActivity {
    private RecyclerView rcv_service;
    private ConfirmServiceAdapter adapter;
    private String artistId;
    private List<BookingConfirmInfo.DataBean> bookingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirm);

        rcv_service = findViewById(R.id.rcv_service);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.booking_confirm));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        bookingList = new ArrayList<>();
        adapter = new ConfirmServiceAdapter(this,bookingList);
        rcv_service.setAdapter(adapter);

        if(getIntent().getStringExtra("serviceId") != null){
            artistId = getIntent().getStringExtra("artistId");
        }

        GetServices(artistId);
    }


    private void GetServices(final String artistId) {
        Progress.show(BookingConfirmActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingConfirmActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        GetServices( artistId);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", artistId);

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingConfirmActivity.this, "getBookedServices", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingConfirmActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    bookingList.clear();
                    if (status.equals("success")) {
                        Gson gson = new Gson();
                        BookingConfirmInfo confirmInfo = gson.fromJson(response,BookingConfirmInfo.class);
                        bookingList.addAll(confirmInfo.data);

                    }else {

                    }

                    adapter.notifyDataSetChanged();

                } catch (Exception e) {
                    Progress.hide(BookingConfirmActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingConfirmActivity.this);
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
