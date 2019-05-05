package com.example.xianyang.libraryproject;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by xianyang on 2018/4/23.
 * 绘制底部工具栏
 */

public class ChangeColorIconWithText extends View {
    private int mColor=0xff45c01a;
    private Bitmap mIconBitmap;//图片位图
    private String mtext="微信";
    private int mTextSize= (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,12,getResources().getDisplayMetrics());
    private Canvas mcavas;
    private Bitmap mbitmap;
    private Paint mPaint;
    private float mAlpha;//透明度
    private Rect mIconRect;
    private Rect mTextBound;
    private Paint mTextPaint;
    private static final String INSTANCE_STATUS="instance_status";
    private static final String STATUS_ALPHA="status_alpha";
    public ChangeColorIconWithText(Context context) {
        this(context,null);
    }

    public ChangeColorIconWithText(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ChangeColorIconWithText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChangeColorIconWithText);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++)
        {
            int attr=a.getIndex(i);//设置view属性
            switch (attr)
            {
                case R.styleable.ChangeColorIconWithText_icon:
                    BitmapDrawable drawable=(BitmapDrawable) a.getDrawable(attr);
                    mIconBitmap=drawable.getBitmap();//得到icon位图
                    break;
                case R.styleable.ChangeColorIconWithText_color:
                    mColor=a.getColor(attr,0xff45c01a);
                    break;
                case R.styleable.ChangeColorIconWithText_text:
                    mtext=a.getString(attr);
                    break;
                case R.styleable.ChangeColorIconWithText_text_size:
                    mTextSize=(int)a.getDimension(attr,TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,12,getResources().getDisplayMetrics()));
                    break;
            }
        }
        a.recycle();//回收资源
        mTextBound=new Rect();
        mTextPaint=new Paint();
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(0xff555555);
        mTextPaint.getTextBounds(mtext,0,mtext.length(),mTextBound);
    }

    @Nullable
    @Override
    //保存当前状态，避免aciivity被回收，程序出错
    protected Parcelable onSaveInstanceState() {
        Bundle bundle=new Bundle();
        bundle.putParcelable(INSTANCE_STATUS,super.onSaveInstanceState());
        bundle.putFloat(STATUS_ALPHA,mAlpha);
        return bundle;
    }
   //保存透明度值，防止重置
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(state instanceof Bundle)
        {
            Bundle bundle=(Bundle) state;
            mAlpha=bundle.getFloat("STATUS_ALPHA");
            super.onRestoreInstanceState(bundle.getParcelable("INSTANCE_STATUS"));
            return;
        }
        super.onRestoreInstanceState(state);
    }
    //绘制Icon位置
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int iconWidth=Math.min(getMeasuredWidth()-getPaddingLeft()-getPaddingRight(),
                getMeasuredHeight()-getPaddingTop()-mTextBound.height());
        int left=getMeasuredWidth()/2-iconWidth/2;
        int top=(getMeasuredHeight()-mTextBound.height())/2-iconWidth/2;
        mIconRect=new Rect(left,top,left+iconWidth,top+iconWidth);
    }
//绘制bitmap
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mIconBitmap,null,mIconRect,null);
        //内存准备bitmap，setAlpha,纯色，xfermode,图标
        int alpha=(int)Math.ceil(255*mAlpha);
        setupTargetBitmap(alpha);
        //绘制文本，绘制变色文本
        drawSourceText(canvas,alpha);
        drawTargetText(canvas,alpha);
        canvas.drawBitmap(mbitmap,0,0,null);
    }
//绘制变色文本
    private void drawTargetText(Canvas canvas, int alpha) {
        mTextPaint.setColor(mColor);
        mTextPaint.setAlpha(alpha);
        int x=getMeasuredWidth()/2-mTextBound.width()/2;
        int y=mIconRect.bottom+mTextBound.height();
        canvas.drawText(mtext,x,y,mTextPaint);
    }

    private void drawSourceText(Canvas canvas, int alpha) {
       mTextPaint.setColor(0xffffff);
       mTextPaint.setAlpha(255-alpha);
       int x=getMeasuredWidth()/2-mTextBound.width()/2;
       int y=mIconRect.bottom+mTextBound.height();
       canvas.drawText(mtext,x,y,mTextPaint);
    }
//绘制图标
    private void setupTargetBitmap(int alpha) {
      mbitmap =Bitmap.createBitmap(getMeasuredWidth(),getMeasuredHeight(), Bitmap.Config.ARGB_8888);
      mcavas=new Canvas(mbitmap);
      mPaint=new Paint();
      mPaint.setColor(mColor);
      mPaint.setAntiAlias(true);
      mPaint.setDither(true);
      mPaint.setAlpha(alpha);
      mcavas.drawRect(mIconRect,mPaint);
      mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
      mPaint.setAlpha(255);
      mcavas.drawBitmap(mIconBitmap,null,mIconRect,mPaint);
    }
    //设置透明度
    public void setIconAlpha(float alpha)
    {
        this.mAlpha=alpha;
        invalidateView();
    }
//重绘
    private void invalidateView() {
        if (Looper.getMainLooper()==Looper.myLooper())
        {
            invalidate();
        }else {
            postInvalidate();
        }
    }
}
