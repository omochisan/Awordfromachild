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
import com.example.awordfromachild.constant.appSharedPreferences;
import com.example.awordfromachild.constant.twitterValue;

import java.util.Objects;

import twitter4j.Status;

/**
 * 「こどものひとことをツイート」画面
 */
public class CreateTweetActivity extends activityBase implements callBacksCreateTweet {
    private static TwitterUtils.tweet tweet;
    /**
     * 画面タイプ変更時
     */
    private final RadioGroup.OnCheckedChangeListener radioChanged = (radioGroup, i) -> {
        if (radioGroup.getCheckedRadioButtonId() == R.id.ct_free_radio) {
            setDisplayType(twitterValue.createTweetValue.TYPE_OF_TWEET_CREATION_FREE);
        } else {
            setDisplayType(twitterValue.createTweetValue.TYPE_OF_TWEET_CREATION_FORM);
        }
    };
    /**
     * 投稿ボタン押下時
     */
    private final View.
            OnClickListener btnSentClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String text = "";
            TextView vt;
            RadioGroup type_rGroup = findViewById(R.id.ct_inputType);
            int checked = type_rGroup.getCheckedRadioButtonId();
            //自由入力
            if (checked == R.id.ct_form_radio) {
                EditText edit_when = findViewById(R.id.ct_input_when);
                String str_when = (edit_when.getText()).toString();
                EditText edit_where = findViewById(R.id.ct_input_where);
                String str_where = (edit_where.getText()).toString();
                EditText edit_how = findViewById(R.id.ct_input_how);
                String str_how = (edit_how.getText()).toString();
                EditText edit_word = findViewById(R.id.ct_input_word);
                String str_word = (edit_word.getText()).toString();
                text = str_when + str_where + str_how + str_word;
            } else if (checked == R.id.ct_free_radio) {
                vt = findViewById(R.id.ct_input_free);
                text = (vt.getText()).toString();
            }
            //ツイート投稿
            tweet.setText(text);
            tweet.execute();
        }
    };
    /**
     * フォーム入力監視
     */
    private final TextWatcher editText = new TextWatcher() {
        /**
         * 文字入力時に呼び出される。
         * 文字数カウント用
         */
        @Override
        public void afterTextChanged(Editable s) {
            TextView textCount = null;
            int all_len_count = 0;
            RadioGroup type_rGroup = findViewById(R.id.ct_inputType);
            int checked = type_rGroup.getCheckedRadioButtonId();
            //自由入力
            if (checked == R.id.ct_free_radio) {
                textCount = findViewById(R.id.ct_count_free);
                EditText vt = findViewById(R.id.ct_input_free);
                all_len_count = (vt.getText()).toString().length();
            } else if (checked == R.id.ct_form_radio) {
                textCount = findViewById(R.id.ct_count_form);
                //全フォーム項目から入力文字取得
                EditText edit_when = findViewById(R.id.ct_input_when);
                String str_when = (edit_when.getText()).toString();
                EditText edit_where = findViewById(R.id.ct_input_where);
                String str_where = (edit_where.getText()).toString();
                EditText edit_how = findViewById(R.id.ct_input_how);
                String str_how = (edit_how.getText()).toString();
                EditText edit_word = findViewById(R.id.ct_input_word);
                String str_word = (edit_word.getText()).toString();
                // 文字長をカウントして、制限文字数を超えると「オーバー」とする
                all_len_count =
                        str_when.length() + str_where.length() + str_how.length() + str_word.length();
            }

            String str;
            if (all_len_count > twitterValue.createTweetValue.CHARALIMIT_FREE) {
                btnEnableSwitch(false);
                str = "文字数オーバーです。　" + all_len_count + "／"
                        + twitterValue.createTweetValue.CHARALIMIT_FREE;
                textCount.setText(str);
                textCount.setTextColor(Color.RED);
            } else {
                btnEnableSwitch(true);
                str = all_len_count + "／"
                        + twitterValue.createTweetValue.CHARALIMIT_FREE;
                Objects.requireNonNull(textCount).setText(str);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.createtweet_layout);
        super.onCreate(savedInstanceState);
        tweet = new TwitterUtils.tweet(this);
        //画面の初期表示処理
        setDisplay();

        //ボタン処理
        final Button btn_sent = findViewById(R.id.ct_btn_sent);
        btn_sent.setOnClickListener(btnSentClick);

        //入力監視（フォーム入力）
        final EditText input_when = findViewById(R.id.ct_input_when);
        input_when.addTextChangedListener(editText);
        final EditText input_where = findViewById(R.id.ct_input_where);
        input_where.addTextChangedListener(editText);
        final EditText input_how = findViewById(R.id.ct_input_how);
        input_how.addTextChangedListener(editText);
        final EditText input_word = findViewById(R.id.ct_input_word);
        input_word.addTextChangedListener(editText);
        //入力監視（自由入力）
        final EditText input_tweet_free = findViewById(R.id.ct_input_free);
        input_tweet_free.addTextChangedListener(editText);

        //ラジオボタン処理
        final RadioGroup type_rGroup = findViewById(R.id.ct_inputType);
        type_rGroup.setOnCheckedChangeListener(radioChanged);
    }

    /**
     * 画面表示処理
     */
    private void setDisplay() {
        //preferenceから設定値の呼び出し
        SharedPreferences preferences =
                this.getSharedPreferences(appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        //ツイート作成タイプ（自由入力orフォーム入力）
        String set_displayType =
                preferences.getString(appSharedPreferences.SET_DISPLAY_TYPE_TWEET_CREATE, twitterValue.createTweetValue.DEFAULT_TYPE_OF_TWEET_CREATION);
        setDisplayType(set_displayType);
    }

    /**
     * タイプごとに画面を表示
     *
     * @param set_displayType 画面タイプ
     */
    private void setDisplayType(String set_displayType) {
        RadioGroup type_rGroup = findViewById(R.id.ct_inputType);
        LinearLayout linear_form = findViewById(R.id.ct_formInput);
        LinearLayout linear_free = findViewById(R.id.ct_freeInput);

        //自由入力
        if (set_displayType.equals(twitterValue.createTweetValue.TYPE_OF_TWEET_CREATION_FREE)) {
            type_rGroup.check(R.id.ct_free_radio);
            TextView count_free = findViewById(R.id.ct_count_free);
            String text = "0／" + twitterValue.createTweetValue.CHARALIMIT_FREE;
            count_free.setText(text);
            linear_form.setVisibility(View.GONE);
            linear_free.setVisibility(View.VISIBLE);
        } else { //フォーム入力
            type_rGroup.check(R.id.ct_form_radio);
            TextView count_form = findViewById(R.id.ct_count_form);
            String text = "0／" + twitterValue.createTweetValue.CHARALIMIT_FORM;
            count_form.setText(text);
            linear_form.setVisibility(View.VISIBLE);
            linear_free.setVisibility(View.GONE);
        }
    }

    /**
     * ボタン活性／非活性切替
     *
     * @param enable 活性／非活性
     */
    private void btnEnableSwitch(boolean enable) {
        Button btn_sent = findViewById(R.id.ct_btn_sent);
        btn_sent.setEnabled(enable);
    }

    /**
     * コールバック関数
     * ツイート成功後
     */
    @Override
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
