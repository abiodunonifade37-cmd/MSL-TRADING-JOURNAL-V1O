package com.msl.tradingjournal;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Calendar;

public final class AlarmScheduler {
    public static final String PREFS = "msl_alarm_prefs";
    public static final String CHANNEL = "msl_session_alarms";
    private AlarmScheduler() {}

    public static void ensureChannel(Context c) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL, "Trading session alarms", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Daily Asia, London and New York trading session reminders");
            channel.enableVibration(true);
            NotificationManager nm = c.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    public static void scheduleAll(Context c, JSONArray alarms) {
        ensureChannel(c);
        AlarmManager manager = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        for (int i=0; i<3; i++) {
            String name = i==0 ? "Asia session" : i==1 ? "London session" : "New York session";
            cancel(c, i);
        }
        for (int i=0; i<alarms.length(); i++) {
            JSONObject a = alarms.optJSONObject(i); if (a == null) continue;
            String name = a.optString("name", "Trading session");
            String time = a.optString("time", "08:00");
            boolean enabled = a.optBoolean("enabled", false);
            int id = alarmId(name, i);
            if (!enabled) { cancel(c, id); continue; }
            try {
                String[] hm = time.split(":");
                int hour = Integer.parseInt(hm[0]), minute = Integer.parseInt(hm[1]);
                Calendar next = Calendar.getInstance();
                next.set(Calendar.HOUR_OF_DAY, hour); next.set(Calendar.MINUTE, minute); next.set(Calendar.SECOND, 0); next.set(Calendar.MILLISECOND, 0);
                if (next.getTimeInMillis() <= System.currentTimeMillis()) next.add(Calendar.DAY_OF_YEAR, 1);
                Intent intent = new Intent(c, AlarmReceiver.class).putExtra("name", name).putExtra("time", time).putExtra("id", id);
                PendingIntent pi = PendingIntent.getBroadcast(c, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                if (Build.VERSION.SDK_INT >= 31 && !manager.canScheduleExactAlarms()) {
                    manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
                } else if (Build.VERSION.SDK_INT >= 23) {
                    manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
                } else manager.setExact(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
            } catch (Exception ignored) {}
        }
    }

    public static void cancel(Context c, int id) {
        Intent intent = new Intent(c, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(c, id, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pi != null) { AlarmManager m=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE); if(m!=null)m.cancel(pi); pi.cancel(); }
    }
    private static int alarmId(String name, int fallback) { return Math.abs(name.toLowerCase().hashCode() % 100000) + 1000 + fallback; }

    public static void rescheduleSaved(Context c) {
        String json = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("alarms", "[]");
        try { scheduleAll(c, new JSONArray(json)); } catch (Exception ignored) {}
    }
}
