package io.smartlogic.smartchat.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

import io.smartlogic.smartchat.R;
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

    /**
     * A safe way to get an instance of the Camera object.
     */
    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

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
    }

    @Override
    public void onResume() {
        super.onResume();

        mCamera = getCameraInstance();

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(90);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(getActivity(), mCamera);
        mPreviewLayout = (FrameLayout) getView().findViewById(R.id.camera_preview);
        mPreviewLayout.addView(mPreview);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        mPreviewLayout.removeView(mPreview);
        mPreview = null;
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
                File pictureFile = File.createTempFile("smartchat", ".jpg", getActivity().getExternalCacheDir());

                Matrix matrix = new Matrix();
                matrix.postRotate(90);

                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 4, bitmap.getHeight() / 4, false);
                Bitmap rotated = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), matrix, true);

                OutputStream out = new FileOutputStream(pictureFile);
                rotated.compress(Bitmap.CompressFormat.JPEG, 80, out);
                out.flush();
                out.close();

                new File(filePath).delete();

                resizedPhotoPath = pictureFile.getPath();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
