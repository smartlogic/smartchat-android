package io.smartlogic.smartchat.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.Stack;

public class DrawingView extends View {
    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;

    private boolean mDrawingExists = false;
    private boolean mDrawing = false;
    private boolean mDisplaySwatch = true;
    private boolean mTouchingSwatch = false;
    private boolean mTextShowing = true;

    private SwatchView mSwatchView;
    private BrushView mBrushView;
    private DrawingTextView mDrawingTextView;

    private Stack<DrawingPath> mPaths;

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

        drawPaint.setColor(SwatchView.STARTING_COLOR);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(BrushView.STARTING_BRUSH_SIZE);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

        setDrawingCacheEnabled(true);

        mPaths = new Stack<DrawingPath>();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mSwatchView = new SwatchView(displayMetrics.density, drawPaint);
        mBrushView = new BrushView(displayMetrics.density);
        mDrawingTextView = new DrawingTextView(displayMetrics);
    }

    public void toggleDrawing() {
        this.mDrawing = !mDrawing;
        invalidate();
    }

    public void hideSwatch() {
        this.mDisplaySwatch = false;
        invalidate();
    }

    public void setTextShowing(boolean textShowing) {
        this.mTextShowing = textShowing;
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

    public void setText(String text) {
        mDrawingExists = true;
        mDrawingTextView.setText(text);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mSwatchView.setStartingLocation(w - 50, 450);
        mBrushView.setStartingLocation(mSwatchView.left, mSwatchView.bottom);

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);

        if (mDrawing && mDisplaySwatch) {
            mSwatchView.onDraw(canvas);
            mBrushView.onDraw(canvas, mSwatchView.getCurrentColor());
        }

        if (mTextShowing) {
            mDrawingTextView.onDraw(canvas);
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

                mPaths.add(new DrawingPath(drawPath, mSwatchView.getCurrentColorSwatchColor(), mBrushView.getCurrentBrush()));

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
        if (mSwatchView.touchIntersecting(x, y)) {
            drawPaint.setColor(mSwatchView.getCurrentColor());
            return true;
        }

        if (mBrushView.touchIntersecting(x, y)) {
            drawPaint.setStrokeWidth(mBrushView.getCurrentBrushSize());
        }

        return false;
    }

    private class DrawingPath {
        Path path;
        SwatchView.ColorSwatchColor swatchColor;
        BrushView.Brush brush;

        public DrawingPath(Path path, SwatchView.ColorSwatchColor swatchColor, BrushView.Brush brush) {
            this.path = path;
            this.swatchColor = swatchColor;
            this.brush = brush;
        }
    }
}
