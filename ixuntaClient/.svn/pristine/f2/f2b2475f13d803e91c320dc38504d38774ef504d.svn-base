package com.ixunta.client.myview;

import com.ixunta.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
 
public class RoundedCornerImageView extends ImageView {
    private float radius;
 
    public RoundedCornerImageView(Context context) {
        super(context);
    }
 
    public RoundedCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RoundedCornerImageView,
                0, 0);

           try {
               radius = a.getFloat(R.styleable.RoundedCornerImageView_radius, 0);
           } finally {
               a.recycle();
           }
    }
 
    public RoundedCornerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
 
    @Override
    public void setImageDrawable(Drawable d) {
    	if (!(d instanceof BitmapDrawable)){
    		super.setImageDrawable(d);
    		return;
    	}
    	
        Bitmap b = ((BitmapDrawable) d).getBitmap();
        Bitmap bitmap = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Config.ARGB_8888);
        
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 
        canvas.drawARGB(0, 0, 0, 0);
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
        
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(rectF, radius, radius, paint);
 
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(b, rect, rect, paint);
        paint.setXfermode(null);
        
        super.setImageDrawable(new BitmapDrawable(bitmap));
    }
}