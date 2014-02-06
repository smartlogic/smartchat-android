package io.smartlogic.smartchat.helpers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.activities.SmartChatPreviewActivity_;
import io.smartlogic.smartchat.fragments.CameraFragment;
import io.smartlogic.smartchat.tasks.ResizeTask;

public class CameraHelper implements CameraFragment.ICamera {
    private static final String TAG = "CameraHelper";

    private boolean mAutofocusing = false;
    private Activity mActivity;
    private Camera mCamera;
    private int mCameraId;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File outputDir = mActivity.getExternalCacheDir();
            try {
                File pictureFile = File.createTempFile("smartchat", ".jpg", outputDir);

                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                new ResizeTask(mActivity, pictureFile.getPath(), mCameraId, new ResizeTask.OnResizeCompletedListener() {
                    @Override
                    public void resizeCompleted(String resizedPhotoPath) {
                        Intent intent = new Intent(mActivity, SmartChatPreviewActivity_.class);
                        intent.putExtra(Constants.EXTRA_PHOTO_PATH, resizedPhotoPath);
                        mActivity.startActivity(intent);
                    }
                }).execute();

                Log.d(TAG, "Captured " + pictureFile.toString());
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    private VideoHelper mVideoHelper;

    public CameraHelper(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public Camera getCamera() {
        return mCamera;
    }

    @Override
    public void takePicture() {
        mCamera.takePicture(null, null, mPicture);
    }

    @Override
    public void takeVideo(Surface surface) {
        if (mVideoHelper == null) {
            mVideoHelper = new VideoHelper(mActivity, mCamera, mCameraId);
        }

        mVideoHelper.takeVideo(surface, new VideoHelper.OnVideoRecordedListener() {
            @Override
            public void videoRecorded(String path) {
                Intent intent = new Intent(mActivity, SmartChatPreviewActivity_.class);
                intent.putExtra(Constants.EXTRA_VIDEO_PATH, path);
                mActivity.startActivity(intent);
            }
        });
    }

    @Override
    public void cameraResume(Point screenSize) {
        mCamera = getCameraInstance(mCameraId);
        Camera.Parameters parameters = mCamera.getParameters();

        setPictureSize(parameters, screenSize);
        setPreviewSize(parameters, screenSize);

        parameters.setRotation(90);
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(parameters);
    }

    @Override
    public void cameraPause() {
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            mCamera.release();
            mCamera = null;
        }

        if (mVideoHelper != null) {
            mVideoHelper.releaseMediaRecorder();
        }
    }

    @Override
    public void toggleFrontBack() {
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            mCameraId = getFrontCameraId();
        }
    }

    @Override
    public void autoFocus(MotionEvent event, Point screenSize) {
        if (mAutofocusing || event.getAction() != MotionEvent.ACTION_UP) {
            Log.d(TAG, "Not auto focusing");
            return;
        }

        mAutofocusing = true;

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
                mAutofocusing = false;

                Log.d(TAG, "Autofocused");
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
}
