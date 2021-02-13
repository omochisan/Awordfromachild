package com.example.awordfromachild;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CreateTweetActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ヘッダー
        TextView title = findViewById(R.id.hd_dispTitle);
        title.setText(R.string.hd_title_tweet);
    }
}
