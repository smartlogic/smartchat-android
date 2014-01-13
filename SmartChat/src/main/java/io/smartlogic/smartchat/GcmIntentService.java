package io.smartlogic.smartchat;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import io.smartlogic.smartchat.activities.DisplaySmartChatActivity;
import io.smartlogic.smartchat.activities.MainActivity;

public class GcmIntentService extends IntentService {
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("New SmartChat")
                .setContentText("SmartChat from " + extras.getString("creator_email"))
                .setDefaults(Notification.FLAG_SHOW_LIGHTS)
                .setLights(0xff6AC8C8, 1000, 750);
        Intent resultIntent = new Intent(this, DisplaySmartChatActivity.class);
        resultIntent.putExtras(extras);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(extras.getInt("id", 1), mBuilder.build());
    }
}
