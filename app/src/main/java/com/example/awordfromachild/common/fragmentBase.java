package com.example.awordfromachild.common;

import java.lang.ref.WeakReference;
import androidx.fragment.app.Fragment;

public class fragmentBase extends Fragment {
    WeakReference<Fragment> weak_fragment;

    public Boolean checkViewDetach(Fragment base){
        weak_fragment = new WeakReference<Fragment>(base);
        Fragment fragment = weak_fragment.get();
        if (fragment.isDetached() || fragment.getActivity() == null){
            return true;
        }else{
            return false;
        }
    }
}
