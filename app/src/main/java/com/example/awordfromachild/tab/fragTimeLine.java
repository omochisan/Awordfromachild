package com.example.awordfromachild.tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import twitter4j.ResponseList;
import twitter4j.Status;

import com.example.awordfromachild.R;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksTimeLine;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.timelineType;

public class fragTimeLine extends fragmentBase implements callBacksTimeLine {
    //Twitter処理クラス
    private TwitterUtils twitterUtils;
    //画面
    public View ft_view;

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
        ft_view = view;
        twitterUtils = new TwitterUtils(this);
        twitterUtils.setTwitterInstance(getContext());

        //タイムライン取得
        twitterUtils.getTimeLine(timelineType.HOME);
    }

    /**
     * コールバック
     * タイムライン取得後
     */
    @Override
    public void callBackGetTimeLine(ResponseList<Status> result){
        LinearLayout view_result = ft_view.findViewById(R.id.tl_main);
        for (Status status : result) {
            //つぶやきのユーザーIDの取得
            String userName = status.getUser().getScreenName();
            //つぶやきの取得
            String text = status.getText();
            // TextView インスタンス生成
            TextView textView = new TextView(getContext());
            textView.setText(text);
            textView.setBackgroundResource(R.drawable.box_lightgray);
            view_result.addView(textView,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));

        }
    }
}