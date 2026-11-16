package com.example.hogwatch;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import java.util.Locale;

/** Illustrative history only; the final point matches the displayed demo reading. */
final class SensorTrendView extends View {
    private final MainActivity ui;
    private final int metric;
    private final double[] points = new double[25];
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int color;
    SensorTrendView(MainActivity ui, int pen, int metric) {
        super(ui); this.ui=ui; this.metric=metric;
        color=new int[]{0xff237454,0xff367CAE,0xffB37B25,0xff7861A8}[metric];
        double latest=pen<0?ui.mean(metric):ui.sample(pen,metric);
        double amplitude=new double[]{1.6,6,4,180}[metric];
        double end=Math.sin(24*.35+metric);
        for(int i=0;i<points.length;i++)points[i]=latest+amplitude*(Math.sin(i*.35+metric)-end);
        setContentDescription(MainActivity.METRICS[metric]+" illustrative 24-hour line graph. Latest "+ui.value(latest,metric));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        double min=points[0],max=min;
        for(double v:points){min=Math.min(min,v);max=Math.max(max,v);}
        double padding=(max-min)*.2;min-=padding;max+=padding;
        float left=ui.dp(42),right=getWidth()-ui.dp(8),top=ui.dp(12),bottom=getHeight()-ui.dp(28);
        paint.setTextSize(ui.dp(10));paint.setStrokeWidth(ui.dp(1));paint.setStyle(Paint.Style.FILL);
        for(int i=0;i<3;i++){
            float y=top+(bottom-top)*i/2;
            paint.setColor(0xffDFE8DF);c.drawLine(left,y,right,y,paint);
            double value=max-(max-min)*i/2;
            paint.setColor(MainActivity.MUTED);
            c.drawText(String.format(Locale.US,metric==0?"%.1f":"%.0f",value),0,y+ui.dp(3),paint);
        }
        Path line=new Path();
        for(int i=0;i<points.length;i++){
            float x=left+(right-left)*i/24;
            float y=bottom-(float)((points[i]-min)/(max-min))*(bottom-top);
            if(i==0)line.moveTo(x,y);else line.lineTo(x,y);
        }
        paint.setColor(color);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(ui.dp(2.5f));c.drawPath(line,paint);
        paint.setStyle(Paint.Style.FILL);
        c.drawCircle(right,bottom-(float)((points[24]-min)/(max-min))*(bottom-top),ui.dp(4),paint);
        paint.setColor(MainActivity.MUTED);
        c.drawText("24h ago",left,bottom+ui.dp(20),paint);
        c.drawText("12h ago",(left+right)/2-ui.dp(18),bottom+ui.dp(20),paint);
        c.drawText("Now",right-ui.dp(20),bottom+ui.dp(20),paint);
    }
}
