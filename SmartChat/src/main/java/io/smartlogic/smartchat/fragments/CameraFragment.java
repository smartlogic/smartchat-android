package io.smartlogic.smartchat.fragments;

import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.activities.SmartChatPreviewActivity;
import io.smartlogic.smartchat.helpers.CameraHelper;
import io.smartlogic.smartchat.tasks.CopyFromGalleryTask;
import io.smartlogic.smartchat.views.CameraPreview;

public class CameraFragment extends Fragment {
    private static int RESULT_LOAD_IMAGE = 1;
    private CameraPreview mPreview;
    private FrameLayout mPreviewLayout;
    private ICamera mCameraHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mCameraHelper = new CameraHelper(getActivity());

        Button captureButton = (Button) getView().findViewById(R.id.capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCameraHelper.takePicture();
                    }
                }
        );

        Button switchCamera = (Button) getView().findViewById(R.id.switch_camera);
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCameraInstance();
                mCameraHelper.toggleFrontBack();
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

        new CopyFromGalleryTask(getActivity(), new CopyFromGalleryTask.OnCopyCompletedListener() {
            @Override
            public void copyCompleted(String picturePath) {
                Intent intent = new Intent(getActivity(), SmartChatPreviewActivity.class);
                intent.putExtra(Constants.EXTRA_PHOTO_PATH, picturePath);
                startActivity(intent);
            }
        }, data.getData()).execute();
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

    private void resumeCamera() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        final Point screenSize = new Point();
        display.getSize(screenSize);

        mCameraHelper.cameraResume(screenSize);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(getActivity(), mCameraHelper.getCamera());
        mPreviewLayout = (FrameLayout) getView().findViewById(R.id.camera_preview);
        mPreviewLayout.addView(mPreview);
        mPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mCameraHelper.autoFocus(event, screenSize);

                return true;
            }
        });
    }

    private void removeCameraInstance() {
        mCameraHelper.cameraPause();

        mPreviewLayout.removeView(mPreview);
        mPreview = null;
    }

    public interface ICamera {
        public Camera getCamera();

        public void takePicture();

        public void cameraResume(Point screenSize);

        public void cameraPause();

        public void toggleFrontBack();

        public void autoFocus(MotionEvent event, Point screenSize);
    }
}
