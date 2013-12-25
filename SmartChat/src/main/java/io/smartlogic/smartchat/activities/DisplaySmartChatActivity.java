package io.smartlogic.smartchat.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.api.SmartChatDownloader;

public class DisplaySmartChatActivity extends Activity {
    private File pictureFile;
    private File drawingFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_smart_chat);

        new ImageDownloaderTask(this).execute();
    }

    private class ImageDownloaderTask extends AsyncTask<Void, Void, Void> {
        private Context context;

        public ImageDownloaderTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (String key : getIntent().getExtras().keySet()) {
                Log.d("smartchat", key);
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String encodedPrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");
            String encryptedAesKey = getIntent().getExtras().getString(Constants.EXTRA_ENCRYPTED_AES_KEY);
            String encryptedAesIv = getIntent().getExtras().getString(Constants.EXTRA_ENCRYPTED_AES_IV);
            String s3Url = getIntent().getExtras().getString(Constants.EXTRA_FILE_URL);

            SmartChatDownloader downloader = new SmartChatDownloader(context, encodedPrivateKey, encryptedAesKey, encryptedAesIv, s3Url);
            pictureFile = downloader.download();

            if (!getIntent().getExtras().getString(Constants.EXTRA_DRAWING_FILE_URL, "").equals("")) {
                String drawingEncryptedAesKey = getIntent().getExtras().getString(Constants.EXTRA_DRAWING_ENCRYPTED_AES_KEY);
                String drawingEncryptedAesIv = getIntent().getExtras().getString(Constants.EXTRA_DRAWING_ENCRYPTED_AES_IV);
                String drawingS3Url = getIntent().getExtras().getString(Constants.EXTRA_DRAWING_FILE_URL);

                downloader = new SmartChatDownloader(context, encodedPrivateKey, drawingEncryptedAesKey, drawingEncryptedAesIv, drawingS3Url);
                drawingFile = downloader.download();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());

            ImageView photoView = (ImageView) findViewById(R.id.smartchat);
            photoView.setImageBitmap(bitmap);

            pictureFile.delete();

            if (drawingFile != null) {
                bitmap = BitmapFactory.decodeFile(drawingFile.getAbsolutePath());
                ImageView drawingView = (ImageView) findViewById(R.id.drawing);
                drawingView.setVisibility(View.VISIBLE);
                drawingView.setImageBitmap(bitmap);

                drawingFile.delete();
            }

            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    finish();
                }
            };
            timer.schedule(task, 10 * 1000);

            super.onPostExecute(aVoid);
        }
    }
}
