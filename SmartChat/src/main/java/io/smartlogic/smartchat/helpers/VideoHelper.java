package io.smartlogic.smartchat.helpers;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.Surface;

import java.io.File;
import java.io.IOException;

public class VideoHelper {
    private Activity mActivity;
    private Camera mCamera;
    private int mCameraId;

    private boolean mTakingVideo = false;
    private MediaRecorder mMediaRecorder;

    private String mVideoPath;

    public interface OnVideoRecordedListener {
        public void videoRecorded(String path);
    }

    public VideoHelper(Activity activity, Camera camera, int cameraId) {
        this.mActivity = activity;
        this.mCamera = camera;
        this.mCameraId = cameraId;
    }

    public void takeVideo(Surface surface, OnVideoRecordedListener onVideoRecorededListener) {
        if (!mTakingVideo) {
            mTakingVideo = true;
            setUpRecording(surface);
            mMediaRecorder.start();
        } else {
            mTakingVideo = false;
            mMediaRecorder.stop();
            releaseMediaRecorder();

            onVideoRecorededListener.videoRecorded(mVideoPath);
        }
    }

    private void setUpRecording(Surface surface) {
        mMediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_480P)) {
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
        } else {
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
        }

        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mMediaRecorder.setOrientationHint(270);
        } else {
            mMediaRecorder.setOrientationHint(90);
        }

        mVideoPath = getOutputMediaFile().toString();
        mMediaRecorder.setOutputFile(mVideoPath);

        mMediaRecorder.setPreviewDisplay(surface);

        try {
            mMediaRecorder.prepare();

        } catch (IllegalStateException e) {
            releaseMediaRecorder();
        } catch (IOException e) {
            releaseMediaRecorder();
        }
    }

    private File getOutputMediaFile() {
        File outputDir = mActivity.getExternalCacheDir();
        File pictureFile = null;
        try {
            pictureFile = File.createTempFile("smartchat", ".mp4", outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pictureFile;
    }

    protected void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }
}