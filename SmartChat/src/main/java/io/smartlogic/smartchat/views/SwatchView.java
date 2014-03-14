package io.smartlogic.smartchat.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.List;

import rainbowvis.Rainbow;

public class SwatchView {
    final static int STARTING_COLOR = Color.parseColor("#FFFFFF");

    private Paint mDrawPaint;
    private DisplayMetrics mDisplayMetrics;
    private Rect mSwatchBorder;
    private List<ColorSwatchColor> mColorSwatches;
    private ColorSwatchColor mCurrentColorSwatchColor;

    int left, top, right, bottom;

    public SwatchView(DisplayMetrics displayMetrics, Paint drawPaint) {
        this.mDisplayMetrics = displayMetrics;
        this.mDrawPaint = drawPaint;
        this.mColorSwatches = new ArrayList<ColorSwatchColor>();
    }

    public int getCurrentColor() {
        return mCurrentColorSwatchColor.color;
    }

    public ColorSwatchColor getCurrentColorSwatchColor() {
        return mCurrentColorSwatchColor;
    }

    protected boolean touchIntersecting(int x, int y) {
        for (ColorSwatchColor swatchColor : mColorSwatches) {
            if (swatchColor.location.intersects(x, y, x, y)) {
                mCurrentColorSwatchColor = swatchColor;

                return true;
            }
        }

        return false;
    }

    protected void setStartingLocation(int x, int y) {
        mColorSwatches.clear();

        int width = dip(50);
        int height = dip(2);

        left = x - width;
        top = y - height;
        right = left + width;
        bottom = top + height;

        mSwatchBorder = new Rect(left, top, right, top + height * 100);

        Rainbow rainbow = new Rainbow();
        for (int i = 0; i < 100; i++) {
            Rect rect = new Rect(left, top, right, bottom);
            mColorSwatches.add(new ColorSwatchColor(Color.parseColor("#" + rainbow.colorAt(i)), rect));

            top += height;
            bottom += height;
        }

        mCurrentColorSwatchColor = mColorSwatches.get(0);
    }

    protected void onDraw(Canvas canvas) {
        for (ColorSwatchColor swatchColor : mColorSwatches) {
            canvas.drawRect(swatchColor.location, swatchColor.paint);
        }

        canvas.drawRect(mSwatchBorder, mDrawPaint);
    }

    private int dip(int size) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, mDisplayMetrics);
    }

    protected static class ColorSwatchColor {
        int color;
        Paint paint;
        Rect location;

        public ColorSwatchColor(int color, Rect location) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setStrokeWidth(0);

            this.color = color;
            this.paint = paint;
            this.location = location;
        }
    }
}
