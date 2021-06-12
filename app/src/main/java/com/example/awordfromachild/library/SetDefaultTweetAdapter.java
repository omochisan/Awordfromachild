package com.example.awordfromachild.library;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.awordfromachild.ApplicationController;
import com.example.awordfromachild.R;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.asynctask.callBacksDefaultTweet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import androidx.annotation.RequiresApi;
import twitter4j.DirectMessage;
import twitter4j.DirectMessageList;
import twitter4j.Status;

public class SetDefaultTweetAdapter extends ArrayAdapter<twitter4j.Status> implements callBacksDefaultTweet {
    private final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    private final static String ptn_favo = "favo";
    private final static String ptn_retweet = "ret";
    private final static String mapKey_favorite = "favo";
    private final static String mapKey_retweet = "ret";
    //ツイート群の現在の状態を保持
    public List<Map<String, Object>> arr_mItems_status = new ArrayList<Map<String, Object>>();
    //Twitter処理クラス
    private static TwitterUtils twitterUtils;
    private List<twitter4j.Status> mItems;
    private DirectMessageList mItems_dm;
    private Context app_context;
    //表示ツイート打ち止め
    public boolean frg_end = false;
    private int mResource;
    private LayoutInflater mInflater;

    /**
     * コンストラクタ
     *
     * @param context  コンテキスト
     * @param resource リソースID
     * @param items    リストビューの要素
     */
    public SetDefaultTweetAdapter(Context context, int resource, List<Status> items, DirectMessageList items_d) {
        super(context, resource, items);
        mResource = resource;
        if(items != null){
            mItems = items;
            arr_mItems_status.addAll(setNewestStatus(items));
        }else if(items_d != null){
            mItems_dm = items_d;
        }
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Twitter共通処理クラス生成
        twitterUtils = new TwitterUtils((callBacksBase) context);
        twitterUtils.setTwitterInstance(getContext());
        app_context = ApplicationController.getInstance().getApplicationContext();
    }

    /**
     * アイコンを設定
     *
     * @param ptn
     * @param validity
     * @param view
     */
    private void setIcon(String ptn, boolean validity, TextView view) {
        switch (ptn) {
            case ptn_favo:
                // アイコンの設定
                if (validity) {
                    Drawable leftDrawable = app_context.getDrawable(R.drawable.ic_favo_already);
                    leftDrawable.setBounds(0, 0, leftDrawable.getIntrinsicWidth(),
                            leftDrawable.getIntrinsicHeight());
                    view.setCompoundDrawables(leftDrawable, null, null, null);
                } else {
                    Drawable leftDrawable = app_context.getDrawable(R.drawable.ic_favo);
                    leftDrawable.setBounds(0, 0, leftDrawable.getIntrinsicWidth(),
                            leftDrawable.getIntrinsicHeight());
                    view.setCompoundDrawables(leftDrawable, null, null, null);
                }
                break;

            case ptn_retweet:
                if (validity) {
                    Drawable leftDrawable = app_context.getDrawable(R.drawable.ic_retweet_already);
                    leftDrawable.setBounds(0, 0, leftDrawable.getIntrinsicWidth(),
                            leftDrawable.getIntrinsicHeight());
                    view.setCompoundDrawables(leftDrawable, null, null, null);
                } else {
                    Drawable leftDrawable = app_context.getDrawable(R.drawable.ic_retweet);
                    leftDrawable.setBounds(0, 0, leftDrawable.getIntrinsicWidth(),
                            leftDrawable.getIntrinsicHeight());
                    view.setCompoundDrawables(leftDrawable, null, null, null);
                }
                break;
        }
    }

