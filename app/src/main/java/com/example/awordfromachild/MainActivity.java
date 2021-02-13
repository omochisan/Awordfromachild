package com.example.awordfromachild;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.awordfromachild.library.GlideApp;
import com.example.awordfromachild.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import twitter4j.Twitter;
import twitter4j.User;

/**
 * メインスレッド
 */
public class MainActivity extends AppCompatActivity {
    //context取得用
    private static MainActivity instance = null;
    private Context applicationContext;

    private Twitter twitter;
    //Twitter処理クラスインスタンス
    private TwitterUtils twitterUtils;
    //タブ
    private TabLayout tabLayout;
    private int[] tabIcons = {
            R.drawable.main_ic_timeline,
            R.drawable.main_ic_attention,
            R.drawable.main_ic_search,
            R.drawable.main_ic_noti
    };

    public static MainActivity getInstance() {
        return instance;
    }

    /**
     * onCreate*
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        twitterUtils = new TwitterUtils();
        instance = this;

        //Twitter認証用画面よりアクセストークンを取得
        //取得済みの場合、端末に保存してあるアクセストークンをTwitterインスタンスにセット
        if (!TwitterUtils.hasAccessToken(this)) {
            Intent intent = new Intent(getApplication(), TwitterLoginActivity.class);
            startActivity(intent);
            finish();
        }else{
            twitter = twitterUtils.getTwitterInstance(this);
        }

        //画面基礎描画
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        twitterUtils.getTwitterUserInfo(twitter, instance); //自ユーザー情報取得
        //タブ
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setUpTabIcon();
        //ヘッダー
        TextView title = findViewById(R.id.hd_dispTitle);
        title.setText(R.string.hd_title_main);
        //ツイートボタン
        ImageView tweet_btn = (ImageView) findViewById(R.id.fs_img_tweet);
        tweet_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //非同期処理（Twitterへ接続）開始
                twitterUtils.tweet(twitter);
            }
        });
    }

    /**
     * コールバック関数（Twitterの自ユーザー情報取得後）
     * アカウント画像をアイコンとして表示
     * @param user
     */
    public void onAsyncFinished_getUserInfo(User user){
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


}