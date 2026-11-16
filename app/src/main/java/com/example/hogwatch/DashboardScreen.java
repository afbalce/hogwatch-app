package com.example.hogwatch;

import android.widget.*;

final class DashboardScreen {
    private final MainActivity ui;
    DashboardScreen(MainActivity ui){this.ui=ui;}
    void show(){
        ui.getLayoutInflater().inflate(R.layout.screen_dashboard,ui.content,true);
        ((TextView)ui.findViewById(R.id.dashboard_greeting)).setText("Hello, "+ui.pref("name","John Doe").trim()+"!");
        SolarFlowView flow=ui.findViewById(R.id.solar_flow);
        flow.setSolar(15.4f);
        ((TextView)ui.findViewById(R.id.sim_summary)).setText("SIM · Dummy SIM\n4G LTE · −75 dBm");
        LinearLayout summaries=ui.findViewById(R.id.dashboard_sensors);
        int[] metrics={3,2,0,1};
        LinearLayout graphRow=null;
        for(int index=0;index<metrics.length;index++){
            int metric=metrics[index];
            if(index%2==0){
                graphRow=new LinearLayout(ui);graphRow.setBaselineAligned(false);
                summaries.addView(graphRow,new LinearLayout.LayoutParams(-1,-2));
            }
            LinearLayout card=ui.card(graphRow,MainActivity.METRICS[metric]);
            LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(0,-2,1);
            size.topMargin=ui.dp(12);if(index%2==0)size.setMarginEnd(ui.dp(8));
            card.setLayoutParams(size);card.setPadding(ui.dp(10),ui.dp(12),ui.dp(10),ui.dp(10));
            ((TextView)card.findViewById(R.id.cardTitle)).setTextSize(15);
            ui.text(card,ui.value(ui.mean(metric),metric),20,MainActivity.INK,true);
            ui.text(card,"Average · online pens",11,MainActivity.MUTED,false);
            HorizontalScrollView scroll=new HorizontalScrollView(ui);scroll.setFillViewport(true);
            SensorBarView bars=new SensorBarView(ui,metric);bars.setMinimumWidth(ui.dp(ui.count()*18));
            scroll.addView(bars,new android.widget.FrameLayout.LayoutParams(-1,ui.dp(120)));card.addView(scroll);
        }
        LinearLayout pens=ui.findViewById(R.id.dashboard_pens);
        ui.text(pens,"Pen monitoring",18,MainActivity.INK,true);
        ui.text(pens,"Tap a pen for readings, alerts and camera feeds.",13,MainActivity.MUTED,false);
        PenGrid.add(ui,pens,false);
    }
}
