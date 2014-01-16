package io.smartlogic.smartchat.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DrawingView extends View {
    private static final String TAG = "DrawingView";

    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;

    private boolean mDrawingExists = false;
    private boolean mDrawing = false;
    private boolean mDisplaySwatch = false;
    private boolean mTouchingSwatch = false;

    private Rect mSwatchBorder;
    private int[] mAvailableColors = new int[]{
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#000000"),
            Color.parseColor("#FF0000"),
            Color.parseColor("#00FF00"),
            Color.parseColor("#0000FF"),
            Color.parseColor("#FFFF00"),
            Color.parseColor("#00FFFF"),
            Color.parseColor("#FF00FF"),
    };
    private List<ColorSwatchColor> mColorSwatches;
    private ColorSwatchColor mCurrentColorSwatchColor;

    private int[] mAvailableBrushSizes = new int[]{
            5, 10, 20, 40
    };
    private List<Brush> mBrushes;
    private Brush mCurrentBrush;

    private Stack<DrawingPath> mPaths;

    private float scale;

    @SuppressWarnings("unused")
    public DrawingView(Context context) {
        super(context);
        setupPainting();
    }

    @SuppressWarnings("unused")
    public DrawingView(Context context, AttributeSet set) {
        super(context, set);
        setupPainting();
    }

    private void setupPainting() {
        drawPath = new Path();
        drawPaint = new Paint();

        drawPaint.setColor(mAvailableColors[0]);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(mAvailableBrushSizes[0]);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

        setDrawingCacheEnabled(true);

        mColorSwatches = new ArrayList<ColorSwatchColor>();
        mBrushes = new ArrayList<Brush>();
        mPaths = new Stack<DrawingPath>();

        scale = getResources().getDisplayMetrics().density;
    }

    public void toggleDrawing() {
        this.mDrawing = !mDrawing;
        invalidate();
    }

    public void toggleSwatch() {
        this.mDisplaySwatch = !mDisplaySwatch;
        invalidate();
    }

    public boolean doesDrawingExist() {
        return mDrawingExists;
    }

    public void undoPath() {
        if (mPaths.size() == 0) {
            return;
        }

        mPaths.pop();

        canvasBitmap.recycle();
        canvasBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

        Paint paint = new Paint(drawPaint);
        for (DrawingPath drawingPath : mPaths) {
            paint.setColor(drawingPath.swatchColor.color);
            paint.setStrokeWidth(drawingPath.brush.size);
            drawCanvas.drawPath(drawingPath.path, paint);
        }

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mColorSwatches.clear();
        mBrushes.clear();

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

        int sideLength = scalePixel(75);

        int left = scalePixel(getWidth() - 250 + sideLength);
        int top = scalePixel(250 - sideLength);
        int right = left + scalePixel(sideLength);
        int bottom = top + scalePixel(sideLength);

        mSwatchBorder = new Rect(left, top, right, bottom);

        for (int i = 0; i < mAvailableColors.length; i++) {
            if (i % 2 == 0) {
                left -= sideLength;
                right -= sideLength;

                top += sideLength;
                bottom += sideLength;
            } else {
                left += sideLength;
                right += sideLength;
            }


            Rect rect = new Rect(left, top, right, bottom);

            mColorSwatches.add(new ColorSwatchColor(mAvailableColors[i], rect));
        }

        mCurrentColorSwatchColor = mColorSwatches.get(0);

        mSwatchBorder.set(mSwatchBorder.left - sideLength, mSwatchBorder.top + sideLength, right, bottom);

        top += sideLength;
        for (int size : mAvailableBrushSizes) {
            top += scalePixel(100);

            Rect rect = new Rect(mSwatchBorder.left, top, mSwatchBorder.right, top);

            mBrushes.add(new Brush(size, rect));
        }

        mCurrentBrush = mBrushes.get(0);
    }

    private int scalePixel(int pixel) {
        return (int) (pixel - 0.5f / scale);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);

        if (mDrawing && mDisplaySwatch) {
            for (ColorSwatchColor swatchColor : mColorSwatches) {
                canvas.drawRect(swatchColor.location, swatchColor.paint);
            }

            canvas.drawRect(mSwatchBorder, drawPaint);

            for (Brush brush : mBrushes) {
                brush.paint.setColor(mCurrentColorSwatchColor.color);
                canvas.drawLine(brush.location.left, brush.location.top, brush.location.right, brush.location.bottom, brush.paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mDrawing) {
            return false;
        }

        mDrawingExists = true;

        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mDisplaySwatch) {
                    mTouchingSwatch = touchIntersecting((int) touchX, (int) touchY);
                }

                if (!mTouchingSwatch) {
                    drawPath.moveTo(touchX, touchY);
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (mDisplaySwatch && mTouchingSwatch) {
                    touchIntersecting((int) touchX, (int) touchY);
                } else {
                    drawPath.lineTo(touchX, touchY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchingSwatch) {
                    mTouchingSwatch = false;
                }

                mPaths.add(new DrawingPath(drawPath, mCurrentColorSwatchColor, mCurrentBrush));

                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath = new Path();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    private boolean touchIntersecting(int x, int y) {
        for (ColorSwatchColor swatchColor : mColorSwatches) {
            if (swatchColor.location.intersects(x, y, x, y)) {
                setPaintColor(swatchColor);

                return true;
            }
        }

        for (Brush brush : mBrushes) {
            if (brushLineIntersects(brush, x, y)) {
                setPaintStroke(brush);
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

    private void setPaintColor(ColorSwatchColor colorSwatchColor) {
        mCurrentColorSwatchColor = colorSwatchColor;
        drawPaint.setColor(colorSwatchColor.color);
    }

    private void setPaintStroke(Brush brush) {
        mCurrentBrush = brush;
        drawPaint.setStrokeWidth(brush.size);
    }

    private class ColorSwatchColor {
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

    private class Brush {
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

    private class DrawingPath {
        Path path;
        ColorSwatchColor swatchColor;
        Brush brush;

        public DrawingPath(Path path, ColorSwatchColor swatchColor, Brush brush) {
            this.path = path;
            this.swatchColor = swatchColor;
            this.brush = brush;
        }
    }
}
