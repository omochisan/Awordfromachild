package com.example.awordfromachild.library;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.awordfromachild.R;
import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.asynctask.callBacksDefaultTweet;
import com.example.awordfromachild.common.TwitterUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import androidx.annotation.RequiresApi;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.User;

public class SetDefaultTweetAdapter_DM extends ArrayAdapter<DirectMessage> implements callBacksDefaultTweet {
    private final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    //表示ツイート打ち止めフラグ
    public boolean frg_end = false;
    private List<DirectMessage> dmList;
    public List<User> userList;
    private final int mResource;
    private final LayoutInflater mInflater;

    /**
     * コンストラクタ
     *
     * @param context  コンテキスト
     * @param resource リソースID
     * @param items    リストビューの要素
     * @param userItems DM送信ユーザーリスト
     */
    public SetDefaultTweetAdapter_DM(Context context, int resource,
                                     List<DirectMessage> items, List<User> userItems) {
        super(context, resource, items);
        mResource = resource;
        dmList = items;
        userList = userItems;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Twitter共通処理クラス生成
        //Twitter処理クラス
        TwitterUtils twitterUtils = new TwitterUtils((callBacksBase) context);
        twitterUtils.setTwitterInstance(getContext());
    }

    /**
     * ユーザーアイコンをセット
     *
     * @param view         ビュー
     * @param user         送信ユーザー
     */
    public void setUserIcon(View view, User user) {
        ImageView userIcon = view.findViewById(R.id.tw_userIcon);
        String getUrl = user.getProfileImageURLHttps();
        GlideApp.with(view)
                .load(getUrl)
                .circleCrop()
                .into(userIcon);

        //ユーザー名を設定
        TextView userName = view.findViewById(R.id.tw_userName);
        userName.setText(user.getName());
        //ユーザーIDを設定
        TextView userID = view.findViewById(R.id.tw_userID);
        userID.setText(String.format("@%s", user.getScreenName()));
    }

    /**
     * ツイートの各種値を設定
     *
     * @param view              ビュー
     * @param flg_detailDisplay ツイート詳細画面の描画かどうか
     * @param item              　ツイート
     */
    public void setValue(View view, DirectMessage item, boolean flg_detailDisplay) {
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

        TextView createDate = view.findViewById(R.id.tw_time);
        createDate.setText(disp_date);
        // ツイートを設定
        TextView tweet = view.findViewById(R.id.tw_main);
        tweet.setText(item.getText());
    }

    /**
     * リストにセットするツイート一覧を最後尾に追加設定。
     *
     * @param items 追加ツイート
     * @param userItems DM送信ユーザーリスト
     */
    public void addItems(List<DirectMessage> items, List<User> userItems) {
        if (dmList == null) {
            dmList = items;
            userList = userItems;
        } else {
            dmList.addAll(items);
            userList.addAll(userItems);
        }
    }

    /**
     * リストにセットするツイート一覧を先頭に追加設定。
     *
     * @param items 追加ツイート
     * @param userItems DM送信ユーザーリスト
     */
    public void unShiftItems(List<DirectMessage> items, List<User> userItems) {
        if(dmList == null){
            dmList = items;
            userList = userItems;
        }else{
            dmList.addAll(0, items);
            userList.addAll(0, userItems);
        }
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

        //フッター非表示
        view.findViewById(R.id.tw_footer).setVisibility(View.GONE);

        //表示ツイート打ち止めの場合
        LinearLayout l = view.findViewById(R.id.tw_linear_);
        if (frg_end && ((dmList != null && dmList.size() == position + 1))) {
            l.setVisibility(View.VISIBLE);
        } else {
            l.setVisibility(View.GONE);
        }

        //リストビューに表示するDMを取得
        DirectMessage item = dmList.get(position);
        //DM送信ユーザーを取得
        User user = userList.get(position);
        // ユーザーアイコンを設定
        setUserIcon(view, Objects.requireNonNull(user));
        // ツイートの各種値を設定
        setValue(view, item, false);

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
