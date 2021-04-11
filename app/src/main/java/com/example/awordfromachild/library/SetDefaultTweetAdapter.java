package com.example.awordfromachild.library;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.awordfromachild.ApplicationController;
import com.example.awordfromachild.MainActivity;
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

import androidx.annotation.RequiresApi;
import twitter4j.Status;

public class SetDefaultTweetAdapter extends ArrayAdapter<twitter4j.Status> implements callBacksDefaultTweet {
    private final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    private final static String ptn_favo = "favo";
    private final static String ptn_retweet = "ret";
    //Twitter処理クラス
    private static TwitterUtils twitterUtils;
    private int mResource;
    private List<twitter4j.Status> mItems;
    private LayoutInflater mInflater;
    private Context app_context;
    //ツイート群の現在の状態を保持
    private List<Map<String, Object>> arr_mItems_status = new ArrayList<Map<String, Object>>();
    private final static String mapKey_favorite = "favo";
    private final static String mapKey_retweet = "ret";

    /**
     * コンストラクタ
     *
     * @param context  コンテキスト
     * @param resource リソースID
     * @param items    リストビューの要素
     */
    public SetDefaultTweetAdapter(Context context, int resource, ArrayList<Status> items) {
        super(context, resource, items);

        mResource = resource;
        mItems = items;
        arr_mItems_status.addAll(setNewestStatus(items));
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Twitter共通処理クラス生成
        twitterUtils = new TwitterUtils((callBacksBase) context);
        twitterUtils.setTwitterInstance(getContext());
        app_context = ApplicationController.getInstance().getApplicationContext();
    }

    /**
     * リストにセットするツイート一覧を最後尾に追加設定。
     *
     * @param items
     */
    public void addItems(ArrayList<twitter4j.Status> items) {
        mItems.addAll(items);
        arr_mItems_status.addAll(setNewestStatus(items));
    }
    /**
     * リストにセットするツイート一覧を先頭に追加設定。
     *
     * @param items
     */
    public void unShiftItems(ArrayList<twitter4j.Status> items) {
        mItems.addAll(0, items);
        arr_mItems_status.addAll(0, setNewestStatus(items));
    }

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
        }
    }

    /**
     * 現在のツイート群の状態を保持する
     * @param items
     * @return
     */
    private List<Map<String, Object>> setNewestStatus(ArrayList<twitter4j.Status> items){
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        for(int i=0; i < items.size(); i++){
            twitter4j.Status item = items.get(i);
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
        //（表示領域から消えた1行は、スクロールして表
        // 示されようとする次の新しい行に対して
        // インスタンスを再利用しようとする）
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(mResource, null);
        }

        // リストビューに表示する要素を取得
        twitter4j.Status item = mItems.get(position);

        //お気に入り
        TextView like = view.findViewById(R.id.tw_like);
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

        //リツイートの場合、元のツイート情報を取得
        Status origin_item;
        if (item.isRetweet()) {
            origin_item = item;
            item = item.getRetweetedStatus();
            TextView tweet_header = view.findViewById(R.id.tw_tweetheader);
            tweet_header.setVisibility(View.VISIBLE);
            tweet_header.setText(origin_item.getUser().getName() + "さんがリツイート");
        }

        // ユーザーアイコンを設定
        ImageView userIcon = (ImageView) view.findViewById(R.id.tw_userIcon);
        String getUrl = item.getUser().getProfileImageURLHttps();
        GlideApp.with(view)
                .load(getUrl)
                .circleCrop()
                .into(userIcon);

        //ユーザー名を設定
        TextView userName = (TextView) view.findViewById(R.id.tw_userName);
        userName.setText(item.getUser().getName());

        //ツイート日時を設定
        // 去年以前のもの＝〇年〇月〇日
        // 年内のもの＝〇月〇日
        // 直近3日間以内のもの＝〇日前
        // 24時間以内のもの＝〇時間前
        String disp_date = "";
        Date n_date = new Date();
        Date t_date = item.getCreatedAt();
        // year
        SimpleDateFormat ysdf = new SimpleDateFormat("yyyy");
        String n_datey = ysdf.format(n_date);
        String t_datey = ysdf.format(t_date);
        // month
        SimpleDateFormat msdf = new SimpleDateFormat("MM");
        String n_datem = msdf.format(n_date);
        String t_datem = msdf.format(t_date);
        // day
        SimpleDateFormat dsdf = new SimpleDateFormat("dd");
        String n_dated = dsdf.format(n_date);
        String t_dated = dsdf.format(t_date);

        if (n_datey.equals(t_datey)) { //年内のもの
            if (n_datem == t_datem &&
                    (Integer.parseInt(n_dated) - 3) <= Integer.parseInt(t_dated)) { ///直近3日間以内のもの
                long diffTime = n_date.getTime() - t_date.getTime();
                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH");
                String diffTimeStr = timeFormatter.format(new Date(diffTime));

                boolean moreThanDay = Math.abs(n_date.getTime() - t_date.getTime()) < MILLIS_PER_DAY;
                if (moreThanDay) { //24時間以内のもの
                    disp_date = diffTimeStr + "時間前";
                } else { //直近3日間以内 and 24時間超えて前のもの
                    disp_date = String.valueOf(Integer.parseInt(n_dated) - Integer.parseInt(t_dated)) + "日前";
                }
            } else { //年内 and 4日以上前のもの
                disp_date = t_datem + "月" + t_dated + "日";
            }
        } else { // 去年以前のもの
            disp_date = t_datey + "年" + t_datem + "月" + t_dated + "日";
        }

        TextView createDate = view.findViewById(R.id.tw_time);
        //SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd E HH:mm:ss");
        //createDate.setText(df.format(item.getCreatedAt()));
        createDate.setText(disp_date);
        //ユーザーIDを設定
        TextView userID = (TextView) view.findViewById(R.id.tw_userID);
        userID.setText("@" + item.getUser().getScreenName());
        // ツイートを設定
        TextView tweet = (TextView) view.findViewById(R.id.tw_main);
        tweet.setText(item.getText());
        //お気に入りを設定
        TextView favo = (TextView) view.findViewById(R.id.tw_like);
        favo.setText(String.valueOf(item.getFavoriteCount()));
        //リツイートを設定
        TextView retweet = (TextView) view.findViewById(R.id.tw_retweet);
        retweet.setText(String.valueOf(item.getRetweetCount()));
        //リプライを取得・設定
        /*Status s = twitter.showStatus(status.getInReplyToStatusId());
        TextView favo = (TextView)view.findViewById(R.id.tw_like);
        favo.setText(item.getFavoriteCount());*/

        return view;
    }

    @Override
    public void callBackTwitterLimit(int secondsUntilReset) {

    }

    private void startTranslate() {
        ImageView view = new ImageView(ApplicationController.getInstance().getApplicationContext());
        TranslateAnimation translateAnimation;
        translateAnimation = new TranslateAnimation(
                Animation.ABSOLUTE, 0.0f,
                Animation.ABSOLUTE, 500.0f,
                Animation.ABSOLUTE, 0.0f,
                Animation.ABSOLUTE, 1200.0f);

        // animation時間 msec
        translateAnimation.setDuration(2000);
        // 繰り返し回数
        translateAnimation.setRepeatCount(0);
        // animationが終わったそのまま表示にする
        translateAnimation.setFillAfter(true);
        //アニメーションの開始
        view.startAnimation(translateAnimation);
    }

    @Override
    public void callBackCreateFavo(Status status) {

    }

    @Override
    public void callBackDestroyFavo(Status status) {

    }
}
