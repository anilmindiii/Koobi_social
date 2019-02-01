package com.mualab.org.user.activity.booking;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.adapter.NewBookingHistoryAdapter;
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
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private BalloonAdapter balloonAdapter;
    protected GoogleMap mGoogleMap;
    private MapFragment mapFragment;
    private TrackInfo trackUser;
    private TrackInfo trackArtist;
    private ArrayList<TrackInfo> mapBeanArrayList;
    private TextView tvDistance, tvstaffName, tv_date_n_time_text, tvServices;
    private ImageView ivartistProfilePic;
    private RelativeLayout ly_satelite_view, ly_my_location;
    private boolean isSatelliteMode;
    private  Timer timer;
    private int bookingId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        mapBeanArrayList = new ArrayList<>();
        if (getIntent() != null) {
            trackUser = (TrackInfo) getIntent().getSerializableExtra("trackUser");
            trackArtist = (TrackInfo) getIntent().getSerializableExtra("trackArtist");

            bookingId = getIntent().getIntExtra("bookingId",0);

            mapBeanArrayList.add(trackUser);
            mapBeanArrayList.add(trackArtist);
        }

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        tvDistance = findViewById(R.id.tvDistance);
        tvstaffName = findViewById(R.id.tvstaffName);
        ivartistProfilePic = findViewById(R.id.ivartistProfilePic);
        tv_date_n_time_text = findViewById(R.id.tv_date_n_time_text);
        tvServices = findViewById(R.id.tvServices);
        ly_satelite_view = findViewById(R.id.ly_satelite_view);
        ly_my_location = findViewById(R.id.ly_my_location);

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ly_my_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapBeanArrayList.size() != 0)
                    if (!mapBeanArrayList.get(0).latitude.equals("") && !mapBeanArrayList.get(0).longitude.equals(""))
                        setUpMap(mapBeanArrayList.get(0).latitude, mapBeanArrayList.get(0).longitude);// my locaton
            }
        });

        ly_satelite_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isSatelliteMode) {
                    isSatelliteMode = false;
                    //for normal view of map:
                    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else {
                    isSatelliteMode = true;
                    //for satellite view of map:
                    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }

            }
        });

        balloonAdapter = new BalloonAdapter(getLayoutInflater());

        tvServices.setText(trackArtist.artistServiceName);
        String selectedDate = Helper.formateDateFromstring("yyyy-MM-dd", "dd/MM/yyyy", trackArtist.bookingDate);//2019-02-01
        tv_date_n_time_text.setText(selectedDate + ", " + trackArtist.startTime);
        Glide.with(this).load(trackArtist.profileImage).placeholder(R.drawable.default_placeholder).into(ivartistProfilePic);
        tvstaffName.setText(trackArtist.staffName);
        if (!trackArtist.latitude.equals("") && !trackUser.latitude.equals("")) {
            tvDistance.setText(getDistance() + " Miles");
        }


        startTimer();
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mGoogleMap.setMyLocationEnabled(false);
            }
        } else {
            mGoogleMap.setMyLocationEnabled(false);
        }

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker != null) {
                    int position = Integer.parseInt(marker.getTitle());

                }
            }
        });

        wattingForImage(mapBeanArrayList);
        if (mapBeanArrayList.size() != 0)
            if (!mapBeanArrayList.get(0).latitude.equals("") && !mapBeanArrayList.get(0).longitude.equals(""))
                setUpMap(mapBeanArrayList.get(0).latitude, mapBeanArrayList.get(0).longitude);// my locaton


    }

    private void setUpMap(String appointLatitude, String appointLongitude) {
        LatLngBounds bounds = new LatLngBounds(new LatLng(Double.parseDouble(appointLatitude),
                Double.parseDouble(appointLongitude)), new LatLng(Double.parseDouble(appointLatitude),
                Double.parseDouble(appointLongitude)));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 10));
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }


    private void moveToNext(final ArrayList<TrackInfo> mapBeanArrayList, final int i) {

        if (i < mapBeanArrayList.size()) {

            final View markerView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custommarkerlayout, null);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            markerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            markerView.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
            markerView.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
            markerView.buildDrawingCache();
            ImageView imageuser = markerView.findViewById(R.id.marker_image);
            ImageView iv_outer = markerView.findViewById(R.id.iv_outer);

            if (trackUser.bookingDate == null) {
                iv_outer.setColorFilter(ContextCompat.getColor(this, R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN);
            } else
                iv_outer.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);


            /*.............info window Adapter...........................*/
            mGoogleMap.setInfoWindowAdapter(balloonAdapter);


            final TrackInfo mapBean = mapBeanArrayList.get(i);

            if (!mapBean.profileImage.equals("")) {
                Picasso.with(TrackingActivity.this)
                        .load(mapBean.profileImage).placeholder(R.drawable.default_placeholder)
                        .into(imageuser, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                if (!mapBean.latitude.equals(""))
                                    getMatkerDrop(markerView, mapBean, i);
                            }

                            @Override
                            public void onError() {
                                if (!mapBean.latitude.equals(""))
                                    getMatkerDrop(markerView, mapBean, i);
                            }
                        });

            }


        }
    }

    private void getMatkerDrop(View markerView, TrackInfo mapBean, int i) {
        Bitmap finalBitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(finalBitmap);
        markerView.draw(canvas);
        // update views
        LatLng point;
        double newLat = Double.parseDouble(mapBean.latitude) + (Math.random() - .5) / 1500;// * (Math.random() * (max - min) + min);
        double newLng = Double.parseDouble(mapBean.longitude) + (Math.random() - .5) / 1500;// * (Math.random() * (max - min) + min);
        point = new LatLng(newLat, newLng);
        // point = new LatLng(Double.parseDouble(mapBean.latitude), Double.parseDouble(mapBean.longitude));

        // Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.title("" + i);
        markerOptions.snippet(mapBean.address);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(finalBitmap));
        dropPinEffect(mGoogleMap.addMarker(markerOptions));

        if (i < mapBeanArrayList.size()) {
            moveToNext(mapBeanArrayList, i + 1);
        }
    }


    private void dropPinEffect(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;
        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 14 * t);
                if (t > 0.0) {
                    // Post again 15ms later.
                    handler.postDelayed(this, 15);
                }
            }
        });
    }

    private class BalloonAdapter implements GoogleMap.InfoWindowAdapter {

        LayoutInflater inflater = null;

        public BalloonAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            View v = inflater.inflate(R.layout.info_window_layout, null);
            if (marker != null) {
                String pos = marker.getTitle();
                int position = Integer.parseInt(pos);
               /* TextView titleTextview =  v.findViewById(R.id.tv_title);
                ImageView imageView = v.findViewById(R.id.imageView21);
                TextView addTextview =  v.findViewById(R.id.tv_address);
                titleTextview.setText(mapBeanArrayList.get(position).fullName);
                addTextview.setText(mapBeanArrayList.get(position).address);
                Picasso.with(TrackingActivity.this).load(mapBeanArrayList.get(position).profileImage).into(imageView);
*/

            }
            return (v);
        }

        @Override
        public View getInfoContents(Marker marker) {
            return (null);
        }

    }


    private void wattingForImage(final ArrayList<TrackInfo> mapBeanArrayList) {
        moveToNext(mapBeanArrayList, 0);
    }

    private String getDistance() {
        Location startPoint = new Location("locationA");

        startPoint.setLatitude(Double.parseDouble(trackArtist.latitude));
        startPoint.setLongitude(Double.parseDouble(trackArtist.longitude));

        Location endPoint = new Location("locationA");
        endPoint.setLatitude(Double.parseDouble(trackUser.latitude));
        endPoint.setLongitude(Double.parseDouble(trackArtist.longitude));

        double distance = startPoint.distanceTo(endPoint);
        distance = (distance / 1609.344);
        String roundValue = String.format("%.2f", distance);
        return roundValue;
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    public void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(Timer_Tick);
            }

        }, 0,30000);//30 sec

    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            getDetailsBookService(bookingId);
        }
    };

    private void getDetailsBookService(final int bookingId) {
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(TrackingActivity.this, new NoConnectionDialog.Listner() {
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


        HttpTask task = new HttpTask(new HttpTask.Builder(TrackingActivity.this, "bookingDetail", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equals("success")) {
                        Gson gson = new Gson();
                        BookingListInfo historyInfo = gson.fromJson(response, BookingListInfo.class);

                        String trackingLat = historyInfo.data.bookingInfo.get(0).trackingLatitude;
                        String trackingLng = historyInfo.data.bookingInfo.get(0).trackingLongitude;


                    } else {
                        MyToast.getInstance(TrackingActivity.this).showDasuAlert(message);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
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
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(this.getClass().getName());
    }

}
