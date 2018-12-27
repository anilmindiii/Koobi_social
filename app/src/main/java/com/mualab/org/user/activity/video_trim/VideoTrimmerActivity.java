package com.mualab.org.user.activity.video_trim;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.deep.videotrimmer.DeepVideoTrimmer;
import com.deep.videotrimmer.interfaces.OnTrimVideoListener;
import com.deep.videotrimmer.view.RangeSeekBarView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.activity.FeedPostActivity;
import com.mualab.org.user.data.model.MediaUri;
import com.mualab.org.user.databinding.ActivityVideoTrimmerBinding;
import com.mualab.org.user.utils.constants.Constant;

public class VideoTrimmerActivity extends AppCompatActivity implements OnTrimVideoListener {
    ActivityVideoTrimmerBinding mBinder;
    private DeepVideoTrimmer mVideoTrimmer;
    TextView textSize, tvCroppingMessage;
    RangeSeekBarView timeLineBar;
    private MediaUri mediaUri;
    private String caption;
    private int feedType;
    private Bitmap thumbImage = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinder = DataBindingUtil.setContentView(this, R.layout.activity_video_trimmer);

        String path = "";

        Intent intent = getIntent();
        if (intent!= null) {
            caption = intent.getStringExtra("caption");
            thumbImage = intent.getParcelableExtra("thumbImage");
            mediaUri = (MediaUri) intent.getSerializableExtra("mediaUri");
            feedType = intent.getIntExtra("feedType", Constant.IMAGE_STATE);


            if(!mediaUri.isFromGallery){
                path = mediaUri.videoFile.toString();
            }else path = mediaUri.uri;

            /*file:///storage/emulated/0/Android/data/com.mualab.org.user/cache/tmp.mp4*/
        }



        mVideoTrimmer = ((DeepVideoTrimmer) findViewById(R.id.timeLine));
        timeLineBar = (RangeSeekBarView) findViewById(R.id.timeLineBar);
        textSize = (TextView) findViewById(R.id.textSize);
        tvCroppingMessage = (TextView) findViewById(R.id.tvCroppingMessage);

        if (mVideoTrimmer != null && path != null) {
            mVideoTrimmer.setMaxDuration(60);
            mVideoTrimmer.setMaxFileSize(100);
            mVideoTrimmer.setOnTrimVideoListener(this);
            mVideoTrimmer.setVideoURI(Uri.parse(path));
        } else {
           // showToastLong(getString(R.string.toast_cannot_retrieve_selected_video));
        }
    }

    @Override
    public void getResult(final Uri uri) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvCroppingMessage.setVisibility(View.GONE);
            }
        });
        Constant.croppedVideoURI = uri.toString();


        if (mediaUri!=null){

            Intent  intent = new Intent(this, FeedPostActivity.class);

            mediaUri.uri = uri.toString();
            intent.putExtra("caption", "");
            intent.putExtra("mediaUri", mediaUri);
            intent.putExtra("thumbImage", thumbImage);
            intent.putExtra("feedType", mediaUri.mediaType);
            intent.putExtra("requestCode", Constant.POST_FEED_DATA);

            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setData(uri);
            setResult(RESULT_OK, intent);
            startActivityForResult(intent, Constant.POST_FEED_DATA);

        }


       /* Intent intent = new Intent();
        intent.setData(uri);
        setResult(RESULT_OK, intent);
        finish();*/

    }

    @Override
    public void cancelAction() {
        mVideoTrimmer.destroy();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvCroppingMessage.setVisibility(View.GONE);
            }
        });
        finish();
    }
}
