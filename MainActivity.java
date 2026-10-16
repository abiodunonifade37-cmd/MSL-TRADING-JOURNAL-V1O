package com.msl.tradingjournal;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.view.View;
import android.view.Window;
import android.graphics.Color;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private WebView web;
    private static final int REQ_NOTIFICATIONS = 301;
    // Update this URL only if the GitHub Pages project URL changes.
    private static final String JOURNAL_URL = "https://abiodunonifade37-cmd.github.io/MSL-TRADING-JOURNAL-V/";

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(Color.rgb(7,11,18));
        getWindow().setNavigationBarColor(Color.rgb(7,11,18));
        if (Build.VERSION.SDK_INT >= 29) getWindow().getDecorView().setSystemUiVisibility(0);
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(7,11,18));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setDatabaseEnabled(true);
        web.getSettings().setLoadsImagesAutomatically(true);
        web.getSettings().setMediaPlaybackRequiresUserGesture(true);
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient());
        web.addJavascriptInterface(new AlarmBridge(), "AndroidAlarm");
        setContentView(web);
        AlarmScheduler.ensureChannel(this);
        web.loadUrl(JOURNAL_URL);
    }

    public class AlarmBridge {
        @JavascriptInterface public void scheduleAlarms(String json) {
            try {
                JSONArray alarms = new JSONArray(json);
                getSharedPreferences(AlarmScheduler.PREFS, MODE_PRIVATE).edit().putString("alarms", alarms.toString()).apply();
                AlarmScheduler.scheduleAll(MainActivity.this, alarms);
            } catch (Exception e) { runOnUiThread(() -> Toast.makeText(MainActivity.this,"Could not schedule session reminders",Toast.LENGTH_SHORT).show()); }
        }
        @JavascriptInterface public void requestPermissions() {
            runOnUiThread(() -> {
                if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    MainActivity.this.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATIONS);
                    return;
                }
                openExactAlarmSettingsIfNeeded();
            });
        }
        @JavascriptInterface public boolean isNative() { return true; }
    }

    private void openExactAlarmSettingsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 31 && !getSystemService(android.app.AlarmManager.class).canScheduleExactAlarms()) {
            try { startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName()))); }
            catch (Exception ignored) { startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()))); }
        }
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIFICATIONS) openExactAlarmSettingsIfNeeded();
    }
    @Override protected void onResume() { super.onResume(); if (web != null) web.evaluateJavascript("try{if(typeof syncNativeAlarms==='function')syncNativeAlarms();if(typeof updateNotificationStatus==='function')updateNotificationStatus()}catch(e){}", null); }
    @Override public void onBackPressed() { if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
