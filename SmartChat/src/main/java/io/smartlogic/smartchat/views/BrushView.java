package io.smartlogic.smartchat.views;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

public class BrushView {
    final static int STARTING_BRUSH_SIZE = 20;
    private int[] mAvailableBrushSizes = new int[]{
            5, 20, 40
    };
    private List<Brush> mBrushes;
    private Brush mCurrentBrush;

    private float scale;

    public BrushView(float scale) {
        this.mBrushes = new ArrayList<Brush>();
        this.scale = scale;
    }

    public int getCurrentBrushSize() {
        return mCurrentBrush.size;
    }

    public Brush getCurrentBrush() {
        return mCurrentBrush;
    }

    protected void setStartingLocation(int left, int top) {
        mBrushes.clear();

        int right = scalePixel(left + 150);

        for (int size : mAvailableBrushSizes) {
            top += scalePixel(100);

            Rect rect = new Rect(left, top, right, top);

            mBrushes.add(new Brush(size, rect));
        }

        for (Brush brush : mBrushes) {
            if (brush.size == STARTING_BRUSH_SIZE) {
                mCurrentBrush = brush;
                break;
            }
        }
    }

    public void onDraw(Canvas canvas, int color) {
        for (Brush brush : mBrushes) {
            brush.paint.setColor(color);
            canvas.drawLine(brush.location.left, brush.location.top, brush.location.right, brush.location.bottom, brush.paint);
        }
    }

    protected boolean touchIntersecting(int x, int y) {
        for (Brush brush : mBrushes) {
            if (brushLineIntersects(brush, x, y)) {
                mCurrentBrush = brush;

                return true;
            }
        }

        return false;
    }

    private boolean brushLineIntersects(Brush brush, int x, int y) {
        Rect brushRectangle = new Rect(
                brush.location.left,
                brush.location.top - 50,
                brush.location.right,
                brush.location.bottom + 50
        );
        return brushRectangle.intersects(x, y, x, y);
    }

    private int scalePixel(int pixel) {
        return (int) Math.ceil(pixel - 0.5f / scale);
    }

    protected static class Brush {
        int size;
        Paint paint;
        Rect location;

        public Brush(int size, Rect location) {
            Paint paint = new Paint();
            paint.setStrokeWidth(size);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);

            this.size = size;
            this.paint = paint;
            this.location = location;
        }
    }
}
