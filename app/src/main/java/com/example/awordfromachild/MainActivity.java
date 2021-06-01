package com.example.awordfromachild;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.awordfromachild.asynctask.callBacksMain;
import com.example.awordfromachild.common.activityBase;
import com.example.awordfromachild.library.GlideApp;
import com.example.awordfromachild.tab.fragAttention;
import com.example.awordfromachild.tab.fragFavorite;
import com.example.awordfromachild.tab.fragNewArrival;
import com.example.awordfromachild.tab.fragNoti;
import com.example.awordfromachild.tab.fragSearch;
import com.example.awordfromachild.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import twitter4j.QueryResult;
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
            R.drawable.main_ic_new,
            R.drawable.main_ic_attention,
            R.drawable.main_ic_search,
            R.drawable.main_ic_favorite,
            R.drawable.main_ic_noti
    };
    //タブのタイトル
    private String newArrival;
    private String attention;
    private String search;
    private String favorite;
    private String noti;
    //タブ
    private TabLayout tabLayout;
    private LinearLayout popup_userMenu;

    WeakReference<fragNewArrival> wr_fragNewArrival;
    WeakReference<fragAttention> wr_fragAttention;
    WeakReference<fragNoti> wr_fragNoti;
    WeakReference<fragFavorite> wr_fragFavorite;
    WeakReference<fragSearch> wr_fragSearch;

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
     * じぶんの投稿を表示 押下時
     */
    private final View.OnTouchListener popupItemMyTweetClick = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            //じぶんの投稿一覧画面へ遷移
            Intent intent = new Intent(getApplication(), MyTweetActivity.class);
            startActivity(intent);
            return false;
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
                    }).show();
            return false;
        }
    };

    /**
     * リロードアイコン押下時
     */
    private final View.OnClickListener reloadIconClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //新しいツイートをlistViewの先頭に追加
            //※追加分ツイートが200以上ある場合、洗い替えして表示
            final String tab_text = tabInfo.get(String.valueOf(tabLayout.getSelectedTabPosition()));
            //表示中のフラグメントにより処理を変化
            if(tab_text.equals(newArrival)){
                wr_fragNewArrival.get().addTheLatestTweets();
            }
            else if(tab_text.equals(attention)){
                wr_fragAttention.get().addTheLatestTweets();
            }
            else if(tab_text.equals(search)){
                wr_fragFavorite.get().addTheLatestTweets();
            }
            else if(tab_text.equals(favorite)){

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

                LinearLayout l_myTweet = (LinearLayout) layout.findViewById(R.id.p_item_myTweet);
                l_myTweet.setOnTouchListener(popupItemMyTweetClick);
                TextView p_m_text = (TextView) layout.findViewById(R.id.p_text_myTweet);
                ImageView p_m_ic = (ImageView) layout.findViewById(R.id.p_ic_myTweet);
                p_m_text.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });
                p_m_ic.setOnTouchListener(new View.OnTouchListener() {
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
        if (fragment instanceof fragNewArrival) {
            wr_fragNewArrival = new WeakReference<>((fragNewArrival) fragment);
        }
        if(fragment instanceof fragAttention){
            wr_fragAttention = new WeakReference<>((fragAttention) fragment);
        }
        if(fragment instanceof fragSearch){
            wr_fragSearch = new WeakReference<>((fragSearch) fragment);
        }
        if(fragment instanceof fragFavorite){
            wr_fragFavorite = new WeakReference<>((fragFavorite) fragment);
        }
        if(fragment instanceof fragNoti){
            wr_fragNoti = new WeakReference<>((fragNoti) fragment);
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
            setTabInfo();
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
        newArrival = getResources().getString(R.string.tab_text_newArrival);
        attention = getResources().getString(R.string.tab_text_attention);
        search = getResources().getString(R.string.tab_text_search);
        favorite = getResources().getString(R.string.tab_text_favorite);
        noti = getResources().getString(R.string.tab_text_noti);

        tabInfo.put("0", newArrival);
        tabInfo.put("1", attention);
        tabInfo.put("2", search);
        tabInfo.put("3", favorite);
        tabInfo.put("4", noti);
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
        tabLayout.getTabAt(4).setIcon(tabIcons[4]);
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

    @Override
    public void callBackStreamAddList(Status status) {

    }

    /**
     * コールバック
     * 非チェック例外発生時
     */
    @Override
    public void callBackException() {
        fail_result();
    }

    @Override
    public void callBackGetTweets(Object list, String howToDisplay) {

    }
}