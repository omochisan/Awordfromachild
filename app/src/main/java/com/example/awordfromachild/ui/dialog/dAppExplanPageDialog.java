package com.example.awordfromachild.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.example.awordfromachild.R;

import java.util.Objects;

import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

public class dAppExplanPageDialog extends DialogFragment {

    /**
     * 閉じる押下時
     */
    private final View.OnClickListener closeClick = view -> dismiss();

    public static dAppExplanPageDialog newInstance() {
        return new dAppExplanPageDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.d_appexplain_common_layout,
                container, false);

        final ViewPager2 viewPager = view.findViewById(R.id.dap_common);
        final DialogPagePagerAdapter adapter = new DialogPagePagerAdapter(getActivity());
        viewPager.setAdapter(adapter);

        Objects.requireNonNull(getDialog()).getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        Button btn_close = view.findViewById(R.id.d_ap_close);
        btn_close.setOnClickListener(closeClick);

        return view;
    }
}
