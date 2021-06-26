package com.example.awordfromachild.tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RadioGroup;

import com.example.awordfromachild.R;
import com.example.awordfromachild.common.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksNoti;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.twitterValue;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import twitter4j.DirectMessageList;
import twitter4j.ResponseList;
import twitter4j.Status;

public class fragNoti extends fragmentBase implements callBacksNoti {
    static ArrayList<Status> bk_list_favorite;
    static ArrayList<Status> bk_list_reTweet;
    static DirectMessageList bk_list_dm;
    static int vid_nowChecked;
    static final List<Status> merge_list = new ArrayList<>();
    static String dm_getNextCursor;

    /**
     * 画面タイプ変更時
     */
    private final RadioGroup.OnCheckedChangeListener radioChanged = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            int checkRadioID = radioGroup.getCheckedRadioButtonId();
            setGetMethod(checkRadioID);
            getData(checkRadioID, twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH);
            vid_nowChecked = checkRadioID; //現在の選択状態を保持
        }
    };

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        //Twitter共通処理クラス生成
        twitterUtils = new TwitterUtils(this);
        mPopupWindow = new PopupWindow(getContext()); //スピナー用
        vid_listView = R.id.fno_main;
        return inflater.inflate(R.layout.fragnoti_layout, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        RadioGroup radioGroup = view.findViewById(R.id.fno_select);
        int checkRadioID = radioGroup.getCheckedRadioButtonId();
        vid_nowChecked = checkRadioID; //現在の選択状態を保持
        setGetMethod(checkRadioID); //選択ラジオボタンごとにデータ取得
        getData(checkRadioID, twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH);

        super.onViewCreated(view, savedInstanceState);

        //ラジオボタン処理
        radioGroup.setOnCheckedChangeListener(radioChanged);
    }

    /**
     * リロードボタン押下時、
     * 最新ツイートを取得
     */
    public void addTheLatestTweets() {
        dispSpinner(mPopupWindow);

        RadioGroup radioGroup = requireActivity().findViewById(R.id.fno_select);
        int checkRadioID = radioGroup.getCheckedRadioButtonId();
        getData(checkRadioID, twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT);
    }

    /**
     * 選択ラジオボタンごとにデータ取得方法を設定
     *
     * @param checkRadioID 選択ラジオボタン　リソースID
     */
    private void setGetMethod(int checkRadioID) {
        if (checkRadioID == R.id.fno_rb_favorite || checkRadioID == R.id.fno_rb_retweet) {
            getMethod = twitterValue.getMethod.TIMELINE;
        } else if (checkRadioID == R.id.fno_rb_dm) {
            getMethod = twitterValue.getMethod.DM;
        }

        //バックアップ
        if (adapter == null) return;
        if (vid_nowChecked == R.id.fno_rb_favorite) {
            bk_list_favorite = getItemList();
        } else if (vid_nowChecked == R.id.fno_rb_retweet) {
            bk_list_reTweet = getItemList();
        } else if (vid_nowChecked == R.id.fno_rb_dm) {
            bk_list_dm = getItemList_dm();
        }
    }

    /**
     * 選択ラジオボタンごとにデータ取得
     *
     * @param checkRadioID 選択ラジオボタン　リソースID
     */
    private void getData(int checkRadioID, String howToDisplay) {
        if (checkRadioID == R.id.fno_rb_favorite || checkRadioID == R.id.fno_rb_retweet) {
            TwitterUtils.getTimeLine getTimeLine = new TwitterUtils.getTimeLine(this);
            //最新ツイート取得
            if (howToDisplay.equals(twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT)) {
                long sinceID = adapter.getItem(0).getId();
                getTimeLine.setParam(twitterValue.timeLineType.USER, 0, sinceID,
                        twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET_MAX, howToDisplay);
                getTimeLine.execute();
            } else {
                List<Status> _bk = checkRadioID == R.id.fno_rb_favorite ?
                        bk_list_favorite : bk_list_reTweet;
                if (_bk != null) {  //バックアップがある場合
                    adapter.clear();
                    adapter.addItems(_bk, null);
                    adapter.notifyDataSetChanged();
                } else { //バックアップない場合、取得
                    getTimeLine.setParam(twitterValue.timeLineType.USER, 0, 0,
                            twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET_MAX, howToDisplay);
                    getTimeLine.execute();
                }
            }
        } else if (checkRadioID == R.id.fno_rb_dm) {
            if (bk_list_dm != null) {  //バックアップがある場合
                adapter.clear();
                adapter.addItems(null, bk_list_dm);
                adapter.notifyDataSetChanged();
            } else { //バックアップない場合、取得
                TwitterUtils.getDirectMessages getDirectMessages =
                        new TwitterUtils.getDirectMessages(this);
                getDirectMessages.setGetParam(dm_getNextCursor,
                        twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH);
                getDirectMessages.execute();
            }
        }
    }

    @Override
    public void callBackGetTweets(Object list, String howToDisplay) {
        if (checkViewDetach(this)) return;

        List<Status> s_list = null;
        DirectMessageList d_list = null;
        List<Status> set_list = new ArrayList<>();
        if (list instanceof DirectMessageList) {
            d_list = (DirectMessageList) list;
        } else if (list instanceof ResponseList) {
            s_list = autoCast(list);
        }

        //リストviewにセット
        if (s_list != null) {
            boolean flg_endDate = false;
            //いいね・リツイート取得の場合、直近30日以降 or 1回読込上限数取得できた段階で1読込を終了
            if (vid_nowChecked == R.id.fno_rb_favorite) {
                for (Status s : s_list) {
                    //31日前以前の場合、取得終了
                    flg_endDate = checkBeforeDate(s);
                    if (flg_endDate) break;
                    //いいねされているツイート
                    if (s.getFavoriteCount() >= 1) set_list.add(s);
                }
            } else if (vid_nowChecked == R.id.fno_rb_retweet) {
                for (Status s : s_list) {
                    //31日前以前の場合、取得終了
                    flg_endDate = checkBeforeDate(s);
                    if (flg_endDate) break;
                    //リツイートされているツイート
                    if (s.getRetweetCount() >= 1) set_list.add(s);
                }
            }
            //if (flg_endDate) adapter.frg_end = true;
            merge_list.addAll(set_list);
            //直近31日以降ではない ＆ 1回読込上限数取得できていない場合、追加読込
            if (s_list.size() >= 1 && !flg_endDate &&
                    merge_list.size() < twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET) {
                TwitterUtils.getTimeLine getTimeLine = new TwitterUtils.getTimeLine(this);
                getTimeLine.setParam(twitterValue.timeLineType.USER,
                        s_list.get(s_list.size() - 1).getId(), 0,
                        twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET_MAX,
                        twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_PUSH);
                getTimeLine.execute();
            } else {
                List<Status> _list = new ArrayList<>(merge_list);
                setListView(_list, howToDisplay);
                merge_list.clear();
            }
        } else {
            dm_getNextCursor = Objects.requireNonNull(d_list).getNextCursor();
            setListView_directMessage(d_list, howToDisplay);
        }
        hideSpinner(mPopupWindow);
    }

    /**
     * 表示範囲(30日以内)外かどうか判断
     *
     * @param status 対象ツイート
     * @return true=31日以前
     */
    private Boolean checkBeforeDate(Status status) {
        // 加算される現在時間の取得(Date型)
        Date date = new Date();
        // Date型の日時をCalendar型に変換
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // 日時を加算する
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        // Calendar型の日時をDate型に戻す
        Date _date = calendar.getTime();
        Date t_date = status.getCreatedAt();

        //31日前以前の場合、終了
        return t_date.before(_date);
    }
}
