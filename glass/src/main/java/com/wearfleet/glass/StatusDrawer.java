package com.wearfleet.glass;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import com.google.android.glass.timeline.DirectRenderingCallback;

public class StatusDrawer implements SurfaceHolder.Callback, DirectRenderingCallback {
    private static final String TAG = "StatusDrawer";
    private SurfaceHolder mHolder;
    private StatusView mView;
    private boolean paused, dirty;

    private StatusView.ChangeListener mListener = new StatusView.ChangeListener() {
        @Override
        public void onChange() {
            setDirty();
        }
    };

    public StatusDrawer(Context context) {
        mView = new StatusView(context);
        mView.setListener(mListener);
        paused = false;
        dirty = false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mHolder = surfaceHolder;
        dirty = true;
        draw();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

        mView.measure(measuredWidth, measuredHeight);
        mView.layout(0, 0, mView.getMeasuredWidth(), mView.getMeasuredHeight());
        dirty = true;
        draw();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mHolder = null;
    }

    public void setDirty() {
        dirty = true;
        draw();
    }
    private void draw() {
        if(paused || mHolder == null)
            return;
        Canvas canvas;
        try {
            canvas = mHolder.lockCanvas();
            if (canvas != null) {
                mView.draw(canvas);
                mHolder.unlockCanvasAndPost(canvas);
                dirty = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not draw.", e);
            return;
        }

    }

    @Override
    public void renderingPaused(SurfaceHolder surfaceHolder, boolean paused) {
        this.paused = paused;
        if(!paused && dirty)
            draw();
    }
}
