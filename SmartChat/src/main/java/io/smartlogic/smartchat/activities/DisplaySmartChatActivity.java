package io.smartlogic.smartchat.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;

public class DisplaySmartChatActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_smart_chat);

        Bundle extras = getIntent().getExtras();

        File photo = new File(extras.getString(Constants.EXTRA_PHOTO_PATH));
        Bitmap bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath());

        ImageView photoView = (ImageView) findViewById(R.id.smartchat);
        photoView.setImageBitmap(bitmap);
    }
}
