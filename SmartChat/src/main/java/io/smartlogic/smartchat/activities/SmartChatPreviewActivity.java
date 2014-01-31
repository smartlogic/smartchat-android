package io.smartlogic.smartchat.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.views.DrawingView;

public class SmartChatPreviewActivity extends Activity {
    private File pictureFile;
    private DrawingView mDrawingView;
    private EditText mMessageEdit;
    private int mExpireIn = Constants.DEFAULT_EXPIRE_IN;

    private boolean mMessageShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_chat_preview);

        if (getActionBar() != null) {
            getActionBar().hide();
        }

        mMessageEdit = (EditText) findViewById(R.id.message_edit);

        pictureFile = new File(getIntent().getExtras().getString(Constants.EXTRA_PHOTO_PATH));
        Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());

        ImageView preview = (ImageView) findViewById(R.id.smartchat);
        preview.setImageBitmap(bitmap);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.container);

        RelativeLayout.LayoutParams previewLayoutParams = (RelativeLayout.LayoutParams) preview.getLayoutParams();

        mDrawingView = new DrawingView(this);
        mDrawingView.setLayoutParams(previewLayoutParams);

        int drawingViewIndex = layout.indexOfChild(preview) + 1;
        layout.addView(mDrawingView, drawingViewIndex);

        Button mUploadButton = (Button) findViewById(R.id.upload);
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SmartChatPreviewActivity.this, PickFriendsActivity.class);

                if (mDrawingView.doesDrawingExist()) {
                    String drawingPhotoPath = saveDrawing();
                    intent.putExtra(Constants.EXTRA_DRAWING_PATH, drawingPhotoPath);
                }

                intent.putExtra(Constants.EXTRA_PHOTO_PATH, getIntent().getExtras().getString(Constants.EXTRA_PHOTO_PATH));
                intent.putExtra(Constants.EXTRA_EXPIRE_IN, mExpireIn);
                startActivity(intent);
            }
        });

        final Button undoButton = (Button) findViewById(R.id.undo);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawingView.undoPath();
            }
        });

        Button drawingButton = (Button) findViewById(R.id.draw);
        drawingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoButton.setVisibility(undoButton.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);

                mDrawingView.toggleDrawing();
            }
        });

        Button expireIn = (Button) findViewById(R.id.expire_in);
        expireIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final NumberPicker picker = new NumberPicker(SmartChatPreviewActivity.this);
                picker.setMinValue(3);
                picker.setMaxValue(20);
                picker.setWrapSelectorWheel(false);
                picker.setValue(mExpireIn);

                AlertDialog dialog = new AlertDialog.Builder(SmartChatPreviewActivity.this)
                        .setTitle(R.string.expire_in)
                        .setView(picker)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mExpireIn = picker.getValue();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();
                dialog.show();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return super.onTouchEvent(event);
        }

        toggleMessage();

        return true;
    }

    private boolean toggleMessage() {
        if (mMessageShowing) {
            mMessageEdit.setVisibility(View.INVISIBLE);
            mDrawingView.setText(mMessageEdit.getText().toString());
        } else {
            mMessageEdit.setVisibility(View.VISIBLE);
            mMessageEdit.requestFocus();
        }

        mMessageShowing = !mMessageShowing;

        mDrawingView.setTextShowing(!mMessageShowing);

        return mMessageShowing;
    }

    @Override
    public void onBackPressed() {
        if (mMessageShowing) {
            toggleMessage();
            return;
        }

        pictureFile.delete();

        super.onBackPressed();
    }

    private String saveDrawing() {
        try {
            File pictureFile = File.createTempFile("drawing", ".png", getExternalCacheDir());

            OutputStream out = new FileOutputStream(pictureFile);
            mDrawingView.hideSwatch();
            mDrawingView.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            return pictureFile.getAbsolutePath();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
