package com.mualab.org.user.activity.feeds.activity;

import android.app.Dialog;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.core.Repo;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.authentication.ForgotPasswordActivity;
import com.mualab.org.user.activity.authentication.LoginActivity;
import com.mualab.org.user.activity.authentication.Registration2Activity;
import com.mualab.org.user.activity.feeds.adapter.ReportAdapter;
import com.mualab.org.user.activity.feeds.model.ReportInfo;
import com.mualab.org.user.activity.feeds.model.SubmitReportInfo;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.API;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.Util;
import com.mualab.org.user.utils.constants.Constant;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {
    private TextView tv_link,btn_submit;
    private TextView tv_reason;
    private EditText ed_description;
    private RecyclerView rev_reason;
    private ReportInfo reportInfo;
    private ReportAdapter adapter;
    private RelativeLayout ly_reason;
    ArrayList<ReportInfo.DataBean> dataBeans;
    private CardView cv_reason;
    private ImageView iv_back;
    ReportInfo.DataBean dataBean;
    int feedOwenerId = 0;
    int feedId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        tv_link = findViewById(R.id.tv_link);
        tv_reason = findViewById(R.id.tv_reason);
        ed_description = findViewById(R.id.ed_description);
        rev_reason = findViewById(R.id.rev_reason);
        cv_reason = findViewById(R.id.cv_reason);
        ly_reason = findViewById(R.id.ly_reason);
        iv_back = findViewById(R.id.iv_back);
        btn_submit = findViewById(R.id.btn_submit);

        if(getIntent().getIntExtra("feedOwenerId",0) != 0){
            feedOwenerId = getIntent().getIntExtra("feedOwenerId",0);
            feedId = getIntent().getIntExtra("feedId",0);
            tv_link.setText(API.BASE_URL+"feedDetails/"+ feedId+"");
        }


        dataBeans = new ArrayList<>();

        adapter = new ReportAdapter(dataBeans, ReportActivity.this, new ReportAdapter.getClick() {
            @Override
            public void OnClikcItem(ReportInfo.DataBean bean) {
                dataBean = bean;
                cv_reason.setVisibility(View.GONE);
                tv_reason.setText(bean.title);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rev_reason.setLayoutManager(manager);
        rev_reason.setAdapter(adapter);

        checkSocialUser();

        ly_reason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                KeyboardUtil.hideKeyboard(ly_reason, ReportActivity.this);

                if( cv_reason.getVisibility() == View.VISIBLE){
                    cv_reason.setVisibility(View.GONE);
                }else cv_reason.setVisibility(View.VISIBLE);



            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(tv_reason.getText().toString().trim().equals("")){
                    MyToast.getInstance(ReportActivity.this).showDasuAlert("Please select reason");
                    return;
                }

                SubmitReportInfo submitReportInfo = new SubmitReportInfo();
                submitReportInfo.description = ed_description.getText().toString().trim();
                submitReportInfo.feedId = feedId;
                submitReportInfo.feedOwnerId = feedOwenerId;
                submitReportInfo.myId = Mualab.currentUser.id+"";
                submitReportInfo.reason = tv_reason.getText().toString().trim();
                submitReportInfo.link = API.BASE_URL+"feedDetails/"+ feedId+"";
                submitReportInfo.timeStamp = ServerValue.TIMESTAMP;

                String key = FirebaseDatabase.getInstance().getReference().child("feed_report").push().getKey();

                FirebaseDatabase.getInstance().getReference().child("feed_report").child(key).setValue(submitReportInfo);
                MyToast.getInstance(ReportActivity.this).showDasuAlert("Report sent successfully");
                finish();
            }
        });
    }


    private void checkSocialUser() {
        Progress.show(ReportActivity.this);
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ReportActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        checkSocialUser();
                    }

                }
            }).show();
        }

        new HttpTask(new HttpTask.Builder(this, "reportReason", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {

                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        Gson gson = new Gson();
                        reportInfo = gson.fromJson(response,ReportInfo.class);
                        dataBeans.addAll(reportInfo.data);
                    } else{
                        // goto 3rd screen for register
                         }

                } catch (Exception e) {
                    e.printStackTrace();
                    Progress.hide(ReportActivity.this);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(ReportActivity.this);
            }
        })
                .setMethod(Request.Method.GET)
                .setProgress(true))
                .execute(this.getClass().getName());

    }

}
