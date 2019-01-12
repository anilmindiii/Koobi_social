package com.mualab.org.user.activity.myprofile.activity.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mualab.org.user.R;
import com.mualab.org.user.Views.refreshviews.CircleHeaderView;
import com.mualab.org.user.Views.refreshviews.OnRefreshListener;
import com.mualab.org.user.Views.refreshviews.RjRefreshLayout;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.artist_profile.activity.FollowersActivity;
import com.mualab.org.user.activity.artist_profile.adapter.ArtistFeedAdapter;
import com.mualab.org.user.activity.artist_profile.model.UserProfileData;
import com.mualab.org.user.activity.feeds.activity.CommentsActivity;
import com.mualab.org.user.activity.feeds.activity.FeedSingleActivity;
import com.mualab.org.user.activity.feeds.activity.LikeFeedActivity;
import com.mualab.org.user.activity.feeds.activity.PreviewImageActivity;
import com.mualab.org.user.activity.feeds.adapter.ViewPagerAdapter;
import com.mualab.org.user.activity.myprofile.activity.adapter.NavigationMenuAdapter;
import com.mualab.org.user.activity.myprofile.activity.model.NavigationItem;
import com.mualab.org.user.activity.people_tag.instatag.InstaTag;
import com.mualab.org.user.activity.people_tag.models.TagDetail;
import com.mualab.org.user.activity.people_tag.instatag.TagToBeTagged;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.listener.RecyclerViewScrollListener;
import com.mualab.org.user.menu.MenuAdapter;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.WrapContentLinearLayoutManager;
import com.mualab.org.user.utils.constants.Constant;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener,
        ArtistFeedAdapter.Listener, NavigationMenuAdapter.Listener {
    private DrawerLayout drawer;
    private String TAG = this.getClass().getName();
    ;
    private User user;
    private TextView tvImages, tvVideos, tvFeeds, tv_msg, tv_no_data_msg, tv_dot1, tv_dot2, tv_profile_followers;
    private LinearLayout ll_progress;
    private RecyclerView rvFeed;
    private RjRefreshLayout mRefreshLayout;
    private RecyclerViewScrollListener endlesScrollListener;
    private int CURRENT_FEED_STATE = 0, lastFeedTypeId;
    private String feedType = "", userId;
    private ArtistFeedAdapter feedAdapter;
    private List<Feeds> feeds;
    private boolean isPulltoRefrash = false;
    private long mLastClickTime = 0;
    private UserProfileData profileData = null;
    private List<NavigationItem> navigationItems;
    private AppCompatButton btnFollow, btnMsg;
    private ViewPagerAdapter.LongPressListner longPressListner;
    private TextView tvFilter;
    private ImageView ivFilter;
    private PopupWindow popupWindow;
    private ArrayList<String> arrayList = new ArrayList<>();
    private boolean isMenuOpen;
    private ImageView iv_gride_view, iv_list_view;
    private boolean isGrideView;
    private EditText ed_search;
    private ImageView iv_search_icon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        Intent i = getIntent();
        if (i != null) {
            userId = i.getStringExtra("userId");
        }
        init();
    }

    private void init() {
        Session session = Mualab.getInstance().getSessionManager();
        user = session.getUser();
        feeds = new ArrayList<>();
        navigationItems = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbar);
        LinearLayout lyImage = toolbar.findViewById(R.id.ly_images);
        LinearLayout lyVideos = toolbar.findViewById(R.id.ly_videos);
        LinearLayout lyFeed = toolbar.findViewById(R.id.ly_feeds);

        tvImages = toolbar.findViewById(R.id.tv_image);
        tvFilter = toolbar.findViewById(R.id.tvFilter);
        LinearLayout ll_filter;
        tvVideos = findViewById(R.id.tv_videos);
        tvFeeds = findViewById(R.id.tv_feed);
        ivFilter = findViewById(R.id.ivFilter);
        ed_search = findViewById(R.id.ed_search);
        iv_search_icon = findViewById(R.id.iv_search_icon);
        iv_gride_view = toolbar.findViewById(R.id.iv_gride_view);
        iv_list_view = toolbar.findViewById(R.id.iv_list_view);
        /*tv_dot1 =  findViewById(R.id.tv_dot1);
        tv_dot2 =  findViewById(R.id.tv_dot2);
*/
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView ivChat = findViewById(R.id.ivChat);
        // ivChat.setVisibility(View.VISIBLE);
        ImageView ivUserProfile = findViewById(R.id.ivUserProfile);
        ImageView ivDrawer = findViewById(R.id.btnNevMenu);
        ivUserProfile.setVisibility(View.GONE);

        btnFollow = findViewById(R.id.btnFollow);
        btnMsg = findViewById(R.id.btnMsg);
        //   final AppBarLayout mainView = findViewById(R.id.appbar);
        if (userId.equals(String.valueOf(user.id))) {
            ivDrawer.setVisibility(View.VISIBLE);
            btnFollow.setVisibility(View.GONE);
            btnMsg.setVisibility(View.GONE);
        } else {
            ivDrawer.setVisibility(View.GONE);
            btnFollow.setVisibility(View.VISIBLE);
            btnMsg.setVisibility(View.VISIBLE);
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        drawer = findViewById(R.id.drawer_layout);
        // navigationView.setNavigationItemSelectedListener(this);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        ;
        //  drawer.setScrimColor(getResources().getColor(android.R.color.transparent));
        navigationView.setItemIconTintList(null);

        addItems();

        RecyclerView rycslidermenu = findViewById(R.id.rycslidermenu);
        LinearLayoutManager layoutManager = new LinearLayoutManager(UserProfileActivity.this);
        rycslidermenu.setLayoutManager(layoutManager);
        NavigationMenuAdapter listAdapter = new NavigationMenuAdapter(UserProfileActivity.this, navigationItems, drawer, this);

        rycslidermenu.setAdapter(listAdapter);

        final RelativeLayout rlContent = findViewById(R.id.rlContent);
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                // mainView.setTranslationX(slideOffset * drawerView.getWidth());
                drawer.bringChildToFront(drawerView);
                drawer.requestLayout();
                float slideX = drawerView.getWidth() * slideOffset;
                rlContent.setTranslationX(-slideX);
            }
        });
        TextView user_name = findViewById(R.id.user_name);
        CircleImageView user_image = findViewById(R.id.user_image);
        final User user = Mualab.getInstance().getSessionManager().getUser();

        if (!user.profileImage.isEmpty()) {
            Picasso.with(this).load(user.profileImage).placeholder(R.drawable.default_placeholder).
                    fit().into(user_image);
        }

        user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.profileImage != null && !user.profileImage.equals("")) {
                    ArrayList<String> tempList = new ArrayList<>();
                    tempList.add(user.profileImage);

                    Intent intent = new Intent(UserProfileActivity.this, PreviewImageActivity.class);
                    intent.putExtra("imageArray", tempList);
                    intent.putExtra("startIndex", 0);
                    startActivity(intent);
                }
            }
        });

        user_name.setText(user.userName);


        rvFeed = findViewById(R.id.rvFeed);

        LinearLayout llFollowers = findViewById(R.id.llFollowers);
        LinearLayout llFollowing = findViewById(R.id.llFollowing);
        LinearLayout llPost = findViewById(R.id.llPost);

        tv_msg = findViewById(R.id.tv_msg);
        ll_filter = findViewById(R.id.ll_filter);
        ll_filter.setOnClickListener(this);

        tv_no_data_msg = findViewById(R.id.tv_no_data_msg);
        ll_progress = findViewById(R.id.ll_progress);

        WrapContentLinearLayoutManager lm = new WrapContentLinearLayoutManager(UserProfileActivity.this, LinearLayoutManager.VERTICAL, false);
        rvFeed.setItemAnimator(null);
        rvFeed.setLayoutManager(lm);
        rvFeed.setHasFixedSize(true);

        feedAdapter = new ArtistFeedAdapter(UserProfileActivity.this, feeds, this);

        endlesScrollListener = new RecyclerViewScrollListener(lm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (feedAdapter != null) {
                    feedAdapter.showHideLoading(true);
                    apiForGetAllFeeds(page, 200, false, "");
                }
            }

            @Override
            public void onScroll(RecyclerView view, int dx, int dy) {

            }
        };

        rvFeed.setAdapter(feedAdapter);
        rvFeed.addOnScrollListener(endlesScrollListener);

        mRefreshLayout = findViewById(R.id.mSwipeRefreshLayout);
        final CircleHeaderView header = new CircleHeaderView(UserProfileActivity.this);
        mRefreshLayout.addHeader(header);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                endlesScrollListener.resetState();
                isPulltoRefrash = true;
                apiForGetAllFeeds(0, 200, false, "");

            }

            @Override
            public void onLoadMore() {

            }
        });

        rvFeed.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP)
                    hideQuickView();
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });


        ed_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                feeds.clear();
                endlesScrollListener.resetState();
                apiForGetAllFeeds(0, 200, false, s.toString().trim());
            }
        });

        iv_search_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ed_search.getVisibility() == View.VISIBLE) {
                    ed_search.setVisibility(View.GONE);
                } else ed_search.setVisibility(View.VISIBLE);

            }
        });


        lyImage.setOnClickListener(this);
        lyVideos.setOnClickListener(this);
        lyFeed.setOnClickListener(this);
        llFollowers.setOnClickListener(this);
        llFollowing.setOnClickListener(this);
        llPost.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        ivUserProfile.setOnClickListener(this);
        ivChat.setOnClickListener(this);
        ivDrawer.setOnClickListener(this);
        btnFollow.setOnClickListener(this);


        iv_gride_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGrideView = true;
                endlesScrollListener.resetState();
                apiForGetAllFeeds(0, 200, false, "");


                iv_gride_view.setImageResource(R.drawable.active_grid_icon);
                iv_list_view.setImageResource(R.drawable.inactive_list);
            }
        });

        iv_list_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endlesScrollListener.resetState();
                apiForGetAllFeeds(0, 200, false, "");


                isGrideView = false;

                iv_gride_view.setImageResource(R.drawable.inactive_grid_icon);
                iv_list_view.setImageResource(R.drawable.active_list);

            }
        });


        apiForGetProfile();
    }

    private void apiForGetProfile() {

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(UserProfileActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetProfile();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("viewBy", "");
        params.put("loginUserId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(UserProfileActivity.this, "getProfile", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    if (feeds != null && feeds.size() == 0)
                        updateViewType(R.id.ly_feeds);

                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    Progress.hide(UserProfileActivity.this);
                    if (status.equalsIgnoreCase("success")) {
                        JSONArray userDetail = js.getJSONArray("userDetail");
                        JSONObject object = userDetail.getJSONObject(0);
                        Gson gson = new Gson();

                        profileData = gson.fromJson(String.valueOf(object), UserProfileData.class);

                        // profileData = gson.fromJson(response, UserProfileData.class);
                        setProfileData(profileData);
                        // updateViewType(profileData,R.id.ly_videos);

                    } else {
                        MyToast.getInstance(UserProfileActivity.this).showDasuAlert(message);
                    }
                    updateViewType(R.id.ly_feeds);

                } catch (Exception e) {
                    Progress.hide(UserProfileActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try {
                    updateViewType(R.id.ly_feeds);
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
                        FirebaseInstanceId.getInstance().deleteInstanceId();
                        //      MyToast.getInstance(BookingActivity.this).showSmallCustomToast(helper.error_Messages(error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        })
                .setAuthToken(user.authToken)
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(getClass().getName());
    }

    @SuppressLint("SetTextI18n")
    private void setProfileData(UserProfileData profileData) {

        TextView tv_ProfileName = findViewById(R.id.tv_ProfileName);
        TextView tv_username = findViewById(R.id.tv_username);
        TextView tvRatingCount = findViewById(R.id.tvRatingCount);
        final CircleImageView user_image = findViewById(R.id.user_image);

        TextView tv_distance = findViewById(R.id.tv_distance);
        TextView tv_profile_post = findViewById(R.id.tv_profile_post);
        TextView tv_profile_following = findViewById(R.id.tv_profile_following);
        tv_profile_followers = findViewById(R.id.tv_profile_followers);
        final CircleImageView iv_Profile = findViewById(R.id.iv_Profile);
        ImageView ivActive = findViewById(R.id.ivActive);
        RatingBar rating = findViewById(R.id.rating);

        if (profileData.followerStatus.equals("1")) {
            btnFollow.setText("Unfollow");
        } else {
            btnFollow.setText("Follow");
        }


        if (profileData != null) {
            tv_ProfileName.setText(profileData.firstName + " " + profileData.lastName);
            tv_distance.setText(profileData.radius + " Miles");
            tv_username.setText("@" + profileData.userName);
            tv_profile_followers.setText(profileData.followersCount);
            tv_profile_following.setText(profileData.followingCount);
            tv_profile_post.setText(profileData.postCount);
            tvRatingCount.setText("(" + profileData.reviewCount + ")");
            float ratingCount = Float.parseFloat(profileData.ratingCount);
            rating.setRating(ratingCount);

            if (!profileData.profileImage.isEmpty()) {
                Picasso.with(this).load(profileData.profileImage).
                        placeholder(R.drawable.default_placeholder).fit().into(iv_Profile);
            }
        }

        iv_Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_image.callOnClick();
            }
        });

    }

    private void updateViewType(int id) {
        tvVideos.setTextColor(getResources().getColor(R.color.text_color));
        tvImages.setTextColor(getResources().getColor(R.color.text_color));
        tvFeeds.setTextColor(getResources().getColor(R.color.text_color));
        endlesScrollListener.resetState();
        int prevSize = feeds.size();
        switch (id) {
            case R.id.ly_feeds:
                //addRemoveHeader(true);
                tvFeeds.setTextColor(getResources().getColor(R.color.colorPrimary));

                if (lastFeedTypeId != R.id.ly_feeds) {
                    feeds.clear();
                    endlesScrollListener.resetState();
                    feedType = "";
                    CURRENT_FEED_STATE = Constant.FEED_STATE;
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                    apiForGetAllFeeds(0, 200, true, "");
                }
                break;

            case R.id.ly_images:
                tvImages.setTextColor(getResources().getColor(R.color.colorPrimary));
                // addRemoveHeader(false);
                if (lastFeedTypeId != R.id.ly_images) {
                    feeds.clear();
                    endlesScrollListener.resetState();
                    feedType = "image";
                    CURRENT_FEED_STATE = Constant.IMAGE_STATE;
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                    apiForGetAllFeeds(0, 200, true, "");
                }

                break;

            case R.id.ly_videos:
                tvVideos.setTextColor(getResources().getColor(R.color.colorPrimary));
                // addRemoveHeader(false);
                if (lastFeedTypeId != R.id.ly_videos) {
                    feeds.clear();
                    endlesScrollListener.resetState();
                    feedType = "video";
                    CURRENT_FEED_STATE = Constant.VIDEO_STATE;
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                    apiForGetAllFeeds(0, 200, true, "");
                }
                break;


        }

        lastFeedTypeId = id;
    }

    private void apiForGetAllFeeds(final int page, final int feedLimit, final boolean isEnableProgress, final String searchText) {
        ll_progress.setVisibility(View.VISIBLE);
        tv_no_data_msg.setVisibility(View.GONE);

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(UserProfileActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetAllFeeds(page, feedLimit, isEnableProgress, searchText);
                    }

                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("feedType", feedType);
        params.put("search", "");
        params.put("page", String.valueOf(page));
        params.put("limit", String.valueOf(feedLimit));
        params.put("type", "");
        params.put("userId", userId);
        params.put("loginUserId", String.valueOf(user.id));
        params.put("viewBy", "");
        params.put("search", searchText);
        // params.put("appType", "user");
        Mualab.getInstance().cancelPendingRequests(this.getClass().getName());
        new HttpTask(new HttpTask.Builder(UserProfileActivity.this, "profileFeed", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                ll_progress.setVisibility(View.GONE);
                feedAdapter.showHideLoading(false);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        //removeProgress();
                        ParseAndUpdateUI(response);

                    } else if (page == 0) {
                        rvFeed.setVisibility(View.GONE);
                        tv_no_data_msg.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    ll_progress.setVisibility(View.GONE);
                    tv_no_data_msg.setVisibility(View.VISIBLE);
                    tv_no_data_msg.setText("Something went wrong!");
                    e.printStackTrace();
                    // MyToast.getInstance(mContext).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                ll_progress.setVisibility(View.GONE);
                if (isPulltoRefrash) {
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(false, 500);
                    int prevSize = feeds.size();
                    feeds.clear();
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                }
                //MyToast.getInstance(mContext).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
            }
        })
                .setAuthToken(Mualab.currentUser.authToken)
                .setParam(params)
                .setMethod(Request.Method.POST)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute(TAG);
        //ll_progress.setVisibility(isEnableProgress ? View.VISIBLE : View.GONE);
    }

    private void ParseAndUpdateUI(final String response) {

        try {
            JSONObject js = new JSONObject(response);
            String status = js.getString("status");
            // String message = js.getString("message");

            if (status.equalsIgnoreCase("success")) {
                rvFeed.setVisibility(View.VISIBLE);
                JSONArray array = js.getJSONArray("AllFeeds");
                if (isPulltoRefrash) {
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(true, 500);
                    int prevSize = feeds.size();
                    feeds.clear();
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                }

                Gson gson = new Gson();
                if (array.length() != 0) {

                    if (isGrideView) {
                        feeds.clear();
                    }

                    for (int i = 0; i < array.length(); i++) {
                        try {
                            JSONObject jsonObject = array.getJSONObject(i);
                            Feeds feed = gson.fromJson(String.valueOf(jsonObject), Feeds.class);
                            //  feed.taggedImgMap = new HashMap<>();
                            /*tmp get data and set into actual json format*/
                            if (feed.userInfo != null && feed.userInfo.size() > 0) {
                                Feeds.User user = feed.userInfo.get(0);
                                feed.userName = user.userName;
                                feed.fullName = user.firstName + " " + user.lastName;
                                feed.profileImage = user.profileImage;
                                feed.userId = user._id;
                                feed.crd = feed.timeElapsed;
                            }

                            if (feed.feedData != null && feed.feedData.size() > 0) {

                                feed.feed = new ArrayList<>();
                                feed.feedThumb = new ArrayList<>();

                                for (Feeds.Feed tmp : feed.feedData) {
                                    feed.feed.add(tmp.feedPost);
                                    if (!TextUtils.isEmpty(feed.feedData.get(0).videoThumb))
                                        feed.feedThumb.add(tmp.feedPost);
                                }

                                if (feed.feedType.equals("video"))
                                    feed.videoThumbnail = feed.feedData.get(0).videoThumb;
                            }

                            JSONArray jsonArray = jsonObject.getJSONArray("peopleTag");
                            if (jsonArray.length() != 0) {

                                for (int j = 0; j < jsonArray.length(); j++) {

                                    feed.peopleTagList = new ArrayList<>();
                                    JSONArray arrayJSONArray = jsonArray.getJSONArray(j);

                                    for (int k = 0; k < arrayJSONArray.length(); k++) {
                                        JSONObject object = arrayJSONArray.getJSONObject(k);

                                        HashMap<String, TagDetail> tagDetails = new HashMap<>();

                                        String unique_tag_id = object.getString("unique_tag_id");
                                        double x_axis = Double.parseDouble(object.getString("x_axis"));
                                        double y_axis = Double.parseDouble(object.getString("y_axis"));

                                        JSONObject tagOjb = object.getJSONObject("tagDetails");
                                        TagDetail tag;
                                        if (tagOjb.has("tabType")) {
                                            tag = gson.fromJson(String.valueOf(tagOjb), TagDetail.class);
                                        } else {
                                            JSONObject details = tagOjb.getJSONObject(unique_tag_id);
                                            tag = gson.fromJson(String.valueOf(details), TagDetail.class);
                                        }
                                        tagDetails.put(tag.title, tag);
                                        TagToBeTagged tagged = new TagToBeTagged();
                                        tagged.setUnique_tag_id(unique_tag_id);
                                        tagged.setX_co_ord(x_axis);
                                        tagged.setY_co_ord(y_axis);
                                        tagged.setTagDetails(tagDetails);

                                        feed.peopleTagList.add(tagged);
                                    }
                                    feed.taggedImgMap.put(j, feed.peopleTagList);
                                }
                            }

                            if (isGrideView) {
                                if (!feed.feedType.equals("text")) {
                                    feeds.add(feed);
                                }
                            } else {
                                feeds.add(feed);
                            }


                        } catch (JsonParseException e) {
                            ll_progress.setVisibility(View.GONE);
                            tv_no_data_msg.setVisibility(View.VISIBLE);
                            tv_no_data_msg.setText("Something went wrong!");
                            e.printStackTrace();
                        }

                    }// loop end.
                } else if (feeds.size() == 0) {
                    rvFeed.setVisibility(View.GONE);
                    tv_no_data_msg.setVisibility(View.VISIBLE);
                }

                if (isGrideView) {
                    rvFeed.setLayoutManager(new GridLayoutManager(UserProfileActivity.this, 3));
                    feedAdapter.isGrideView(true);
                    endlesScrollListener.resetState();
                } else {
                    rvFeed.setLayoutManager(new LinearLayoutManager(UserProfileActivity.this));
                    feedAdapter.isGrideView(false);
                    endlesScrollListener.resetState();
                }


                feedAdapter.notifyDataSetChanged();

            } else if (status.equals("fail") && feeds.size() == 0) {
                rvFeed.setVisibility(View.GONE);
                tv_msg.setVisibility(View.VISIBLE);

                if (isPulltoRefrash) {
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(false, 500);

                }
                feedAdapter.notifyDataSetChanged();
            }

        } catch (
                JSONException e)

        {
            e.printStackTrace();
            feedAdapter.notifyDataSetChanged();
            //MyToast.getInstance(mContext).showSmallCustomToast(getString(R.string.alert_something_wenjt_wrong));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Mualab.getInstance().cancelPendingRequests(TAG);
        tvImages = null;
        tvVideos = null;
        tvFeeds = null;
        tv_msg = null;
        ll_progress = null;
        endlesScrollListener = null;
        feedAdapter = null;
        feeds = null;
        rvFeed = null;
        user = null;
        profileData = null;
    }

    @Override
    public void onCommentBtnClick(Feeds feed, int pos) {
        Intent intent = new Intent(UserProfileActivity.this, CommentsActivity.class);
        intent.putExtra("feed_id", feed._id);
        intent.putExtra("feedPosition", pos);
        intent.putExtra("feed", feed);
        intent.putExtra("commentCount", feed.commentCount);
        startActivityForResult(intent, Constant.ACTIVITY_COMMENT);
    }

    @Override
    public void onLikeListClick(Feeds feed) {
        //addFragment(LikeFragment.newInstance(feed._id, user.id), true);

        Intent intent = new Intent(UserProfileActivity.this, LikeFeedActivity.class);
        intent.putExtra("feedId", feed._id);
        intent.putExtra("userId", user.id);
        startActivity(intent);


    }

    @Override
    public void onFeedClick(Feeds feed, int index, View v) {
        //publicationQuickView(feed, index);
        showLargeImage(feed, index);
    }

    @Override
    public void onClickProfileImage(Feeds feed, ImageView v) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            apiForGetProfile();
            //apiForGetAllFeeds(0, 10, true);
        } else if (data != null) {
            if (requestCode == Constant.ACTIVITY_COMMENT) {
                if (CURRENT_FEED_STATE == Constant.FEED_STATE) {
                    int pos = data.getIntExtra("feedPosition", 0);
                    Feeds feed = data.getParcelableExtra("feed");
                    // feeds.get(pos).commentCount = feed.commentCount;
                    feeds.get(pos).commentCount = data.getIntExtra("commentCount", 0);
                    feedAdapter.notifyItemChanged(pos);
                }
            }
        }
    }

    /* frangment replace code */
    public void addFragment(Fragment fragment, boolean addToBackStack) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_in, 0, 0);
            transaction.add(R.id.container, fragment, backStackName);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }

    }

    private Dialog builder;

    public void publicationQuickView(Feeds feeds, int index) {
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.dialog_image_detail_view, null);

        ImageView postImage = view.findViewById(R.id.ivFeedCenter);
        ImageView profileImage = view.findViewById(R.id.ivUserProfile);
        TextView tvUsername = view.findViewById(R.id.txtUsername);
        tvUsername.setText(feeds.userName);

        view.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideQuickView();
            }
        });

        view.findViewById(R.id.tvUnfollow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyToast.getInstance(UserProfileActivity.this).showSmallCustomToast(getString(R.string.under_development));
            }
        });

        Picasso.with(this).load(feeds.feed.get(index)).priority(Picasso.Priority.HIGH).noPlaceholder().into(postImage);

        if (TextUtils.isEmpty(feeds.profileImage))
            Picasso.with(this).load(R.drawable.default_placeholder).noPlaceholder().into(profileImage);
        else Picasso.with(this).load(feeds.profileImage).noPlaceholder().into(profileImage);

        builder = new Dialog(UserProfileActivity.this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //noinspection ConstantConditions
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        builder.setContentView(view);
        builder.setCancelable(true);
        builder.show();
    }

    public void hideQuickView() {
        if (builder != null) builder.dismiss();
    }

    @Override
    public void onClick(View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 800) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (view.getId()) {
            case R.id.ly_feeds:
            case R.id.ly_images:
            case R.id.ly_videos:
                updateViewType(view.getId());
                break;

            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.btnFollow:
                int followersCount = Integer.parseInt(profileData.followersCount);

                if (profileData.followerStatus.equals("1")) {
                    profileData.followerStatus = "0";
                    btnFollow.setText("Follow");
                    followersCount--;
                } else {
                    profileData.followerStatus = "1";
                    btnFollow.setText("Unfollow");
                    followersCount++;
                }
                profileData.followersCount = String.valueOf(followersCount);
                tv_profile_followers.setText("" + followersCount);
                apiForGetFollowUnFollow();
                break;

            case R.id.btnNevMenu:
                if (drawer.isDrawerOpen(GravityCompat.END)) {
                    drawer.closeDrawer(GravityCompat.END);
                } else {
                    drawer.openDrawer(GravityCompat.END);
                }
                break;

          /*  case R.id.llAboutUs:
                MyToast.getInstance(UserProfileActivity.this).showDasuAlert("Under development");
                break;
*/
            case R.id.ivChat:
             /*   Intent chat_intent = new Intent(UserProfileActivity.this, ChatActivity.class);
                chat_intent.putExtra("userId",profileData._id);
                startActivity(chat_intent);*/
                break;

            case R.id.llFollowing:
               /* Intent intent1 = new Intent(UserProfileActivity.this,FollowersActivity.class);
                intent1.putExtra("isFollowers",false);
                intent1.putExtra("artistId",user.id);
                startActivityForResult(intent1,10);*/
                Bundle bundle1 = new Bundle();
                bundle1.putBoolean("isFollowers", false);
                bundle1.putString("artistId", String.valueOf(user.id));
                Intent intent1 = new Intent(UserProfileActivity.this, FollowersActivity.class);
                intent1.putExtras(bundle1);
                startActivityForResult(intent1, 10);
                break;

            case R.id.llFollowers:
                Bundle bundle2 = new Bundle();
                bundle2.putBoolean("isFollowers", true);
                bundle2.putString("artistId", String.valueOf(user.id));
                Intent intent2 = new Intent(UserProfileActivity.this, FollowersActivity.class);
                intent2.putExtras(bundle2);
                startActivityForResult(intent2, 10);
                //startActivity(new Intent(mContext,FollowersActivity.class));
                break;

            case R.id.llPost:
                updateViewType(R.id.ly_feeds);
                break;

            case R.id.ll_filter:
                int[] location = new int[2];
                // Get the x, y location and store it in the location[] array
                // location[0] = x, location[1] = y.
                ivFilter.getLocationOnScreen(location);

                //Initialize the Point with x, and y positions
                Point p = new Point();
                p.x = location[0];
                p.y = location[1];
                arrayList.clear();

                if (!isMenuOpen) {
                    initiatePopupWindow(p);
                } else {
                    isMenuOpen = false;
                    popupWindow.dismiss();
                }


                break;


        }
    }

    private void initiatePopupWindow(Point p) {

        try {
            LayoutInflater inflater = (LayoutInflater) UserProfileActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View layout = inflater.inflate(R.layout.layout_popup_menu, (ViewGroup) findViewById(R.id.parent));
            popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);
            int OFFSET_X = 460;
            int OFFSET_Y = 35;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.setElevation(5);
            }
            arrayList.add("All");
            arrayList.add("Photo");
            arrayList.add("Video");
            popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
            RecyclerView recycler_view = layout.findViewById(R.id.recycler_view);
            MenuAdapter menuAdapter = new MenuAdapter(UserProfileActivity.this, arrayList, new MenuAdapter.Listener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onMenuClick(int pos) {
                    String data = arrayList.get(pos);
                    int prevSize = feeds.size();
                    switch (data) {
                        case "All":
                            tvFilter.setText(R.string.all);


                            feeds.clear();
                            endlesScrollListener.resetState();
                            feedType = "";
                            CURRENT_FEED_STATE = Constant.FEED_STATE;
                            feedAdapter.notifyItemRangeRemoved(0, prevSize);
                            apiForGetAllFeeds(0, 200, true, "");

                            popupWindow.dismiss();
                            break;

                        case "Photo":
                            tvFilter.setText(R.string.photo);
                            feeds.clear();
                            endlesScrollListener.resetState();
                            feedType = "image";
                            CURRENT_FEED_STATE = Constant.IMAGE_STATE;
                            feedAdapter.notifyItemRangeRemoved(0, prevSize);
                            apiForGetAllFeeds(0, 200, true, "");
                            popupWindow.dismiss();
                            break;

                        case "Video":
                            tvFilter.setText(R.string.video);
                            popupWindow.dismiss();
                            feeds.clear();
                            endlesScrollListener.resetState();
                            feedType = "video";
                            CURRENT_FEED_STATE = Constant.VIDEO_STATE;
                            feedAdapter.notifyItemRangeRemoved(0, prevSize);
                            apiForGetAllFeeds(0, 200, true, "");
                            break;

                    }
                }
            });

            LinearLayoutManager layoutManager = new LinearLayoutManager(UserProfileActivity.this);
            recycler_view.setLayoutManager(layoutManager);
            recycler_view.setAdapter(menuAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void apifortokenUpdate() {
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(UserProfileActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apifortokenUpdate();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("deviceToken", "");
        params.put("firebaseToken", "");
        //  params.put("followerId", String.valueOf(user.id));

        // params.put("loginUserId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(UserProfileActivity.this, "updateRecord", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {


                        NotificationManager notificationManager = (NotificationManager) UserProfileActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
                        assert notificationManager != null;
                        notificationManager.cancelAll();
                        FirebaseAuth.getInstance().signOut();
                        Mualab.getInstance().getSessionManager().logout();


                    } else {
                        MyToast.getInstance(UserProfileActivity.this).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(UserProfileActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try {
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
                        // MyToast.getInstance(BookingActivity.this).showDasuAlert(helper.error_Messages(error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        })
                .setAuthToken(user.authToken)
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

    private void addItems() {
        NavigationItem item;
        for (int i = 0; i < 9; i++) {
            item = new NavigationItem();
            switch (i) {
                case 0:
                    item.itemName = getString(R.string.edit_profile);
                    item.itemImg = R.drawable.profile_ico;

                    break;
                case 1:
                    item.itemName = getString(R.string.inbox);
                    item.itemImg = R.drawable.chat_ico;

                    break;

                case 2:
                    item.itemName = getString(R.string.title_booking);
                    item.itemImg = R.drawable.booking_ico;
                    break;

                case 3:
                    item.itemName = getString(R.string.payment_history);
                    item.itemImg = R.drawable.payment_history_ico;
                    break;

                case 4:
                    item.itemName = getString(R.string.business_invitation);
                    item.itemImg = R.drawable.id_card_ico;
                    break;

                case 5:
                    item.itemName = getString(R.string.rate_this_app);
                    item.itemImg = R.drawable.rating_star_ico;
                    break;

                case 6:
                    item.itemName = getString(R.string.setting);
                    item.itemImg = R.drawable.settings_ico;
                    break;

                case 7:
                    item.itemName = getString(R.string.about_us);
                    item.itemImg = R.drawable.slider_about_us_ico;
                    break;

                case 8:
                    item.itemName = getString(R.string.log_out);
                    item.itemImg = R.drawable.logout_ico;
                    break;

            }
            navigationItems.add(item);
        }
    }

    boolean isShow = false;

    private void showLargeImage(Feeds feeds, int index) {
        View dialogView = View.inflate(UserProfileActivity.this, R.layout.dialog_large_image_view, null);
        final Dialog dialog = new Dialog(UserProfileActivity.this, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.InOutAnimation;
        dialog.setContentView(dialogView);
        final InstaTag postImage = dialogView.findViewById(R.id.post_image);
        ImageView btnBack = dialogView.findViewById(R.id.btnBack);
        TextView tvHeaderTitle = dialogView.findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText("Images");

        postImage.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        postImage.setRootWidth(postImage.getMeasuredWidth());
        postImage.setRootHeight(postImage.getMeasuredHeight());

        if (feeds.feed.get(index) != null) {
            Glide.with(UserProfileActivity.this).load(feeds.feed.get(index)).placeholder(R.drawable.gallery_placeholder)
                    .skipMemoryCache(false).into(postImage.getTagImageView());
        }

        postImage.setImageToBeTaggedEvent(taggedImageEvent);

        ArrayList<TagToBeTagged> tags = feeds.taggedImgMap.get(index);
        if (tags != null && tags.size() != 0) {
            postImage.addTagViewFromTagsToBeTagged(tags, false);
            postImage.hideTags();
        }

        //   Picasso.with(mContext).load(feeds.feed.get(index)).priority(Picasso.Priority.HIGH).noPlaceholder().into(postImage);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        longPressListner = new ViewPagerAdapter.LongPressListner() {
            @Override
            public void onLongPress() {
                if (!isShow) {
                    isShow = true;
                    postImage.showTags();
                } else {
                    isShow = false;
                    postImage.hideTags();
                }

            }
        };

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        FragmentManager fm = getSupportFragmentManager();
        int i = fm.getBackStackEntryCount();
        if (i > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public void OnClick(int pos) {
        apifortokenUpdate();
    }

    private void apiForGetFollowUnFollow() {
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(UserProfileActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetFollowUnFollow();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));
        //  params.put("followerId", String.valueOf(user.id));
        params.put("followerId", userId);
        // params.put("loginUserId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(UserProfileActivity.this, "followFollowing", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {

                    } else {
                        MyToast.getInstance(UserProfileActivity.this).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(UserProfileActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try {
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
                        // MyToast.getInstance(BookingActivity.this).showDasuAlert(helper.error_Messages(error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        })
                .setAuthToken(user.authToken)
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

    private InstaTag.TaggedImageEvent taggedImageEvent = new InstaTag.TaggedImageEvent() {
        @Override
        public void singleTapConfirmedAndRootIsInTouch(int x, int y) {
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (longPressListner != null)
                longPressListner.onLongPress();
        }

        @Override
        public void onSinglePress(MotionEvent e) {
        }
    };


}
