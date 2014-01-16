package io.smartlogic.smartchat.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

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

        getActionBar().hide();

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
                Log.d("smartchat", key + ": " + getIntent().getExtras().get(key));
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String encodedPrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");
            String fileUrl = getIntent().getExtras().getString(Constants.EXTRA_FILE_URL);

            SmartChatDownloader downloader = new SmartChatDownloader(context, encodedPrivateKey, fileUrl);
            pictureFile = downloader.download();

            if (!getIntent().getExtras().getString(Constants.EXTRA_DRAWING_FILE_URL, "").equals("")) {
                String drawingS3Url = getIntent().getExtras().getString(Constants.EXTRA_DRAWING_FILE_URL);

                downloader = new SmartChatDownloader(context, encodedPrivateKey, drawingS3Url);
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

            final TextView countDownText = (TextView) findViewById(R.id.count_down);

            new CountDownTimer(10 * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    countDownText.setText(String.valueOf(millisUntilFinished / 1000));
                }

                @Override
                public void onFinish() {
                    finish();
                }
            }.start();

            super.onPostExecute(aVoid);
        }
    }
}
