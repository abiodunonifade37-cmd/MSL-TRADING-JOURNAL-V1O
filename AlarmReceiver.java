package com.msl.tradingjournal;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("name"); if (name == null) name = "Trading session";
        String time = intent.getStringExtra("time");
        AlarmScheduler.ensureChannel(context);
        Intent open = new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(context, 7001, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(context, AlarmScheduler.CHANNEL) : new Notification.Builder(context);
        b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(name + " reminder").setContentText("Your scheduled trading session is starting" + (time == null ? "." : " at " + time + "."))
          .setStyle(new Notification.BigTextStyle().bigText("Your scheduled trading session is starting. Review your liquidity plan, risk limits and execution checklist before trading."))
          .setAutoCancel(true).setContentIntent(content).setCategory(Notification.CATEGORY_REMINDER).setPriority(Notification.PRIORITY_HIGH).setDefaults(Notification.DEFAULT_ALL);
        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(intent.getIntExtra("id", 9001), b.build());
        // Daily recurrence: schedule this same reminder for tomorrow.
        String json = context.getSharedPreferences(AlarmScheduler.PREFS, Context.MODE_PRIVATE).getString("alarms", "[]");
        try { AlarmScheduler.scheduleAll(context, new org.json.JSONArray(json)); } catch (Exception ignored) {}
    }
}
