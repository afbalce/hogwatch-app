package com.example.hogwatch;

import android.content.Context;
import android.util.AttributeSet;
import android.graphics.*;
import android.view.View;
import java.util.Locale;

/** Illustrative solar, battery and load flow. No physical device commands. */
public final class SolarFlowView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private float solar=15.4f;
    private float d(float value){return value*getResources().getDisplayMetrics().density;}
    public SolarFlowView(Context context,AttributeSet attrs){super(context,attrs);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
    void setSolar(float watts){solar=watts;setContentDescription(String.format(Locale.US,"Simulation. Solar %.1f watts. Battery 85 percent. Load 3.8 watts. Battery %s.",solar,solar>=3.8f?"charging":"discharging"));invalidate();}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);float w=getWidth(),y=d(50),r=d(22);float[] xs={w/6,w/2,w*5/6};
        paint.setStrokeWidth(d(1.5f));paint.setColor(0xffB6CDBD);
        for(int i=0;i<2;i++)c.drawLine(xs[i]+r,y,xs[i+1]-r,y,paint);
        float phase=(android.os.SystemClock.uptimeMillis()%1800)/1800f;
        paint.setColor(0xff31956A);
        if(solar>0){float t=solar>=3.8f?phase:1-phase;c.drawCircle(xs[0]+r+(xs[1]-xs[0]-2*r)*t,y,d(3),paint);}
        c.drawCircle(xs[1]+r+(xs[2]-xs[1]-2*r)*phase,y,d(3),paint);
        for(int i=0;i<3;i++){
            paint.setStyle(Paint.Style.FILL);paint.setColor(new int[]{0xffEDF3FF,0xffE8F5EB,0xffFFF3E5}[i]);c.drawCircle(xs[i],y,r,paint);
            paint.setColor(new int[]{0xff426CC0,0xff237454,0xffA56B28}[i]);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(d(1.7f));
            float x=xs[i];
            if(i==0){c.drawRect(x-d(11),y-d(7),x+d(11),y+d(7),paint);c.drawLine(x,y-d(7),x,y+d(7),paint);c.drawLine(x-d(11),y,x+d(11),y,paint);c.drawLine(x,y+d(7),x,y+d(12),paint);c.drawLine(x-d(6),y+d(12),x+d(6),y+d(12),paint);}
            else if(i==1){c.drawRoundRect(x-d(12),y-d(7),x+d(10),y+d(7),d(2),d(2),paint);c.drawLine(x+d(13),y-d(3),x+d(13),y+d(3),paint);paint.setStyle(Paint.Style.FILL);c.drawRect(x-d(9),y-d(4),x+d(5),y+d(4),paint);}
            else{c.drawRoundRect(x-d(10),y-d(11),x+d(10),y+d(11),d(3),d(3),paint);c.drawLine(x-d(5),y-d(3),x+d(5),y-d(3),paint);c.drawLine(x-d(5),y+d(4),x+d(5),y+d(4),paint);}
            paint.setStyle(Paint.Style.FILL);paint.setTextAlign(Paint.Align.CENTER);paint.setColor(0xff183A2B);paint.setTextSize(d(12));
            c.drawText(new String[]{"Solar","Battery","Load"}[i],x,y+r+d(21),paint);
            paint.setTypeface(Typeface.DEFAULT_BOLD);c.drawText(new String[]{String.format(Locale.US,"%.1f W",solar),"85%","3.8 W"}[i],x,y+r+d(39),paint);paint.setTypeface(Typeface.DEFAULT);
        }
        if(isShown()&&getWindowVisibility()==VISIBLE)postInvalidateDelayed(50);
    }
}
