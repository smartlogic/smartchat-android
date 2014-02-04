package io.smartlogic.smartchat.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DrawingTextView {

    private String mText = "";
    private Paint mTextPaint;
    private Rect mTextBounds;
    private Paint mTextBorderPaint;
    private Rect mTextBorder;

    private DisplayMetrics mDisplayMetrics;

    public DrawingTextView(DisplayMetrics displayMetrics) {
        this.mDisplayMetrics = displayMetrics;

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(sp(20));

        mTextBorderPaint = new Paint();
        mTextBorderPaint.setColor(Color.parseColor("#80000000"));
    }

    public boolean isTextEmpty() {
        return TextUtils.isEmpty(mText);
    }

    public void setText(String text) {
        this.mText = text;

        mTextBounds = new Rect();
        mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);

        int padding = (int) dip(20);
        mTextBorder = new Rect(mTextBounds.left - padding, mTextBounds.top - padding, mTextBounds.right + padding, mTextBounds.bottom + padding);
    }

    public void onDraw(Canvas canvas) {
        float x = canvas.getWidth() / 2 - mTextBounds.width() / 2;
        float y = canvas.getHeight() / 2 + mTextBounds.height() / 2;

        int left = canvas.getWidth() / 2 - mTextBorder.width() / 2;
        int top = canvas.getHeight() / 2 - mTextBorder.height() / 2;
        int right = canvas.getWidth() / 2 + mTextBorder.width() / 2;
        int bottom = canvas.getHeight() / 2 + mTextBorder.height() / 2;

        canvas.drawRect(left, top, right, bottom, mTextBorderPaint);
        canvas.drawText(mText, x, y, mTextPaint);
    }

    private float dip(int size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, mDisplayMetrics);
    }

    private float sp(int size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, mDisplayMetrics);
    }
}
