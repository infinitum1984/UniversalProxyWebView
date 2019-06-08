package com.infnitum.universal_proxy_webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.Toast;


public class MainActivity extends Activity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;


    public View mDecorView;

    ProxyData FireProxy;

    EditText ViewHost;
    EditText ViewPort;


    Switch aSwitch_proxy;


    static CheckBox is_refresh_swipe;

    private WebView webView;
    private FrameLayout customViewContainer;

    private WebChromeClient.CustomViewCallback customViewCallback;
    private View mCustomView;
    private myWebChromeClient mWebChromeClient;
    private myWebViewClient mWebViewClient;

    private EditText viewUrl;


    private static final String TAG = "myapp";
    private static String main_url="";
    static SwipeRefreshLayout Web_swipe_refresh;


    ClipboardManager clipboardManager;
    ClipData clipData;

    /**
     * Called when the activity is first created.
     */
    @SuppressLint({"WrongViewCast", "ClickableViewAccessibility"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Config.ConfigInit(this);



        //WebView
        webView = (WebView) findViewById(R.id.webView);
        mWebViewClient = new myWebViewClient();
        webView.setWebViewClient(mWebViewClient);
        mWebChromeClient = new myWebChromeClient();
        webView.setWebChromeClient(mWebChromeClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        WebSettings mWebSettings = webView.getSettings();
        mWebSettings.setAllowFileAccess(true);
        mWebSettings.setAllowFileAccess(true);
        mWebSettings.setAllowContentAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setWebContentsDebuggingEnabled(false);
        }


        //Proxy Info
        aSwitch_proxy = (Switch) findViewById(R.id.switch_is_use_proxy);
        aSwitch_proxy.setChecked(Config.SetProxy);
        aSwitch_proxy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!aSwitch_proxy.isChecked()){
                    Config.saveSetProxyState(getApplicationContext(), isChecked);
                    doRestart(getApplicationContext());
                    return;
                }

                if (aSwitch_proxy.isChecked() && !ViewHost.getText().toString().equals("") && !ViewHost.getText().toString().equals("")){
                    Config.saveSetProxyState(getApplicationContext(), isChecked);
                    doRestart(getApplicationContext());
                }else {
                    aSwitch_proxy.setChecked(!aSwitch_proxy.isChecked());
                }

            }
        });
        ViewHost = (EditText) findViewById(R.id.viewHost);
        ViewPort = (EditText) findViewById(R.id.viewPort);
        if (!Config.UserHost.equals("")) {
            ViewHost.setText(Config.UserHost);
            ViewPort.setText(String.valueOf(Config.UserPort));
        }


        //layouts

        customViewContainer = (FrameLayout) findViewById(R.id.customViewContainer);
        mDecorView = getWindow().getDecorView();

        //URL
        main_url=Config.URL;
        viewUrl = findViewById(R.id.viewUrl);
        viewUrl.setText(main_url);
        Log.d(TAG, "onCreate: mainurl "+main_url);


        //NavigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Swipe Refresh
        Web_swipe_refresh = findViewById(R.id.web_swipe_refresh_layout);
        is_refresh_swipe = findViewById(R.id.isRefresh_page_swipe);

        is_refresh_swipe.setChecked(Config.EnableSwipeRefresh);
        Web_swipe_refresh.setEnabled(Config.EnableSwipeRefresh);

        is_refresh_swipe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Config.saveEnableSwipeRefresh(getApplicationContext(), isChecked);
                Web_swipe_refresh.setEnabled(isChecked);
            }
        });
        Web_swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh_page();
            }
        });
        Web_swipe_refresh.setRefreshing(true);


        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (Config.SetProxy) {

            if (!Config.UserHost.equals("")) {
                Proxy.setProxy(Config.UserHost, Config.UserPort, webView, getApplicationContext());
            }
        }
        webView.loadUrl(main_url);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else
            Toast.makeText(getApplicationContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        webView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();    //To change body of overridden methods use File | Settings | File Templates.
        if (inCustomView()) {
            hideCustomView();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (inCustomView()) {
                hideCustomView();
                return true;
            }

            if ((mCustomView == null) && webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }




    public void ApplyChanges(View view) {
//        if (ViewHost.getText().equals("") && (ViewPort.getText().equals(""))) {
//            Config.deleteUserProxy(getApplicationContext());
//            doRestart(getApplicationContext());
//            return;
//        }

        if ((!ViewHost.getText().equals("")) && (!ViewPort.getText().equals("")) ) {
            ProxyData p = new ProxyData();
            p.Host = ViewHost.getText().toString();
            try {
                p.Port = Integer.parseInt(String.valueOf(ViewPort.getText()));

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.Input_error), Toast.LENGTH_SHORT).show();
                return;
            }
            Config.saveSetProxyState(getApplicationContext(),true);
            Config.saveUserProxy(getApplicationContext(), p);
            doRestart(getApplicationContext());


        }
    }

    public void ToDefault(View view) {
        Config.deleteUserProxy(getApplicationContext());
        Config.saveSetProxyState(getApplicationContext(), false);
        doRestart(getApplicationContext());
    }

    public void WebBack(View view) {

        if (webView.canGoBack()){
            webView.goBack();
        }
    }




    public void CopyLink(View view) {
        clipData = ClipData.newPlainText("text", webView.getUrl());
        clipboardManager.setPrimaryClip(clipData);

        Toast.makeText(getApplicationContext(), this.getString(R.string.link_copied), Toast.LENGTH_SHORT).show();

    }

    public void RefreshPage(View view) {
        refresh_page();

    }

    public void refresh_page() {
        Web_swipe_refresh.setRefreshing(true);
        webView.reload();
    }


    // Прячем панель навигации и строку состояния
    private void hideSystemUI() {
        // Используем флаг IMMERSIVE.


        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // прячем панель навигации
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // прячем строку состояния

                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY


        );

        Web_swipe_refresh.setEnabled(false);
        Web_swipe_refresh.setVisibility(View.GONE);

    }

    // Программно выводим системные панели обратно
    private void showSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // прячем панель навигации
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // прячем строку состояния

                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY


        );

        Web_swipe_refresh.setEnabled(Config.EnableSwipeRefresh);

        Web_swipe_refresh.setVisibility(View.VISIBLE);
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        mDecorView.setSystemUiVisibility(uiOptions);
        uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        mDecorView.setSystemUiVisibility(uiOptions);


    }


    public static void doRestart(Context c) {
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                    }
                } else {
                }
            } else {
            }
        } catch (Exception ex) {
        }
    }


    public boolean inCustomView() {
        return (mCustomView != null);
    }

    public void hideCustomView() {
        mWebChromeClient.onHideCustomView();
    }

    public void loadUrl(View view) {
        Web_swipe_refresh.setRefreshing(true);
        Config.saveUrl(getApplicationContext(),viewUrl.getText().toString());
        webView.loadUrl(viewUrl.getText().toString());


    }



    class myWebChromeClient extends WebChromeClient {
        private Bitmap mDefaultVideoPoster;
        private View mVideoProgressView;

        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            onShowCustomView(view, callback);    //To change body of overridden methods use File | Settings | File Templates.

        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {

            // if a view already exists then immediately terminate the new one

            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            mCustomView = view;
            webView.setVisibility(View.GONE);
            customViewContainer.setVisibility(View.VISIBLE);
            customViewContainer.addView(view);
            customViewCallback = callback;
            hideSystemUI();

        }

        @Override
        public View getVideoLoadingProgressView() {

            if (mVideoProgressView == null) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                mVideoProgressView = inflater.inflate(R.layout.video_progress, null);
            }
            return mVideoProgressView;
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();


            //To change body of overridden methods use File | Settings | File Templates.
            if (mCustomView == null)
                return;

            customViewContainer.setVisibility(View.GONE);

            // Hide the custom view.

            // Remove the custom view from its container
            //
            mCustomView.setVisibility(View.GONE);

            customViewContainer.removeView(mCustomView);
            customViewCallback.onCustomViewHidden();

            mCustomView = null;

            webView.setVisibility(View.VISIBLE);


            showSystemUI();

        }

        // For 3.0+ Devices (Start)
        // onActivityResult attached before constructor
        protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            showUploadDialog(uploadMsg);
        }


        // For Lollipop 5.0+ Devices
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {


            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }

            uploadMessage = filePathCallback;

            Intent intent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                intent = fileChooserParams.createIntent();
            }
            try {
                startActivityForResult(intent, REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e) {
                uploadMessage = null;
                return false;
            }
            return true;
        }

        //For Android 4.1 only
        protected void openFileChooser(final ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            showUploadDialog(uploadMsg);
            return;
        }

        protected void openFileChooser(final ValueCallback<Uri> uploadMsg) {
            showUploadDialog(uploadMsg);
            return;

        }

        void showUploadDialog(final ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
        }


    }


    class myWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            Config.saveUrl(getApplicationContext(),url);

            Web_swipe_refresh.setRefreshing(false);
            viewUrl.setText(url);


        }


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

                return super.shouldOverrideUrlLoading(view, url);

        }

    }

}