package dem.vaccae.autoupgradedemo.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Vaccae on 2016-12-06.
 * 获取Retrofit类用于Http通信
 */

public class retrofitAPIManager<T> {
    //基本URL地址
    public static String SERVER_URL = "url";
    //Cookies类型  0-每次注册时登记   1-按每次访问的URL登记
    public static int Cookiestype = 0;
    //Cookies类型如果为每次注册登记时用到检索关键前
    public static String Cookiecontains = "login";
    //Cookies类型如果为每次注册登记时用到Key
    public static String CookiesKey = "SumSoft";

    public static<T> T provideClientApi(Class<T> tClass) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(genericClient())
                .build();
        return retrofit.create(tClass);
    }

    //获取OkHttpClient
    public static OkHttpClient genericClient() {
        OkHttpClient httpClient=new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    private final HashMap<String, List<Cookie>> cookieStore=new HashMap<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        //根据类型来判断存入的Cookies用于后面读取用
                        if (Cookiestype == 0) {
                            //判断url里面是注册的更新Key
                            if (url.toString().contains(Cookiecontains)) {
                                cookieStore.put(CookiesKey, cookies);
                            }
                        } else {
                            cookieStore.put(url.toString(), cookies);
                        }

                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies;
                        if (Cookiestype == 0) {
                            cookies=cookieStore.get(CookiesKey);
                        } else {
                            cookies=cookieStore.get(url.toString());
                        }
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                })
                .connectTimeout(1000, TimeUnit.MILLISECONDS)
                .build();

        return httpClient;
    }
}