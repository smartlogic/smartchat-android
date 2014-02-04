package io.smartlogic.smartchat.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class SwatchView {
    final static int STARTING_COLOR = Color.parseColor("#FFFFFF");

    private Paint mDrawPaint;
    private Rect mSwatchBorder;
    private List<ColorSwatchColor> mColorSwatches;
    private ColorSwatchColor mCurrentColorSwatchColor;

    int left, top, right, bottom;

    private float scale;

    public SwatchView(float scale, Paint drawPaint) {
        this.mDrawPaint = drawPaint;
        this.mColorSwatches = new ArrayList<ColorSwatchColor>();
        this.scale = scale;
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

        int width = scalePixel(150);
        int height = scalePixel(5);

        left = scalePixel(x - width);
        top = scalePixel(y - height);
        right = left + scalePixel(width);
        bottom = top + scalePixel(height);

        int[] values = new int[]{0, 64, 128, 192, 255};
        SortedMap<Long, Integer> colors = new TreeMap<Long, Integer>(Collections.reverseOrder());
        for (int r : values) {
            for (int g : values) {
                for (int b : values) {
                    long colourScore = (r << 16) + (g << 8) + b;
                    colors.put(colourScore, Color.rgb(r, g, b));
                }
            }
        }

        mSwatchBorder = new Rect(left, top, right, top + height * colors.size());

        for (Integer color : colors.values()) {
            Rect rect = new Rect(left, top, right, bottom);
            mColorSwatches.add(new ColorSwatchColor(color, rect));

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

    private int scalePixel(int pixel) {
        return (int) Math.ceil(pixel - 0.5f / scale);
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
