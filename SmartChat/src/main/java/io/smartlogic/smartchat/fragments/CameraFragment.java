package io.smartlogic.smartchat.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.activities.SmartChatPreviewActivity;
import io.smartlogic.smartchat.views.CameraPreview;

public class CameraFragment extends Fragment {
    private static String TAG = "CameraFragment";
    private static int RESULT_LOAD_IMAGE = 1;
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

        Button uploadFromGallery = (Button) getView().findViewById(R.id.upload_from_gallery);
        uploadFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri selectedImage = data.getData();

        if (selectedImage != null) {
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(selectedImage);

                File pictureFile = File.createTempFile("smartchat", ".jpg", getActivity().getExternalCacheDir());
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

                Intent intent = new Intent(getActivity(), SmartChatPreviewActivity.class);
                intent.putExtra(Constants.EXTRA_PHOTO_PATH, pictureFile.getPath());
                startActivity(intent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        final Point screenSize = new Point();
        display.getSize(screenSize);

        setPictureSize(parameters, screenSize);
        setPreviewSize(parameters, screenSize);

        parameters.setRotation(90);
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(parameters);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(getActivity(), mCamera);
        mPreviewLayout = (FrameLayout) getView().findViewById(R.id.camera_preview);
        mPreviewLayout.addView(mPreview);
        mPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Camera.Parameters focusParameters = mCamera.getParameters();

                if (focusParameters.getMaxNumFocusAreas() > 0) {
                    List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();

                    float percentageX = event.getX() / screenSize.x;
                    float percentageY = event.getY() / screenSize.y;

                    int x = (int) (percentageX * 2000) - 1000;
                    int y = (int) (percentageY * 2000) - 1000;

                    int left = x - 100;
                    int top = y - 100;
                    int right = x + 100;
                    int bottom = y + 100;

                    Rect area = new Rect(left, top, right, bottom);
                    focusAreas.add(new Camera.Area(area, 1000));
                    focusParameters.setFocusAreas(focusAreas);
                    mCamera.setParameters(focusParameters);
                }

                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        Log.d(TAG, "started autofocus");
                    }
                });

                return true;
            }
        });
    }

    private void setPictureSize(Camera.Parameters parameters, Point screenSize) {
        float screenRatio = (float) screenSize.x / screenSize.y;

        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        Camera.Size selectedSize = parameters.getPictureSize();

        float startingRatio = (float) selectedSize.height / selectedSize.width;

        Log.i(TAG, "Screen ratio: " + screenRatio);
        Log.i(TAG, "Starting ratio: " + startingRatio);
        Log.i(TAG, "Current size: " + selectedSize.width + "x" + selectedSize.height);

        boolean foundMatchingSize = false;

        if (sizes != null) {
            for (Camera.Size size : sizes) {
                float ratio = (float) size.height / size.width;
                Log.i(TAG, "Found picture size: " + size.width + "x" + size.height + " with ratio: " + ratio);
                if (ratio == screenRatio) {
                    selectedSize = size;
                    foundMatchingSize = true;
                    break;
                }
            }

            if (!foundMatchingSize) {
                for (Camera.Size size : sizes) {
                    float ratio = (float) size.height / size.width;
                    Log.i(TAG, "Found picture size: " + size.width + "x" + size.height + " with ratio: " + ratio);
                    if (ratio == startingRatio) {
                        selectedSize = size;
                        break;
                    }
                }
            }
        }

        if (selectedSize != null) {
            Log.i(TAG, "Setting picture size: " + selectedSize.width + "x" + selectedSize.height);
            parameters.setPictureSize(selectedSize.width, selectedSize.height);
        }
    }

    private void setPreviewSize(Camera.Parameters parameters, Point screenSize) {
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size previewSize = parameters.getPreviewSize();

        Log.i(TAG, "Preview size: " + previewSize.width + "x" + previewSize.height);

        if (previewSizes != null) {
            Integer currentDifference = null;

            for (Camera.Size size : previewSizes) {
                float ratio = (float) size.height / size.width;
                int difference = Math.abs((screenSize.y + screenSize.x) - (size.height + size.width));
                Log.i(TAG, "Found preview size: " + size.width + "x" + size.height + " with ratio: " + ratio + " and difference of " + difference);

                if (currentDifference == null || difference < currentDifference) {
                    currentDifference = difference;
                    previewSize = size;
                }
            }
        }

        if (previewSize != null) {
            Log.i(TAG, "Setting screen size: " + previewSize.width + "x" + previewSize.height);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
        }
    }

    public void removeCameraInstance() {
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
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
