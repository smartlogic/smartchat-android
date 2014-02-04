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
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.api.SmartChatDownloader;

public class DisplaySmartChatActivity extends Activity {
    private File file;
    private File drawingFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_smart_chat);

        if (getActionBar() != null) {
            getActionBar().hide();
        }

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
            file = downloader.download();

            if (!getIntent().getExtras().getString(Constants.EXTRA_DRAWING_FILE_URL, "").equals("")) {
                String drawingFileUrl = getIntent().getExtras().getString(Constants.EXTRA_DRAWING_FILE_URL);

                downloader = new SmartChatDownloader(context, encodedPrivateKey, drawingFileUrl);
                drawingFile = downloader.download();
            }

            return null;
        }

        public String getMimeType(String url) {
            String type = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(url);
            if (extension != null) {
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                type = mime.getMimeTypeFromExtension(extension);
            }
            return type;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            String mimeType = getMimeType(file.getAbsolutePath());

            if (mimeType.matches("image")) {
                ImageView photoView = (ImageView) findViewById(R.id.smartchat);
                photoView.setImageBitmap(bitmap);
            }

            file.delete();

            if (drawingFile != null) {
                bitmap = BitmapFactory.decodeFile(drawingFile.getAbsolutePath());
                ImageView drawingView = (ImageView) findViewById(R.id.drawing);
                drawingView.setVisibility(View.VISIBLE);
                drawingView.setImageBitmap(bitmap);

                drawingFile.delete();
            }

            final TextView countDownText = (TextView) findViewById(R.id.count_down);

            int expireIn = getIntent().getExtras().getInt(Constants.EXTRA_EXPIRE_IN);
            new CountDownTimer(expireIn * 1000, 1000) {
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
