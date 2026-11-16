package com.example.hogwatch;

import android.graphics.*;
import android.view.View;

final class SensorBarView extends View {
    private final MainActivity ui;
    private final int metric;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    SensorBarView(MainActivity ui,int metric){super(ui);this.ui=ui;this.metric=metric;
        StringBuilder description=new StringBuilder(MainActivity.METRICS[metric]+" demo readings. ");
        for(int i=0;i<ui.count();i++)description.append(ui.name(i)).append(": ").append(ui.reading(i,metric)).append(". ");
        setContentDescription(description.toString());setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        int count=ui.count();if(count==0)return;
        float step=getWidth()/(float)count,base=getHeight()-ui.dp(25),top=ui.dp(24);
        double max=1;for(int i=0;i<count;i++)if(ui.hasReading(i))max=Math.max(max,ui.sample(i,metric));
        paint.setTextSize(ui.dp(step<ui.dp(32)?9:10));paint.setTextAlign(Paint.Align.CENTER);
        for(int i=0;i<count;i++){
            float x=step*(i+.5f),half=Math.min(ui.dp(16),step*.26f);
            float height=ui.hasReading(i)?(float)(ui.sample(i,metric)/max)*(base-top):0;
            paint.setColor(new int[]{0xff237454,0xff367CAE,0xffB37B25,0xff7861A8}[metric]);
            if(ui.hasReading(i))canvas.drawRoundRect(x-half,base-height,x+half,base,ui.dp(4),ui.dp(4),paint);
            paint.setColor(MainActivity.MUTED);
            String value=ui.hasReading(i)?ui.value(ui.sample(i,metric),metric).split(" ")[0]:"—";
            if(step>=ui.dp(32)||!ui.hasReading(i))canvas.drawText(value,x,base-height-ui.dp(6),paint);
            canvas.drawText("P"+ui.penNumber(i),x,base+ui.dp(18),paint);
        }
    }
}

