package com.sunday.thumbup;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by agentchen on 2017/10/19.
 */

public class ThumbUpView extends View {
    public static final int UP = 0;
    public static final int DOWN = 1;

    private boolean mChecked = false;
    private int mCount;
    private char[] mCurrentChars = new char[0];
    private char[] mPreviousChars = mCurrentChars;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator mValueAnimator;
    private float mAnimationCompleteness = 1f;

    private int mCheckedColor = 0xFFE4583E;
    private int mUncheckedColor = 0xFFCCCCCC;
    private VectorDrawableCompat mDrawableCompat = VectorDrawableCompat.create(
            getResources(), R.drawable.ic_thumb_up_black_24dp, null);

    private float[] mLines = new float[16];
    private float mLineLength = 8;

    private int mDirection = UP;
    private float mCharTranslationY;
    private int mDrawableMargin;

    {
        mPaint.setTextSize(50);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mCharTranslationY = mPaint.getFontSpacing();
        mDrawableMargin = (int) (mPaint.getFontSpacing() * 0.2f);

        mValueAnimator = ValueAnimator
                .ofFloat(0, 1)
                .setDuration(300L);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimationCompleteness = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mValueAnimator.setFloatValues();
        mValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mCurrentChars.length > mPreviousChars.length) {
                    if (getLayoutParams().width == WindowManager.LayoutParams.WRAP_CONTENT) {
                        requestLayout();
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mCurrentChars.length < mPreviousChars.length) {
                    if (getLayoutParams().width == WindowManager.LayoutParams.WRAP_CONTENT) {
                        requestLayout();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        setClickable(true);
    }

    public ThumbUpView(Context context) {
        super(context);
    }

    public ThumbUpView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int defaultWidth = (int) (getDrawableBound() + mDrawableMargin * 2
                + mPaint.measureText(mCurrentChars, 0, mCurrentChars.length)
                + getPaddingLeft() + getPaddingRight());
        int defaultHeight = (int) (mPaint.getFontSpacing() * 3
                + getPaddingTop() + getPaddingBottom());

        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defaultWidth, defaultHeight);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defaultWidth, heightSize);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, defaultHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制图案
        int bound = getDrawableBound();
        int drawableY = (getHeight() - getPaddingTop() - getPaddingBottom() - bound) / 2;

        int drawableLeft = getPaddingLeft() + mDrawableMargin;
        int drawableTop = getPaddingTop() + drawableY;
        mDrawableCompat.setBounds(drawableLeft, drawableTop,
                drawableLeft + bound, drawableTop + bound);
        Rect bounds = mDrawableCompat.getBounds();

