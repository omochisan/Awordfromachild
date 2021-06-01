package com.example.awordfromachild;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
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
import com.example.awordfromachild.constant.appSharedPreferences;
import com.example.awordfromachild.constant.twitterValue;

import twitter4j.QueryResult;
import twitter4j.Status;

/**
 * 「こどものひとことをツイート」画面
 */
public class CreateTweetActivity extends activityBase implements callBacksCreateTweet {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.createtweet_layout);
        super.onCreate(savedInstanceState);
        //画面の初期表示処理
        setDisplay();

        //ボタン処理
        final Button btn_sent = findViewById(R.id.ct_btn_sent);
        btn_sent.setOnClickListener(btnSentClick);

        //入力監視（フォーム入力）
        final EditText input_when = findViewById(R.id.ct_input_when);
        input_when.addTextChangedListener(editTextForm);
        final EditText input_where = findViewById(R.id.ct_input_where);
        input_where.addTextChangedListener(editTextForm);
        final EditText input_how = findViewById(R.id.ct_input_how);
        input_how.addTextChangedListener(editTextForm);
        final EditText input_word = findViewById(R.id.ct_input_word);
        input_word.addTextChangedListener(editTextForm);
        //入力監視（自由入力）
        final EditText input_tweet_free = findViewById(R.id.ct_input_free);
        //input_tweet_free.addTextChangedListener(editTextFree);

        //ラジオボタン処理
        final RadioGroup type_rgroup = findViewById(R.id.ct_inputType);
        type_rgroup.setOnCheckedChangeListener(radioChanged);
    }

    /**
     * フォーム入力監視
     */
    private TextWatcher editTextForm = new TextWatcher() {
        @Override
        /**
         * 文字入力時に呼び出される。
         * 文字数カウント用
         */
        public void afterTextChanged(Editable s) {
            TextView textCount = findViewById(R.id.ct_count_form);
            //全フォーム項目から入力文字取得
            EditText edit_when = findViewById(R.id.ct_input_when);
            String str_when = ((SpannableStringBuilder)edit_when.getText()).toString();
            EditText edit_where = findViewById(R.id.ct_input_where);
            String str_where = ((SpannableStringBuilder)edit_where.getText()).toString();
            EditText edit_how = findViewById(R.id.ct_input_how);
            String str_how = ((SpannableStringBuilder)edit_how.getText()).toString();
            EditText edit_word = findViewById(R.id.ct_input_word);
            String str_word = ((SpannableStringBuilder)edit_word.getText()).toString();
            // 文字長をカウントして、制限文字数を超えると「オーバー」とする
            int all_len_count =
                    str_when.length() + str_where.length() + str_how.length() + str_word.length();
            String str;
            if (all_len_count > twitterValue.CHARALIMIT_FREE) {
                str = "文字数オーバーです。　" + String.valueOf(all_len_count) + "／"
                        + String.valueOf(twitterValue.CHARALIMIT_FREE);
                textCount.setText(str);
                textCount.setTextColor(Color.RED);
            } else {
                str = String.valueOf(all_len_count) + "／"
                        + String.valueOf(twitterValue.CHARALIMIT_FREE);
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
    };

    /**
     * 画面表示処理
     */
    private void setDisplay(){
        //preferenceから設定値の呼び出し
        SharedPreferences preferences =
                this.getSharedPreferences(appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        //ツイート作成タイプ（自由入力orフォーム入力）
        String set_dispType =
                preferences.getString(appSharedPreferences.SET_DISPLAY_TYPE_TWEET_CREATE, twitterValue.DEFAULT_TYPE_OF_TWEET_CREATION);
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

        //自由入力
        if(set_dispType.equals(twitterValue.TYPE_OF_TWEET_CREATION_FREE)){
            type_rgroup.check(R.id.ct_free_radio);
            TextView count_free = findViewById(R.id.ct_count_free);
            count_free.setText("0／" + String.valueOf(twitterValue.CHARALIMIT_FREE));
            linear_form.setVisibility(View.GONE);
            linear_free.setVisibility(View.VISIBLE);
        }else{ //フォーム入力
            type_rgroup.check(R.id.ct_form_radio);
            TextView count_form = findViewById(R.id.ct_count_form);
            count_form.setText("0／" + String.valueOf(twitterValue.CHARALIMIT_FORM));
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
    private final View.
            OnClickListener btnSentClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //ツイート投稿
            twitterUtils.tweet();
        }
    };

    /**
     * 文字数チェック&オーバー時処理
     * @return
     */
    private void checkCharaCount(EditText editText, String type){
        String tweet = String.valueOf(editText.getText());
        int countLimit = 0;
        if(type.equals(twitterValue.TYPE_OF_TWEET_CREATION_FREE)){
            countLimit = twitterValue.CHARALIMIT_FREE;
        }else {
            countLimit = twitterValue.CHARALIMIT_FORM;
        }
        //文字数オーバーの場合、投稿ボタン非活性
        Button btn_sent = findViewById(R.id.ct_btn_sent);
        if (tweet.length() > countLimit) {
            btn_sent.setEnabled(false);
        }else{
            btn_sent.setEnabled(true);
        }
    }

    @Override
    /**
     * コールバック関数
     * ツイート成功後
     */
    public void callBackTweeting(Status status) {
        if (checkViewDetach(this)) return;
        if (status != null) {
            //ツイート成功時、ホーム画面に戻る
            Intent intent = new Intent(getApplication(), MainActivity.class);
            startActivity(intent);
        } else {
            //ツイート失敗時、トースト表示
            Toast.makeText(this, "投稿に失敗しました。時間をおいて再度試してみて下さい。", Toast.LENGTH_LONG).show();
        }
        finish();
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

    @Override
    public void callBackException() {
        fail_result();
    }

    @Override
    public void callBackGetTweets(Object list, String howToDisplay) {

    }
}
