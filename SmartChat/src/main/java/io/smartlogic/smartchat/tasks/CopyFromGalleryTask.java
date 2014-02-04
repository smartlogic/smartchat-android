package io.smartlogic.smartchat.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyFromGalleryTask extends AsyncTask<Void, Void, String> {
    private Context mContext;
    private OnCopyCompletedListener mOnCopyCompletedListener;
    private Uri mSelectedImage;

    public interface OnCopyCompletedListener {
        public void copyCompleted(String picturePath);
    }

    public CopyFromGalleryTask(Context context, OnCopyCompletedListener onCopyCompletedListener, Uri selectedImage) {
        this.mContext = context;
        this.mOnCopyCompletedListener = onCopyCompletedListener;
        this.mSelectedImage = selectedImage;
    }

    @Override
    protected String doInBackground(Void... params) {
        if (mSelectedImage != null) {
            try {
                InputStream inputStream = mContext.getContentResolver().openInputStream(mSelectedImage);

                File pictureFile = File.createTempFile("smartchat", ".jpg", mContext.getExternalCacheDir());
                OutputStream outputStream = new FileOutputStream(pictureFile);

                try {
                    final byte[] buffer = new byte[1024];
                    int read;

                    while ((read = inputStream.read(buffer)) != -1)
                        outputStream.write(buffer, 0, read);

                    outputStream.flush();
                } finally {
                    outputStream.close();
                }

                return pictureFile.getPath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String picturePath) {
        if (!TextUtils.isEmpty(picturePath)) {
            mOnCopyCompletedListener.copyCompleted(picturePath);
        }
    }
}