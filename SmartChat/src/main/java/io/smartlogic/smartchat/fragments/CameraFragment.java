package io.smartlogic.smartchat.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.activities.SmartChatPreviewActivity;
import io.smartlogic.smartchat.views.CameraPreview;

public class CameraFragment extends Fragment {
    private static String TAG = "CameraFragment";
    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout mPreviewLayout;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File outputDir = getActivity().getExternalCacheDir();
            try {
                File pictureFile = File.createTempFile("smartchat", ".jpg", outputDir);

                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                new ResizeTask(pictureFile.getPath()).execute();

                Log.d(TAG, "Captured " + pictureFile.toString());
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button captureButton = (Button) getView().findViewById(R.id.capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );

        Button switchCamera = (Button) getView().findViewById(R.id.switch_camera);
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCameraInstance();

                if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                } else {
                    mCameraId = getFrontCameraId();
                }

                resumeCamera();
            }
        });
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    private Camera getCameraInstance(int cameraId) {
        Camera c = null;
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private int getFrontCameraId() {
        Camera.CameraInfo ci = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return i;
            }
        }

        return Camera.CameraInfo.CAMERA_FACING_BACK; // No front-facing camera found
    }

    public void resumeCamera() {
        mCamera = getCameraInstance(mCameraId);
        Camera.Parameters parameters = mCamera.getParameters();

        parameters.setRotation(90);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(getActivity(), mCamera);
        mPreviewLayout = (FrameLayout) getView().findViewById(R.id.camera_preview);
        mPreviewLayout.addView(mPreview);
    }

    public void removeCameraInstance() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        mPreviewLayout.removeView(mPreview);
        mPreview = null;
    }

    @Override
    public void onPause() {
        super.onPause();

        removeCameraInstance();
    }

    @Override
    public void onResume() {
        super.onResume();

        resumeCamera();
    }

    private class ResizeTask extends AsyncTask<Void, Void, Void> {
        private String filePath;
        private String resizedPhotoPath;

        public ResizeTask(String filePath) {
            this.filePath = filePath;
        }

        @Override
        protected Void doInBackground(Void... unused) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);

                bitmap = rotateImage(bitmap);
                bitmap = scaleImage(bitmap);

                File pictureFile = File.createTempFile("smartchat", ".jpg", getActivity().getExternalCacheDir());
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
            Display display = getActivity().getWindowManager().getDefaultDisplay();
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
            Intent intent = new Intent(getActivity(), SmartChatPreviewActivity.class);
            intent.putExtra(Constants.EXTRA_PHOTO_PATH, resizedPhotoPath);
            startActivity(intent);
        }
    }
}
