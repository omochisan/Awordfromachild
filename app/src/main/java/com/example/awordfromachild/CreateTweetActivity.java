package com.example.awordfromachild;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.awordfromachild.asynctask.callBacksCreateTweet;
import com.example.awordfromachild.common.activityBase;
import com.example.awordfromachild.constant.appSharedPrerence;
import com.example.awordfromachild.constant.twitterValue;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 「こどものひとことをツイート」画面
 */
public class CreateTweetActivity extends activityBase implements callBacksCreateTweet, TextWatcher {
    //Twitter処理クラス
    private TwitterUtils twitterUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        twitterUtils = new TwitterUtils(this);
        setContentView(R.layout.createtweet_layout);
        //画面の初期表示処理
        setDisplay();
        //ボタン処理
        final Button btn_sent = findViewById(R.id.ct_btn_sent);
        btn_sent.setOnClickListener(btnSentClick);
        final EditText input_tweet = findViewById(R.id.ct_input_word);
        input_tweet.addTextChangedListener(this);
        //ラジオボタン処理
        final RadioGroup type_rgroup = findViewById(R.id.ct_inputType);
        type_rgroup.setOnCheckedChangeListener(radioChanged);
    }

    @Override
    public void onResume() {
        super.onResume();
        //画面の初期表示処理
        setDisplay();
    }

    /**
     * 画面表示処理
     */
    private void setDisplay(){
        //preferenceから設定値の呼び出し
        SharedPreferences preferences =
                this.getSharedPreferences(appSharedPrerence.PREF_NAME, Context.MODE_PRIVATE);
        //ツイート作成タイプ（自由入力orフォーム入力）
        String set_dispType =
                preferences.getString(appSharedPrerence.SET_DISPLAY_TYPE_TWEET_CREATE, twitterValue.DEFAULT_TYPE_OF_TWEET_CREATION);
        setDisplayType(set_dispType);
    }

    /**
     * タイプごとに画面を表示
     * @param set_dispType
     */
    private void setDisplayType(String set_dispType){
        RadioGroup type_rgroup = findViewById(R.id.ct_inputType);
        LinearLayout linear_form = findViewById(R.id.ct_formInput);
        LinearLayout linear_free = findViewById(R.id.ct_freeInput);

        if(set_dispType.equals(twitterValue.TYPE_OF_TWEET_CREATION_FREE)){
            type_rgroup.check(R.id.ct_free_radio);
            linear_form.setVisibility(View.GONE);
            linear_free.setVisibility(View.VISIBLE);
        }else{
            type_rgroup.check(R.id.ct_form_radio);
            linear_form.setVisibility(View.VISIBLE);
            linear_free.setVisibility(View.GONE);
        }
    }

    /**
     * 画面タイプ変更時
     */
    private final RadioGroup.OnCheckedChangeListener radioChanged = new RadioGroup.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            if(radioGroup.getCheckedRadioButtonId() == R.id.ct_free_radio) {
                setDisplayType(twitterValue.TYPE_OF_TWEET_CREATION_FREE);
            }else{
                setDisplayType(twitterValue.TYPE_OF_TWEET_CREATION_FORM);
            }
        }
    };

    /**
     * 投稿ボタン押下時
     */
    private final View.OnClickListener btnSentClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //文字数制限チェック
            TextView inputText = view.findViewById(R.id.ct_freeInput);
            String tweet = (String) inputText.getText();
            if (tweet.length() > twitterValue.CHARALIMIT_FREE) {
                return;
            }
            //ツイート投稿
            twitterUtils.tweet();
        }
    };

    @Override
    /**
     * コールバック関数
     * ツイート成功後
     */
    public void callBackTweeting(Boolean result) {
        if (checkViewDetach(this)) return;
        if (result) {
            //ツイート成功時、ホーム画面に戻る
            Intent intent = new Intent(getApplication(), MainActivity.class);
            startActivity(intent);
        } else {
            //ツイート失敗時、トースト表示
            Toast.makeText(this, "投稿に失敗しました。時間をおいて再度試してみて下さい。", Toast.LENGTH_LONG).show();
        }
        finish();
    }

    @Override
    /**
     * 文字入力時に呼び出される。
     * 文字数カウント用
     */
    public void afterTextChanged(Editable s) {
        // テキスト変更後に変更されたテキストを取り出す
        String inputStr = s.toString();
        TextView textCount = findViewById(R.id.ct_count_form);
        // 文字長をカウントして、制限文字数を超えると「オーバー」とする
        String str;
        if (inputStr.length() > twitterValue.CHARALIMIT_FREE) {
            str = "文字数オーバーです。　" + String.valueOf(inputStr.length()) + "／" + String.valueOf(twitterValue.CHARALIMIT_FREE);
            textCount.setText(str);
            textCount.setTextColor(Color.RED);
        } else {
            str = String.valueOf(inputStr.length()) + "／" + String.valueOf(twitterValue.CHARALIMIT_FREE);
            textCount.setText(str);
            textCount.setTextColor(Color.DKGRAY);
        }
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

}
