package com.wearfleet.glass;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.View;

import de.greenrobot.event.EventBus;

public class StatusDrawer implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private StatusView mView;

    private StatusView.ChangeListener mListener = new StatusView.ChangeListener() {
        @Override
        public void onChange() {
            if (mHolder != null) {
                draw();
            }
        }
    };

    public StatusDrawer(Context context) {
        mView = new StatusView(context);
        mView.setListener(mListener);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mHolder = surfaceHolder;
        draw();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

        mView.measure(measuredWidth, measuredHeight);
        mView.layout(0, 0, mView.getMeasuredWidth(), mView.getMeasuredHeight());
        draw();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mHolder = null;
        EventBus.getDefault().unregister(this);
    }

    public void draw() {
        Canvas canvas;
        try {
            canvas = mHolder.lockCanvas();
        } catch (Exception e) {
            return;
        }
        if (canvas != null) {
            mView.draw(canvas);
            mHolder.unlockCanvasAndPost(canvas);
        }
    }
}
