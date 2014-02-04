package io.smartlogic.smartchat.tasks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.view.Display;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ResizeTask extends AsyncTask<Void, Void, Void> {
    private String filePath;
    private String resizedPhotoPath;

    private Activity mActivity;
    private int mCameraId;
    private OnResizeCompletedListener mOnResizeCompletedListener;

    public interface OnResizeCompletedListener {
        public void resizeCompleted(String resizedPhotoPath);
    }

    public ResizeTask(Activity context, String filePath, int cameraId, OnResizeCompletedListener onResizeCompletedListener) {
        this.mActivity = context;
        this.filePath = filePath;
        this.mCameraId = cameraId;
        this.mOnResizeCompletedListener = onResizeCompletedListener;
    }

    @Override
    protected Void doInBackground(Void... unused) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);

            bitmap = rotateImage(bitmap);
            bitmap = scaleImage(bitmap);

            File pictureFile = File.createTempFile("smartchat", ".jpg", mActivity.getExternalCacheDir());
            OutputStream out = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            bitmap.recycle();

            new File(filePath).delete();

            resizedPhotoPath = pictureFile.getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Bitmap scaleImage(Bitmap bitmap) {
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        if (bitmap.getWidth() > point.x || bitmap.getHeight() > point.y) {
            int height = point.y;
            int width = (int) (point.y * ((float) bitmap.getWidth() / (float) bitmap.getHeight()));

            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, false);
            bitmap.recycle();
            return scaled;
        }

        return bitmap;
    }

    private Bitmap rotateImage(Bitmap bitmap) {
        if (bitmap.getWidth() > bitmap.getHeight()) {
            Matrix matrix = new Matrix();

            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                matrix.postRotate(-90);
            } else {
                matrix.postRotate(90);
            }

            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return rotated;
        }

        return bitmap;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mOnResizeCompletedListener.resizeCompleted(resizedPhotoPath);
    }
}