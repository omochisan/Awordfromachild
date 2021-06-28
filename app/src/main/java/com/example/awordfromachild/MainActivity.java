package com.example.awordfromachild;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.awordfromachild.asynctask.callBacksMain;
import com.example.awordfromachild.common.TwitterUtils;
import com.example.awordfromachild.common.activityBase;
import com.example.awordfromachild.constant.appSharedPreferences;
import com.example.awordfromachild.constant.twitterValue;
import com.example.awordfromachild.library.GlideApp;
import com.example.awordfromachild.tab.fragAttention;
import com.example.awordfromachild.tab.fragFavorite;
import com.example.awordfromachild.tab.fragNewArrival;
import com.example.awordfromachild.tab.fragNoti;
import com.example.awordfromachild.tab.fragSearch;
import com.example.awordfromachild.ui.main.SectionsPagerAdapter;
import com.example.awordfromshild.ui.dialog.dAppExplanPageDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;
import twitter4j.User;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * メインスレッド
 */
public class MainActivity extends activityBase implements callBacksMain {
    //タブ情報（インデックス／タブ名）
    private final Map<String, String> tabInfo = new HashMap<>();

    private WeakReference<fragNewArrival> wr_fragNewArrival;
    private WeakReference<fragAttention> wr_fragAttention;
    private WeakReference<fragNoti> wr_fragNoti;
    private WeakReference<fragFavorite> wr_fragFavorite;
    private WeakReference<fragSearch> wr_fragSearch;

    //タブアイコン
    private final int[] tabIcons = {
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

    /**
     * ツイートアイコン押下時
     */
    private final View.OnClickListener iconTweetClick = view -> {
        //ツイート作成画面へ遷移
        Intent intent = new Intent(getApplication(), CreateTweetActivity.class);
        startActivity(intent);
    };
    /**
     * ポップアップ
     * じぶんの投稿を表示 押下時
     */
    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener popupItemMyTweetClick = (view, motionEvent) -> {
        //じぶんの投稿一覧画面へ遷移
        Intent intent = new Intent(getApplication(), MyTweetActivity.class);
        startActivity(intent);
        return false;
    };
    /**
     * ポップアップ
     * ログアウト押下時
     */
    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener popupItemLogoutClick = (view, motionEvent) -> {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_TwitterLogout_title)
                .setMessage(R.string.dialog_TwitterLogout_message)
                .setPositiveButton("OK", (dialog1, which) -> {
                    TwitterUtils.removeAccessToken();
                    dialogTwitterLogin();
                })
                .setNegativeButton("キャンセル", (dialog12, which) -> dialog12.dismiss())
                .show();
        return false;
    };
    /**
     * ポップアップ
     * 「アプリについて」押下時
     */
    @SuppressLint("ClickableViewAccessibility")
    private final View.OnClickListener popupAppExplainClick  = view -> {
        dAppExplanPageDialog newFragment = dAppExplanPageDialog.newInstance();
        newFragment.show(getSupportFragmentManager(), TAG);

        /*new AlertDialog.Builder(view.getContext())
                .setTitle(R.string.dialog_appExplain_title)
                .setMessage(R.string.dialog_appExplain)
                .setNegativeButton("閉じる", (dialog12, which) -> dialog12.dismiss()).show();*/
    };

