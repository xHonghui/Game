package com.nft.quizgame.utils;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.nft.quizgame.R;


public class NotifyManager {

    private static class Holder {
        private static final NotifyManager INSTANCE = new NotifyManager();
    }


    private static final String IMPORTANCE_MIN = "IMPORTANCE_MIN";
    private static final String IMPORTANCE_LOW = "IMPORTANCE_LOW";
    private static final String IMPORTANCE_DEFAULT = "IMPORTANCE_DEFAULT";
    private static final String IMPORTANCE_HIGH = "IMPORTANCE_HIGH";
    private static final String IMPORTANCE_MAX = "IMPORTANCE_MAX";

    private NotifyManager() {
    }

    public static NotifyManager getInstance() {
        return Holder.INSTANCE;
    }

    public void init(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            // 设置通知出现时声音，默认通知是有声音的
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
               /*
               删除之前创建的id
               NotificationChannel notificationChannel = manager.getNotificationChannel(MUSIC_ID);
                if (notificationChannel != null) {
                    if (notificationChannel.getImportance() == NotificationManager.IMPORTANCE_DEFAULT) {
                        manager.deleteNotificationChannel(MUSIC_ID);
                    }
                }*/

//                NotificationChannel channel = new NotificationChannel(IMPORTANCE_MIN, IMPORTANCE_MIN, NotificationManager.IMPORTANCE_MIN);
//                manager.createNotificationChannel(channel);
//                channel = new NotificationChannel(IMPORTANCE_LOW, IMPORTANCE_LOW, NotificationManager.IMPORTANCE_LOW);
//                manager.createNotificationChannel(channel);
                NotificationChannel  channel = new NotificationChannel(IMPORTANCE_DEFAULT, context.getString(R.string.operational_activities_msg), NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
                channel = new NotificationChannel(IMPORTANCE_HIGH, context.getString(R.string.notify_msg), NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
//                channel = new NotificationChannel(IMPORTANCE_MAX, IMPORTANCE_MAX, NotificationManager.IMPORTANCE_MAX);
//                manager.createNotificationChannel(channel);
            }
        }

    }

   /* public NotificationCompat.Builder getBuilder(Context context) {
        return new NotificationCompat.Builder(context, CHANNEL_ID);
    }

    public NotificationCompat.Builder getNotifyToolsBuilder(Context context) {
        return new NotificationCompat.Builder(context, NOTIFY_TOOLS_ID);
    }
*/
}
