package com.wearfleet.glass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import com.google.android.glass.timeline.DirectRenderingCallback;

public class StatusViewRenderer implements DirectRenderingCallback {
    private static final String TAG = "StatusDrawer";
    private static final long FRAME_TIME_MILLIS = 33;
    private SurfaceHolder mHolder;
    private StatusView mView;
    private boolean mPaused;

    private RenderThread mRenderThread;

    public StatusViewRenderer(Context context) {
        mView = new StatusView(context);
        mPaused = false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mHolder = surfaceHolder;
        updateRendering();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

        mView.measure(measuredWidth, measuredHeight);
        mView.layout(0, 0, mView.getMeasuredWidth(), mView.getMeasuredHeight());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mHolder = null;
        updateRendering();
    }

    private void draw() {
        Canvas canvas;
        try {
            canvas = mHolder.lockCanvas();
        } catch (Exception e) {
            Log.e(TAG, "Could not lock canvas");
            return;
        }
        if (canvas != null) {
            canvas.drawColor(Color.BLACK);
            mView.draw(canvas);
            mHolder.unlockCanvasAndPost(canvas);
        }
    }

    private synchronized void updateRendering() {
        boolean shouldRender = (mHolder != null) && !mPaused;
        boolean rendering = mRenderThread != null;

        if (shouldRender != rendering) {
            if (shouldRender) {
                mRenderThread = new RenderThread();
                mRenderThread.start();
            } else {
                mRenderThread.quit();
                mRenderThread = null;
            }
        }
    }

    @Override
    public void renderingPaused(SurfaceHolder surfaceHolder, boolean paused) {
        this.mPaused = paused;
        updateRendering();
    }

    private class RenderThread extends Thread {
        private boolean mShouldRun;

        /**
         * Initializes the background rendering thread.
         */
        public RenderThread() {
            mShouldRun = true;
        }

        /**
         * Returns true if the rendering thread should continue to run.
         *
         * @return true if the rendering thread should continue to run
         */
        private synchronized boolean shouldRun() {
            return mShouldRun;
        }

        /**
         * Requests that the rendering thread exit at the next
         * opportunity.
         */
        public synchronized void quit() {
            mShouldRun = false;
        }

        @Override
        public void run() {
            while (shouldRun()) {
                draw();
                SystemClock.sleep(FRAME_TIME_MILLIS);
            }
        }
    }
}
