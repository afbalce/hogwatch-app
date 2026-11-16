package com.example.hogwatch;

import android.graphics.*;
import android.view.View;

/** Signal strength illustration on an explicitly labeled -120 to -50 dBm scale. */
final class GatewaySignalView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    GatewaySignalView(MainActivity ui){super(ui);setContentDescription("Sample signal: minus 75 dBm. Scale minus 120 to minus 50 dBm.");}
    private float d(float n){return n*getResources().getDisplayMetrics().density;}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        float cx=getWidth()/2f,cy=d(76),radius=Math.min(d(62),getWidth()/2f-d(16));
        RectF arc=new RectF(cx-radius,cy-radius,cx+radius,cy+radius);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(d(10));paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(0xffEAF0EC);canvas.drawArc(arc,150,240,false,paint);
        paint.setColor(0xff287C63);canvas.drawArc(arc,150,240*45f/70f,false,paint);
        paint.setStyle(Paint.Style.FILL);paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);paint.setColor(0xff183A2B);paint.setTextSize(d(27));canvas.drawText("−75",cx,cy+d(4),paint);
        paint.setTypeface(Typeface.DEFAULT);paint.setTextSize(d(12));paint.setColor(0xff587062);canvas.drawText("dBm · 4G LTE",cx,cy+d(24),paint);
        paint.setTextSize(d(11));canvas.drawText("−120",cx-radius,cy+radius+d(10),paint);canvas.drawText("−50",cx+radius,cy+radius+d(10),paint);
    }
}
