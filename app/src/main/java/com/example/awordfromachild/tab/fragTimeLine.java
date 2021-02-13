package com.example.awordfromachild.tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.example.awordfromachild.R;
import com.example.awordfromachild.TwitterUtils;

public class fragTimeLine extends Fragment {
    //Twitter
    private Twitter twitter;
    //Twitter処理クラス
    private TwitterUtils twitterUtils;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragtimeline_layout,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        twitter = TwitterUtils.getTwitterInstance(getContext());

    }
}