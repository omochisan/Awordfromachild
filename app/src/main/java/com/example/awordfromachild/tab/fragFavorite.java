package com.example.awordfromachild.tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.awordfromachild.R;
import com.example.awordfromachild.asynctask.callBacksFavorite;
import com.example.awordfromachild.common.fragmentBase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import twitter4j.Status;

public class fragFavorite extends fragmentBase implements callBacksFavorite {
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fraggood_layout,container,false);
    }

    @Override
    public void callBackTwitterLimit(int secondsUntilReset) {

    }

    @Override
    public void callBackStreamAddList(Status status) {

    }

    @Override
    public void callBackException() {

    }
}