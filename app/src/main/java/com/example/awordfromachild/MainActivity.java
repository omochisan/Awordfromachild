package com.example.awordfromachild;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.asynctask.callBacksMain;
import com.example.awordfromachild.common.activityBase;
import com.example.awordfromachild.constant.twitterValue;
import com.example.awordfromachild.library.GlideApp;
import com.example.awordfromachild.library.SetDefaultTweetAdapter;
import com.example.awordfromachild.tab.fragAttention;
import com.example.awordfromachild.tab.fragNoti;
import com.example.awordfromachild.tab.fragSearch;
import com.example.awordfromachild.tab.fragTimeLine;
import com.example.awordfromachild.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import twitter4j.Status;
import twitter4j.User;

/**
 * メインスレッド
 */
public class MainActivity extends activityBase implements callBacksMain {
    //タブ情報（インデックス／タブ名）
    private final Map<String, String> tabInfo = new HashMap<String, String>();
    //タブアイコン
    private int[] tabIcons = {
            R.drawable.main_ic_timeline,
            R.drawable.main_ic_attention,
            R.drawable.main_ic_search,
            R.drawable.main_ic_noti
    };
    //タブのタイトル
    private String timeLine;
    private String attention;
    private String search;
    private String noti;
    //タブ
    private TabLayout tabLayout;
    //Twitter処理クラス
    private TwitterUtils twitterUtils;
    private LinearLayout popup_userMenu;
    //スピナー用
    private static PopupWindow mPopupWindow;
    //表示中のsince ID
    private long sinceID;
    //TwitterUtils タイムライン
    TwitterUtils tu_timeLine;
    TwitterUtils tu_attention;
    TwitterUtils tu_search;
    TwitterUtils tu_noti;

