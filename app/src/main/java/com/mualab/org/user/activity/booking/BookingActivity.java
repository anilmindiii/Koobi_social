package com.mualab.org.user.activity.booking;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.Views.calender.data.CalendarAdapter;
import com.mualab.org.user.Views.calender.data.Day;
import com.mualab.org.user.Views.calender.widget.widget.MyFlexibleCalendar;
import com.mualab.org.user.activity.artist_profile.adapter.CustomStringAdapter;
import com.mualab.org.user.activity.artist_profile.adapter.IncallOutCallAdapter;
import com.mualab.org.user.activity.artist_profile.model.Services;
import com.mualab.org.user.activity.booking.adapter.StaffAdapter;
import com.mualab.org.user.activity.booking.adapter.TimeSlotAdapter;
import com.mualab.org.user.activity.booking.listner.DeleteServiceListener;
import com.mualab.org.user.activity.booking.listner.TimeSlotClickListener;
import com.mualab.org.user.activity.booking.model.ServiceInfoBooking;
import com.mualab.org.user.activity.booking.model.TimeSlotInfo;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingActivity extends AppCompatActivity implements View.OnClickListener
        , TimeSlotClickListener, DeleteServiceListener, StaffAdapter.click {
    private String artistId;
    private int childId;
    private LinearLayout ly_biz_type, ly_category;
    private CardView cv_ly_biz_type, cv_ly_category;
    private CustomStringAdapter adapterBizType, adapterCategory;
    private RecyclerView rcv_biz_type, rcv_category_type, rcv_incall;
    private Services services;
    private TextView tv_bizType, tv_category, tvArtistName, tvbizDate, btnCOnfirmBooking;
    private ImageView iv_down_arrow_bizType, iv_down_arrow_category, ivProfile;
    private ArrayList<Services.ArtistServicesBean.SubServiesBean.ArtistservicesBean> inCallList, outCallList;
    private ScrollView main_scroll_view;
    private TextView tv_msg, tvNoSlot;
    private LinearLayout ly_incall;
    private IncallOutCallAdapter inCallAdapter;
    private IncallOutCallAdapter.childItemClick click;
    private boolean isOpenCategory;
    private RelativeLayout ly_outcall;
    private CheckBox chbOutcall;
    private boolean isOutCallSelected;
    private int childPos = 0;
    private String checkPositon = "", callType = "";
    private String mainServiceName = "", subServiceName = "", artistServiceId = "";

    private RecyclerView rcv_staff;
    private StaffAdapter staffAdapter;
    private ArrayList<ServiceInfoBooking.StaffInfoBean> staffInfoBeanList;

    private RecyclerView rcv_timeSlot;
    private TimeSlotAdapter timeSlotAdapter;
    private ArrayList<TimeSlotInfo> timeSlotList;
    private String businessType = "", preprationTime = "";

    private String selectedDate, sMonth = "", sDay, currentTime, lat = "", lng = "", startTime = "";
    private MyFlexibleCalendar viewCalendar;
    private boolean isTodayClicked = false;
    private SimpleDateFormat input, dateFormat;
    private int dayId, staff = 0, serviceId, subServiceId;
    private SimpleDateFormat dateSdf, timeSdf;
    private RatingBar rating;
    private LinearLayout ly_staff_main, ly_time_slot_main;
    private boolean outcallStaff, incallStaff,isBankAdded;
    private int totalTime, endTime;
    private Double price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        Intent i = getIntent();

        childId = i.getIntExtra("_id", 0);
        artistServiceId = String.valueOf(childId);
        artistId = i.getStringExtra("artistId");
        callType = i.getStringExtra("callType");
        mainServiceName = i.getStringExtra("mainServiceName");
        subServiceName = i.getStringExtra("subServiceName");
        outcallStaff = i.getBooleanExtra("outcallStaff", false);
        incallStaff = i.getBooleanExtra("incallStaff", false);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.booking));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnCOnfirmBooking = findViewById(R.id.btnCOnfirmBooking);
        tvNoSlot = findViewById(R.id.tvNoSlot);
        ly_staff_main = findViewById(R.id.ly_staff_main);
        ly_time_slot_main = findViewById(R.id.ly_time_slot_main);
        ly_time_slot_main.setVisibility(View.GONE);
        tvbizDate = findViewById(R.id.tvbizDate);
        rating = findViewById(R.id.rating);
        rcv_timeSlot = findViewById(R.id.rcv_timeSlot);
        rcv_staff = findViewById(R.id.rcv_staff);
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
        AppCompatButton btnToday = findViewById(R.id.btnToday);

        cv_ly_category = findViewById(R.id.cv_ly_category);
        cv_ly_biz_type = findViewById(R.id.cv_ly_biz_type);


        click = new IncallOutCallAdapter.childItemClick() {
            @Override
            public void childClick(Services.ArtistServicesBean.SubServiesBean.ArtistservicesBean artistservicesBean, String callType, int adapterPosition) {
                childId = artistservicesBean._id;
                artistServiceId = String.valueOf(artistservicesBean._id);


                String completeTime = Helper.formateDateFromstring("HH:mm", "mm", artistservicesBean.completionTime);
                String preprationminutes = Helper.formateDateFromstring("HH:mm", "mm", preprationTime);
                totalTime = Integer.parseInt(completeTime) + Integer.parseInt(preprationminutes);

                apiForserviceStaff(String.valueOf(artistservicesBean._id));

                if (isOutCallSelected) {
                    if (artistservicesBean.outcallStaff) {
                        ly_staff_main.setVisibility(View.VISIBLE);
                    } else ly_staff_main.setVisibility(View.GONE);
                } else {
                    if (artistservicesBean.incallStaff) {
                        ly_staff_main.setVisibility(View.VISIBLE);
                    } else ly_staff_main.setVisibility(View.GONE);
                }
            }
        };


        ly_biz_type.setOnClickListener(this);
        ly_category.setOnClickListener(this);
        main_scroll_view.setOnClickListener(this);
        ly_outcall.setOnClickListener(this);
        btnCOnfirmBooking.setOnClickListener(this);
        btnToday.setOnClickListener(this);


        staffInfoBeanList = new ArrayList<>();
        staffAdapter = new StaffAdapter(this, staffInfoBeanList, callType, this);
        rcv_staff.setAdapter(staffAdapter);

        timeSlotList = new ArrayList<>();
        timeSlotAdapter = new TimeSlotAdapter(this, timeSlotList, new TimeSlotAdapter.getSelectTime() {
            @Override
            public void getSelectedTimeSlot(TimeSlotInfo slotInfo) {
                startTime = slotInfo.timeSlots;
            }
        });
        rcv_timeSlot.setLayoutManager(new GridLayoutManager(this, 3));
        rcv_timeSlot.setAdapter(timeSlotAdapter);

        inCallList = new ArrayList<>();
        outCallList = new ArrayList<>();

        if (callType.equals("Out Call")) {
            isOutCallSelected = true;
            chbOutcall.setChecked(true);
        }

        viewCalendar = findViewById(R.id.calendar);

        // init calendar
        Calendar cal = Calendar.getInstance();
        CalendarAdapter adapter = new CalendarAdapter(this, cal);
        viewCalendar.setAdapter(adapter);
        dateFormat = new SimpleDateFormat("EEE, d MMMM yyyy");
        dateFormat.setTimeZone(cal.getTimeZone());
        input = new SimpleDateFormat("yyyy-MM-dd");


        dateSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        timeSdf = new SimpleDateFormat("hh:mm a", Locale.US);
        // init calendar

        setCalenderClickListner(viewCalendar);

        //selectedDate = CalanderUtils.formatDate(CalanderUtils.getCurrentDate(), dateFormat.toString(),dateFormat.toString());
        dayId = cal.get(GregorianCalendar.DAY_OF_WEEK) - 2;

        selectedDate = getCurrentDate();
        currentTime = getCurrentTime();


        apiForGetAllServices();
    }

    @Override
    public void OnClickAdapter(ServiceInfoBooking.StaffInfoBean bean) {
        staff = bean.staffId;

        String completeTime = Helper.formateDateFromstring("HH:mm", "mm", bean.completionTime);
        String preprationminutes = Helper.formateDateFromstring("HH:mm", "mm", preprationTime);

        endTime = (getMinutes(bean.completionTime) + getMinutes(preprationTime));

        endTime = Integer.parseInt(completeTime) + Integer.parseInt(preprationminutes);
        if (isOutCallSelected) {
            price = bean.outCallPrice;
        } else price = bean.inCallPrice;


        apiForstaffSlot(String.valueOf(bean.staffId));
    }


    private String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("hh:mm a");
        System.out.println("currentTime" + date.format(currentLocalTime));
        return date.format(currentLocalTime);
    }

    private String getCurrentDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if (month < 10) {
            sMonth = "0" + month;
        } else {
            sMonth = String.valueOf(month);
        }

        if (day < 10) {
            sDay = "0" + day;
        } else {
            sDay = String.valueOf(day);
        }
        return year + "-" + sMonth + "-" + sDay;
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

            case R.id.ly_outcall:
                if (chbOutcall.isChecked()) {
                    chbOutcall.setChecked(false);
                    isOutCallSelected = false;

                    if (!mainServiceName.equals("")) {
                        for (int i = 0; i < services.artistServices.size(); i++) {
                            if (services.artistServices.get(i).serviceName.equals(mainServiceName)) {
                                adapterBizType.clickItem(i);
                            }

                        }
                    } else adapterBizType.clickItem();

                } else {
                    chbOutcall.setChecked(true);
                    isOutCallSelected = true;

                    if (!mainServiceName.equals("")) {
                        for (int i = 0; i < services.artistServices.size(); i++) {
                            if (services.artistServices.get(i).serviceName.equals(mainServiceName)) {
                                adapterBizType.clickItem(i);
                            }
                        }
                    } else adapterBizType.clickItem();

                }

                //reset All services
                resetAllServices();

                break;

            case R.id.btnToday:
                selectedDate = getCurrentDate();
                viewCalendar.isFirstimeLoad = true;
                if (selectedDate.contains("-")) {
                    isTodayClicked = true;
                    int year, month, day;
                    String[] separated = selectedDate.split("-");
                    year = Integer.parseInt(separated[0]);
                    month = Integer.parseInt(separated[1]);
                    day = Integer.parseInt(separated[2]);

                    viewCalendar.select(new Day(year, month, day));
                    viewCalendar.expand(500);
                    selectedDate = year + "-" + month + "-" + day;
                }
                //     apiForGetSlots();
                break;

            case R.id.btnCOnfirmBooking:
                if (childId == 0) {
                    MyToast.getInstance(BookingActivity.this).showDasuAlert("Please select service");
                } else if (TextUtils.isEmpty(startTime)){
                    MyToast.getInstance(BookingActivity.this).showDasuAlert("Please select time slot");
                }else  apiForContinueBooking("0");


                break;

        }
    }

    private void resetAllServices() {
        staffInfoBeanList.clear();
        staffAdapter.notifyDataSetChanged();

        timeSlotList.clear();
        timeSlotAdapter.notifyDataSetChanged();

        for (int i = 0; i < outCallList.size(); i++) {
            outCallList.get(i).isSelected = false;
        }

        for (int i = 0; i < inCallList.size(); i++) {
            inCallList.get(i).isSelected = false;
        }

        inCallAdapter.notifyDataSetChanged();
        ly_time_slot_main.setVisibility(View.GONE);
        ly_staff_main.setVisibility(View.GONE);
        childId = 0;
    }


    private void setCalenderClickListner(final MyFlexibleCalendar viewCalendar) {
        viewCalendar.setCalendarListener(new MyFlexibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {
                Day day = viewCalendar.getSelectedDay();
                viewCalendar.isFirstimeLoad = false;
                Log.i(getClass().getName(), "Selected Day: "
                        + day.getYear() + "/" + (day.getMonth() + 1) + "/" + day.getDay());

                Date date = new Date(day.getYear(), day.getMonth(), day.getDay() - 1);
                dayId = date.getDay() - 1;

                if (dayId == -1) {
                    dayId = 6;
                }

                int month = day.getMonth() + 1;

                if (month < 10) {
                    sMonth = "0" + month;
                } else {
                    sMonth = String.valueOf(month);
                }

                if (day.getDay() < 10) {
                    sDay = "0" + day.getDay();
                } else {
                    sDay = String.valueOf(day.getDay());
                }
                selectedDate = day.getYear() + "-" + sMonth + "-" + sDay;

                /*Date selectedDateTemp = CalanderUtils.getDateFormat(selectedDate, Constants.SERVER_TIMESTAMP_FORMAT);
                Date today = CalanderUtils.getDateFormat(CalanderUtils.formatDate(CalanderUtils.getCurrentDate(), Constants.SERVER_TIMESTAMP_FORMAT, Constants.SERVER_TIMESTAMP_FORMAT), Constants.SERVER_TIMESTAMP_FORMAT);


                assert selectedDateTemp != null;
                if (selectedDateTemp.before(today)) {
                    bkTempList.clear();
                    bkTempList.addAll(bkPrevDate);
                    bkDateAdapter.notifyDataSetChanged();
                    spBkDate.setSelection(0);
                } else if (selectedDateTemp.after(today)) {
                    bkTempList.clear();
                    bkTempList.addAll(bkAfterdate);
                    bkDateAdapter.notifyDataSetChanged();
                    spBkDate.setSelection(0);
                } else {
                    bkTempList.clear();
                    bkTempList.addAll(bkTodayDate);
                    bkDateAdapter.notifyDataSetChanged();
                    spBkDate.setSelection(1);
                }
*/

                if (viewCalendar.isSelectedDay(day)) {
                    Calendar todayCal = Calendar.getInstance();
                    int cYear = todayCal.get(Calendar.YEAR);
                    int cMonth = todayCal.get(Calendar.MONTH) + 1;
                    int cDay = todayCal.get(Calendar.DAY_OF_MONTH);

                    int year = day.getYear();
                    int dayOfMonth = day.getDay();

                    if (year >= cYear && month >= cMonth) {
                        if (year == cYear && month == cMonth && dayOfMonth < cDay) {
                            Log.i("Date Test", "can't select previous date");
                            MyToast.getInstance(BookingActivity.this).showDasuAlert("can't select previous date");
                        } else {
                            if (childId == 0) {
                                MyToast.getInstance(BookingActivity.this).showDasuAlert("Please select service");
                            } else apiForserviceStaff(String.valueOf(childId));
                        }
                    } else {
                        Log.i("Date Test", "can't select previous date");
                    }


                }
            }

            @Override
            public void onItemClick(View v) {
                viewCalendar.isFirstimeLoad = false;
                Day day = viewCalendar.getSelectedDay();
                Log.i(getClass().getName(), "The Day of Clicked View: "
                        + day.getYear() + "/" + (day.getMonth() + 1) + "/" + day.getDay());
            }

            @Override
            public void onDataUpdate() {
                Log.i(getClass().getName(), "Data Updated");
            }

            @Override
            public void onMonthChange() {
                Log.i(getClass().getName(), "Month Changed"
                        + ". Current Year: " + viewCalendar.getYear()
                        + ", Current Month: " + (viewCalendar.getMonth() + 1));
            }

            @Override
            public void onWeekChange(int position) {
                Log.i(getClass().getName(), "Week Changed"
                        + ". Current Year: " + viewCalendar.getYear()
                        + ", Current Month: " + (viewCalendar.getMonth() + 1)
                        + ", Current Week position of Month: " + position);
            }
        });
    }

    @Override
    public void onRemoveClick(int position) {

    }

    @Override
    public void onButtonClick(int position, String buttonText, int selectedCount) {

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
                        businessType = services.artistDetail.businessType;

                        rating.setRating(Float.parseFloat(services.artistDetail.ratingCount));

                        if (isOutCallSelected) {
                            preprationTime = services.artistDetail.outCallpreprationTime;
                        } else preprationTime = services.artistDetail.inCallpreprationTime;

                        if(services.artistDetail.bankStatus == 0){
                            isBankAdded = false;
                        }else isBankAdded = true;

                        if (!services.artistDetail.profileImage.isEmpty() && !services.artistDetail.profileImage.equals("")) {
                            Picasso.with(BookingActivity.this).load(services.artistDetail.profileImage).placeholder(R.drawable.default_placeholder).
                                    fit().into(ivProfile);
                        } else {
                            ivProfile.setImageResource(R.drawable.default_placeholder);
                        }
                        tvArtistName.setText(services.artistDetail.userName + "");

                        String from = "";
                        String end = "";


                        for (int i = 0; i < services.artistDetail.busineshours.size(); i++) {
                            if (services.artistDetail.busineshours.get(i).day == dayId) {
                                from = services.artistDetail.busineshours.get(i).startTime + " to " + services.artistDetail.busineshours.get(i).endTime;
                                if (!from.equals("")) {
                                    end = from;
                                    from = "";
                                }
                            }
                        }

                        if (!from.equals(""))
                            tvbizDate.setText(end + " & " + from);
                        else if (end.equals("")) {
                            tvbizDate.setText("NA");
                        } else tvbizDate.setText(end);

/*.......................................................................................................................*/

                        adapterBizType = new CustomStringAdapter("bizType", services, null, BookingActivity.this, new CustomStringAdapter.onClickItem() {
                            @Override
                            public void onclick(final Services.ArtistServicesBean artistServicesBean, int adapterPosition) {


                                if (!checkPositon.equals(artistServicesBean.serviceName)) {
                                    childPos = 0;
                                }

                                serviceId = artistServicesBean.serviceId;

                                if (!mainServiceName.equals(artistServicesBean.serviceName)) {
                                    if (!TextUtils.isEmpty(mainServiceName)) {
                                        resetAllServices();
                                    }
                                    mainServiceName = "";

                                }

                                checkPositon = artistServicesBean.serviceName;


                                if (!subServiceName.equals("")) {
                                    for (int i = 0; i < artistServicesBean.subServies.size(); i++) {
                                        if (artistServicesBean.subServies.get(i).subServiceName.equals(subServiceName)) {
                                            childPos = i;
                                            subServiceId = artistServicesBean.subServies.get(i).subServiceId;
                                        }
                                    }
                                    subServiceName = "";
                                } else {
                                    if (artistServicesBean.subServies.size() > 0)
                                        subServiceId = artistServicesBean.subServies.get(childPos).subServiceId;
                                }


                                tv_bizType.setText(artistServicesBean.serviceName + "");
                                cv_ly_biz_type.setVisibility(View.GONE);

                                if (artistServicesBean.subServies.size() == 1) {
                                    isOpenCategory = false;
                                } else isOpenCategory = true;

                                if (artistServicesBean.subServies.size() > 0) {
                                    if (artistServicesBean.subServies.get(childPos) != null) {
                                        tv_category.setText(artistServicesBean.subServies.get(childPos).subServiceName + "");

                                        inCallList.clear();
                                        outCallList.clear();

                                        for (int i = 0; i < artistServicesBean.subServies.get(childPos).artistservices.size(); i++) {

                                            if (artistServicesBean.subServies.get(childPos).artistservices.get(i).bookingType.equals("Both")) {
                                                if (childId == (artistServicesBean.subServies.get(childPos).artistservices.get(i)._id)) {
                                                    artistServicesBean.subServies.get(childPos).artistservices.get(i).isSelected = true;
                                                    totalTime = (getMinutes(artistServicesBean.subServies.get(childPos).artistservices.get(i).completionTime) + getMinutes(preprationTime));
                                                }
                                                inCallList.add(artistServicesBean.subServies.get(childPos).artistservices.get(i));
                                                outCallList.add(artistServicesBean.subServies.get(childPos).artistservices.get(i));
                                            } else if (artistServicesBean.subServies.get(childPos).artistservices.get(i).bookingType.equals("Incall")) {
                                                if (childId == (artistServicesBean.subServies.get(childPos).artistservices.get(i)._id)) {
                                                    artistServicesBean.subServies.get(childPos).artistservices.get(i).isSelected = true;

                                                    totalTime = (getMinutes(artistServicesBean.subServies.get(childPos).artistservices.get(i).completionTime) + getMinutes(preprationTime));
                                                }
                                                inCallList.add(artistServicesBean.subServies.get(childPos).artistservices.get(i));
                                            } else if (artistServicesBean.subServies.get(childPos).artistservices.get(i).bookingType.equals("Outcall")) {
                                                if (childId == (artistServicesBean.subServies.get(childPos).artistservices.get(i)._id)) {
                                                    artistServicesBean.subServies.get(childPos).artistservices.get(i).isSelected = true;

                                                    totalTime = (getMinutes(artistServicesBean.subServies.get(childPos).artistservices.get(i).completionTime) + getMinutes(preprationTime));


                                                }
                                                outCallList.add(artistServicesBean.subServies.get(childPos).artistservices.get(i));
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

                                            if (isOutCallSelected) {
                                                if (outCallList.size() == 0) {
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
                                    public void onclick(Services.ArtistServicesBean.SubServiesBean bean, int position) {
                                        subServiceId = bean.subServiceId;
                                        resetAllServices();
                                        childPos = position;
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

                                            if (isOutCallSelected) {
                                                if (outCallList.size() == 0) {
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

//this is the case when we are coming from service tab
                        if (services.artistServices.size() != 0) {
                            for (int i = 0; i < services.artistServices.size(); i++) {
                                if (services.artistServices.get(i).serviceName.equals(mainServiceName)) {
                                    adapterBizType.clickItem(i);
                                }
                            }
                        } else if (services.artistServices.size() == 0) {
                            tv_bizType.setText("No business type available");
                            tv_category.setText("No category available");
                            iv_down_arrow_bizType.setVisibility(View.GONE);
                            iv_down_arrow_category.setVisibility(View.GONE);
                        }

//this is the case when we are coming from other tab not from service tab
                        if (mainServiceName.equals("")) {
                            adapterBizType.clickItem();
                        } else {
                            apiForserviceStaff(String.valueOf(childId));
                        }
                        rcv_biz_type.setAdapter(adapterBizType);

                        // Calling api fot staff
                        if (isOutCallSelected) {
                            if (outcallStaff) {
                                ly_staff_main.setVisibility(View.VISIBLE);
                            } else ly_staff_main.setVisibility(View.GONE);
                        } else {
                            if (incallStaff) {
                                ly_staff_main.setVisibility(View.VISIBLE);
                            } else ly_staff_main.setVisibility(View.GONE);
                        }


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

    private void apiForserviceStaff(String artistServiceId) {
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
        params.put("businessId", artistId);
        params.put("artistServiceId", artistServiceId);
        params.put("day", String.valueOf(dayId));

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingActivity.this, "serviceStaff", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equals("success")) {
                        staffInfoBeanList.clear();
                        Gson gson = new Gson();
                        ServiceInfoBooking infoBooking = gson.fromJson(response, ServiceInfoBooking.class);
                        infoBooking.staffInfo.get(0).isSelected = true;

                        if (isOutCallSelected) {
                            price = infoBooking.staffInfo.get(0).outCallPrice;
                        } else price = infoBooking.staffInfo.get(0).inCallPrice;

                        endTime = (getMinutes(infoBooking.staffInfo.get(0).completionTime) + getMinutes(preprationTime));

                        if (infoBooking.staffInfo.size() == 1) {
                            ly_staff_main.setVisibility(View.GONE);
                        }

                        staffInfoBeanList.addAll(infoBooking.staffInfo);

                        staffAdapter.notifyDataSetChanged();
                        apiForstaffSlot(String.valueOf(0));
                        ly_time_slot_main.setVisibility(View.VISIBLE);
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

    /*.................................Apis for getting staff info..............................*/

    private int getMinutes(String input) {
        int totalMinues = 0;
        try {
            String hours = Helper.formateDateFromstring("HH:mm", "HH", input);
            String minutes = Helper.formateDateFromstring("HH:mm", "mm", input);

            int hoursInMinutes = (Integer.parseInt(hours) * 60);
            totalMinues = (Integer.parseInt(minutes) + hoursInMinutes);

        } catch (Exception e) {

        }

        return totalMinues;
    }

    private void apiForstaffSlot(String staffId) {
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

        /*artistId:2
        artistServiceId:1
        businessType:business
        currentTime:12:00 AM
        date:2019-01-15
        day:1
        latitude:22.75
        longitude:75.88
        serviceTime:00:20 // praperession time
        staffId:1
        userId:1*/

        Map<String, String> params = new HashMap<>();
        params.put("artistId", artistId);
        params.put("artistServiceId", artistServiceId);
        params.put("staffId", staffId);
        params.put("userId", String.valueOf(Mualab.currentUser.id));

        params.put("businessType", businessType);
        params.put("currentTime", currentTime);
        params.put("date", selectedDate);
        params.put("day", String.valueOf(dayId));
        params.put("latitude", String.valueOf(Mualab.currentLocation.lat));
        params.put("longitude", String.valueOf(Mualab.currentLocation.lng));
        params.put("serviceTime", ("00:" + String.valueOf(totalTime)));

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingActivity.this, "artistTimeSlotNew", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    timeSlotList.clear();
                    if (status.equals("success")) {
                        JSONArray array = js.getJSONArray("timeSlots");

                        for (int i = 0; i < array.length(); i++) {
                            TimeSlotInfo timeSlotInfo = new TimeSlotInfo();
                            timeSlotInfo.timeSlots = array.get(i).toString();
                            timeSlotInfo.isSelectSlot = false;
                            timeSlotList.add(timeSlotInfo);
                        }
                    } else {
                        MyToast.getInstance(BookingActivity.this).showDasuAlert(message);
                    }

                    if (timeSlotList.size() == 0) {
                        tvNoSlot.setVisibility(View.VISIBLE);
                        ly_time_slot_main.setVisibility(View.GONE);
                    } else {
                        tvNoSlot.setVisibility(View.GONE);
                        ly_time_slot_main.setVisibility(View.VISIBLE);
                    }

                    rcv_timeSlot.scrollToPosition(0);
                    timeSlotAdapter.notifyDataSetChanged();

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

    private static Date addMinutesToDate(int minutes, Date beforeTime){
        final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs

        long curTimeInMs = beforeTime.getTime();
        Date afterAddingMins = new Date(curTimeInMs + (minutes * ONE_MINUTE_IN_MILLIS));
        return afterAddingMins;
    }

    private void apiForContinueBooking(String bookingId) {
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
        params.put("startTime", startTime);

        String myTime = startTime;

        SimpleDateFormat df = new SimpleDateFormat("HH:mm aa");
        Date d = new Date();
        try {
            d = df.parse(myTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.MINUTE, endTime);
        String newTime = df.format(cal.getTime());


        params.put("endTime", newTime);
        params.put("artistId", artistId);
        params.put("staff", String.valueOf(staff));
        params.put("userId", String.valueOf(Mualab.currentUser.id));

        params.put("serviceId", String.valueOf(serviceId));// business type Id
        params.put("subServiceId", String.valueOf(subServiceId));// category Id
        params.put("artistServiceId", artistServiceId);
        params.put("bookingDate", selectedDate);

        if (isOutCallSelected) {
            params.put("serviceType", "2");// incall = 1 or out call = 2
        } else params.put("serviceType", "1");// incall = 1 or out call = 2


        params.put("price", String.valueOf(price));
        params.put("bookingId", bookingId);// first blank this is use for update service


        HttpTask task = new HttpTask(new HttpTask.Builder(BookingActivity.this, "bookArtist", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(BookingActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");


                    if (status.equals("success")) {
                        Intent intent = new Intent(BookingActivity.this, BookingConfirmActivity.class);
                        intent.putExtra("artistId", artistId);
                        intent.putExtra("isBankAdded", isBankAdded);
                        intent.putExtra("isOutCallSelected", isOutCallSelected);
                        startActivityForResult(intent, 111);
                        startTime = "";
                    } else {
                        MyToast.getInstance(BookingActivity.this).showDasuAlert(message);
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


}
