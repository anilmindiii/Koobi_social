package com.mualab.org.user.activity.searchBoard.activity;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.VolleyError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.activity.searchBoard.adapter.RefineServiceExpandListAdapter;
import com.mualab.org.user.activity.searchBoard.adapter.ServiceAdapter;
import com.mualab.org.user.activity.searchBoard.fragment.RefineSubServiceAdapter;
import com.mualab.org.user.activity.searchBoard.fragment.SearchBoardFragment;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.SearchBoard.RefineSearchBoard;
import com.mualab.org.user.data.model.SearchBoard.RefineServices;
import com.mualab.org.user.data.model.SearchBoard.RefineSubServices;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.listener.DatePickerListener;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
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

import static com.mualab.org.user.utils.constants.Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE;

public class RefineArtistActivity extends AppCompatActivity implements View.OnClickListener, DatePickerListener {
    private ExpandableListView lvExpandable;
    private boolean isServiceOpen = false, isClear = false, isFavClick;
    private ImageView ivDistance;
    private TextView tv_refine_dnt, tv_refine_loc;
    private RefineServiceExpandListAdapter expandableListAdapter;
    private ArrayList<RefineServices> services, tempSerevice;
    private String mainServId = "", sortType = "", sortSearch = "", serviceType = "", lat = "", lng = "", day = "", date_time = "", format, time = "", subServiceId = "", location = "";
    private int mHour, mMinute, dayId = 100;
    private float rating;
    private RefineSearchBoard refineSearchBoard;
    private CheckBox chbOutcall;
    private Session session;
    private TextView tvShowDistance, tv_price;
    private IndicatorSeekBar seekBarLocation;
    private RelativeLayout rlSelectLocation;
    private RelativeLayout rlSelectradiusSeekbar;
    private int seekBarrange;
    private boolean isAlreadySelectedLocation = false;
    private IndicatorSeekBar seekBarPrice;
    private int SeekBarPriceValue;
    private RelativeLayout rlPriceSeekBar;
    private TextView tv_service_category;
    private View viewSelectRadius;
    private ArrayList<RefineSubServices> arrayList = new ArrayList<>();
    private ProgressBar progress_bar;
    ArrayList<RefineSubServices> tempSubList;
    ArrayList<RefineSubServices> finalSubList;
    TextView tv_business;
    String serviceName = "";
    Handler seekBarHandler;
    RatingBar userRating;

