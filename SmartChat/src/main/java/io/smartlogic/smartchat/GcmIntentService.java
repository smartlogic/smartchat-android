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

import io.smartlogic.smartchat.activities.MainActivity;
import io.smartlogic.smartchat.data.DataUriManager;

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
                .setContentText("SmartChat from " + extras.getString("creator_username"))
                .setDefaults(Notification.FLAG_SHOW_LIGHTS)
                .setVibrate(new long[]{750})
                .setLights(0xff6AC8C8, 1000, 750);
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(Constants.EXTRA_GO_TO_NOTIFICATIONS, true);

        io.smartlogic.smartchat.models.Notification notification = new io.smartlogic.smartchat.models.Notification();
        notification.setCreatorId(extras.getInt("creator_id"));
        notification.setCreatorUsername(extras.getString("creator_username"));
        notification.setFileUrl(extras.getString("file_url"));
        notification.setDrawingUrl(extras.getString("drawing_url", ""));

        getContentResolver().insert(DataUriManager.getNotificationsUri(), notification.getAttributes());

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(extras.getInt("id", 1), mBuilder.build());
    }
}
