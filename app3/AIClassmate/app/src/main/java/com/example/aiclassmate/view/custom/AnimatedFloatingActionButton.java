package com.example.aiclassmate.view.custom;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class AnimatedFloatingActionButton extends View {
    private Paint mWavePaint;
    private float mRadius = 0;
    private ObjectAnimator mAnimator;

    public AnimatedFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setColor(0x55E91E63);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mAnimator = ObjectAnimator.ofFloat(this, "radius", .8f, 1f);
        this.mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        this.mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        this.mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        this.mAnimator.setDuration(1500).start();
    }

    public void setRadius(float radius) {
        this.mRadius = radius;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public float getRadius() {
        return this.mRadius;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(getWidth() >> 1, getHeight() >> 1, (getWidth() >> 1) * mRadius, mWavePaint);
        super.draw(canvas);
    }
}