    public RefineArtistActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refine_artist);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            refineSearchBoard = (RefineSearchBoard) bundle.getSerializable("params");
            isFavClick = bundle.getBoolean("param2");
        }
        tempSubList = new ArrayList<>();
        finalSubList = new ArrayList<>();
        tv_business = findViewById(R.id.tv_business);
        tv_service_category = findViewById(R.id.tv_service_category);
        tv_price = findViewById(R.id.tv_price);
        userRating = findViewById(R.id.userRating);
        initView();
        setViewId();

    }

    private void initView() {
        tempSerevice = new ArrayList<>();
        session = Mualab.getInstance().getSessionManager();
        if (refineSearchBoard != null) {
            services = refineSearchBoard.refineServices;
            tempSerevice.addAll(refineSearchBoard.tempSerevice);
            expandableListAdapter = new RefineServiceExpandListAdapter(RefineArtistActivity.this, services);
        } else {
            services = new ArrayList<>();
            expandableListAdapter = new RefineServiceExpandListAdapter(RefineArtistActivity.this, services);
        }
    }

    private void setViewId() {
        // ImageView ivBack = findViewById(R.id.ivHeaderBack);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(R.string.titel_refine);
        //ivBack.setVisibility(View.VISIBLE);
        ImageView ivPrice = findViewById(R.id.ivPrice);

        progress_bar = findViewById(R.id.progress_bar);
        viewSelectRadius = findViewById(R.id.viewSelectRadius);
        RelativeLayout rlRefineLocation1 = findViewById(R.id.rlRefineLocation);
        RelativeLayout rlPrice1 = findViewById(R.id.rlPrice);
        rlPriceSeekBar = findViewById(R.id.rlPriceSeekBar);
        seekBarPrice = findViewById(R.id.seekBarPrice);
        seekBarLocation = findViewById(R.id.seekBarLocation);
        tvShowDistance = findViewById(R.id.textShowRadius);
        rlSelectLocation = findViewById(R.id.rlSelectLocation);
        ImageView ivHeaderBack = findViewById(R.id.btnBack);
        ivHeaderBack.setOnClickListener(this);
        //   tvShowDistance = findViewById(R.id.tvShowDistance);
        RelativeLayout rlBusiness = findViewById(R.id.rlBusiness);

        rlSelectradiusSeekbar = findViewById(R.id.rlSelectradiusSeekbar);


        rlBusiness.setOnClickListener(this);
        rlPrice1.setOnClickListener(this);
        rlRefineLocation1.setOnClickListener(this);
        rlSelectLocation.setOnClickListener(this);

        tv_refine_dnt = findViewById(R.id.tv_refine_dnt);
        tv_refine_loc = findViewById(R.id.tv_refine_loc);
        chbOutcall = findViewById(R.id.chbOutcall);

        AppCompatButton btnApply = findViewById(R.id.btnApply);
        AppCompatButton btnClear = findViewById(R.id.btnClear);

        chbOutcall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    serviceType = "1";
                } else {
                    serviceType = "";
                }
            }
        });

        userRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating_, boolean fromUser) {
                rating = rating_;
            }
        });

        seekBarLocation.with(this)
                .max(20)
                .min(1)
                .build();

        seekBarLocation.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                seekBarrange = seekParams.progress;
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });


        seekBarPrice.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                SeekBarPriceValue = seekParams.progress;
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

        seekBarHandler = new Handler();
        seekBarHandler.post(new Runnable() {
            @Override
            public void run() {
                if (seekBarLocation != null) {
                    seekBarLocation.setMin(1);
                    seekBarLocation.setMax(20);
                    seekBarLocation.setProgress(5);
                }

                if (seekBarPrice != null) {
                    seekBarPrice.setMin(0);
                    seekBarPrice.setMax(995);
                    seekBarPrice.setProgress(0);
                }
            }
        });

        lvExpandable = findViewById(R.id.lvService);
        RelativeLayout rlService = findViewById(R.id.rlService);
        RelativeLayout rlPrice = findViewById(R.id.rlPrice);
        RelativeLayout rlRefineLocation = findViewById(R.id.rlRefineLocation);
        RelativeLayout rlDnT = findViewById(R.id.rlDnT);


        setRefineData();


        lvExpandable.setAdapter(expandableListAdapter);

        lvExpandable.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {

            }
        });

        lvExpandable.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                RefineServices item = services.get(groupPosition);
                item.isExpand = true;
            }
        });

        // Listview Group collasped listener
        lvExpandable.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                RefineServices item = services.get(groupPosition);
                item.isExpand = false;

            }
        });


        lvExpandable.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
                onGroupClickListener(expandableListView, view, groupPosition, l);
                return false;
            }
        });

        lvExpandable.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                return false;
            }
        });

        rlService.setOnClickListener(this);
        rlPrice.setOnClickListener(this);
        rlDnT.setOnClickListener(this);
        rlRefineLocation.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnApply.setOnClickListener(this);
    }


    private void setRefineData() {
        String latitude = refineSearchBoard.latitude;
        String longitude = refineSearchBoard.longitude;
        String location = refineSearchBoard.location;
        this.location = location;

        if (session.getSaveSearch() != null) {
            refineSearchBoard = session.getSaveSearch();
            refineSearchBoard.latitude = latitude;
            refineSearchBoard.longitude = longitude;
            refineSearchBoard.location = location;
            this.location = location;
        }

        if (refineSearchBoard != null) {
            lat = refineSearchBoard.latitude;
            lng = refineSearchBoard.longitude;

            subServiceId = refineSearchBoard.subservice;
            mainServId = refineSearchBoard.service;
            serviceType = refineSearchBoard.serviceType;
            sortSearch = refineSearchBoard.sortSearch;
            sortType = refineSearchBoard.sortType;
            time = refineSearchBoard.time;
            date_time = refineSearchBoard.date;


            if (refineSearchBoard.priceFilter != null)
                SeekBarPriceValue = Integer.parseInt(refineSearchBoard.priceFilter);

            if (refineSearchBoard.LocationFilter != null)
                seekBarrange = Integer.parseInt(refineSearchBoard.LocationFilter);

            seekBarHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (refineSearchBoard.LocationFilter != null) {
                        seekBarLocation.setProgress(Float.parseFloat(refineSearchBoard.LocationFilter));
                    }
                }
            });

            seekBarHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (refineSearchBoard.priceFilter != null) {
                        seekBarPrice.setProgress(Float.parseFloat(refineSearchBoard.priceFilter));
                    }
                }
            });

            location = refineSearchBoard.location;
            isFavClick = refineSearchBoard.isFavClick;

            if (refineSearchBoard.day != null)
                day = refineSearchBoard.day;

            if (date_time != null)
                if (!date_time.equals("")) {
                    tv_refine_dnt.setVisibility(View.VISIBLE);
                    tv_refine_dnt.setText(date_time + " " + time);

                }
            if (location.equals("")) {
                tv_refine_loc.setVisibility(View.GONE);
            } else tv_refine_loc.setVisibility(View.VISIBLE);

            tv_refine_loc.setText(location);


            if (refineSearchBoard.LocationFilter != null) {
                rlSelectradiusSeekbar.setVisibility(View.GONE);
                tvShowDistance.setVisibility(View.VISIBLE);
                tvShowDistance.setText("1-" + refineSearchBoard.LocationFilter + " Miles");
            }

            if (refineSearchBoard.priceFilter != null) {
                rlPriceSeekBar.setVisibility(View.GONE);
                tv_price.setVisibility(View.VISIBLE);
                tv_price.setText("0-£" + refineSearchBoard.priceFilter);
            }


            if (refineSearchBoard.rating != null) {
                rating = Float.parseFloat(refineSearchBoard.rating);
                userRating.setRating(Float.parseFloat(refineSearchBoard.rating));
            }


            if (sortSearch != null)
                if (sortSearch.equals("price")) {
//                ivPrice.setImageResource(R.drawable.active_price_ico);
//                ivDistance.setImageResource(R.drawable.route_ico);
                }

            if (serviceType != null)
                if (serviceType.equals("1")) {
                    chbOutcall.setChecked(true);
                }


            apiForGetAllServices();
        } else {
            apiForGetAllServices();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                if (session.getSaveSearch() == null) {
                    onBackPressed();
                } else filterApply();

                break;

            case R.id.rlRefineLocation:
                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(RefineArtistActivity.this, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if (isConnected) {
                                dialog.dismiss();
                                getAddress();
                                isAlreadySelectedLocation = true;
                            }
                        }
                    }).show();
                } else {
                    getAddress();
                }

                break;

            case R.id.rlDnT:
                // Initialize
                SwitchDateTimeDialogFragment dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                        "Select Date & Time",
                        "OK",
                        "Cancel"
                );


                // Assign values
                dateTimeDialogFragment.startAtCalendarView();
                dateTimeDialogFragment.set24HoursMode(false);
                Calendar c = new GregorianCalendar();
                Date d1 = c.getTime();
                dateTimeDialogFragment.setMinimumDateTime(d1);
                dateTimeDialogFragment.setMaximumDateTime(new GregorianCalendar(2040, 11, 31).getTime());


                dateTimeDialogFragment.setDefaultDateTime(d1);

