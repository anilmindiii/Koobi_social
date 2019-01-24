package com.mualab.org.user.activity.booking;

import android.app.Dialog;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistServiceDetailsActivity;
import com.mualab.org.user.activity.authentication.AddAddressActivity;
import com.mualab.org.user.activity.booking.adapter.ConfirmServiceAdapter;
import com.mualab.org.user.activity.booking.model.BookingConfirmInfo;
import com.mualab.org.user.activity.booking.model.VoucherInfo;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.model.booking.Address;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.constants.Constant;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BookingConfirmActivity extends AppCompatActivity {
    private RecyclerView rcv_service;
    private ConfirmServiceAdapter adapter;
    private String artistId;
    private List<BookingConfirmInfo.DataBean> bookingList;
    private boolean isBankAdded, isOutCallSelected;
    private RadioGroup radioGroup;
    private RadioButton rb_case, rb_online;
    private RelativeLayout ly_location;
    private TextView tv_address, tv_apply,tv_new_amount,tv_amount;
    private EditText ed_vouchar_code;
    private ImageView iv_voucher_arrow;
    double total_price = 0.0;
    private FrameLayout ly_amount;
    private String voucher = "",bookingType = "1",paymentType = "1",discountPrice = "",bookingDate = "",bookingTime = "";
    private AppCompatButton btn_confirm_booking,brn_add_more;
    private ImageView iv_location_arrow,iv_voucher_cancel;
    private TextView tv_call_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirm);

        rcv_service = findViewById(R.id.rcv_service);
        radioGroup = findViewById(R.id.radioGroup);
        rb_case = findViewById(R.id.rb_case);
        rb_online = findViewById(R.id.rb_online);
        ly_location = findViewById(R.id.ly_location);
        tv_address = findViewById(R.id.tv_address);
        tv_apply = findViewById(R.id.tv_apply);
        ed_vouchar_code = findViewById(R.id.ed_vouchar_code);
        ed_vouchar_code.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        iv_voucher_arrow = findViewById(R.id.iv_voucher_arrow);
        tv_new_amount = findViewById(R.id.tv_new_amount);
        tv_amount = findViewById(R.id.tv_amount);
        ly_amount = findViewById(R.id.ly_amount);
        btn_confirm_booking = findViewById(R.id.btn_confirm_booking);
        iv_location_arrow = findViewById(R.id.iv_location_arrow);
        iv_voucher_cancel = findViewById(R.id.iv_voucher_cancel);
        brn_add_more = findViewById(R.id.brn_add_more);
        tv_call_type = findViewById(R.id.tv_call_type);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.booking_confirm));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        bookingList = new ArrayList<>();
        adapter = new ConfirmServiceAdapter(this, bookingList, new ConfirmServiceAdapter.getValue() {
            @Override
            public void deleteService(int bookingId,int position) {
                deleteBookService(String.valueOf(bookingId),position);
            }

            @Override
            public void editService(BookingConfirmInfo.DataBean dataBean) {
                Intent intent = new Intent();

                intent.putExtra("_id",dataBean.staffId);
                intent.putExtra("artistId",artistId);

                if(isOutCallSelected){
                    intent.putExtra("callType","Out Call");
                    intent.putExtra("outcallStaff",true);
                    intent.putExtra("incallStaff",false);
                }else{
                    intent.putExtra("callType","In Call");
                    intent.putExtra("outcallStaff",false);
                    intent.putExtra("incallStaff",true);
                }


                intent.putExtra("serviceId",dataBean.serviceId);
                intent.putExtra("subServiceId",dataBean.subServiceId);

                intent.putExtra("mainServiceName","");// should be empty
                intent.putExtra("subServiceName",dataBean.artistServiceName);

                intent.putExtra("incallStaff",false);


                setResult(-2,intent);
                finish();
            }
        });
        rcv_service.setAdapter(adapter);

        if (getIntent().getStringExtra("artistId") != null) {
            artistId = getIntent().getStringExtra("artistId");
            isBankAdded = getIntent().getBooleanExtra("isBankAdded", false);
            isOutCallSelected = getIntent().getBooleanExtra("isOutCallSelected", false);
            if(isOutCallSelected){
                bookingType = "2";
                iv_location_arrow.setVisibility(View.VISIBLE);
                tv_call_type.setText("Out Call");
            }else  {
                bookingType = "1";
                iv_location_arrow.setVisibility(View.GONE);
                tv_call_type.setText("In Call");
            }
        }

        if (!isBankAdded) {
            rb_online.setVisibility(View.GONE);
            rb_case.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_case:
                        paymentType = "1";
                        break;

                    case R.id.rb_online:
                        paymentType = "2";
                        break;

                }

            }
        });

        ly_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookingConfirmActivity.this, AddAddressActivity.class);
                startActivityForResult(intent, 1001);
            }
        });

        tv_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtil.hideKeyboard(v,BookingConfirmActivity.this);
                String voucherCode = ed_vouchar_code.getText().toString().trim();
                if (!TextUtils.isEmpty(voucherCode)) {
                    applyVoucherCode(artistId,voucherCode);
                } else
                    MyToast.getInstance(BookingConfirmActivity.this).showDasuAlert("Please enter voucher code");
            }
        });

        iv_voucher_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookingConfirmActivity.this, VoucherCodesActivity.class);
                intent.putExtra("artistId", artistId);
                startActivityForResult(intent, Constant.REQUEST_CODE_CHOOSE);
            }
        });

        iv_voucher_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ed_vouchar_code.setText("");
                iv_voucher_arrow.setVisibility(View.VISIBLE);
                iv_voucher_cancel.setVisibility(View.INVISIBLE);

                tv_new_amount.setText("£"+total_price);

                tv_new_amount.setVisibility(View.VISIBLE);
                tv_amount.setVisibility(View.GONE);

                tv_apply.setText("Apply");
            }
        });

        btn_confirm_booking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmBooking();
            }
        });

        brn_add_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        GetServices(artistId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001) {

            if (data != null)
                if (data.getSerializableExtra("address") != null) {
                    Address address = (Address) data.getSerializableExtra("address");
                    tv_address.setVisibility(View.VISIBLE);
                    tv_address.setText(String.format("%s",
                            TextUtils.isEmpty(address.placeName) ? address.stAddress1 : address.placeName));
                }

        }

        if(requestCode == Constant.REQUEST_CODE_CHOOSE){
            if (data != null){
                if (data.getParcelableExtra("voucherItem") != null) {
                    VoucherInfo.DataBean voucherItem = data.getParcelableExtra("voucherItem");

                    ed_vouchar_code.setText(voucherItem.voucherCode);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("isOutCallSelected",isOutCallSelected);
        setResult(RESULT_OK,intent);
        super.onBackPressed();
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
                        GetServices(artistId);
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
                        BookingConfirmInfo confirmInfo = gson.fromJson(response, BookingConfirmInfo.class);
                        bookingList.addAll(confirmInfo.data);

                        if (isOutCallSelected) {
                            tv_address.setText(Mualab.currentUser.address + "");
                            ly_location.setEnabled(true);
                        } else {
                            ly_location.setEnabled(false);
                            if (bookingList.size() > 0)
                                tv_address.setText(bookingList.get(0).companyAddress);
                        }

                        if (bookingList.size() > 0){
                            bookingDate = bookingList.get(0).bookingDate;
                            bookingTime = bookingList.get(0).startTime;

                        }


                        total_price = 0.0;
                        for(BookingConfirmInfo.DataBean bean : bookingList){
                            total_price = Double.parseDouble(bean.bookingPrice)+total_price;
                        }
                        tv_new_amount.setText("£"+total_price+"");

                    } else {

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

    private void applyVoucherCode(final String artistId,final String voucherCode) {
        Progress.show(BookingConfirmActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingConfirmActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        applyVoucherCode(artistId,voucherCode);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", artistId);
        params.put("voucherCode", voucherCode);

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingConfirmActivity.this, "applyVoucher", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingConfirmActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");


                    if (status.equals("success")) {
                        Gson gson = new Gson();

                        JSONObject object = new JSONObject(response);
                        JSONObject data = object.getJSONObject("data");
                        voucher = data.toString();

                        VoucherInfo.DataBean voucherItem = gson.fromJson(data.toString(),VoucherInfo.DataBean.class);


                        tv_apply.setText("Applied");

                        ly_amount.setVisibility(View.VISIBLE);
                        tv_amount.setText("£"+total_price+"");
                        tv_amount.setTextColor(ContextCompat.getColor(BookingConfirmActivity.this,R.color.red));

                        if(voucherItem.discountType.equals("1")){// for fix amount
                            Double newDiscountedPrice = (total_price - (Double.parseDouble(voucherItem.amount)));
                            tv_new_amount.setText("£"+newDiscountedPrice+"");
                            discountPrice = String.valueOf(newDiscountedPrice);
                        }
                        else if(voucherItem.discountType.equals("2")){// for % percentage
                            Double newDiscountedPrice =  ((total_price * (Double.parseDouble(voucherItem.amount))) / 100);
                            tv_new_amount.setText("£"+newDiscountedPrice+"");
                            discountPrice = String.valueOf(newDiscountedPrice);
                        }

                        iv_voucher_arrow.setVisibility(View.INVISIBLE);
                        iv_voucher_cancel.setVisibility(View.VISIBLE);




                    } else {
                        MyToast.getInstance(BookingConfirmActivity.this).showDasuAlert(message);
                    }



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

    private void deleteBookService(final String bookingId, final int position) {
        Progress.show(BookingConfirmActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingConfirmActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        deleteBookService(bookingId, position);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("bookingId", bookingId);


        HttpTask task = new HttpTask(new HttpTask.Builder(BookingConfirmActivity.this, "deleteBookService", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingConfirmActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equals("success")) {
                        bookingList.remove(position);
                        if(bookingList.size() == 0){
                            onBackPressed();
                            return;
                        }else {
                            GetServices(artistId);
                        }


                    } else {
                        MyToast.getInstance(BookingConfirmActivity.this).showDasuAlert(message);
                    }



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

    private void confirmBooking() {
        Progress.show(BookingConfirmActivity.this);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingConfirmActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        confirmBooking();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", artistId);
        params.put("bookingDate", bookingDate);
        params.put("bookingTime", bookingTime);
        params.put("location", tv_address.getText().toString().trim());
        params.put("totalPrice", String.valueOf(total_price));
        params.put("discountPrice", discountPrice);
        params.put("paymentType", paymentType);
        params.put("bookingType", bookingType);
        params.put("voucher", voucher);


        HttpTask task = new HttpTask(new HttpTask.Builder(BookingConfirmActivity.this, "confirmBooking", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingConfirmActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equals("success")) {
                        SweetAlertDialog dialog = new SweetAlertDialog(BookingConfirmActivity.this, SweetAlertDialog.SUCCESS_TYPE);

                        dialog.setTitleText("Congratulation!");
                        dialog.setContentText("Your booking request has been successfully sent to artist.");
                        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        Intent showLogin = new Intent(BookingConfirmActivity.this, MainActivity.class);
                                        showLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        showLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(showLogin);
                                    }
                                });
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    } else {
                        MyToast.getInstance(BookingConfirmActivity.this).showDasuAlert(message);
                    }



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
