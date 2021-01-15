package com.example.awordfromachild;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.awordfromachild.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.lang.reflect.Array;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import twitter4j.*;

public class MainActivity extends AppCompatActivity {
    private AsyncConnect asyncCon;
    private Twitter twitter = TwitterFactory.getSingleton();
    private Query query = new Query();
    private TabLayout tabLayout;
    private int[] tabIcons = {
            R.drawable.main_ic_timeline,
            R.drawable.main_ic_mypost,
            R.drawable.main_ic_good,
            R.drawable.main_ic_follow,
            R.drawable.main_ic_follower
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setUpTabIcon();

        //ログイン（アクセストークン未取得の場合）
        if(!TwitterUtils.hasAccessToken(this)){
            Intent intent = new Intent(getApplication(), LoginTwitterActivity.class);
            startActivity(intent);
            finish();
        }

        //ツイートボタン
        ImageView tweet_btn = (ImageView) findViewById(R.id.img_tweet);
        ImageView search_btn = (ImageView) findViewById(R.id.ic_search);
        tweet_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //非同期処理（Twitterへ接続）開始
                asyncCon = new AsyncConnect();
                asyncCon.execute("tweet");
            }
        });

        //検索ボタン
        search_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //非同期処理（Twitterへ接続）開始
                EditText word = (EditText) findViewById(R.id.edittext_search);
                String async_arg[] = new String[2];
                async_arg[0] = "search";
                async_arg[1] = "マヂカルラブリー";
                //async_arg[1] = word.getText().toString();
                asyncCon = new AsyncConnect();
                asyncCon.execute(async_arg);
                /*android.os.AsyncTask<Void,Void,String> task = new android.os.AsyncTask<Void, Void, String>(){
                    @Override
                    protected String doInBackground(Void... aVoid) {
                        search("マヂカルラブリー");
                        return null;
                    }
                };
                task.execute();*/
            }
        });
    }

    private void setUpTabIcon() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
        tabLayout.getTabAt(4).setIcon(tabIcons[4]);
    }

}