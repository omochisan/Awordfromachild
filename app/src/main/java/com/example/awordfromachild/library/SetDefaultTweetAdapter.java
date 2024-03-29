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
import com.example.awordfromachild.common.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.asynctask.callBacksDefaultTweet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import twitter4j.Status;

public class SetDefaultTweetAdapter extends ArrayAdapter<twitter4j.Status> implements callBacksDefaultTweet {
    private final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    private final static String ptn_favo = "favo";
    private final static String ptn_retweet = "ret";
    private final static String mapKey_favorite = "favo";
    private final static String mapKey_retweet = "ret";
    //ツイート群の現在の状態を保持
    public final List<Map<String, Object>> arr_mItems_status = new ArrayList<>();
    //表示ツイート打ち止め
    public boolean frg_end = false;
    private List<twitter4j.Status> mItems;
    private final Context app_context;
    private final int mResource;
    private final LayoutInflater mInflater;

    /**
     * コンストラクタ
     *
     * @param context  コンテキスト
     * @param resource リソースID
     * @param items    リストビューの要素
     */
    public SetDefaultTweetAdapter(Context context, int resource, List<Status> items) {
        super(context, resource, items);
        mResource = resource;
        if (items != null) {
            mItems = items;
            arr_mItems_status.addAll(setNewestStatus(items));
        }
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Twitter共通処理クラス生成
        //Twitter処理クラス
        TwitterUtils twitterUtils = new TwitterUtils((callBacksBase) context);
        twitterUtils.setTwitterInstance(getContext());

        app_context = ApplicationController.getInstance().getApplicationContext();
    }

    /**
     * アイコンを設定
     *
     * @param ptn      アイコンの種類
     * @param validity アイコンON・OFF
     * @param view     対象アイコンがセットされたtextView
     */
    private void setIcon(String ptn, boolean validity, TextView view) {
        switch (ptn) {
            case ptn_favo:
                // アイコンの設定
                if (validity) {
                    Drawable leftDrawable = AppCompatResources.getDrawable(app_context, R.drawable.ic_favo_already);
                    Objects.requireNonNull(leftDrawable).setBounds(0, 0, leftDrawable.getIntrinsicWidth(),
                            leftDrawable.getIntrinsicHeight());
                    view.setCompoundDrawables(leftDrawable, null, null, null);
                } else {
                    Drawable leftDrawable = AppCompatResources.getDrawable(app_context, R.drawable.ic_favo);
                    Objects.requireNonNull(leftDrawable).setBounds(0, 0, leftDrawable.getIntrinsicWidth(),
                            leftDrawable.getIntrinsicHeight());
                    view.setCompoundDrawables(leftDrawable, null, null, null);
                }
                break;

            case ptn_retweet:
                if (validity) {
                    Drawable leftDrawable = AppCompatResources.getDrawable(app_context, R.drawable.ic_retweet_already);
                    Objects.requireNonNull(leftDrawable).setBounds(0, 0, leftDrawable.getIntrinsicWidth(),
                            leftDrawable.getIntrinsicHeight());
                    view.setCompoundDrawables(leftDrawable, null, null, null);
                } else {
                    Drawable leftDrawable = AppCompatResources.getDrawable(app_context, R.drawable.ic_retweet);
                    Objects.requireNonNull(leftDrawable).setBounds(0, 0, leftDrawable.getIntrinsicWidth(),
                            leftDrawable.getIntrinsicHeight());
                    view.setCompoundDrawables(leftDrawable, null, null, null);
                }
                break;
        }
    }

    /**
     * 戻り値の型に合わせてキャスト
     *
     * @param obj キャスト前
     * @param <T> ジェネリクス
     * @return キャスト後
     */
    @SuppressWarnings("unchecked")
    public static <T> T autoCast(Object obj) {
        return (T) obj;
    }