        mPaint.setColor(mCheckedColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(8);

        if (mChecked) {
            mPaint.setAlpha((int) ((mCheckedColor >>> 24) * (1 - mAnimationCompleteness)));
            canvas.drawCircle(bounds.left + bounds.width() / 2,
                    bounds.top + bounds.height() / 2,
                    (bounds.width() + mDrawableMargin * 2) / 2 * mAnimationCompleteness,
                    mPaint);
        }

        canvas.save();

        float scale = (mAnimationCompleteness < 0.5 ?
                1 - mAnimationCompleteness * 0.5f : 0.5f + mAnimationCompleteness * 0.5f);
        canvas.scale(scale, scale, bounds.left + bounds.width() / 2,
                bounds.top + bounds.height() / 2);

        if (mChecked) {
            mDrawableCompat.setTint(
                    evaluateHsv(mAnimationCompleteness, mUncheckedColor, mCheckedColor));
        } else {
            mDrawableCompat.setTint(
                    evaluateHsv(mAnimationCompleteness, mCheckedColor, mUncheckedColor));
        }
        mDrawableCompat.draw(canvas);

        mPaint.setStrokeWidth(6);
        if (mChecked) {
            mLines[0] = bounds.left + bounds.width() * 0.25f;
            mLines[1] = bounds.top + bounds.height() * 0.15f;
            mLines[2] = (float) (mLines[0] -
                    Math.cos(Math.PI / 9) * mAnimationCompleteness * mLineLength);
            mLines[3] = (float) (mLines[1] -
                    Math.sin(Math.PI / 9) * mAnimationCompleteness * mLineLength);

            mLines[4] = bounds.left + bounds.width() * 0.4f;
            mLines[5] = bounds.top;
            mLines[6] = (float) (mLines[4] -
                    Math.cos(Math.PI / 3) * mAnimationCompleteness * mLineLength);
            mLines[7] = (float) (mLines[5] -
                    Math.sin(Math.PI / 3) * mAnimationCompleteness * mLineLength);

            mLines[8] = bounds.left + bounds.width() * 0.62f;
            mLines[9] = bounds.top - bounds.height() * 0.08f;
            mLines[10] = mLines[8];
            mLines[11] = mLines[9] - mAnimationCompleteness * mLineLength;

            mLines[12] = bounds.left + bounds.width() * 0.8f;
            mLines[13] = bounds.top + bounds.height() * 0.1f;
            mLines[14] = (float) (mLines[12] +
                    Math.cos(Math.PI / 4) * mAnimationCompleteness * mLineLength);
            mLines[15] = (float) (mLines[13] -
                    Math.sin(Math.PI / 4) * mAnimationCompleteness * mLineLength);

            mPaint.setAlpha((int) ((mCheckedColor >>> 24) * mAnimationCompleteness));
        } else {
            mPaint.setAlpha((int) ((mCheckedColor >>> 24) * (1 - mAnimationCompleteness)));
        }
        canvas.drawLines(mLines, mPaint);

        canvas.restore();

        // 绘制字符
        mPaint.setColor(mUncheckedColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(1);

        float textX = bounds.right + mDrawableMargin;
        Paint.FontMetrics metrics = mPaint.getFontMetrics();
        float baseLine = (metrics.bottom - metrics.top) / 2 - metrics.bottom;
        float textY = getPaddingTop() + (getHeight() - getPaddingBottom() - getPaddingTop()) / 2
                + baseLine;

        // 根据动画完成度计算当前字符y轴坐标与透明度
        float currentY = textY;
        if (mDirection == UP) {
            currentY = textY + mCharTranslationY * (1 - mAnimationCompleteness);
        } else if (mDirection == DOWN) {
            currentY = textY - mCharTranslationY * (1 - mAnimationCompleteness);
        }
        int currentAlpha = (int) (mAnimationCompleteness * (mUncheckedColor >>> 24));

        // 根据动画完成度计算上次字符y轴坐标与透明度
        float previousY = textY;
        if (mDirection == UP) {
            previousY = textY - mCharTranslationY * mAnimationCompleteness;
        } else if (mDirection == DOWN) {
            previousY = textY + mCharTranslationY * mAnimationCompleteness;
        }
        int previousAlpha = (int) ((1 - mAnimationCompleteness) * (mUncheckedColor >>> 24));

        for (int i = 0; i < mCurrentChars.length && i < mPreviousChars.length; i++) {
            if (mCurrentChars[i] != mPreviousChars[i]) {
                // 当前字符与上次字符不同，根据动画完成度绘制
                mPaint.setAlpha(currentAlpha);
                canvas.drawText(mCurrentChars, i, 1, textX, currentY, mPaint);
                mPaint.setAlpha(previousAlpha);
                canvas.drawText(mPreviousChars, i, 1, textX, previousY, mPaint);
            } else {
                // 当前字符与上次字符相同，直接绘制当前字符
                mPaint.setAlpha(mUncheckedColor >>> 24);
                canvas.drawText(mCurrentChars, i, 1, textX, textY, mPaint);
            }
            textX += mPaint.measureText(mCurrentChars, i, 1);
        }

        if (mCurrentChars.length > mPreviousChars.length) {
            mPaint.setAlpha(currentAlpha);
            canvas.drawText(mCurrentChars, mPreviousChars.length,
                    mCurrentChars.length - mPreviousChars.length, textX, currentY, mPaint);
        } else if (mPreviousChars.length > mCurrentChars.length) {
            mPaint.setAlpha(previousAlpha);
            canvas.drawText(mPreviousChars, mCurrentChars.length,
                    mPreviousChars.length - mCurrentChars.length, textX, previousY, mPaint);
        }
    }

    @Override
    public boolean performClick() {
        if (mChecked) {
            mCount--;
            mDirection = DOWN;
        } else {
            mCount++;
            mDirection = UP;
        }
        mChecked = !mChecked;
        mPreviousChars = mCurrentChars;
        mCurrentChars = String.valueOf(mCount).toCharArray();
        dataChange(true);
        return super.performClick();
    }

    private void dataChange(boolean animation) {
        if (animation) {
            mValueAnimator.start();
        } else {
            if (mCurrentChars.length != mPreviousChars.length) {
                if (getLayoutParams().width == WindowManager.LayoutParams.WRAP_CONTENT) {
                    requestLayout();
                }
            }
            mAnimationCompleteness = 1;
            invalidate();
        }
    }

    private int getDrawableBound() {
        return (int) (mPaint.getFontSpacing());
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        dataChange(false);
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        mCount = count;
        mPreviousChars = mCurrentChars;
        mCurrentChars = String.valueOf(count).toCharArray();
        dataChange(false);
    }

    public int getCheckedColor() {
        return mCheckedColor;
    }

    public void setCheckedColor(int checkedColor) {
        mCheckedColor = checkedColor;
        dataChange(false);
    }

    public int getUncheckedColor() {
        return mUncheckedColor;
    }

    public void setUncheckedColor(int uncheckedColor) {
        mUncheckedColor = uncheckedColor;
        dataChange(false);
    }

    public static int evaluateHsv(float fraction, int startValue, int endValue) {
        float[] startHsv = new float[3];
        float[] endHsv = new float[3];
        float[] resultHsv = new float[3];

        Color.colorToHSV(startValue, startHsv);
        Color.colorToHSV(endValue, endHsv);

        float h = endHsv[0] - startHsv[0];
        if (h > 180) {
            endHsv[0] -= 360;
        } else if (h < -180) {
            endHsv[0] += 360;
        }
        resultHsv[0] = startHsv[0] + (endHsv[0] - startHsv[0]) * fraction;
        if (resultHsv[0] > 360) {
            resultHsv[0] -= 360;
        } else if (resultHsv[0] < 0) {
            resultHsv[0] += 360;
        }
        resultHsv[1] = startHsv[1] + (endHsv[1] - startHsv[1]) * fraction;
        resultHsv[2] = startHsv[2] + (endHsv[2] - startHsv[2]) * fraction;
        int alpha = startValue >> 24 + (int) ((endValue >> 24 - startValue >> 24) * fraction);

        return Color.HSVToColor(alpha, resultHsv);
    }
}