    /**
     * 現在のツイート群の状態を保持する
     *
     * @param target
     * @return
     */
    public List<Map<String, Object>> setNewestStatus(Object target) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        //複数ツイートを保持
        if (target instanceof ArrayList) {
            ArrayList<Status> items = (ArrayList<Status>) target;
            for (int i = 0; i < items.size(); i++) {
                twitter4j.Status item = items.get(i);
                Map<String, Object> _map = new HashMap<String, Object>();
                //お気に入りの状態
                _map.put(mapKey_favorite, item.isFavorited());
                //リツイートの状態
                _map.put(mapKey_retweet, item.isRetweetedByMe());
                list.add(_map);
            }
        } else if (target instanceof Status) {
            //単数ツイートを保持
            Status item = (Status) target;
            Map<String, Object> _map = new HashMap<String, Object>();
            //お気に入りの状態
            _map.put(mapKey_favorite, item.isFavorited());
            //リツイートの状態
            _map.put(mapKey_retweet, item.isRetweetedByMe());
            list.add(_map);
        }

        return list;
    }

    /**
     * ユーザーアイコンをセット
     *
     * @param view
     * @param item         ツイート
     * @param vid_userIcon ユーザーアイコンのviewID
     * @param vid_userName ユーザーネームボックスのviewID
     */
    public void setUserIcon(View view, Status item, int vid_userIcon, int vid_userName) {
        // ユーザーアイコンを設定
        ImageView userIcon = (ImageView) view.findViewById(vid_userIcon);
        String getUrl = item.getUser().getProfileImageURLHttps();
        GlideApp.with(view)
                .load(getUrl)
                .circleCrop()
                .into(userIcon);

        //ユーザー名を設定
        TextView userName = (TextView) view.findViewById(vid_userName);
        userName.setText(item.getUser().getName());
    }

    /**
     * リツイート元のツイートを取得
     * @param view
     * @param item
     * @param vid_tweetHeader
     * @return 元ツイート
     */
    public Status setReTweet(View view, Status item, int vid_tweetHeader) {
        //リツイートの場合、元のツイート情報を取得
        Status _item = item;
        if (item.isRetweet()) {
            _item = item.getRetweetedStatus();
            TextView tweet_header = view.findViewById(vid_tweetHeader);
            tweet_header.setVisibility(View.VISIBLE);
            tweet_header.setText(item.getUser().getName() + "さんがリツイート");
        }
        return _item;
    }

    /**
     * フッター設定（リツイート、いいね等）
     *
     * @param view
     * @param position
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setFooterIcon(View view, int position, int vid_like, int vid_favorite) {
        //お気に入り
        TextView like = view.findViewById(vid_like);
        // お気に入り状態の場合、アイコン変更
        if ((boolean) arr_mItems_status.get(position).get(mapKey_favorite)) {
            setIcon(ptn_favo, true, like);
        } else {
            setIcon(ptn_favo, false, like);
        }
        // お気に入りクリックイベント
        like.setOnClickListener(view1 -> {
            Status _item = mItems.get(position);
            boolean st_favo = (boolean) arr_mItems_status.get(position).get(mapKey_favorite);
            if (!st_favo) {
                setIcon(ptn_favo, true, like);
                twitterUtils.createFavorite(_item.getId());
                arr_mItems_status.get(position).replace(mapKey_favorite, true);
            } else {
                setIcon(ptn_favo, false, like);
                twitterUtils.destroyFavorite(_item.getId());
                arr_mItems_status.get(position).replace(mapKey_favorite, false);
            }
        });

        //リツイート
        TextView ret = view.findViewById(vid_favorite);
        // リツイートクリックイベント
        ret.setOnClickListener(view1 -> {
            Status _item = mItems.get(position);
            boolean st_ret = (boolean) arr_mItems_status.get(position).get(mapKey_retweet);
            if (!st_ret) {
                setIcon(ptn_retweet, true, ret);
                twitterUtils.createReTweet(_item.getId());
                arr_mItems_status.get(position).replace(mapKey_retweet, true);
            } else {
                setIcon(ptn_retweet, false, ret);
                twitterUtils.destroyReTweet(_item.getId());
                arr_mItems_status.get(position).replace(mapKey_retweet, false);
            }
        });
    }

    /**
     * オーバーロード
     * @param view
     * @param item
     * @param flg_detailDisplay
     * @param vid_time
     * @param vid_userID
     * @param vid_main
     * @param vid_like
     * @param vid_reTweet
     * @param vid_reply
     */
    public void setValue(View view, Status item, boolean flg_detailDisplay,
    int vid_time, int vid_userID, int vid_main,
    int vid_like, int vid_reTweet, int vid_reply){
        setValue(view, item, flg_detailDisplay, true,
        vid_time, vid_userID, vid_main,
        vid_like, vid_reTweet, vid_reply);
    }

    /**
     * ツイートの各種値を設定
     *
     * @param view ビュー
     * @param flg_detailDisplay ツイート詳細画面の描画かどうか
     * @param flg_footerSet フッター情報をセットするかどうか
     * @param item　ツイート
     * @param vid_time　ビューID＿ツイート日時
     * @param vid_userID　ビューID＿ユーザーID
     * @param vid_main　ビューID＿メイン
     * @param vid_like　ビューID＿いいね
     * @param vid_reTweet　ビューID＿リツイート
     * @param vid_reply　ビューID＿リプライ
     */
    public void setValue(View view, Status item, boolean flg_detailDisplay, boolean flg_footerSet,
                         int vid_time, int vid_userID, int vid_main,
                                int vid_like, int vid_reTweet, int vid_reply) {
        //ツイート日時表示
        // ツイート詳細表示の場合＝JST日時表示
        // ツイート一覧表示の場合
        /*
         * 去年以前のもの＝〇年〇月〇日
         * 年内のもの＝〇月〇日
         * 直近3日間以内のもの＝〇日前
         * 24時間以内のもの＝〇時間前
         * 1時間以内のもの＝〇分前
         * 1分以内のもの＝〇秒前
         */
        String disp_date = "";
        Date n_date = new Date();
        Date t_date = item.getCreatedAt();

        if(flg_detailDisplay){
            SimpleDateFormat tokyoSdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
            tokyoSdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            disp_date = tokyoSdf.format(t_date);
        }else {
            // year
            SimpleDateFormat ysdf = new SimpleDateFormat("yyyy");
            String n_datey = ysdf.format(n_date);
            String t_datey = ysdf.format(t_date);
            // month
            SimpleDateFormat msdf = new SimpleDateFormat("MM");
            int n_datem = Integer.parseInt(msdf.format(n_date));
            int t_datem = Integer.parseInt(msdf.format(t_date));
            // day
            SimpleDateFormat dsdf = new SimpleDateFormat("dd");
            String n_dated = dsdf.format(n_date);
            String t_dated = dsdf.format(t_date);

            if (n_datey.equals(t_datey)) { //年内のもの
                if (n_datem == t_datem &&
                        (Integer.parseInt(n_dated) - 2) <= Integer.parseInt(t_dated)) { ///直近3日間以内のもの
                    long diffTime = n_date.getTime() - t_date.getTime();
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH");
                    int diffTimeStr = Integer.parseInt(timeFormatter.format(new Date(diffTime)));

                    boolean moreThanDay = Math.abs(n_date.getTime() - t_date.getTime()) < MILLIS_PER_DAY;
                    if (moreThanDay) { //24時間以内のもの
                        long diff_sec = TimeUnit.MILLISECONDS.toSeconds(n_date.getTime() - t_date.getTime());
                        long diff_min = TimeUnit.MILLISECONDS.toSeconds(n_date.getTime() - t_date.getTime()) / 60;
                        if (diff_min < 60) {
                            if (diff_sec < 60) {
                                disp_date = diff_sec + "秒前";
                            } else {
                                disp_date = diff_min + "分前";
                            }
                        } else {
                            disp_date = diffTimeStr + "時間前";
                        }
                    } else { //直近3日間以内 and 24時間超えて前のもの
                        disp_date = String.valueOf(Integer.parseInt(n_dated) - Integer.parseInt(t_dated)) + "日前";
                    }
                } else { //年内 and 4日以上前のもの
                    disp_date = t_datem + "月" + t_dated + "日";
                }
            } else { // 去年以前のもの
                disp_date = t_datey + "年" + t_datem + "月" + t_dated + "日";
            }
        }

        TextView createDate = view.findViewById(vid_time);
        createDate.setText(disp_date);
        //ユーザーIDを設定
        TextView userID = (TextView) view.findViewById(vid_userID);
        userID.setText("@" + item.getUser().getScreenName());
        // ツイートを設定
        TextView tweet = (TextView) view.findViewById(vid_main);
        tweet.setText(item.getText());
        //フッター情報を設定
        if(flg_footerSet) {
            //お気に入りを設定
            TextView favo = (TextView) view.findViewById(vid_like);
            favo.setText(" " + String.valueOf(item.getFavoriteCount()));
            //リツイートを設定
            TextView retweet = (TextView) view.findViewById(vid_reTweet);
            retweet.setText(" " + String.valueOf(item.getRetweetCount()));
            //リプライを取得・設定
            // ※リプライ取得が実現難しいため、現在非表示
            //TextView rep = (TextView) view.findViewById(vid_reply);
            //rep.setText(item.getInReplyToScreenName());
        }
    }

    /**
     * リストにセットするツイート一覧を最後尾に追加設定。
     *
     * @param items
     */
    public void addItems(List<Status> items, DirectMessageList items_dm) {
        if(items != null){
            mItems.addAll(items);
            arr_mItems_status.addAll(setNewestStatus(items));
        }else{
            mItems_dm.addAll(items_dm);
        }
    }

    /**
     * リストにセットするツイート一覧を先頭に追加設定。
     *
     * @param items
     */
    public void unShiftItems(List<Status> items, List<DirectMessage> items_dm) {
        if(items != null){
            mItems.addAll(0, items);
            arr_mItems_status.addAll(0, setNewestStatus(items));
        }else{
            mItems_dm.addAll(0, items_dm);
        }
    }

    /**
     * 画面をスクロールし、新しい一行が表示されるたびに呼ばれ、1行のUIを作成する。
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        //再利用できるviewがある場合はそれを使う
        //（表示領域から消えた1行は、スクロールして表示されようとする次の新しい行に対して
        // インスタンスを再利用しようとする）
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(mResource, null);
        }

        //表示ツイート打ち止めの場合
        LinearLayout l = view.findViewById(R.id.tw_linear_);
        if (frg_end &&
                ((mItems != null && mItems.size() == position + 1) ||
                        (mItems_dm != null && mItems_dm.size() == position + 1))) {
            l.setVisibility(View.VISIBLE);
        } else {
            l.setVisibility(View.GONE);
        }

        // リストビューに表示する要素を取得
        twitter4j.Status item = null;
        if(mItems != null){
            item = mItems.get(position);
        }else if(mItems_dm != null){
            item = (Status) mItems_dm.get(position);
        }

        // ユーザーアイコンを設定
        setUserIcon(view, item, R.id.tw_userIcon, R.id.tw_userName);
        // リツイートの場合、元ツイートを設定
        setReTweet(view, item, R.id.tw_tweetheader);
        // フッター設定
        setFooterIcon(view, position, R.id.tw_like, R.id.tw_retweet);
        // ツイートの各種値を設定
        setValue(view, item, false,
                R.id.tw_time,
                R.id.tw_userID,
                R.id.tw_main,
                R.id.tw_like,
                R.id.tw_retweet,
                R.id.tw_reply);

        return view;
    }

    @Override
    public void callBackTwitterLimit(int secondsUntilReset) {

    }

    @Override
    public void callBackStreamAddList(Status status) {

    }

    @Override
    public void callBackException() {

    }

    @Override
    public void callBackGetTweets(Object list, String howToDisplay) {

    }
}
