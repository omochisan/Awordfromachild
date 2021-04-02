package com.example.awordfromachild.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.awordfromachild.ApplicationController;
import com.example.awordfromachild.R;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.constant.twitterValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import twitter4j.Status;

public class SetDefaultTweetAdapter extends ArrayAdapter<twitter4j.Status> {
    private final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    private int mResource;
    private List<twitter4j.Status> mItems;
    private LayoutInflater mInflater;
    public long maxID = 0;
    public long sinceID = 0;

    //TwitterUtils タイムライン
    TwitterUtils tu_timeLine;
    TwitterUtils tu_attention;
    TwitterUtils tu_search;
    TwitterUtils tu_noti;

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
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * リストにセットするツイート一覧を追加設定。
     *
     * @param items
     */
    public void addItems(ArrayList<twitter4j.Status> items) {
        mItems.addAll(items);
    }

    /**
     * 画面をスクロールし、新しい一行が表示されるたびに呼ばれ、1行のUIを作成する。
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        //追加読込ボタンを表示する場合
        if(position >= 1 && mItems.get(position - 1).getId() == sinceID){
            Button btn = new Button(ApplicationController.getInstance().getApplicationContext());
            btn.setText("さらにツイートを表示する");

            //Button button =

            //追加読込
            btn.setOnClickListener(view1 -> {
                tu_timeLine.getTimeLine(
                        twitterValue.HOME, maxID, sinceID, twitterValue.GET_TYPE_EVEN_NEWER,
                        twitterValue.TWEET_HOW_TO_DISPLAY_MIDDLE_ADD);
                //ボタン削除
                parent.removeView(btn);
            });
            return btn;
        }

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
}
