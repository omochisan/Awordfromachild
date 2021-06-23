package com.example.awordfromachild.library;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.awordfromachild.MainActivity;
import com.example.awordfromachild.R;
import com.example.awordfromachild.SettingActivity;
import com.example.awordfromachild.constant.activityClassName;
import com.google.android.material.tabs.TabLayout;

/**
 * カスタマイズビュー
 * 画面ヘッダーを生成します
 */
public class SetHeader extends LinearLayout {
    //タブ
    private TabLayout tabLayout;
    //ヘッダーに紐づいたactivityクラス名
    private String activity_className;

    public SetHeader(Context context) {
        super(context);
        init(context);
    }

    public SetHeader(Context context, AttributeSet attr) {
        super(context, attr);
        init(context);
    }

    public SetHeader(Context context, AttributeSet attr, int defStyleAttr) {
        super(context, attr, defStyleAttr);
        init(context);
    }

    /**
     * 初期設定
     *
     * @param context
     */
    private void init(Context context) {
        // 第 2 引数で this を指定することで、Layout XML を自分自身に inflate する
        View layout = LayoutInflater.from(context).inflate(R.layout.header_layout, this);
        ((ImageView) layout.findViewById(R.id.hd_logo)).setOnClickListener(listenerLogo);
        ((ImageView) layout.findViewById(R.id.hd_icon)).setOnClickListener(listenerIcon);

        //ヘッダーに紐づいたActivityクラス名を取得
        //PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), context.getPackageManager().GET_ACTIVITIES);
        activity_className = context.getClass().getSimpleName();

        //画面名・アイコンを表示
        TextView title = layout.findViewById(R.id.hd_dispTitle);
        switch (activity_className) {
            case activityClassName.activity_main:
            case activityClassName.activity_contextThemeWrapper:
                title.setText(R.string.hd_title_main);
                ((ImageView) layout.findViewById(R.id.hd_icon)).setImageResource(R.drawable.main_ic_setting);
                break;

            case activityClassName.activity_createTweet:
                title.setText(R.string.hd_title_tweet);
                ((ImageView) layout.findViewById(R.id.hd_icon)).setImageResource(R.drawable.main_ic_setting);
                break;

            case activityClassName.activity_setting:
                title.setText(R.string.hd_title_setting);
                ((ImageView) layout.findViewById(R.id.hd_icon)).setImageResource(R.drawable.main_ic_home);
                break;

            case activityClassName.activity_myTweets:
                title.setText("じぶんの投稿一覧");
                ((ImageView) layout.findViewById(R.id.hd_icon)).setImageResource(R.drawable.main_ic_setting);
                break;
        }
    }

    /**
     * ロゴマーク押下時
     */
    private final View.OnClickListener listenerLogo = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (activity_className) {
                case activityClassName.activity_main:
                case activityClassName.activity_contextThemeWrapper:
                    //初期選択タブ
                    tabLayout = getRootView().findViewById(R.id.tabs);
                    tabLayout.getTabAt(0).select();
                    break;

                case activityClassName.activity_createTweet:
                case activityClassName.activity_myTweets:
                case activityClassName.activity_setting:
                    Intent intent = new Intent(getContext().getApplicationContext(), MainActivity.class);
                    getContext().startActivity(intent);
                    break;
            }
        }
    };

    /**
     * アイコン押下時
     */
    private final View.OnClickListener listenerIcon = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (activity_className) {
                case activityClassName.activity_main:
                case activityClassName.activity_contextThemeWrapper:
                case activityClassName.activity_createTweet:
                case activityClassName.activity_myTweets:
                    Intent intent1 = new Intent(getContext().getApplicationContext(), SettingActivity.class);
                    getContext().startActivity(intent1);
                    break;

                case activityClassName.activity_setting:
                    Intent intent2 = new Intent(getContext().getApplicationContext(), MainActivity.class);
                    getContext().startActivity(intent2);
                    break;
            }
        }
    };
}
