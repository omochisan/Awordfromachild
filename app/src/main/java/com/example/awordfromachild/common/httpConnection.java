package com.example.awordfromachild.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.example.awordfromachild.ApplicationController;
import com.example.awordfromachild.constant.appSharedPreferences;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import androidx.annotation.RequiresApi;

/**
 * HTTP接続実行クラス
 */
public class httpConnection {
    private static final String HMAC_SHA1 = "HmacSHA1";
    final Random RAND = new Random();
    final String consumerKey = "hIZujeQjS8pZrPokAKhOyGqbJ";
    final String consumerSecret = "dCTcMVcmfSbMOlgI5Tv9bMZetviqX9WHrJqEOGXxjTyOJ2xxln";
    final String token;
    final String tokenSecret;
    final long timestamp = System.currentTimeMillis() / 1000;
    final long nonce = timestamp + RAND.nextInt();

    public httpConnection() {
        SharedPreferences preferences = ApplicationController.getInstance().getApplicationContext()
                .getSharedPreferences(appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        token = preferences.getString(appSharedPreferences.TOKEN, null);
        tokenSecret = preferences.getString(appSharedPreferences.TOKEN_SECRET, null);
    }

    // HeaderのAuthorizationに設定する文字列を生成
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String generateHeaderString(Map<String, String> reqParams, String requestMethod, String requestUrl) throws UnsupportedEncodingException {
        // 署名用キー生成
        SecretKeySpec secretKeySpec = generateKeySpec(tokenSecret);

        // データ：OAuthパラメータ
        Map<String, String> OAuthParams = new HashMap<>();
        OAuthParams.put("oauth_token", urlEncode(token));
        OAuthParams.put("oauth_consumer_key", urlEncode(consumerKey));
        OAuthParams.put("oauth_signature_method", "HMAC-SHA1");
        OAuthParams.put("oauth_timestamp", Long.toString(timestamp));
        OAuthParams.put("oauth_nonce", Long.toString(nonce));
        OAuthParams.put("oauth_version", "1.0");

        // データ:OAuthパラメータとリクエストパラメータを結合
        Map<String, String> allParams = new HashMap<>();
        allParams.putAll(castQueryMap(reqParams));
        allParams.putAll(OAuthParams);

        // key=val＆key=val・・・の文字列に
        String allParamsStr = convertParamsMapToParamsString(allParams);

        // 署名用データを生成
        // リクエストメソッド + URL + パラメータ

        String OAuthBase = requestMethod + "&" +
                urlEncode(requestUrl) + "&" +
                urlEncode(allParamsStr);

        @SuppressLint({"NewApi", "LocalSuppress"})
        String signature = generateSignature(secretKeySpec, OAuthBase);
        // Mapに署名を追加
        allParams.put("oauth_signature", urlEncode(signature));

        return convertParamsMapToHeaderString(allParams);

    }

    /**
     * クエリ文字列を変換
     *
     * @param target_map クエリ文字列
     * @return 変換後
     * @throws UnsupportedEncodingException エンコードエラー
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public Map<String, String> castQueryMap(Map<String, String> target_map) throws UnsupportedEncodingException {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> parameter : target_map.entrySet()) {
            result.put(parameter.getKey(), URLEncoder.encode(parameter.getValue(), "UTF-8"));
        }
        return result;
    }

    // ベースURLとパラメータからURIを生成
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String createUrlString(String baseUrl, Map<String, String> params) {
        String paramString = params.entrySet().stream()
                .map(entry -> {
                    try {
                        return entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException ignored) {
                    }
                    return null;
                })
                .collect(Collectors.joining("&"));
        return baseUrl + "?" + paramString;

    }

    // 署名生成する用のキーを生成
    public SecretKeySpec generateKeySpec(String accessTokenSecret) {
        // コンシューマーキーとアクセストークン シークレットを結合した文字列からキーを生成
        String keyString = urlEncode(consumerSecret) + "&" + urlEncode(accessTokenSecret);
        return new SecretKeySpec(keyString.getBytes(), HMAC_SHA1);
    }

    // 署名を作成
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String generateSignature(SecretKeySpec OAuthKeySpec, String OAuthData) {
        byte[] byteHMAC = null;
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(OAuthKeySpec);

            // byte配列で署名を作成
            byteHMAC = mac.doFinal(OAuthData.getBytes());

        } catch (InvalidKeyException | NoSuchAlgorithmException ike) {
            ike.printStackTrace();
        }
        // Base64エンコード
        return Base64.getEncoder().encodeToString(byteHMAC);
    }

    // 署名生成時のデータ生成用
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String convertParamsMapToParamsString(Map<String, String> map) {
        String result;

        // Mapをkeyでソートして key1=value1&key2=value2&・・・形式の文字列にする
        result = map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        return result;
    }

    // ヘッダに埋め込む文字列を生成
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String convertParamsMapToHeaderString(Map<String, String> map) {
        String result = "OAuth ";
        result += map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","));
        return result;
    }


    // URL エンコーディング
    public String urlEncode(String value) {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
        }
        StringBuilder buf = new StringBuilder(Objects.requireNonNull(encoded).length());
        char focus;
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i);
            // 特殊文字をフォロー
            // *変換
            if (focus == '*') {
                buf.append("%2A");
                // +変換
            } else if (focus == '+') {
                buf.append("%20");
                // %7E ~ 変換
            } else if (focus == '%' && (i + 1) < encoded.length()
                    && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
                buf.append('~');
                i += 2;
            } else {
                buf.append(focus);
            }
        }
        return buf.toString();
    }
}
