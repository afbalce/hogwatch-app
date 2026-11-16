package com.example.hogwatch;

import android.content.Context;
import android.util.AttributeSet;
import android.graphics.*;
import android.view.View;

public final class PenCameraView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean demo;
    public PenCameraView(Context context,AttributeSet attrs){super(context,attrs);}
    void setDemo(boolean value){demo=value;setContentDescription(value?"Illustrative overhead pen camera demo":"Camera feed unavailable");invalidate();}
    @Override protected void onDraw(Canvas canvas){
        canvas.drawColor(0xff173C2D);
        if(!demo){
            paint.setColor(0xffDDEBE3);paint.setTextSize(14*getResources().getDisplayMetrics().scaledDensity);paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No live stream connected",getWidth()/2f,getHeight()/2f,paint);return;
        }
        canvas.save();
        float scale=Math.min(getWidth()/320f,getHeight()/180f);
        canvas.translate((getWidth()-320*scale)/2,(getHeight()-180*scale)/2);canvas.scale(scale,scale);
        paint.setColor(0xffCBBE9F);canvas.drawRoundRect(8,8,312,172,8,8,paint);
        paint.setColor(0xffA99B7C);canvas.drawRect(18,18,30,162,paint);
        double time=android.os.SystemClock.uptimeMillis()/1700.0;
        pig(canvas,82,115,15);pig(canvas,59,53,175);pig(canvas,210+(float)Math.sin(time)*22,90+(float)Math.cos(time)*20,(float)(time*30));
        canvas.restore();
        if(isShown()&&getWindowVisibility()==VISIBLE)postInvalidateDelayed(100);
    }
    private void pig(Canvas c,float x,float y,float rotation){
        c.save();c.translate(x,y);c.rotate(rotation);paint.setColor(0xffEDBCAA);c.drawOval(-20,-12,16,12,paint);c.drawOval(8,-9,25,9,paint);
        paint.setColor(0xffBF8576);c.drawOval(13,-13,19,-5,paint);c.drawOval(13,5,19,13,paint);c.restore();
    }
}
