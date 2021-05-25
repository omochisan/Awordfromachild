package com.example.awordfromachild.tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.awordfromachild.R;
import com.example.awordfromachild.asynctask.callBacksAttention;
import com.example.awordfromachild.common.fragmentBase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import twitter4j.QueryResult;
import twitter4j.Status;

public class fragAttention extends fragmentBase implements callBacksAttention {
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragattention_layout,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void callBackTwitterLimit(int secondsUntilReset) {

    }

    @Override
    public void callBackStreamAddList(Status status) {

    }

    /**
     * コールバック
     * 非チェック例外発生時
     */
    @Override
    public void callBackException() {
        fail_result();
    }

    @Override
    public void callBackGetTweets(Object list, String howToDisplay) {

    }
}
