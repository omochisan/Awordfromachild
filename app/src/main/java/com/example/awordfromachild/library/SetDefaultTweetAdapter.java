package com.example.awordfromachild.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

import com.example.awordfromachild.*;

import twitter4j.ResponseList;
import twitter4j.Status;

public class SetDefaultTweetAdapter extends ArrayAdapter<twitter4j.Status> {
    private int mResource;
    private List<twitter4j.Status> mItems;
    private LayoutInflater mInflater;

    /**
     * コンストラクタ
     * @param context コンテキスト
     * @param resource リソースID
     * @param items リストビューの要素
     */
    public SetDefaultTweetAdapter(Context context, int resource, ResponseList<twitter4j.Status> items) {
        super(context, resource, items);

        mResource = resource;
        mItems = items;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * リストにセットするツイート一覧を追加設定。
     * @param items
     */
    public void addItems(ResponseList<twitter4j.Status> items){
        mItems.addAll(items);
    }


    /**
     * 画面をスクロールし、新しい一行が表示されるたびに呼ばれ、1行のUIを作成する。
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        //再利用できるviewがある場合はそれを使う
        //（表示領域から消えた1行は、スクロールして表示されようとする次の新しい行に対して
        // インスタンスを再利用しようとする）
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(mResource, null);
        }

        // リストビューに表示する要素を取得
        twitter4j.Status item = mItems.get(position);

        // ユーザーアイコンを設定
        ImageView userIcon = (ImageView)view.findViewById(R.id.tw_userIcon);
        String getUrl = item.getUser().getProfileImageURLHttps();
        GlideApp.with(view)
                .load(getUrl)
                .circleCrop()
                .into(userIcon);

        //ツイートIDを取得
        //long tweetID = item.getId();
        //ユーザー名を設定
        TextView userName = (TextView)view.findViewById(R.id.tw_userName);
        userName.setText(item.getUser().getName());
        //ユーザーIDを設定
        TextView userID = (TextView)view.findViewById(R.id.tw_userID);
        userID.setText(item.getUser().getScreenName());
        // ツイートを設定
        TextView tweet = (TextView)view.findViewById(R.id.tw_main);
        tweet.setText(item.getText());
        //お気に入りを設定
        TextView favo = (TextView)view.findViewById(R.id.tw_like);
        favo.setText(String.valueOf(item.getFavoriteCount()));
        //リツイートを設定
        TextView retweet = (TextView)view.findViewById(R.id.tw_retweet);
        retweet.setText(String.valueOf(item.getRetweetCount()));
        //リプライを取得・設定
        /*Status s = twitter.showStatus(status.getInReplyToStatusId());
        TextView favo = (TextView)view.findViewById(R.id.tw_like);
        favo.setText(item.getFavoriteCount());*/

        return view;
    }
}
