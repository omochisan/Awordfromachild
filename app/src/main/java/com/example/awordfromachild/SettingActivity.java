package com.example.awordfromachild;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.example.awordfromachild.common.activityBase;
import com.example.awordfromachild.constant.appSharedPreferences;
import com.example.awordfromachild.constant.twitterValue;

import org.jetbrains.annotations.NotNull;

public class SettingActivity extends activityBase {
    SharedPreferences preferences;
    /**
     * ボタン押下時
     */
    private final View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(@NotNull View view) {
            int rId = view.getId();
            if (rId == R.id.set_save) {
                //保存ボタン押下時
                SharedPreferences.Editor editor = preferences.edit();
                //ツイート作成画面タイプ
                RadioGroup rgroup = findViewById(R.id.set_ct_defaultType_rgroup);
                if (rgroup.getCheckedRadioButtonId() == R.id.set_ct_defaultType_free) {
                    editor.putString(appSharedPreferences.SET_DISPLAY_TYPE_TWEET_CREATE, twitterValue.createTweetValue.TYPE_OF_TWEET_CREATION_FREE);
                } else {
                    editor.putString(appSharedPreferences.SET_DISPLAY_TYPE_TWEET_CREATE, twitterValue.createTweetValue.TYPE_OF_TWEET_CREATION_FORM);
                }
                //いいね目安
                RadioGroup rGroupLike = findViewById(R.id.set_at_radioGroup);
                int checkId = rGroupLike.getCheckedRadioButtonId();
                if (checkId == R.id.set_at_popular) {
                    editor.putInt(appSharedPreferences.SET_CRITERION_LIKE, twitterValue.DEFAULT_LIKES);
                } else if (checkId == R.id.set_at_s) {
                    editor.putInt(appSharedPreferences.SET_CRITERION_LIKE, twitterValue.attentionValue.CRITERION_LIKE_S);
                } else if (checkId == R.id.set_at_m) {
                    editor.putInt(appSharedPreferences.SET_CRITERION_LIKE, twitterValue.attentionValue.CRITERION_LIKE_M);
                } else if (checkId == R.id.set_at_l) {
                    editor.putInt(appSharedPreferences.SET_CRITERION_LIKE, twitterValue.attentionValue.CRITERION_LIKE_L);
                }
                editor.apply();
                finish();

            } else if (rId == R.id.set_cancel) {
                //キャンセルボタン押下時
                finish();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
        getSetting();
        //保存ボタン
        Button btn_save = findViewById(R.id.set_save);
        btn_save.setOnClickListener(btnClick);
        //キャンセルボタン
        Button btn_cancel = findViewById(R.id.set_cancel);
        btn_cancel.setOnClickListener(btnClick);
    }

    /**
     * 設定値取得
     */
    public void getSetting() {
        //preferenceから設定値の呼び出し
        preferences = this.getSharedPreferences(appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);

        String set_dispType =
                preferences.getString(appSharedPreferences.SET_DISPLAY_TYPE_TWEET_CREATE, twitterValue.createTweetValue.DEFAULT_TYPE_OF_TWEET_CREATION);
        int set_criterionLike =
                preferences.getInt(appSharedPreferences.SET_CRITERION_LIKE, twitterValue.DEFAULT_LIKES);

        //ツイート作成画面　デフォルトタイプ
        RadioGroup dispType = findViewById(R.id.set_ct_defaultType_rgroup);
        if (set_dispType.equals(twitterValue.createTweetValue.TYPE_OF_TWEET_CREATION_FREE)) {
            dispType.check(R.id.set_ct_defaultType_free);
        } else {
            dispType.check(R.id.set_ct_defaultType_form);
        }
        //いいね目安
        RadioGroup criterionLike = findViewById(R.id.set_at_radioGroup);
        switch (set_criterionLike) {
            case twitterValue.DEFAULT_LIKES:
                criterionLike.check(R.id.set_at_popular);
                break;
            case twitterValue.attentionValue.CRITERION_LIKE_S:
                criterionLike.check(R.id.set_at_s);
                break;
            case twitterValue.attentionValue.CRITERION_LIKE_M:
                criterionLike.check(R.id.set_at_m);
                break;
            case twitterValue.attentionValue.CRITERION_LIKE_L:
                criterionLike.check(R.id.set_at_l);
                break;
        }

    }
}
