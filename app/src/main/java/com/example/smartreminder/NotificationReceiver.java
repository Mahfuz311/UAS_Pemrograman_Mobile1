package com.example.smartreminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int notifId = intent.getIntExtra("id", 0);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "task_channel";

        // 1. Setup Channel (Penting untuk Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Pengingat Tugas", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifikasi deadline tugas kuliah");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        // 2. Intent saat diklik
        Intent tapIntent = new Intent(context, MainActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE);

        // 3. Cek Mode Silent
        SharedPreferences prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        boolean isSilent = prefs.getBoolean("isSilentMode", false);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // 4. DESAIN NOTIFIKASI KEREN
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                // Ikon kecil di status bar (Gunakan ikon putih transparan)
                .setSmallIcon(R.drawable.ic_notification)

                // Warna Aksen (Warna teks aplikasi/logo di notif)
                .setColor(Color.parseColor("#2979FF"))

                // Judul & Isi
                .setContentTitle(taskTitle)
                .setContentText(message)

                // Style Teks Panjang (Agar pesan tidak terpotong jika panjang)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))

                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // 5. Atur Suara/Getar
        if (!isSilent) {
            builder.setSound(soundUri);
            builder.setVibrate(new long[]{0, 500, 200, 500}); // Pola getar: Diam-Getar-Diam-Getar
        } else {
            builder.setSound(null);
            builder.setVibrate(new long[]{0});
        }

        notificationManager.notify(notifId, builder.build());
    }
}