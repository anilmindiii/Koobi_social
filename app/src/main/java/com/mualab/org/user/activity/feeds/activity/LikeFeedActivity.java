package com.mualab.org.user.activity.feeds.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.adapter.LikeListAdapter;
import com.mualab.org.user.activity.feeds.model.FeedLike;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.listener.EndlessRecyclerViewScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LikeFeedActivity extends AppCompatActivity {


    private EditText ed_search;
    private ProgressBar progress_bar;
    private LinearLayout ll_loadingBox;
    private EndlessRecyclerViewScrollListener scrollListener;
    private List<FeedLike> likedList=new ArrayList<>();
    private LikeListAdapter likeListAdapter;
    private TextView tvMsg;
    private int feedId;
    private int myUserId;
    private ImageView ivUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_feed);
        Intent intent=getIntent();
        if (intent!=null){
            feedId=intent.getIntExtra("feedId",0);
            myUserId=intent.getIntExtra("userId",1);
        }
        initView();
    }




    public void initView(){
        TextView tvHeaderTitle =findViewById(R.id.tvHeaderTitle);
        ImageView ivHeaderBack =findViewById(R.id.btnBack);
        ImageView ibtnChat = findViewById(R.id.ivChat);
        ImageView ivAppIcon = findViewById(R.id.ivAppIcon);
        tvHeaderTitle.setVisibility(View.VISIBLE);
        ivHeaderBack.setVisibility(View.VISIBLE);
        ivAppIcon.setVisibility(View.GONE);
        ibtnChat.setVisibility(View.GONE);
        tvHeaderTitle.setText(R.string.likes);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        tvMsg =findViewById(R.id.tv_msg);
        ed_search = findViewById(R.id.ed_search);
        ll_loadingBox = findViewById(R.id.ll_loadingBox);
        progress_bar = findViewById(R.id.progress_bar);
        ivUserProfile = findViewById(R.id.ivUserProfile);

        ivUserProfile.setVisibility(View.GONE);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(null);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                String quary = ed_search.getText().toString();
                apiForLikesList(page, TextUtils.isEmpty(quary)?"":quary);
            }
        };


        // Adds the scroll listener to RecyclerView
        recyclerView.addOnScrollListener(scrollListener);

        likeListAdapter = new LikeListAdapter(this, likedList, myUserId);
        recyclerView.setAdapter(likeListAdapter);


        ed_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String quary = ed_search.getText().toString();
                likedList.clear();
                scrollListener.resetState();
                likeListAdapter.notifyDataSetChanged();
                apiForLikesList(0, TextUtils.isEmpty(quary)?"":quary);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        ll_loadingBox.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.VISIBLE);
        tvMsg.setText(getString(R.string.loading));
        likedList.clear();
        apiForLikesList(0,"");
    }


    private void apiForLikesList(final int page, String search) {

        Map<String, String> map = new HashMap<>();
        map.put("feedId", ""+feedId);
        map.put("page", ""+page);
        map.put("limit", "20");
        map.put("search", search.toLowerCase());
        map.put("userId", ""+myUserId);
        String TAG = "";
        Mualab.getInstance().getRequestQueue().cancelAll(TAG);
        new HttpTask(new HttpTask.Builder(this, "likeList", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    progress_bar.setVisibility(View.GONE);
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    // String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        JSONArray array = js.getJSONArray("likeList");
                        Gson gson = new Gson();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            FeedLike likedListInfo = gson.fromJson(String.valueOf(jsonObject), FeedLike.class);
                            likedList.add(likedListInfo);
                        }
                        if (likedList.size() == 0) {
                            ll_loadingBox.setVisibility(View.VISIBLE);
                            tvMsg.setText(getString(R.string.text_empty_data));
                            tvMsg.setVisibility(View.VISIBLE);
                        } else {
                            ll_loadingBox.setVisibility(View.GONE);
                            tvMsg.setVisibility(View.GONE);
                        }
                        likeListAdapter.notifyDataSetChanged();
                    }else {
                        if (likedList.size() == 0) {
                            tvMsg.setText(getString(R.string.text_empty_data));
                            tvMsg.setVisibility(View.VISIBLE);
                        } else {
                            tvMsg.setVisibility(View.GONE);
                            ll_loadingBox.setVisibility(View.GONE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    tvMsg.setText(getString(R.string.msg_some_thing_went_wrong));
                    progress_bar.setVisibility(View.GONE);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                progress_bar.setVisibility(View.GONE);
            }
        }).setProgress(false)
                .setAuthToken(Mualab.currentUser.authToken)
                .setParam(map)).execute(TAG);
    }


}
