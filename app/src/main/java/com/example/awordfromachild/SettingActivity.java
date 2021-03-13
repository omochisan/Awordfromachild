package com.example.awordfromachild;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.example.awordfromachild.common.activityBase;
import com.example.awordfromachild.constant.appSharedPrerence;
import com.example.awordfromachild.constant.twitterValue;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.internal.Ref;

public class SettingActivity extends activityBase {
    SharedPreferences preferences;

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
     * ボタン押下時
     */
    private final View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(@NotNull View view) {
            switch (view.getId()){
                case R.id.set_save:
                    //保存ボタン押下時
                    SharedPreferences.Editor editor = preferences.edit();
                    //ツイート作成画面タイプ
                    RadioGroup rgroup = findViewById(R.id.set_ct_defaultType_rgroup);
                    if(rgroup.getCheckedRadioButtonId() == R.id.set_ct_defaultType_r1){
                        editor.putString(appSharedPrerence.SET_DISPLAY_TYPE_TWEET_CREATE, twitterValue.TYPE_OF_TWEET_CREATION_FREE);
                    }else{
                        editor.putString(appSharedPrerence.SET_DISPLAY_TYPE_TWEET_CREATE, twitterValue.TYPE_OF_TWEET_CREATION_FORM);
                    }
                    //いいね数
                    EditText edit_like = findViewById(R.id.set_at_like_edit);
                    SpannableStringBuilder sb1 = (SpannableStringBuilder)edit_like.getText();
                    editor.putString(appSharedPrerence.SET_COUNT_LIKE, sb1.toString());
                    //リツイート数
                    EditText edit_retweet = findViewById(R.id.set_at_retweet_edit);
                    SpannableStringBuilder sb2 = (SpannableStringBuilder)edit_retweet.getText();
                    editor.putString(appSharedPrerence.SET_COUNT_RETWEET, sb2.toString());

                    editor.commit();
                    finish();
                    break;

                case R.id.set_cancel:
                    //キャンセルボタン押下時
                    finish();
                    break;
            }
        }
    };

    /**
     * 設定値取得
     */
    public void getSetting(){
        //preferenceから設定値の呼び出し
        preferences = this.getSharedPreferences(appSharedPrerence.PREF_NAME, Context.MODE_PRIVATE);

        String set_dispType =
                preferences.getString(appSharedPrerence.SET_DISPLAY_TYPE_TWEET_CREATE, twitterValue.DEFAULT_TYPE_OF_TWEET_CREATION);
        String set_countLike = preferences.getString(appSharedPrerence.SET_COUNT_LIKE, twitterValue.DEFAULT_LIKES);
        String set_countRetweet = preferences.getString(appSharedPrerence.SET_COUNT_RETWEET, twitterValue.DEFAULT_RETWEET);

        //ツイート作成画面　デフォルトタイプ
        RadioGroup dispType = findViewById(R.id.set_ct_defaultType_rgroup);
        if(set_dispType.equals(twitterValue.TYPE_OF_TWEET_CREATION_FREE)){
            dispType.check(R.id.set_ct_defaultType_r1);
        }else{
            dispType.check(R.id.set_ct_defaultType_r2);
        }
        //いいね数
        EditText like = findViewById(R.id.set_at_like_edit);
        like.setText(set_countLike);
        //リツイート数
        EditText retweet = findViewById(R.id.set_at_retweet_edit);
        retweet.setText(set_countRetweet);
    }
}