package com.infnitum.universal_proxy_webview;

import android.content.Context;

public class Config {

    public static String UserHost="";
    public static int UserPort;

    public static boolean SetProxy;
    public static boolean EnableSwipeRefresh;

    public static String URL="";

    private final static String set_proxy_key="is_set_Proxy";
    private final static String enable_swipe_refresh_key="Is_refresh_page_down_swipe";
    private final static String user_host_key="Host";
    private final static String user_port_key="Port";
    private final static String url_key="Url";


    public static void ConfigInit(Context c){
        initUserProxy(c);
        initEnableSwipeRefresh(c);
        initSetProxyState(c);
        initUrl(c);
    }


    //URL
    public static void saveUrl(Context c, String s){
        SharedPreferencesData.save_data(c,url_key,s);
        URL=s;
    }
    public static void initUrl(Context c){
        URL=SharedPreferencesData.get_data(c,url_key,"https://google.com");
    }

    //SetProxy bool
    public static void saveSetProxyState(Context c, boolean b){
        SharedPreferencesData.save_data(c,set_proxy_key,b);
        SetProxy=b;
    }
    public static void initSetProxyState(Context c){
        SetProxy=SharedPreferencesData.get_data(c,set_proxy_key,false);
    }

    //EnableSwipeRefresh
    public static void saveEnableSwipeRefresh(Context c, boolean b){
        SharedPreferencesData.save_data(c,enable_swipe_refresh_key,b);
        EnableSwipeRefresh=b;
    }
    public static void initEnableSwipeRefresh(Context c){
        EnableSwipeRefresh=SharedPreferencesData.get_data(c,enable_swipe_refresh_key,true);
    }


    //User Proxy

    public static void initUserProxy(Context c){
        UserHost = SharedPreferencesData.get_data(c,user_host_key,"");
        UserPort=SharedPreferencesData.get_data(c,user_port_key,0);
    }
    public static void saveUserProxy(Context c, String host, int port){
        SharedPreferencesData.save_data(c,user_host_key,host);
        SharedPreferencesData.save_data(c,user_port_key,port);
    }
    public static void  deleteUserProxy(Context c){
        SharedPreferencesData.delete_data(c,user_host_key);
        SharedPreferencesData.delete_data(c,user_port_key);
        SharedPreferencesData.delete_data(c,set_proxy_key);

    }



}
