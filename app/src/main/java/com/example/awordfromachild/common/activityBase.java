package com.example.awordfromachild.common;

import android.app.Activity;

import java.lang.ref.WeakReference;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class activityBase extends AppCompatActivity {
    WeakReference<Activity> weak_activity;

    public Boolean checkViewDetach(Activity base){
        weak_activity = new WeakReference<Activity>(base);
        Activity activity = weak_activity.get();
        if (activity.isDestroyed()){
            return true;
        }else{
            return false;
        }
    }

}
