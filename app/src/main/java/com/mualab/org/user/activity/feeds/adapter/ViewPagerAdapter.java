package com.mualab.org.user.activity.feeds.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.feeds.listener.OnImageSwipeListener;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.activity.people_tag.instatag.InstaTag;
import com.mualab.org.user.activity.people_tag.instatag.TagDetail;
import com.mualab.org.user.activity.people_tag.instatag.TagToBeTagged;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.listener.OnDoubleTapListener;
import com.mualab.org.user.utils.ScreenUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPagerAdapter extends PagerAdapter implements OnImageSwipeListener {

    private LayoutInflater mLayoutInflater;
    private Context context;
    private List<String> ImagesList;
    private HashMap<Integer,ArrayList<TagToBeTagged>> taggedImgMap;
    private Listner listner;
    private Feeds feeds;
    private ViewGroup container;
    private boolean isShow,isFromFeed;

    public ViewPagerAdapter(Context context, Feeds feeds, boolean isFromFeed, Listner listner) {
        this.context = context;
        this.feeds = feeds;
        this.isFromFeed = isFromFeed;
        this.ImagesList = feeds.feed;
        this.taggedImgMap = feeds.taggedImgMap;
        this.listner = listner;
        this.isShow = false;
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //  tapListener = new MyOnDoubleTapListener(context);
        int widthPixels = ScreenUtils.getScreenWidth(context);
        widthPixels = widthPixels >=1080?1080: widthPixels;
        /*DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;*/
    }

    @Override
    public int getCount() {
        return ImagesList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_layout, container, false);
        // ImageView postImages = itemView.findViewById(R.id.post_image);
        final InstaTag postImages = itemView.findViewById(R.id.post_image);
        ImageView ivShowTag = itemView.findViewById(R.id.ivShowTag);

        this.container = container;

        //itemView.setOnTouchListener(tapListener);
        //  postImages.setTouchListnerDisable();

        this.isShow = false;

        postImages.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        postImages.setRootWidth(postImages.getMeasuredWidth());
        postImages.setRootHeight(postImages.getMeasuredHeight());


      /*  Picasso.with(context)
                .load(ImagesList.get(position)).resize(widthPixels,
                320).centerInside().placeholder(R.drawable.gallery_placeholder)
                .into(postImages.getTagImageView());*/

        Glide.with(context).load(ImagesList.get(position)).fitCenter().
                placeholder(R.drawable.gallery_placeholder).into(postImages.getTagImageView());

        if (taggedImgMap.containsKey(position)){
            ArrayList<TagToBeTagged>tags = taggedImgMap.get(position);
            postImages.addTagViewFromTagsToBeTagged(tags,false);
            postImages.hideTags();
        }

        /*if (postImages.getListOfTagsToBeTagged().size()!=0){
            ivShowTag.setVisibility(View.VISIBLE);
        }else
            ivShowTag.setVisibility(View.GONE);

        ivShowTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShow) {
                    postImages.hideTags();
                    isShow = false;
                }
                else {
                    postImages.showTags();
                    isShow = true;
                }
            }
        });*/

        postImages.setImageToBeTaggedEvent(taggedImageEvent);

        OnChangeImage   onChangeImage = new OnChangeImage() {
            @Override
            public void OnSwipe(int position) {
                isShow = false;
            }
        };

        postImages.setListener(new InstaTag.Listener() {

            @Override
            public void onTagCliked(final com.mualab.org.user.activity.people_tag.models.TagDetail tagDetail) {
                if (isFromFeed){
                    if(tagDetail!=null){
                        apiForgetUserIdFromUserName(tagDetail.title);
                        /*if (tagDetail.userType!=null && !tagDetail.userType.equals("")){
                            if (tagDetail.tagId!=null && !tagDetail.tagId.equals("")){
                                if (tagDetail.userType.equals("user")){
                                    Intent intent = new Intent(context, UserProfileActivity.class);
                                    intent.putExtra("userId",tagDetail.tagId);
                                    context.startActivity(intent);
                                }else {
                                    ArtistsSearchBoard item = new ArtistsSearchBoard();
                                    item._id = tagDetail.tagId;
                                    Intent intent2 = new Intent(context, ArtistProfileActivity.class);
                                    intent2.putExtra("item",item);
                                    context.startActivity(intent2);
                                }
                            }
                        }*/
                    }
                }
            }

            @Override
            public void onTagRemoved(com.mualab.org.user.activity.people_tag.models.TagDetail tagDetail) {
               /* if (taggedImgMap.size()!=0){
                    // taggedImgMap.remove(position);
                    for(Map.Entry map  :  taggedImgMap.entrySet() ) {
                        int key = (int) map.getKey();
                        if (key == position){
                            ArrayList<TagToBeTagged>tags = (ArrayList<TagToBeTagged>) map.getValue();

                            if(tagDetail!=null){
                                for (TagToBeTagged tagToBeTagged : tags){
                                    if (tagToBeTagged.getUnique_tag_id().
                                            equals(tagDetail.title)){
                                        tags.remove(tagToBeTagged);
                                    }
                                }
                            }
                        }
                    }
                }*/
            }
        });

        //  }
        itemView.setTag("myview" + position);
        container.addView(itemView);
        return itemView;
    }

    private void apiForgetUserIdFromUserName(final CharSequence userName) {
        String user_name="";

        final Map<String, String> params = new HashMap<>();
        if(userName.toString().startsWith("@")){
            user_name = userName.toString().replace("@","");
            params.put("userName", user_name+"");
        }else params.put("userName", userName+"");
        new HttpTask(new HttpTask.Builder(context, "profileByUserName", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d("hfjas", response);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {

                        JSONObject userDetail = js.getJSONObject("userDetail");
                        String userType = userDetail.getString("userType");
                        int userId = userDetail.getInt("_id");

                        if (userType.equals("user")) {
                            Intent intent = new Intent(context, UserProfileActivity.class);
                            intent.putExtra("userId", String.valueOf(userId));
                            context.startActivity(intent);
                        }else if (userType.equals("artist") && userId== Mualab.currentUser.id){
                            Intent intent = new Intent(context, UserProfileActivity.class);
                            intent.putExtra("userId", String.valueOf(userId));
                            context.startActivity(intent);
                        }
                        else {
                            Intent intent = new Intent(context, ArtistProfileActivity.class);
                            intent.putExtra("artistId", String.valueOf(userId));
                            context.startActivity(intent);
                        }



                    } else {
                        MyToast.getInstance(context).showDasuAlert(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
            }
        }).setBody(params,HttpTask.ContentType.APPLICATION_JSON)
                .setMethod(Request.Method.POST)
                .setProgress(true))
                .execute("FeedAdapter");


    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }

    @Override
    public void OnImageSwipe(int position) {
        isShow = false;
    }

    public interface Listner {
        void onSingleTap();
        void onDoubleTap();
    }

    public interface LongPressListner {
        void onLongPress();
    }

    public interface OnChangeImage {
        void OnSwipe(int position);
    }

    private InstaTag.TaggedImageEvent taggedImageEvent = new InstaTag.TaggedImageEvent() {

        @Override
        public void singleTapConfirmedAndRootIsInTouch(int x, int y) {
          /*  if (listner != null)
                listner.onSingleTap();*/
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (listner != null)
                listner.onDoubleTap();
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }

        InstaTag mInstaTag = null ;
        @Override
        public void onLongPress(MotionEvent e) {
            //  View view =  container.getRootView();
            ViewPager viewPager = (ViewPager) container;
            View view = viewPager.findViewWithTag("myview" + viewPager.getCurrentItem());

            if(view!=null){
                mInstaTag = view.findViewById(R.id.post_image);
            }
            if(mInstaTag!=null){
                if (!mInstaTag.isTagsShow()) {
                    mInstaTag.showTags();
                    isShow = true;
                }
                else {
                    mInstaTag.hideTags();
                    isShow = false;
                }

            }
        }

        @Override
        public void onSinglePress(MotionEvent e) {
            if (listner != null)
                listner.onSingleTap();
        }
    };

    private class MyOnDoubleTapListener extends OnDoubleTapListener {
        private MyOnDoubleTapListener(Context c) {
            super(c);
        }

        @Override
        public void onClickEvent(MotionEvent e) {
            if (listner != null)
                listner.onSingleTap();
        }

        @Override
        public void onDoubleTap(MotionEvent e) {
            if (listner != null)
                listner.onDoubleTap();
        }
    }

    public Point getDisplaySize(DisplayMetrics displayMetrics) {
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        return new Point(width, height);
    }
}