    /**
     * onAttachFragment
     **/
    private final FragmentOnAttachListener fragmentOnAttachListener = new FragmentOnAttachListener() {
        @Override
        public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
            if (fragment instanceof fragNewArrival) {
                wr_fragNewArrival = new WeakReference<>((fragNewArrival) fragment);
            }
            if (fragment instanceof fragAttention) {
                wr_fragAttention = new WeakReference<>((fragAttention) fragment);
            }
            if (fragment instanceof fragSearch) {
                wr_fragSearch = new WeakReference<>((fragSearch) fragment);
            }
            if (fragment instanceof fragFavorite) {
                wr_fragFavorite = new WeakReference<>((fragFavorite) fragment);
            }
            if (fragment instanceof fragNoti) {
                wr_fragNoti = new WeakReference<>((fragNoti) fragment);
            }
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
            if (Objects.requireNonNull(tab_text).equals(newArrival)) {
                wr_fragNewArrival.get().addTheLatestTweets();
            } else if (tab_text.equals(attention)) {
                wr_fragAttention.get().addTheLatestTweets();
            } else if (tab_text.equals(search)) {
                wr_fragSearch.get().addTheLatestTweets();
            } else if (tab_text.equals(favorite)) {
                wr_fragFavorite.get().addTheLatestTweets();
            } else if (tab_text.equals(noti)) {
                wr_fragNoti.get().addTheLatestTweets();
            }
        }
    };
    private LinearLayout popup_userMenu;
    /**
     * ユーザーアイコン押下時
     */
    private final View.OnClickListener userIconClick = new View.OnClickListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onClick(View view) {
            //ポップアップメニュー表示
            if (popup_userMenu == null) {
                LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(
                        R.layout.popup_usericon, findViewById(R.id.fs_header), false);
                PopupWindow popupWindow = new PopupWindow();
                popupWindow.setWindowLayoutMode(ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT);
                popupWindow.setContentView(layout);
                popupWindow.showAsDropDown(view);
                popup_userMenu = layout;

                //イベント設定
                LinearLayout l_logout = layout.findViewById(R.id.p_item_logout);
                l_logout.setOnTouchListener(popupItemLogoutClick);
                TextView p_text = layout.findViewById(R.id.p_text_logout);
                ImageView p_ic = layout.findViewById(R.id.p_ic_logout);
                p_text.setOnTouchListener((v, event) -> false);
                p_ic.setOnTouchListener((v, event) -> false);

                LinearLayout l_myTweet = layout.findViewById(R.id.p_item_myTweet);
                l_myTweet.setOnTouchListener(popupItemMyTweetClick);
                TextView p_m_text = layout.findViewById(R.id.p_text_myTweet);
                ImageView p_m_ic = layout.findViewById(R.id.p_ic_myTweet);
                p_m_text.setOnTouchListener((v, event) -> false);
                p_m_ic.setOnTouchListener((v, event) -> false);
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
     * onCreate*
     *
     * @param savedInstanceState インスタンス状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setTabInfo();
            //画面基礎描画
            setContentView(R.layout.activity_main);

            TextView app_text = findViewById(R.id.mh_appExplan);
            app_text.setOnClickListener(popupAppExplainClick);
            //初回起動の場合、「アプリについて」ポップアップ起動
            SharedPreferences preferences = getSharedPreferences(
                    appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
            if(preferences.getBoolean(appSharedPreferences.FLG_FIRST_START, true)){
                //起動済フラグ登録
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(appSharedPreferences.FLG_FIRST_START, false);
                editor.apply();

                //ポップアップ起動
                dAppExplanPageDialog newFragment = dAppExplanPageDialog.newInstance();
                newFragment.show(getSupportFragmentManager(), TAG);

                //app_text.performClick();
            }

            //Twitter認証用画面よりアクセストークンを取得
            //取得済みの場合、端末に保存してあるアクセストークンをTwitterインスタンスにセット
            if (!TwitterUtils.hasAccessToken(this)) {
                dialogTwitterLogin();
                return;
            } else {
                twitterUtils.setTwitterInstance(this);
            }

            SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this);
            ViewPager2 viewPager = findViewById(R.id.view_pager);
            viewPager.setAdapter(sectionsPagerAdapter);
            //フラグメント
            FragmentManager fragment = getSupportFragmentManager();
            fragment.addFragmentOnAttachListener(fragmentOnAttachListener);
            //自ユーザー情報取得
            TwitterUtils.getTwitterMyUserInfo getTwitterUserInfo = new TwitterUtils.getTwitterMyUserInfo(this);
            getTwitterUserInfo.execute();
            //タブ
            tabLayout = findViewById(R.id.tabs);
            new TabLayoutMediator(tabLayout, viewPager,
                    (tab, position) -> tab.setText(twitterValue.TAB_TITLES[position])
            ).attach();
            setUpTabIcon();
            //ヘッダー
            TextView title = findViewById(R.id.hd_dispTitle);
            title.setText(R.string.hd_title_main);
            //ユーザーアイコン
            ImageView user_icon = findViewById(R.id.fs_img_account);
            user_icon.setOnClickListener(userIconClick);
            //リロードアイコン
            ImageView reload_icon = findViewById(R.id.fs_img_reload);
            reload_icon.setOnClickListener(reloadIconClick);
            //ツイートボタン
            ImageView tweet_btn = findViewById(R.id.fs_img_tweet);
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
    private void setTabInfo() {
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
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_TwitterLoginCheck_title)
                .setMessage(R.string.dialog_TwitterLoginCheck_message)
                .setPositiveButton("OK(ログイン画面へ)", (dialog12, which) -> {
                    Intent intent = new Intent(getApplication(), TwitterLoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("キャンセル(アプリを閉じる)", (dialog1, which) -> moveTaskToBack(true))
                .show();
    }

    /**
     * コールバック関数（Twitterの自ユーザー情報取得後）
     * アカウント画像をアイコンとして表示
     *
     * @param user ユーザー情報
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
        Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(tabIcons[0]);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(tabIcons[1]);
        Objects.requireNonNull(tabLayout.getTabAt(2)).setIcon(tabIcons[2]);
        Objects.requireNonNull(tabLayout.getTabAt(3)).setIcon(tabIcons[3]);
        Objects.requireNonNull(tabLayout.getTabAt(4)).setIcon(tabIcons[4]);
        //初期選択タブ
        Objects.requireNonNull(tabLayout.getTabAt(0)).select();
    }

    /**
     * コールバック
     * TwitterAPIリミット時
     */
    @Override
    public void callBackTwitterLimit(int secondsUntilReset) {
        ex_twitterAPILimit(secondsUntilReset);
    }

    /**
     * コールバック
     * 非チェック例外発生時
     */
    @Override
    public void callBackException() {
        fail_result();
    }
}