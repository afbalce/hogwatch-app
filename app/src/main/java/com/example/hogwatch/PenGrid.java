package com.example.hogwatch;

import android.widget.*;

final class PenGrid {
    static void add(MainActivity ui,LinearLayout parent,boolean monitoring){
        LinearLayout row=null;
        for(int i=0;i<ui.count();i++){
            final int pen=i;
            if(i%2==0){row=new LinearLayout(ui);row.setBaselineAligned(false);parent.addView(row,new LinearLayout.LayoutParams(-1,-2));}
            LinearLayout card=new LinearLayout(ui);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(ui.dp(10),ui.dp(12),ui.dp(10),ui.dp(12));card.setBackgroundResource(R.drawable.card);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,-2,1);params.topMargin=ui.dp(8);if(i%2==0)params.setMarginEnd(ui.dp(8));row.addView(card,params);
            ui.text(card,ui.name(i),16,MainActivity.INK,true);
            ui.text(card,ui.status(i),12,ui.tint(ui.status(i)),true);
            for(int m:new int[]{3,2,0,1}){
                LinearLayout reading=new LinearLayout(ui);reading.setPadding(0,ui.dp(8),0,0);card.addView(reading);
                TextView label=ui.text(reading,m==0?"Temp":MainActivity.METRICS[m],11,MainActivity.MUTED,false);label.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));
                TextView value=ui.text(reading,ui.reading(i,m),11,MainActivity.INK,true);value.setGravity(android.view.Gravity.END);
            }
            card.setFocusable(true);card.setOnClickListener(v->{ui.selected=pen;PenCameraActivity.open(ui,pen);});
            if(i==ui.count()-1&&i%2==0)row.addView(new android.view.View(ui),new LinearLayout.LayoutParams(0,1,1));
        }
    }
}