// Define new day and month format
                try {
                    dateTimeDialogFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("dd MMMM", Locale.getDefault()));
                } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
                    Log.e("eee", e.getMessage());
                }

// Set listener
                dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Date date) {
                        String selectDate = Helper.formateDateFromstring("EEE MMM dd HH:mm:ss zzz yyyy", "dd/MM/yyyy hh:mm aa", date.toString());
                        date_time = Helper.formateDateFromstring("EEE MMM dd HH:mm:ss zzz yyyy", "dd/MM/yyyy", date.toString());

                        time = Helper.formateDateFromstring("EEE MMM dd HH:mm:ss zzz yyyy", "hh:mm aa", date.toString());

                        tv_refine_dnt.setVisibility(View.VISIBLE);
                        tv_refine_dnt.setText(selectDate);
                        day = day(Helper.formateDateFromstring("EEE MMM dd HH:mm:ss zzz yyyy", "EEE", date.toString()));
                    }

                    @Override
                    public void onNegativeButtonClick(Date date) {
                        // Date is get on negative button click
                    }
                });


                dateTimeDialogFragment.setAlertStyle(R.style.calender_style);
                dateTimeDialogFragment.show(getSupportFragmentManager(), "dialog_time");
                break;

            case R.id.rlService:

                if (tempSubList.size() != 0) {
                    serviceDialog();
                } else {
                    if (!serviceName.equals("")) {
                        MyToast.getInstance(this).showDasuAlert(getString(R.string.no_category));
                    } else
                        MyToast.getInstance(this).showDasuAlert(getString(R.string.please_select_business_type));
                }

                break;

            case R.id.rlSelectLocation:
                tvShowDistance.setVisibility(View.GONE);
                rlSelectradiusSeekbar.setVisibility(View.VISIBLE);
                // seekBarLocation.setProgress(seekBarrange);

                seekBarHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (refineSearchBoard != null) {
                            seekBarLocation.setProgress(Float.parseFloat(refineSearchBoard.LocationFilter));
                        }
                    }
                });


                break;

            case R.id.rlPrice:
                rlPriceSeekBar.setVisibility(View.VISIBLE);
                tv_price.setVisibility(View.GONE);

                seekBarHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (refineSearchBoard != null) {
                            seekBarPrice.setProgress(Float.parseFloat(refineSearchBoard.priceFilter));
                        }
                    }
                });


                break;

            case R.id.btnClear:
                refineSearchBoard = null;
                session.setIsOutCallFilter(false);
                session.saveFilter(null);
                Intent intent = new Intent(RefineArtistActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
                break;

            case R.id.btnApply:
                filterApply();
                break;
            case R.id.rlBusiness:
                showBusinnessDialog();
                break;

        }
    }


    private String day(String weekDay) {
        String day = "";
        switch (weekDay) {
            case "Sun":
                day = "6";
                break;

            case "Mon":
                day = "0";
                break;

            case "Tue":
                day = "1";
                break;

            case "Wed":
                day = "2";
                break;

            case "Thu":
                day = "3";
                break;

            case "Fri":
                day = "4";
                break;

            case "Sat":
                day = "5";
                break;

        }

        return day;

    }


    private void onGroupClickListener(ExpandableListView expandableListView, View view, int groupPosition, long l) {
        RefineServices servicesItem = services.get(groupPosition);

        if (!servicesItem.isChecked) {
            servicesItem.isChecked = true;
            servicesItem.isSubItemChecked = true;
            expandableListAdapter.notifyDataSetChanged();

        } else {
            servicesItem.isChecked = false;
            servicesItem.isSubItemChecked = false;
            expandableListAdapter.notifyDataSetChanged();

        }
    }

    private void filterApply() {
        RefineSearchBoard refineSearchBoard;
        if (isClear) {
            refineSearchBoard = null;
        } else {
            refineSearchBoard = new RefineSearchBoard();

            mainServId = "";
            subServiceId = "";

            for (RefineServices services : services) {
                if (services.isChecked) {
                    mainServId = services.id + "," + mainServId;
                }
            }

            for (RefineSubServices services : tempSubList) {
                if (services.isSubItemChecked) {
                    subServiceId = services.id + "," + subServiceId;
                }
            }

            if (mainServId.endsWith(",")) {
                mainServId = mainServId.substring(0, mainServId.length() - 1);
            }

            if (subServiceId.endsWith(",")) {
                subServiceId = subServiceId.substring(0, subServiceId.length() - 1);
            }


            refineSearchBoard.latitude = lat;
            refineSearchBoard.longitude = lng;
            refineSearchBoard.service = mainServId;
            refineSearchBoard.subservice = subServiceId;
            refineSearchBoard.serviceType = serviceType;
            refineSearchBoard.sortSearch = sortSearch;
            refineSearchBoard.sortType = sortType;
            refineSearchBoard.time = time;
            refineSearchBoard.date = date_time;
            refineSearchBoard.priceFilter = String.valueOf(SeekBarPriceValue);
            refineSearchBoard.LocationFilter = String.valueOf(seekBarrange);
            refineSearchBoard.location = location;
            refineSearchBoard.isFavClick = SearchBoardFragment.isFavClick;
            refineSearchBoard.day = day;
            refineSearchBoard.rating = String.valueOf(rating);

            refineSearchBoard.refineServices.addAll(services);
            refineSearchBoard.tempSerevice.addAll(tempSerevice);
            session.saveFilter(refineSearchBoard);
        }

        if (serviceType != null) {
            if (serviceType.equals("1")) {
                session.setIsOutCallFilter(false);
            } else {
                session.setIsOutCallFilter(true);
            }
        }
        session.setIsOutCallFilter(true);

        Intent intent = new Intent(RefineArtistActivity.this, MainActivity.class);
        intent.putExtra("refineSearchBoard", refineSearchBoard);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void getAddress() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(RefineArtistActivity.this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
        } catch (GooglePlayServicesNotAvailableException e) {
        }
    }

    private void apiForGetAllServices() {
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(RefineArtistActivity.this, new NoConnectionDialog.Listner() {
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
        // params.put("appType", "user");


        HttpTask task = new HttpTask(new HttpTask.Builder(RefineArtistActivity.this, "allCategory", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {

                        JSONArray jsonArray = js.getJSONArray("serviceList");
                        services.clear();
                        tempSerevice.clear();
                        if (jsonArray != null) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                RefineServices service = new RefineServices();
                                service.id = jsonObject.getString("_id");
                                service.title = jsonObject.getString("title");
                                JSONArray subArray = jsonObject.getJSONArray("subService");


                                for (int j = 0; j < subArray.length(); j++) {
                                    RefineSubServices subItem = new RefineSubServices();
                                    JSONObject jsonObject2 = subArray.getJSONObject(j);
                                    subItem.id = jsonObject2.getString("_id").trim();
                                    subItem.image = jsonObject2.getString("image").trim();
                                    subItem.title = jsonObject2.getString("title").trim();
                                    subItem.serviceId = jsonObject2.getString("serviceId").trim();
                                    subItem.isSubItemChecked = false;
                                    arrayList.add(subItem);
                                }
                                service.setArrayList(arrayList);
                                services.add(service);
                                tempSerevice.add(service);
                            }

                            if (refineSearchBoard != null) {
                                if (refineSearchBoard.service != null) {
                                    List<String> items = Arrays.asList(refineSearchBoard.service.split("\\s*,\\s*"));
                                    for (int k = 0; k < items.size(); k++) {
                                        for (int i = 0; i < services.size(); i++) {
                                            if (items.get(k).equals(services.get(i).id)) {

                                                services.get(i).ischeckFinal = true;
                                                services.get(i).ischeckLocal = true;
                                                services.get(i).isChecked = true;
                                                serviceName = services.get(i).title + ", " + serviceName;

                                                for (int j = 0; j < arrayList.size(); j++) {
                                                    RefineSubServices temp = arrayList.get(j);
                                                    if (services.get(i).id.equals(temp.serviceId)) {
                                                        RefineSubServices subServices = new RefineSubServices();
                                                        subServices.id = temp.id;
                                                        subServices.title = temp.title;
                                                        subServices.image = temp.image;
                                                        subServices.serviceId = temp.serviceId;

                                                        tempSubList.add(subServices);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (serviceName.endsWith(", ")) {
                                        serviceName = serviceName.substring(0, serviceName.length() - 2);
                                    }

                                    if (serviceName.equals("")) {
                                        tv_business.setVisibility(View.GONE);
                                    } else {
                                        tv_business.setVisibility(View.VISIBLE);
                                    }
                                    tv_business.setText(serviceName);
                                }

                                if (refineSearchBoard.subservice != null) {
                                    String subserviceName = "";
                                    List<String> items = Arrays.asList(refineSearchBoard.subservice.split("\\s*,\\s*"));
                                    for (int k = 0; k < items.size(); k++) {
                                        for (int i = 0; i < tempSubList.size(); i++) {
                                            if (items.get(k).equals(tempSubList.get(i).id)) {
                                                RefineSubServices services = new RefineSubServices();
                                                services.id = tempSubList.get(i).id;
                                                services.isSubItemChecked = true;
                                                tempSubList.get(i).isSubItemChecked = true;
                                                finalSubList.add(services);
                                                subserviceName = tempSubList.get(i).title + ", " + subserviceName;
                                            }
                                        }
                                    }

                                    if (subserviceName.endsWith(", ")) {
                                        subserviceName = subserviceName.substring(0, subserviceName.length() - 2);
                                    }

                                    if (subserviceName.equals("")) {
                                        tv_service_category.setVisibility(View.GONE);
                                    } else tv_service_category.setVisibility(View.VISIBLE);
                                    tv_service_category.setText(subserviceName);

                                }
                            }

                            expandableListAdapter.notifyDataSetChanged();
                        } else {
                            MyToast.getInstance(RefineArtistActivity.this).showSmallCustomToast("No Artist available!");
                        }
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    // Progress.hide(RefineArtistActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
               /* try{
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")){
                        Mualab.getInstance().getSessionManager().logout();
                        //  MyToast.getInstance(RefineArtistActivity.this).showSmallCustomToast(helper.error_Messages(error));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
*/

            }
        })
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));

        task.execute(this.getClass().getName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                if (isAlreadySelectedLocation) {
                    seekBarLocation.setProgress(0);
                    tvShowDistance.setVisibility(View.GONE);
                    isAlreadySelectedLocation = false;

                } else {
                    tvShowDistance.setVisibility(View.VISIBLE);
                    rlSelectLocation.setVisibility(View.VISIBLE);
                    tvShowDistance.setVisibility(View.GONE);

                    rlSelectradiusSeekbar.setVisibility(View.VISIBLE);

                }

                if (place.getName().equals("")) {
                    tv_refine_loc.setVisibility(View.GONE);
                } else tv_refine_loc.setVisibility(View.VISIBLE);

                tv_refine_loc.setText(place.getName());
                location = "" + place.getName();
                LatLng latLng = place.getLatLng();
                lat = String.valueOf(latLng.latitude);
                lng = String.valueOf(latLng.longitude);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }


    @Override
    public void onBackPressed() {

        if (session.getSaveSearch() == null) {
            Intent intent = new Intent(RefineArtistActivity.this, MainActivity.class);
            intent.putExtra("locationData", refineSearchBoard);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        } else filterApply();


    }

    @Override
    public void onDateSet(int year, int month, int day, int cal_type) {
        tv_refine_dnt.setVisibility(View.VISIBLE);
        tv_refine_dnt.setText(day + "/" + (month + 1) + "/" + year);

    }

    public void showBusinnessDialog() {
        final Dialog dialog = new Dialog(RefineArtistActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_show_service);
        TextView tvHeader = dialog.findViewById(R.id.tvHeader);
        android.support.v7.widget.SearchView searchview = dialog.findViewById(R.id.searchview);
        searchview.setQueryHint("Search Business Type");
        tvHeader.setText(getResources().getString(R.string.select_business_type));
        final RecyclerView recyclerView = dialog.findViewById(R.id.recyclerview);
        ImageView img_cancel = dialog.findViewById(R.id.img_cancel);
        Button btn_done = dialog.findViewById(R.id.btn_done);
        LinearLayoutManager layoutManager = new LinearLayoutManager(RefineArtistActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        for (int i = 0; i < services.size(); i++) {
            if (services.get(i).ischeckFinal) {
                services.get(i).ischeckLocal = true;
                services.get(i).isChecked = true;
            } else {
                services.get(i).ischeckLocal = false;
                services.get(i).isChecked = false;
            }
        }


        final ArrayList<RefineServices> searchList = new ArrayList<>();
        searchview.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList.clear();
                for (RefineServices services : services) {
                    if (services.title.toLowerCase().trim().contains(newText.toLowerCase().trim())) {
                        searchList.add(services);
                    }
                }
                final ServiceAdapter serviceAdapter = new ServiceAdapter(searchList, RefineArtistActivity.this);
                recyclerView.setAdapter(serviceAdapter);
                return false;
            }
        });


        final ServiceAdapter serviceAdapter = new ServiceAdapter(services, RefineArtistActivity.this);
        recyclerView.setAdapter(serviceAdapter);

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceName = "";
                tempSubList.clear();
                for (int i = 0; i < services.size(); i++) {
                    if (services.get(i).ischeckLocal) {

                        services.get(i).ischeckFinal = true;
                        serviceName = services.get(i).title + ", " + serviceName;

                        for (int j = 0; j < arrayList.size(); j++) {
                            RefineSubServices temp = arrayList.get(j);
                            if (services.get(i).id.equals(temp.serviceId)) {
                                RefineSubServices subServices = new RefineSubServices();
                                subServices.id = temp.id;
                                subServices.title = temp.title;
                                subServices.image = temp.image;
                                subServices.serviceId = temp.serviceId;
                                tempSubList.add(subServices);
                            }
                        }

                    } else {
                        services.get(i).ischeckFinal = false;
                    }
                }

                if (tempSubList.size() == 0) {
                    finalSubList.clear();
                    tv_service_category.setText("");
                }

                if (serviceName.endsWith(", ")) {
                    serviceName = serviceName.substring(0, serviceName.length() - 2);
                }

                if (serviceName.equals("")) {
                    tv_business.setVisibility(View.GONE);
                } else {
                    tv_business.setVisibility(View.VISIBLE);
                }


                tv_business.setText(serviceName);
                dialog.dismiss();

            }
        });


        img_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();


    }


    public void serviceDialog() {
        final Dialog dialog = new Dialog(RefineArtistActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_show_service);
        TextView tvHeader = dialog.findViewById(R.id.tvHeader);
        android.support.v7.widget.SearchView searchview = dialog.findViewById(R.id.searchview);
        searchview.setQueryHint("Search Category");
        tvHeader.setText(getResources().getString(R.string.select_category));
        final RecyclerView recyclerView = dialog.findViewById(R.id.recyclerview);
        ImageView img_cancel = dialog.findViewById(R.id.img_cancel);
        Button btn_done = dialog.findViewById(R.id.btn_done);
        img_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        for (int i = 0; i < tempSubList.size(); i++) {
            tempSubList.get(i).isSubItemChecked = false;
        }

        for (int i = 0; i < finalSubList.size(); i++) {
            for (int j = 0; j < tempSubList.size(); j++) {
                if (finalSubList.get(i).id.equals(tempSubList.get(j).id)) {

                    if (tempSubList.get(j).isSubItemCheckedFinal) {
                        tempSubList.get(j).isSubItemChecked = true;
                    } else {
                        tempSubList.get(j).isSubItemChecked = false;
                    }


                }
            }
        }

        final ArrayList<RefineSubServices> searchList = new ArrayList<>();
        searchview.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList.clear();
                for (RefineSubServices services : tempSubList) {
                    if (services.title.toLowerCase().trim().contains(newText.toLowerCase().trim())) {
                        searchList.add(services);
                    }
                }
                recyclerView.setAdapter(new RefineSubServiceAdapter(searchList, RefineArtistActivity.this));
                return false;
            }
        });


        LinearLayoutManager layoutManager = new LinearLayoutManager(RefineArtistActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new RefineSubServiceAdapter(tempSubList, RefineArtistActivity.this));

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalSubList.clear();
                String subserviceName = "";
                RefineSubServices services = null;
                for (int i = 0; i < tempSubList.size(); i++) {
                    services = new RefineSubServices();

                    if (tempSubList.get(i).isSubItemChecked) {
                        services.id = tempSubList.get(i).id;
                        //services.isSubItemChecked = true;
                        tempSubList.get(i).isSubItemCheckedFinal = true;
                        finalSubList.add(services);
                        subserviceName = tempSubList.get(i).title + ", " + subserviceName;
                    } else {
                        tempSubList.get(i).isSubItemCheckedFinal = false;
                    }
                }

                if (subserviceName.endsWith(", ")) {
                    subserviceName = subserviceName.substring(0, subserviceName.length() - 2);
                }
                if (subserviceName.equals("")) {
                    tv_service_category.setVisibility(View.GONE);
                } else tv_service_category.setVisibility(View.VISIBLE);
                tv_service_category.setText(subserviceName);

                dialog.dismiss();


            }
        });

        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();


    }
}
