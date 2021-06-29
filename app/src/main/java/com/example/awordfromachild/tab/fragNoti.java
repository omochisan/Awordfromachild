package com.example.awordfromachild.tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RadioGroup;

import com.example.awordfromachild.R;
import com.example.awordfromachild.asynctask.callBacksNoti;
import com.example.awordfromachild.common.TwitterUtils;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.twitterValue;
import com.example.awordfromachild.library.SetDefaultTweetAdapter;
import com.example.awordfromachild.library.SetDefaultTweetAdapter_DM;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import twitter4j.DirectMessage;
import twitter4j.DirectMessageList;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.User;

public class fragNoti extends fragmentBase implements callBacksNoti {
    private static List<Status> bk_list_favorite;
    private static List<Status> bk_list_reTweet;
    private static List<DirectMessage> bk_list_dm;
    private static List<User> bk_list_dmUser;
    private static int vid_nowChecked;
    private static final List<Status> merge_list = new ArrayList<>();
    private static int getUserCount = 0;
    private static DirectMessageList d_list;
    private RadioGroup radioGroup;
    final static List<User> d_userList = new ArrayList<>();

    /**
     * 画面タイプ変更時
     */
    private final RadioGroup.OnCheckedChangeListener radioChanged =
            new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            int checkRadioID = radioGroup.getCheckedRadioButtonId();
            setGetMethod(checkRadioID);
            getData(checkRadioID, twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH);
            vid_nowChecked = checkRadioID; //現在の選択状態を保持
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        int checkRadioID = radioGroup.getCheckedRadioButtonId();
        vid_nowChecked = checkRadioID; //現在の選択状態を保持
        setGetMethod(checkRadioID); //選択ラジオボタンごとにデータ取得
        getData(checkRadioID, twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH);
    }

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
        getUserCount = 0;
        return inflater.inflate(R.layout.fragnoti_layout, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //ラジオボタン処理
        radioGroup = view.findViewById(R.id.fno_select);
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
            bk_list_dmUser = adapter_dm.userList;
        }
    }

    /**
     * 選択ラジオボタンごとにデータ取得
     *
     * @param checkRadioID 選択ラジオボタン　リソースID
     * @param howToDisplay ツイート　画面追加方法
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
                    if(adapter == null){
                        adapter = new SetDefaultTweetAdapter(getContext(), R.layout.tweet_default, _bk);
                    }else {
                        adapter.clear();
                        adapter.addItems(_bk);
                    }
                    adapter.notifyDataSetChanged();
                    listView.setAdapter(adapter);
                } else { //バックアップない場合、取得
                    getTimeLine.setParam(twitterValue.timeLineType.USER, 0, 0,
                            twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET_MAX, howToDisplay);
                    getTimeLine.execute();
                }
            }
        } else if (checkRadioID == R.id.fno_rb_dm) {
            if (bk_list_dm != null) {  //バックアップがある場合
                if(adapter_dm == null){
                    adapter_dm = new SetDefaultTweetAdapter_DM(getContext(), R.layout.tweet_default, bk_list_dm, bk_list_dmUser);
                }else{
                    adapter_dm.clear();
                    adapter_dm.addItems(bk_list_dm, d_userList);
                }
                adapter_dm.notifyDataSetChanged();
                listView.setAdapter(adapter_dm);
            } else { //バックアップない場合、取得
                TwitterUtils.getDirectMessages getDirectMessages =
                        new TwitterUtils.getDirectMessages(this);
                getDirectMessages.setGetParam(dm_getNextCursor,
                        twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH);
                getDirectMessages.execute();
            }
        }
    }

    /**
     * コールバック
     * ツイート取得後
     * @param list 取得タイムライン
     * @param howToDisplay 取得ツイート　画面追加方法
     */
    @Override
    public void callBackGetTweets(Object list, String howToDisplay) {
        if (checkViewDetach(this)) return;

        List<Status> s_list = null;
        DirectMessageList d_list = null;
        List<Status> set_list = new ArrayList<>();
        if (list instanceof DirectMessageList) {
            d_list = (DirectMessageList) list;
            fragNoti.d_list = d_list;
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
            merge_list.addAll(set_list);

            //直近31日以降ではない ＆ 1回読込上限数取得できていない場合、追加読込
            //※DM取得APIは元々、直近30日しか取れない仕様
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
            //追加読込用カーソルを保持
            dm_getNextCursor = Objects.requireNonNull(d_list).getNextCursor();

            // ユーザー情報を取得
            TwitterUtils.getTwitterUserInfo getTwitterUserInfo =
                    new TwitterUtils.getTwitterUserInfo(this);
            long userID = d_list.get(getUserCount).getSenderId();
            getTwitterUserInfo.setParam(userID, howToDisplay);
            getTwitterUserInfo.execute();
        }
        hideSpinner(mPopupWindow);
    }

    /**
     * コールバック
     * ユーザー情報取得後
     * @param user ユーザー情報
     * @param howToDisplay 取得ツイート　画面追加方法
     */
    @Override
    public void callBackGetUser(User user, String howToDisplay) {
        d_userList.add(user);
        getUserCount++;

        if(getUserCount == d_list.size()){
            setListView_directMessage(d_list, d_userList, howToDisplay);
        }else{
            TwitterUtils.getTwitterUserInfo sUserInfo =
                    new TwitterUtils.getTwitterUserInfo(this);
            sUserInfo.setParam(d_list.get(getUserCount).getSenderId(), howToDisplay);
            sUserInfo.execute();
        }
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
