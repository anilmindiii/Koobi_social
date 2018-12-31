package com.mualab.org.user.activity.feeds.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.hendraanggrian.socialview.SocialView;
import com.hendraanggrian.widget.SocialTextView;
import com.mualab.org.user.R;

import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.authentication.Registration2Activity;
import com.mualab.org.user.activity.explore.SearchFeedActivity;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.activity.feeds.activity.ReportActivity;
import com.mualab.org.user.activity.feeds.listener.OnImageSwipeListener;
import com.mualab.org.user.activity.myprofile.activity.activity.UserProfileActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.feeds.Feeds;
import com.mualab.org.user.data.remote.API;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.listener.OnDoubleTapListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;


/**
 * Created by Dharmraj Acharya on 10/8/17.
 **/

@SuppressWarnings("WeakerAccess")
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected boolean showLoader;
    private final int TEXT_TYPE = 0;
    private final int IMAGE_TYPE = 1;
    private final int VIDEO_TYPE = 2;
    private final int VIEW_TYPE_LOADING = 3;

    private Context mContext;
    private List<Feeds> feedItems;
    private Listener listener;
    private boolean loading;
    String userType;
    Activity activity;


    public void showHideLoading(boolean b) {
        loading = b;
    }

    public interface Listener {
        void onCommentBtnClick(Feeds feed, int pos);

        void onLikeListClick(Feeds feed);

        void onFeedClick(Feeds feed, int index, View v);

        void onClickProfileImage(Feeds feed, ImageView v);
    }

    public void clear() {
        final int size = feedItems.size();
        feedItems.clear();
        notifyItemRangeRemoved(0, size);
    }

    public FeedAdapter(Context mContext,String userType, List<Feeds> feedItems, Listener listener) {
        this.mContext = mContext;
        this.feedItems = feedItems;
        this.listener = listener;
        this.userType = userType;
        activity = (Activity) mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TEXT_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_text_item_layout, parent, false);
                FeedTextHolder textHolder = new FeedTextHolder(view);
                setupTextFeedClickableViews(textHolder);
                return textHolder;
            case IMAGE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_image_item_layout, parent, false);
                CellFeedViewHolder cellFeedViewHolder = new CellFeedViewHolder(view);
                setupClickableViews(cellFeedViewHolder);
                return cellFeedViewHolder;
            case VIDEO_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_video_item_layout, parent, false);
                FeedVideoHolder feedViepoHolder = new FeedVideoHolder(view);
                setupFeedVideoClickableViews(feedViepoHolder);
                return feedViepoHolder;

            case VIEW_TYPE_LOADING:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }

        return null;
    }

    @Override
    public int getItemViewType(int position) {
        Feeds feed = feedItems.get(position);
       /* if(position==feedItems.size()-1) {
            if (loading) {
                return VIEW_TYPE_LOADING;
            }
        }
*/
        switch (feed.feedType) {
            case "text":
                return TEXT_TYPE;
            case "image":
                return IMAGE_TYPE;
            case "video":
                return VIDEO_TYPE;
            default:
                return VIEW_TYPE_LOADING;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loaderViewHolder = (LoadingViewHolder) holder;
            if (showLoader) {
                loaderViewHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                loaderViewHolder.progressBar.setVisibility(View.GONE);
            }
            return;
        }
        final Feeds feeds = feedItems.get(position);

        if (holder instanceof Holder) {
            Holder h = (Holder) holder;
            if (!TextUtils.isEmpty(feeds.profileImage)) {
                Picasso.with(mContext).load(feeds.profileImage).placeholder(R.drawable.default_placeholder).fit().into(h.ivProfile);
            } else Picasso.with(mContext).load(R.drawable.default_placeholder).into(h.ivProfile);

            if (feeds.userId == Mualab.currentUser.id) {
                h.iv_menu.setVisibility(View.GONE);

            } else h.iv_menu.setVisibility(View.VISIBLE);

            h.tvUserName.setText(feeds.userName);
            h.tvPostTime.setText(feeds.crd);
            h.tvUserLocation.setText(TextUtils.isEmpty(feeds.location) ? "N/A" : feeds.location);
            h.tv_like_count.setText(String.valueOf(feeds.likeCount));
            h.tv_comments_count.setText(String.valueOf(feeds.commentCount));
            h.likeIcon.setChecked(feeds.isLike == 1);
            h.tv_text.setText(feeds.caption);

            if (!TextUtils.isEmpty(feeds.caption)) {
                h.tv_text.setVisibility(View.VISIBLE);
                h.tv_text.setText(feeds.caption);
            } else h.tv_text.setVisibility(View.GONE);


            if(activity != null){
                String activityName = activity.getClass().toString();
                if(activityName.equals("class com.mualab.org.user.activity.feeds.activity.FeedSingleActivity")){
                    if (feeds.userId == Mualab.currentUser.id) {
                        h.btnFollow.setVisibility(View.GONE);
                    } else {
                        h.btnFollow.setVisibility(View.VISIBLE);
                        if (feeds.followingStatus == 1) {
                            h.btnFollow.setBackgroundResource(R.drawable.btn_bg_blue_broder);
                            h.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                            h.btnFollow.setText(R.string.following);
                        } else {
                            h.btnFollow.setBackgroundResource(R.drawable.button_effect_invert);
                            h.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                            h.btnFollow.setText(R.string.follow);
                        }
                    }
                }else h.btnFollow.setVisibility(View.GONE);
            }else h.btnFollow.setVisibility(View.GONE);


        }


        switch (feeds.feedType) {

            case "image": {

                final CellFeedViewHolder imageHolder = ((CellFeedViewHolder) holder);

                imageHolder.weakRefAdapter = new WeakReference<>(new ViewPagerAdapter(mContext, feeds, true,
                        new ViewPagerAdapter.Listner() {
                            @Override
                            public void onSingleTap() {
                                int pos = imageHolder.weakRefViewPager.get().getCurrentItem();
                                if (feeds.feedType.equalsIgnoreCase("image")) {
                                    listener.onFeedClick(feeds, pos, imageHolder.rl_imageView);


                                }
                            }

                            @Override
                            public void onDoubleTap() {
                                int pos = imageHolder.getAdapterPosition();
                                Feeds feed = feedItems.get(pos);
                                if (feed.isLike == 0) {
                                    feed.isLike = 1;
                                    feed.likeCount = ++feed.likeCount;
                                    apiForLikes(feeds);
                                }
                                notifyItemChanged(pos);
                            }

                        }));

                imageHolder.weakRefViewPager.get().setAdapter(imageHolder.weakRefAdapter.get());

                if (feeds.feed.size() > 1) {
                    addBottomDots(imageHolder.ll_Dot, feeds.feed.size(), 0);
                    imageHolder.ll_Dot.setVisibility(View.VISIBLE);
                    imageHolder.weakRefViewPager.get().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {

                        }

                        @Override
                        public void onPageSelected(int pos) {
                            //   onImageSwipeListener
                            Feeds feed = feedItems.get(imageHolder.getAdapterPosition());
                            feed.viewPagerlastPos = pos;
                            addBottomDots(imageHolder.ll_Dot, feed.feed.size(), pos);
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {
                        }
                    });
                } else imageHolder.ll_Dot.setVisibility(View.GONE);

                imageHolder.weakRefViewPager.get().setCurrentItem(feeds.viewPagerlastPos);
            }
            break;


            case "video":

                final FeedVideoHolder videoHolder = ((FeedVideoHolder) holder);

                if (!TextUtils.isEmpty(feeds.videoThumbnail)) {
                    Picasso.with(videoHolder.ivFeedCenter.getContext())
                            .load(feeds.videoThumbnail)
                            .placeholder(R.drawable.gallery_placeholder)
                            .into(videoHolder.ivFeedCenter);
                } else Picasso.with(videoHolder.ivFeedCenter.getContext())
                        .load(R.drawable.gallery_placeholder)
                        .into(videoHolder.ivFeedCenter);

                break;
        }
        // }
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    private void setupTextFeedClickableViews(final FeedTextHolder holder) {
        holder.tv_text.setHashtagColor(ContextCompat.getColor(mContext, R.color.text_color));
        holder.tv_text.setMentionColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        holder.tv_text.setOnHyperlinkClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {

                String url = charSequence.toString();
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mContext.startActivity(browserIntent);
                return null;
            }
        });

        holder.tv_text.setOnHashtagClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                goHashTag(charSequence);
                return null;
            }
        });

        holder.tv_text.setOnMentionClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                apiForgetUserIdFromUserName(charSequence);
                return null;
            }
        });

        holder.ly_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                Feeds feed = feedItems.get(pos);
                if (listener != null) {
                    listener.onCommentBtnClick(feed, pos);
                }
            }
        });

        holder.ly_like_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                if (listener != null) {
                    listener.onLikeListClick(feed);
                }
            }
        });

        holder.likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                feed.isLike = feed.isLike == 1 ? 0 : 1;
                feed.likeCount = feed.isLike == 1 ? ++feed.likeCount : --feed.likeCount;
                notifyItemChanged(adapterPosition);
                apiForLikes(feed);
            }
        });

        holder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                sharOnsocial(holder.tv_text.getText().toString(), feed._id);
                // int adapterPosition = holder.getAdapterPosition();
                // Feeds feed = feedItems.get(adapterPosition);
                // shareDialog(feed, 0);
            }
        });

        holder.ly_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.ly_report.setVisibility(View.GONE);
                Intent intent = new Intent(mContext, ReportActivity.class);
                intent.putExtra("feedOwenerId", feedItems.get(holder.getAdapterPosition()).userId);
                intent.putExtra("feedId", feedItems.get(holder.getAdapterPosition())._id);
                mContext.startActivity(intent);
            }
        });

        holder.iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.ly_report.getVisibility() == View.VISIBLE) {
                    holder.ly_report.setVisibility(View.GONE);
                } else {
                    holder.ly_report.setVisibility(View.VISIBLE);
                }
            }
        });

        holder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // holder.btnFollow.setEnabled(false);
                int adapterPosition = holder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                followUnfollow(feed, adapterPosition);
            }
        });

        holder.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Feeds feed = feedItems.get(holder.getAdapterPosition());

                userType = feed.userInfo.get(0).userType;

                if (userType.equals("user")) {
                    Intent intent = new Intent(mContext, UserProfileActivity.class);
                    intent.putExtra("userId", String.valueOf(feed.userId));
                    mContext.startActivity(intent);
                }else if (userType.equals("artist") && feed.userId== Mualab.currentUser.id){
                    Intent intent = new Intent(mContext, UserProfileActivity.class);
                    intent.putExtra("userId", String.valueOf(feed.userId));
                    mContext.startActivity(intent);
                }
                else {
                    Intent intent = new Intent(mContext, ArtistProfileActivity.class);
                    intent.putExtra("artistId", String.valueOf(feed.userId));
                    mContext.startActivity(intent);
                }

            }
        });
    }

    private void setupFeedVideoClickableViews(final FeedVideoHolder videoHolder) {
        videoHolder.tv_text.setHashtagColor(ContextCompat.getColor(mContext, R.color.text_color));
        videoHolder.tv_text.setMentionColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        videoHolder.tv_text.setOnHyperlinkClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                String url = charSequence.toString();
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mContext.startActivity(browserIntent);
                return null;
            }
        });

        videoHolder.tv_text.setOnHashtagClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                goHashTag(charSequence);
                return null;
            }
        });


        videoHolder.tv_text.setOnMentionClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                apiForgetUserIdFromUserName(charSequence);
                return null;
            }
        });

        videoHolder.ly_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(pos);
                if (listener != null) {
                    listener.onCommentBtnClick(feed, pos);
                }
            }
        });

        videoHolder.ly_like_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                if (listener != null) {
                    listener.onLikeListClick(feed);
                }
            }
        });

        videoHolder.likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                feed.isLike = feed.isLike == 1 ? 0 : 1;
                feed.likeCount = feed.isLike == 1 ? ++feed.likeCount : --feed.likeCount;
                notifyItemChanged(adapterPosition);
                apiForLikes(feed);
            }
        });

        videoHolder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                sharOnsocial("", feed._id);
                /*int adapterPosition = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                int innerPosition = 0;
                  shareDialog(feed, innerPosition);*/
            }
        });

        videoHolder.ly_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoHolder.ly_report.setVisibility(View.GONE);
                Intent intent = new Intent(mContext, ReportActivity.class);
                intent.putExtra("feedOwenerId", feedItems.get(videoHolder.getAdapterPosition()).userId);
                intent.putExtra("feedId", feedItems.get(videoHolder.getAdapterPosition())._id);
                mContext.startActivity(intent);
            }
        });

        videoHolder.iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoHolder.ly_report.getVisibility() == View.VISIBLE) {
                    videoHolder.ly_report.setVisibility(View.GONE);
                } else {
                    videoHolder.ly_report.setVisibility(View.VISIBLE);
                }
            }
        });

        videoHolder.ivFeedCenter.setOnTouchListener(new MyOnDoubleTapListener(mContext, videoHolder));

        videoHolder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                followUnfollow(feed, adapterPosition);
            }
        });

        videoHolder.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Feeds feed = feedItems.get(videoHolder.getAdapterPosition());

                if (userType.equals("user")) {
                    Intent intent = new Intent(mContext, UserProfileActivity.class);
                    intent.putExtra("userId", String.valueOf(feed.userId));
                    mContext.startActivity(intent);
                }else if (userType.equals("artist") && feed.userId== Mualab.currentUser.id){
                    Intent intent = new Intent(mContext, UserProfileActivity.class);
                    intent.putExtra("userId", String.valueOf(feed.userId));
                    mContext.startActivity(intent);
                }
                else {
                    Intent intent = new Intent(mContext, ArtistProfileActivity.class);
                    intent.putExtra("artistId", String.valueOf(feed.userId));
                    mContext.startActivity(intent);
                }



            }
        });

    }

    private void setupClickableViews(final CellFeedViewHolder cellFeedViewHolder) {
        cellFeedViewHolder.tv_text.setHashtagColor(ContextCompat.getColor(mContext, R.color.text_color));
        cellFeedViewHolder.tv_text.setMentionColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        cellFeedViewHolder.tv_text.setOnHyperlinkClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                String url = charSequence.toString();
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mContext.startActivity(browserIntent);
                return null;
            }
        });

        cellFeedViewHolder.tv_text.setOnHashtagClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                goHashTag(charSequence);
                return null;
            }
        });


        cellFeedViewHolder.tv_text.setOnMentionClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                apiForgetUserIdFromUserName(charSequence);
                return null;
            }
        });

        cellFeedViewHolder.ly_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(pos);
                if (listener != null) {
                    listener.onCommentBtnClick(feed, pos);
                }
            }
        });


        cellFeedViewHolder.rl_imageView.setOnTouchListener(new MyOnDoubleTapListener(mContext, cellFeedViewHolder));

        cellFeedViewHolder.ly_like_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                if (listener != null) {
                    listener.onLikeListClick(feed);
                }
            }
        });

        cellFeedViewHolder.likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                feed.isLike = feed.isLike == 1 ? 0 : 1;
                feed.likeCount = feed.isLike == 1 ? ++feed.likeCount : --feed.likeCount;
                notifyItemChanged(adapterPosition);
                apiForLikes(feed);
            }
        });

        cellFeedViewHolder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                sharOnsocial("", feed._id);
               /* int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                int innerPosition = 0;
                if (cellFeedViewHolder.weakRefViewPager != null)
                    innerPosition = cellFeedViewHolder.weakRefViewPager.get().getCurrentItem();*/
                // shareDialog(feed, innerPosition);


            }
        });

        cellFeedViewHolder.ly_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cellFeedViewHolder.ly_report.setVisibility(View.GONE);
                Intent intent = new Intent(mContext, ReportActivity.class);
                intent.putExtra("feedOwenerId", feedItems.get(cellFeedViewHolder.getAdapterPosition()).userId);
                intent.putExtra("feedId", feedItems.get(cellFeedViewHolder.getAdapterPosition())._id);
                mContext.startActivity(intent);
            }
        });

        cellFeedViewHolder.iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cellFeedViewHolder.ly_report.getVisibility() == View.VISIBLE) {
                    cellFeedViewHolder.ly_report.setVisibility(View.GONE);
                } else {
                    cellFeedViewHolder.ly_report.setVisibility(View.VISIBLE);
                }
            }
        });


        cellFeedViewHolder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cellFeedViewHolder.btnFollow.setEnabled(false);
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                followUnfollow(feed, adapterPosition);
            }
        });

        cellFeedViewHolder.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Feeds feed = feedItems.get(cellFeedViewHolder.getAdapterPosition());

                apiForgetUserIdFromUserName(feed.userName);

                /*if (userType.equals("user")) {
                    Intent intent = new Intent(mContext, UserProfileActivity.class);
                    intent.putExtra("userId", String.valueOf(feed.userId));
                    mContext.startActivity(intent);
                }else if (userType.equals("artist") && feed.userId== Mualab.currentUser.id){
                    Intent intent = new Intent(mContext, UserProfileActivity.class);
                    intent.putExtra("userId", String.valueOf(feed.userId));
                    mContext.startActivity(intent);
                }
                else {
                    Intent intent = new Intent(mContext, ArtistProfileActivity.class);
                    intent.putExtra("artistId", String.valueOf(feed.userId));
                    mContext.startActivity(intent);
                }*/








            }
        });
    }

    // Anil's work
    private void goHashTag(CharSequence charSequence) {
        Intent intent = new Intent(mContext, SearchFeedActivity.class);
        String tag = charSequence.toString().replace("#","");
        ExSearchTag e = new ExSearchTag();
        e.title = tag;
        e.id = 0;
        e.type = ExSearchTag.SearchType.HASH_TAG;
        intent.putExtra("searchKey",e);
        mContext.startActivity(intent);
    }

    private void addBottomDots(LinearLayout ll_dots, int totalSize, int currentPage) {
        TextView[] dots = new TextView[totalSize];
        ll_dots.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(mContext);
            dots[i].setText(Html.fromHtml("•"));
            dots[i].setTextSize(25);
            dots[i].setTextColor(Color.parseColor("#999999"));
            ll_dots.addView(dots[i]);
        }
        if (dots.length > 0)
            dots[currentPage].setTextColor(Color.parseColor("#212121"));
    }

    private void apiForLikes(final Feeds feed) {
        Map<String, String> map = new HashMap<>();
        map.putAll(Mualab.feedBasicInfo);
        map.put("feedId", "" + feed._id);
        map.put("likeById", "" + Mualab.currentUser.id);
        map.put("userId", "" + feed.userId);
        map.put("type", "feed");// feed or comment
        Mualab.getInstance().getRequestQueue().cancelAll("like" + feed._id);
        new HttpTask(new HttpTask.Builder(mContext, "like", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {

            }

            @Override
            public void ErrorListener(VolleyError error) {

            }
        }).setAuthToken(Mualab.currentUser.authToken).setParam(map)).execute("like" + feed._id);

    }

    static class Holder extends RecyclerView.ViewHolder {
        protected CheckBox likeIcon;
        protected ImageView ivLike;
        protected ImageView ivProfile, ivShare, ivComments, iv_menu; //btnLike
        protected LinearLayout ly_like_count, ly_comments;
        protected TextView tvUserName, tvUserLocation, tvPostTime;
        protected TextView tv_like_count, tv_comments_count;
        protected SocialTextView tv_text;
        protected AppCompatButton btnFollow;
        RelativeLayout ly_report;

        public Holder(View itemView) {
            super(itemView);
            /*Common ui*/
            ivProfile = itemView.findViewById(R.id.iv_user_image);
            ivShare = itemView.findViewById(R.id.iv_share);
            ly_report = itemView.findViewById(R.id.ly_report);
            iv_menu = itemView.findViewById(R.id.iv_menu);
            ivComments = itemView.findViewById(R.id.iv_comments);
            tv_text = itemView.findViewById(R.id.tv_text);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserLocation = itemView.findViewById(R.id.tv_location);
            tvPostTime = itemView.findViewById(R.id.tv_post_time);
            tv_like_count = itemView.findViewById(R.id.tv_like_count);
            tv_comments_count = itemView.findViewById(R.id.tv_comments_count);
            ly_like_count = itemView.findViewById(R.id.ly_like_count);
            ly_comments = itemView.findViewById(R.id.ly_comments);
            ivLike = itemView.findViewById(R.id.ivLike);
            btnFollow = itemView.findViewById(R.id.btnFollow);
            likeIcon = itemView.findViewById(R.id.likeIcon);
        }
    }

    static class FeedTextHolder extends Holder {
        private FeedTextHolder(View itemView) {
            super(itemView);
        }
    }

    public static class FeedVideoHolder extends Holder {
        public ImageView ivFeedCenter;

        private FeedVideoHolder(View itemView) {
            super(itemView);

            ivFeedCenter = itemView.findViewById(R.id.ivFeedCenter);
        }
    }

    static class CellFeedViewHolder extends Holder {

        private LinearLayout ll_Dot;
        private RelativeLayout rl_imageView;
        private WeakReference<ViewPager> weakRefViewPager;
        private WeakReference<ViewPagerAdapter> weakRefAdapter;
        private OnImageSwipeListener onImageSwipeListener = null;

        public void setSwipeListner(OnImageSwipeListener onImageSwipeListener) {
            this.onImageSwipeListener = onImageSwipeListener;
        }

        private CellFeedViewHolder(View itemView) {
            super(itemView);

            ll_Dot = itemView.findViewById(R.id.ll_Dot);
            rl_imageView = itemView.findViewById(R.id.rl_imageView);
            weakRefViewPager = new WeakReference<>((ViewPager) itemView.findViewById(R.id.viewpager));

        }
    }

    private class MyOnDoubleTapListener extends OnDoubleTapListener {
        private CellFeedViewHolder holder;
        private FeedVideoHolder feedVieoHolder;

        private MyOnDoubleTapListener(Context c, CellFeedViewHolder holder) {
            super(c);
            this.holder = holder;
        }

        private MyOnDoubleTapListener(Context c, FeedVideoHolder feedVieoHolder) {
            super(c);
            this.feedVieoHolder = feedVieoHolder;
        }

        @Override
        public void onClickEvent(MotionEvent e) {
            int adapterPosition = getPosition();
            Feeds feed = feedItems.get(adapterPosition);
            if (feed.feedType.equalsIgnoreCase("image")) {
               /* mContext.startActivity(new Intent(mContext, PreviewImageActivity.class)
                        .setData(Uri.parse(feed.feed.get(0)))
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));*/
                /*Intent intent = new Intent(mContext, PreviewImageActivity.class);
                List<String> list = new ArrayList<>();
                for(Feeds.Feed tmp: feed.feedData){
                    list.add(tmp.feedPost);
                }
                intent.putExtra("imageArray", (Serializable) list);
                intent.putExtra("startIndex", 0);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);*/

                // listener.onFeedClick(feed, adapterPosition);

            } else if (feed.feedType.equalsIgnoreCase("video")) {
                if (feed.feedThumb != null && feed.feedThumb.size() > 0) {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW)
                            .setDataAndType(Uri.parse(feed.feed.get(0)), "video/mp4")
                            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
                }
            }
        }

        @Override
        public void onDoubleTap(MotionEvent e) {
            int adapterPosition = getPosition();
            Feeds feed = feedItems.get(adapterPosition);
            if (feed.isLike == 0) {
                feed.isLike = 1;
                feed.likeCount = ++feed.likeCount;
                apiForLikes(feed);
            }
            notifyItemChanged(adapterPosition);
        }

        private int getPosition() {
            if (holder != null) {
                return holder.getAdapterPosition();

            } else if (feedVieoHolder != null) {
                return feedVieoHolder.getAdapterPosition();
            }
            return 0;
        }
    }

    private void followUnfollow(final Feeds feeds, final int position) {

        if (feeds.followingStatus == 1) {
          /*  new UnfollowDialog(mContext, feeds, new UnfollowDialog.UnfollowListner() {
                @Override
                public void onUnfollowClick(Dialog dialog) {
                    dialog.dismiss();
                    apiForFollowUnFollow(feeds, position);
                }
            });*/

            apiForFollowUnFollow(feeds, position);

        } else apiForFollowUnFollow(feeds, position);

    }

    private void apiForgetUserIdFromUserName(final CharSequence userName) {
        String user_name="";

        final Map<String, String> params = new HashMap<>();
        if(userName.toString().contains("@")){
             user_name = userName.toString().replace("@","");
            params.put("userName", user_name+"");
        }else params.put("userName", userName+"");
        new HttpTask(new HttpTask.Builder(mContext, "profileByUserName", new HttpResponceListner.Listener() {
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
                            Intent intent = new Intent(mContext, UserProfileActivity.class);
                            intent.putExtra("userId", userId+"");
                            mContext.startActivity(intent);
                        }
                        else {
                            Intent intent = new Intent(mContext, ArtistProfileActivity.class);
                            intent.putExtra("artistId", userId+"");
                            mContext.startActivity(intent);
                        }

                    } else {
                        MyToast.getInstance(mContext).showDasuAlert(message);
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


    private void apiForFollowUnFollow(final Feeds feeds, final int position) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", "" + Mualab.currentUser.id);
        map.put("followerId", "" + feeds.userId);

        new HttpTask(new HttpTask.Builder(mContext, "followFollowing", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    //String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        if (feeds.followingStatus == 0) {
                            feeds.followingStatus = 1;
                        } else if (feeds.followingStatus == 1) {
                            feeds.followingStatus = 0;
                        }
                    }
                    notifyItemChanged(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                    notifyItemChanged(position);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                notifyItemChanged(position);
            }
        }).setParam(map)).execute("followFollowing");
    }


    private void sharOnsocial(String text, int feedId) {
        Intent sharIntent = new Intent(Intent.ACTION_SEND);
        //sharIntent.setType("image/png");
        sharIntent.setType("text/plain");
        sharIntent.putExtra(Intent.EXTRA_SUBJECT, "Koobi Social");
        sharIntent.putExtra(Intent.EXTRA_TEXT, "http://koobi.co.uk:8042/");
        mContext.startActivity(Intent.createChooser(sharIntent, "Share:"));

    }
}