    /**
     * 現在のツイート群の状態を保持する
     *
     * @param target ツイート（単体もしくは配列）
     * @return ツイート群
     */
    public List<Map<String, Object>> setNewestStatus(Object target) {
        List<Map<String, Object>> list = new ArrayList<>();

        //複数ツイートを保持
        if (target instanceof ArrayList) {
            ArrayList<Status> items = autoCast(target);
            for (int i = 0; i < items.size(); i++) {
                twitter4j.Status item = items.get(i);
                Map<String, Object> _map = new HashMap<>();
                //お気に入りの状態
                _map.put(mapKey_favorite, item.isFavorited());
                //リツイートの状態
                _map.put(mapKey_retweet, item.isRetweetedByMe());
                list.add(_map);
            }
        } else if (target instanceof Status) {
            //単数ツイートを保持
            Status item = (Status) target;
            Map<String, Object> _map = new HashMap<>();
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
     * @param view         ビュー
     * @param item         ツイート
     * @param vid_userIcon ユーザーアイコンのviewID
     * @param vid_userName ユーザーネームボックスのviewID
     */
    public void setUserIcon(View view, Status item, int vid_userIcon, int vid_userName) {
        // ユーザーアイコンを設定
        ImageView userIcon = view.findViewById(vid_userIcon);
        String getUrl = item.getUser().getProfileImageURLHttps();
        GlideApp.with(view)
                .load(getUrl)
                .circleCrop()
                .into(userIcon);

        //ユーザー名を設定
        TextView userName = view.findViewById(vid_userName);
        userName.setText(item.getUser().getName());
    }

    /**
     * リツイート元のツイートを設定
     *
     * @param view            ビュー
     * @param item            リツイート
     * @param vid_tweetHeader リツイート情報表示するviewのID
     */
    public void setReTweet(View view, Status item, int vid_tweetHeader) {
        //リツイートの場合、元のツイート情報を取得
        if (item.isRetweet()) {
            TextView tweet_header = view.findViewById(vid_tweetHeader);
            tweet_header.setVisibility(View.VISIBLE);
            String text = item.getUser().getName() + "さんがリツイート";
            tweet_header.setText(text);
        }
    }

    /**
     * フッター設定（リツイート、いいね等）
     *
     * @param view     ビュー
     * @param position 対象ツイートのリストビュー内index
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setFooterIcon(View view, int position, int vid_like, int vid_favorite) {
        //お気に入り
        TextView like = view.findViewById(vid_like);
        // お気に入り状態の場合、アイコン変更
        setIcon(ptn_favo, (boolean) arr_mItems_status.get(position).get(mapKey_favorite), like);
        // お気に入りクリックイベント
        like.setOnClickListener(view1 -> {
            Status _item = mItems.get(position);
            boolean st_favo = (boolean) arr_mItems_status.get(position).get(mapKey_favorite);
            if (!st_favo) {
                setIcon(ptn_favo, true, like);
                TwitterUtils.createFavorite createFavorite =
                        new TwitterUtils.createFavorite(this);
                createFavorite.setTweetId(_item.getId());
                createFavorite.execute();
                arr_mItems_status.get(position).replace(mapKey_favorite, true);
            } else {
                setIcon(ptn_favo, false, like);
                TwitterUtils.destroyFavorite destroyFavorite =
                        new TwitterUtils.destroyFavorite(this);
                destroyFavorite.setTweetId(_item.getId());
                destroyFavorite.execute();
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
                TwitterUtils.createReTweet createReTweet =
                        new TwitterUtils.createReTweet(this);
                createReTweet.setTweetId(_item.getId());
                createReTweet.execute();
                arr_mItems_status.get(position).replace(mapKey_retweet, true);
            } else {
                setIcon(ptn_retweet, false, ret);
                TwitterUtils.destroyReTweet destroyReTweet =
                        new TwitterUtils.destroyReTweet(this);
                destroyReTweet.setTweetId(_item.getId());
                destroyReTweet.execute();
                arr_mItems_status.get(position).replace(mapKey_retweet, false);
            }
        });
    }

    /**
     * オーバーロード
     *
     * @param view              ビュー
     * @param item              ツイート
     * @param flg_detailDisplay ツイート詳細画面有無
     * @param vid_time          ビューID_ツイート投稿日付
     * @param vid_userID        ビューID_ユーザーID
     * @param vid_main          ビューID_ツイート文
     * @param vid_like          ビューID_いいね
     * @param vid_reTweet       ビューID_リツイート
     * @param vid_reply         ビューID_リプライ
     */
    public void setValue(View view, Status item, boolean flg_detailDisplay,
                         int vid_time, int vid_userID, int vid_main,
                         int vid_like, int vid_reTweet, int vid_reply) {
        setValue(view, item, flg_detailDisplay, true,
                vid_time, vid_userID, vid_main,
                vid_like, vid_reTweet, vid_reply);
    }

    /**
     * ツイートの各種値を設定
     *
     * @param view              ビュー
     * @param flg_detailDisplay ツイート詳細画面の描画かどうか
     * @param flg_footerSet     フッター情報をセットするかどうか
     * @param item              　ツイート
     * @param vid_time          　ビューID＿ツイート日時
     * @param vid_userID        　ビューID＿ユーザーID
     * @param vid_main          　ビューID＿メイン
     * @param vid_like          　ビューID＿いいね
     * @param vid_reTweet       　ビューID＿リツイート
     * @param vid_reply         　ビューID＿リプライ
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
        String disp_date;
        Date n_date = new Date();
        Date t_date = item.getCreatedAt();

        if (flg_detailDisplay) {
            SimpleDateFormat tokyoSdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.JAPANESE);
            tokyoSdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            disp_date = tokyoSdf.format(t_date);
        } else {
            // year
            SimpleDateFormat ysdf = new SimpleDateFormat("yyyy", Locale.JAPANESE);
            String n_datey = ysdf.format(n_date);
            String t_datey = ysdf.format(t_date);
            // month
            SimpleDateFormat msdf = new SimpleDateFormat("MM", Locale.JAPANESE);
            int n_datem = Integer.parseInt(msdf.format(n_date));
            int t_datem = Integer.parseInt(msdf.format(t_date));
            // day
            SimpleDateFormat dsdf = new SimpleDateFormat("dd", Locale.JAPANESE);
            String n_dated = dsdf.format(n_date);
            String t_dated = dsdf.format(t_date);

            if (n_datey.equals(t_datey)) { //年内のもの
                if (n_datem == t_datem &&
                        (Integer.parseInt(n_dated) - 2) <= Integer.parseInt(t_dated)) { ///直近3日間以内のもの
                    long diffTime = n_date.getTime() - t_date.getTime();
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH", Locale.JAPANESE);
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
                        disp_date = (Integer.parseInt(n_dated) - Integer.parseInt(t_dated)) + "日前";
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
        TextView userID = view.findViewById(vid_userID);
        userID.setText(String.format("@%s", item.getUser().getScreenName()));
        // ツイートを設定
        TextView tweet = view.findViewById(vid_main);
        tweet.setText(item.getText());
        //フッター情報を設定
        if (flg_footerSet) {
            //お気に入りを設定
            TextView favorite = view.findViewById(vid_like);
            favorite.setText(String.format(" %s", item.getFavoriteCount()));
            //リツイートを設定
            TextView reTweet = view.findViewById(vid_reTweet);
            reTweet.setText(String.format(" %s", item.getRetweetCount()));
            //リプライを取得・設定
            // ※リプライ取得が実現難しいため、現在非表示
            @SuppressWarnings("unused") TextView rep = view.findViewById(vid_reply);
            //rep.setText(item.getInReplyToScreenName());
        }
    }

    /**
     * リストにセットするツイート一覧を最後尾に追加設定。
     *
     * @param items 追加ツイート
     */
    public void addItems(List<Status> items) {
        if (items != null) {
            if (mItems == null) {
                mItems = items;
            } else {
                mItems.addAll(items);
            }
            arr_mItems_status.addAll(setNewestStatus(items));
        }
    }

    /**
     * リストにセットするツイート一覧を先頭に追加設定。
     *
     * @param items 追加ツイート
     */
    public void unShiftItems(List<Status> items) {
        mItems.addAll(0, items);
        arr_mItems_status.addAll(0, setNewestStatus(items));
    }

    /**
     * 画面をスクロールし、新しい一行が表示されるたびに呼ばれ、1行のUIを作成する。
     *
     * @param position    対象ツイートのリストビュー内のindex
     * @param convertView 再利用可能ビュー
     * @param parent      親ビュー
     * @return ビュー
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        //再利用できるviewがある場合はそれを使う
        //（表示領域から消えた1行は、スクロールして表示されようとする次の新しい行に対して
        // インスタンスを再利用しようとする
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(mResource, null);
        }

        //表示ツイート打ち止めの場合
        LinearLayout l = view.findViewById(R.id.tw_linear_);
        if (frg_end && ((mItems != null && mItems.size() == position + 1))) {
            l.setVisibility(View.VISIBLE);
        } else {
            l.setVisibility(View.GONE);
        }

        // リストビューに表示する要素を取得
        twitter4j.Status item = mItems.get(position);

        // ユーザーアイコンを設定
        setUserIcon(view, Objects.requireNonNull(item), R.id.tw_userIcon, R.id.tw_userName);
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