    /**
     * ツイートアイコン押下時
     */
    private final View.OnClickListener iconTweetClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //ツイート作成画面へ遷移
            Intent intent = new Intent(getApplication(), CreateTweetActivity.class);
            startActivity(intent);
        }
    };

    /**
     * ポップアップ
     * ログアウト押下時
     */
    private final View.OnTouchListener popupItemLogoutClick = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Dialog dialog = new AlertDialog.Builder(view.getContext())
                    .setTitle(R.string.dialog_TwitterLogout_title)
                    .setMessage(R.string.dialog_TwitterLogout_message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            twitterUtils.removeAccessToken();
                            dialogTwitterLogin();
                        }
                    })
                    .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return false;
        }
    };

    /**
     * リロードアイコン押下時
     */
    private final View.OnClickListener reloadIconClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dispSpinner(mPopupWindow);

            //新しいツイートをlistViewの先頭に追加
            //※追加分ツイートが50を超えてある場合、
            // 1～50を追加／～ツイートを更に表示ボタン～／取得表示済ツイート の順に表示する。
            final String tab_text = tabInfo.get(String.valueOf(tabLayout.getSelectedTabPosition()));
            //表示中のフラグメントにより処理を変化
            Adapter adapter = ((ListView)findViewById(R.id.ft_main)).getAdapter();
            sinceID = ((twitter4j.Status) adapter.getItem(0)).getId();
            if(tab_text.equals(timeLine)){
                tu_timeLine.getTimeLine(
                        twitterValue.HOME,
                        ((twitter4j.Status) adapter.getItem(0)).getId(),
                        ((twitter4j.Status) adapter.getItem(5)).getId(),
                        twitterValue.GET_TYPE_EVEN_NEWER, twitterValue.TWEET_HOW_TO_DISPLAY_ADD);
                /*tu_timeLine.getTimeLine(twitterValue.HOME, sinceID,
                        twitterValue.GET_TYPE_EVEN_NEWER, twitterValue.TWEET_HOW_TO_DISPLAY_ADD);*/
            }
            else if(tab_text.equals(attention)){

            }
            else if(tab_text.equals(search)){

            }
            else if(tab_text.equals(noti)){

            }
        }
    };

    /**
     * ユーザーアイコン押下時
     */
    private final View.OnClickListener userIconClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //ポップアップメニュー表示
            if (popup_userMenu == null) {
                LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.popup_usericon, null);
                PopupWindow popupWindow = new PopupWindow();
                popupWindow.setWindowLayoutMode(ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT);
                popupWindow.setContentView(layout);
                popupWindow.showAsDropDown(view);
                popup_userMenu = layout;

                //イベント設定
                LinearLayout l_logout = (LinearLayout) layout.findViewById(R.id.p_item_logout);
                l_logout.setOnTouchListener(popupItemLogoutClick);
                TextView p_text = (TextView) layout.findViewById(R.id.p_text_logout);
                ImageView p_ic = (ImageView) layout.findViewById(R.id.p_ic_logout);
                p_text.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });
                p_ic.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });
                return;
            }
            if (popup_userMenu.getVisibility() == View.VISIBLE) {
                popup_userMenu.setVisibility(View.GONE);
            } else if (popup_userMenu.getVisibility() == View.GONE) {
                popup_userMenu.setVisibility(View.VISIBLE);
            }

        }
    };

    /**
     * onAttachFragment
     * @param fragment
     */
    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof fragTimeLine) {
            tu_timeLine = new TwitterUtils((callBacksBase) fragment);
            tu_timeLine.setTwitterInstance(fragment.getContext());
        }
        if(fragment instanceof fragAttention){
            tu_attention = new TwitterUtils((callBacksBase) fragment);
            tu_attention.setTwitterInstance(fragment.getContext());
        }
        if(fragment instanceof fragSearch){
            tu_search = new TwitterUtils((callBacksBase) fragment);
            tu_search.setTwitterInstance(fragment.getContext());
        }
        if(fragment instanceof fragNoti){
            tu_noti = new TwitterUtils((callBacksBase) fragment);
            tu_noti.setTwitterInstance(fragment.getContext());
        }
    }

    /**
     * onCreate*
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            mPopupWindow = new PopupWindow(this);
            setTabInfo();
            twitterUtils = new TwitterUtils(this);
            //Twitter認証用画面よりアクセストークンを取得
            //取得済みの場合、端末に保存してあるアクセストークンをTwitterインスタンスにセット
            if (!TwitterUtils.hasAccessToken(this)) {
                dialogTwitterLogin();
                return;
            } else {
                twitterUtils.setTwitterInstance(this);
            }

            //画面基礎描画
            setContentView(R.layout.activity_main);
            SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
            ViewPager viewPager = findViewById(R.id.view_pager);
            viewPager.setAdapter(sectionsPagerAdapter);
            twitterUtils.getTwitterUserInfo(); //自ユーザー情報取得
            //タブ
            tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);
            setUpTabIcon();
            //ヘッダー
            TextView title = findViewById(R.id.hd_dispTitle);
            title.setText(R.string.hd_title_main);
            //ユーザーアイコン
            ImageView user_icon = (ImageView) findViewById(R.id.fs_img_account);
            user_icon.setOnClickListener(userIconClick);
            //リロードアイコン
            ImageView reload_icon = (ImageView) findViewById(R.id.fs_img_reload);
            reload_icon.setOnClickListener(reloadIconClick);

            //ツイートボタン
            ImageView tweet_btn = (ImageView) findViewById(R.id.fs_img_tweet);
            tweet_btn.setOnClickListener(iconTweetClick);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * タブ情報をセット
     * ・タブのタイトル
     * ・タブ順
     */
    private void setTabInfo(){
        timeLine = getResources().getString(R.string.tab_text_timeline);
        attention = getResources().getString(R.string.tab_text_attention);
        search = getResources().getString(R.string.tab_text_search);
        noti = getResources().getString(R.string.tab_text_noti);

        tabInfo.put("0", timeLine);
        tabInfo.put("1", attention);
        tabInfo.put("2", search);
        tabInfo.put("3", noti);
    }

    /**
     * Twitterログインを促すダイアログを表示
     * OKボタン…Twitterログイン画面（ブラウザ）を表示
     * キャンセルボタン…アプリを終了（中断）させる
     */
    private void dialogTwitterLogin() {
        // BuilderからAlertDialogを作成
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_TwitterLoginCheck_title)
                .setMessage(R.string.dialog_TwitterLoginCheck_message)
                .setPositiveButton("OK(ログイン画面へ)", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplication(), TwitterLoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("キャンセル(アプリを閉じる)", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);
                    }
                })
                .show();
    }

    /**
     * コールバック関数（Twitterの自ユーザー情報取得後）
     * アカウント画像をアイコンとして表示
     *
     * @param user
     */
    @Override
    public void callBackGetUser(User user) {
        //アカウントアイコンを設置
        ImageView accountImage = findViewById(R.id.fs_img_account);
        String getUrl = user.getProfileImageURLHttps();
        GlideApp.with(this)
                .load(getUrl)
                .circleCrop()
                .into(accountImage);
    }

    /**
     * タブを設定
     */
    private void setUpTabIcon() {
        //アイコン設定
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
        //初期選択タブ
        tabLayout.getTabAt(0).select();
    }

    /**
     * コールバック
     * TwitterAPIリミット時
     */
    @Override
    public void callBackTwitterLimit(int secondsUntilReset) {
        ex_twitterAPILimit(secondsUntilReset);
    }
}