package com.mualab.org.user.activity.booking;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistServicesActivity;
import com.mualab.org.user.activity.artist_profile.adapter.CustomStringAdapter;
import com.mualab.org.user.activity.artist_profile.adapter.IncallOutCallAdapter;
import com.mualab.org.user.activity.artist_profile.model.Services;
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
import java.util.Map;

public class BookingActivity extends AppCompatActivity implements View.OnClickListener {
    private String artistId;
    private LinearLayout ly_biz_type, ly_category;
    private CardView cv_ly_biz_type, cv_ly_category;
    private CustomStringAdapter adapterBizType, adapterCategory;
    private RecyclerView rcv_biz_type, rcv_category_type, rcv_incall;
    private Services services;
    private TextView tv_bizType, tv_category, tvArtistName;
    private ImageView iv_down_arrow_bizType, iv_down_arrow_category, ivProfile;
    private ArrayList<Services.ArtistServicesBean.SubServiesBean.ArtistservicesBean> inCallList, outCallList;
    private ScrollView main_scroll_view;
    private TextView tv_msg;
    private LinearLayout ly_incall;
    IncallOutCallAdapter inCallAdapter;
    IncallOutCallAdapter.childItemClick click;
    private boolean isOpenCategory;
    private RelativeLayout ly_outcall;
    private CheckBox chbOutcall;
    private Services.ArtistServicesBean artistServicesBeans;
    private boolean isOutCallSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        Intent i = getIntent();
        artistId = i.getStringExtra("artistId");

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.booking));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        chbOutcall = findViewById(R.id.chbOutcall);
        ly_outcall = findViewById(R.id.ly_outcall);
        ivProfile = findViewById(R.id.ivProfile);
        tvArtistName = findViewById(R.id.tvArtistName);
        ly_biz_type = findViewById(R.id.ly_biz_type);
        ly_category = findViewById(R.id.ly_category);
        rcv_biz_type = findViewById(R.id.rcv_biz_type);
        rcv_category_type = findViewById(R.id.rcv_category_type);
        rcv_incall = findViewById(R.id.rcv_incall);
        tv_bizType = findViewById(R.id.tv_bizType);
        tv_category = findViewById(R.id.tv_category);
        iv_down_arrow_bizType = findViewById(R.id.iv_down_arrow_bizType);
        iv_down_arrow_category = findViewById(R.id.iv_down_arrow_category);
        main_scroll_view = findViewById(R.id.main_scroll_view);
        ly_incall = findViewById(R.id.ly_incall);
        tv_msg = findViewById(R.id.tv_msg);

        cv_ly_category = findViewById(R.id.cv_ly_category);
        cv_ly_biz_type = findViewById(R.id.cv_ly_biz_type);

        ly_biz_type.setOnClickListener(this);
        ly_category.setOnClickListener(this);
        main_scroll_view.setOnClickListener(this);
        ly_outcall.setOnClickListener(this);


        inCallList = new ArrayList<>();
        outCallList = new ArrayList<>();

        apiForGetAllServices();
    }


    private void apiForGetAllServices() {
        Progress.show(BookingActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetAllServices();
                    }
                }
            }).show();

        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", artistId);

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingActivity.this, "artistService", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equals("success")) {
                        Gson gson = new Gson();
                        services = gson.fromJson(response, Services.class);

                        if (!services.artistDetail.profileImage.isEmpty() && !services.artistDetail.profileImage.equals("")) {
                            Picasso.with(BookingActivity.this).load(services.artistDetail.profileImage).placeholder(R.drawable.default_placeholder).
                                    fit().into(ivProfile);
                        } else {
                            ivProfile.setImageResource(R.drawable.default_placeholder);
                        }
                        tvArtistName.setText(services.artistDetail.firstName + "");
/*.......................................................................................................................*/

                        adapterBizType = new CustomStringAdapter("bizType", services, null, BookingActivity.this, new CustomStringAdapter.onClickItem() {
                            @Override
                            public void onclick(final Services.ArtistServicesBean artistServicesBean) {
                                artistServicesBeans = artistServicesBean;
                                tv_bizType.setText(artistServicesBean.serviceName + "");
                                cv_ly_biz_type.setVisibility(View.GONE);

                                if (artistServicesBean.subServies.size() == 1) {
                                    isOpenCategory = false;
                                } else isOpenCategory = true;

                                if (artistServicesBean.subServies.size() > 0) {
                                    if (artistServicesBean.subServies.get(0) != null) {
                                        tv_category.setText(artistServicesBean.subServies.get(0).subServiceName + "");

                                        inCallList.clear();
                                        outCallList.clear();

                                        for (int i = 0; i < artistServicesBean.subServies.get(0).artistservices.size(); i++) {

                                            if (artistServicesBean.subServies.get(0).artistservices.get(i).bookingType.equals("Both")) {
                                                inCallList.add(artistServicesBean.subServies.get(0).artistservices.get(i));
                                                outCallList.add(artistServicesBean.subServies.get(0).artistservices.get(i));
                                            } else if (artistServicesBean.subServies.get(0).artistservices.get(i).bookingType.equals("Incall")) {
                                                inCallList.add(artistServicesBean.subServies.get(0).artistservices.get(i));
                                            } else if (artistServicesBean.subServies.get(0).artistservices.get(i).bookingType.equals("Outcall")) {
                                                outCallList.add(artistServicesBean.subServies.get(0).artistservices.get(i));
                                            }
                                        }

                                        if (inCallList.size() == 0) {
                                            ly_incall.setVisibility(View.GONE);
                                        } else ly_incall.setVisibility(View.VISIBLE);


                                        if (inCallList.size() == 0 && outCallList.size() == 0) {
                                            main_scroll_view.setVisibility(View.GONE);
                                            tv_msg.setVisibility(View.VISIBLE);
                                        } else {
                                            tv_msg.setVisibility(View.GONE);
                                            main_scroll_view.setVisibility(View.VISIBLE);

                                            if (isOutCallSelected){
                                                if(outCallList.size() == 0){
                                                    main_scroll_view.setVisibility(View.GONE);
                                                    tv_msg.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        }

                                        if (isOutCallSelected) {
                                            inCallAdapter = new IncallOutCallAdapter(BookingActivity.this, outCallList, "Out Call", true);
                                        } else {
                                            inCallAdapter = new IncallOutCallAdapter(BookingActivity.this, inCallList, "In Call", true);
                                        }

                                        inCallAdapter.setClickListner(click);
                                        rcv_incall.setAdapter(inCallAdapter);

                                    }
                                } else {
                                    tv_category.setText("No category found");
                                    iv_down_arrow_category.setVisibility(View.GONE);
                                    main_scroll_view.setVisibility(View.GONE);
                                    tv_msg.setVisibility(View.VISIBLE);
                                }


                                if (artistServicesBean.subServies.size() > 1) {
                                    iv_down_arrow_category.setVisibility(View.VISIBLE);
                                } else iv_down_arrow_category.setVisibility(View.GONE);


                                if (services.artistServices.size() == 1) {
                                    iv_down_arrow_bizType.setVisibility(View.GONE);
                                } else iv_down_arrow_bizType.setVisibility(View.VISIBLE);


                                adapterCategory = new CustomStringAdapter("categoryType", null, artistServicesBean.subServies, BookingActivity.this, null, new CustomStringAdapter.onClickItemCategory() {
                                    @Override
                                    public void onclick(Services.ArtistServicesBean.SubServiesBean bean) {
                                        tv_category.setText(bean.subServiceName + "");
                                        cv_ly_category.setVisibility(View.GONE);

                                        inCallList.clear();
                                        outCallList.clear();


                                        for (int i = 0; i < bean.artistservices.size(); i++) {

                                            if (bean.artistservices.get(i).bookingType.equals("Both")) {

                                                inCallList.add(bean.artistservices.get(i));
                                                outCallList.add(bean.artistservices.get(i));

                                            } else if (bean.artistservices.get(i).bookingType.equals("Incall")) {
                                                inCallList.add(bean.artistservices.get(i));
                                            } else if (bean.artistservices.get(i).bookingType.equals("Outcall")) {
                                                outCallList.add(bean.artistservices.get(i));
                                            }
                                        }

                          /*......................................................................*/
                                        if (isOutCallSelected) {
                                            inCallAdapter = new IncallOutCallAdapter(BookingActivity.this, outCallList, "Out Call", true);
                                        } else {
                                            inCallAdapter = new IncallOutCallAdapter(BookingActivity.this, inCallList, "In Call", true);
                                        }
                                        inCallAdapter.setClickListner(click);
                                        rcv_incall.setAdapter(inCallAdapter);

                                        if (inCallList.size() == 0) {
                                            ly_incall.setVisibility(View.GONE);
                                        } else ly_incall.setVisibility(View.VISIBLE);


                                        if (inCallList.size() == 0 && outCallList.size() == 0) {
                                            main_scroll_view.setVisibility(View.GONE);
                                            tv_msg.setVisibility(View.VISIBLE);
                                        } else {
                                            tv_msg.setVisibility(View.GONE);
                                            main_scroll_view.setVisibility(View.VISIBLE);

                                            if (isOutCallSelected){
                                                if(outCallList.size() == 0){
                                                    main_scroll_view.setVisibility(View.GONE);
                                                    tv_msg.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        }


                                    }
                                });

                                rcv_category_type.setAdapter(adapterCategory);

                            }
                        }, null);

                        adapterBizType.clickItem();

                        rcv_biz_type.setAdapter(adapterBizType);


                    }

                } catch (Exception e) {
                    Progress.hide(BookingActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(BookingActivity.this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ly_biz_type:
                if (services != null)
                    if (services.artistServices.size() == 1) {
                        return;
                    }
                if (cv_ly_biz_type.getVisibility() == View.VISIBLE) {
                    cv_ly_biz_type.setVisibility(View.GONE);
                } else {
                    cv_ly_biz_type.setVisibility(View.VISIBLE);
                }

                cv_ly_category.setVisibility(View.GONE);


                break;

            case R.id.ly_category:
                if (isOpenCategory) {
                    if (cv_ly_category.getVisibility() == View.VISIBLE) {
                        cv_ly_category.setVisibility(View.GONE);
                    } else {
                        cv_ly_category.setVisibility(View.VISIBLE);
                    }

                    cv_ly_biz_type.setVisibility(View.GONE);
                }
                break;

            case R.id.ly_outcall: {
                if (chbOutcall.isChecked()) {
                    chbOutcall.setChecked(false);
                    isOutCallSelected = false;

                    adapterBizType.clickItem();

                   /* inCallAdapter = new IncallOutCallAdapter(BookingActivity.this, inCallList, "In Call", true);
                    inCallAdapter.setClickListner(click);
                    rcv_incall.setAdapter(inCallAdapter);

                    if (inCallList.size() == 0) {
                        iv_down_arrow_category.setVisibility(View.GONE);
                        main_scroll_view.setVisibility(View.GONE);
                        tv_msg.setVisibility(View.VISIBLE);
                    } else {
                        tv_msg.setVisibility(View.GONE);
                        main_scroll_view.setVisibility(View.VISIBLE);
                    }
*/
                } else {
                    chbOutcall.setChecked(true);
                    isOutCallSelected = true;

                    adapterBizType.clickItem();

                   /* inCallAdapter = new IncallOutCallAdapter(BookingActivity.this, outCallList, "Out Call", true);
                    inCallAdapter.setClickListner(click);
                    rcv_incall.setAdapter(inCallAdapter);

                    if (outCallList.size() == 0) {
                        iv_down_arrow_category.setVisibility(View.GONE);
                        main_scroll_view.setVisibility(View.GONE);
                        tv_msg.setVisibility(View.VISIBLE);
                    }else {
                        tv_msg.setVisibility(View.GONE);
                        main_scroll_view.setVisibility(View.VISIBLE);
                    }*/
                }

                break;
            }
        }
    }
}
