package com.example.hogwatch;

import android.widget.LinearLayout;

final class PenManagementScreen {
    private final MainActivity ui;

    PenManagementScreen(MainActivity ui) {
        this.ui = ui;
    }

    void show() {
        ui.getLayoutInflater().inflate(R.layout.screen_pen_management, ui.content, true);


        LinearLayout c = ui.findViewById(R.id.pen_overview);
        LinearLayout directory=ui.findViewById(R.id.pen_directory);
        int sensors = 0;
        int cameras = 0;
        for (int i = 0; i < ui.count(); i++) {
            if (!ui.sensor(i).isEmpty()) sensors++;
            if (!ui.camera(i).isEmpty()) cameras++;
        }
        LinearLayout total=new LinearLayout(ui);
        total.setBaselineAligned(false);
        c.addView(total,new LinearLayout.LayoutParams(-1,-2));
        stat(total,"Total pens",ui.count(),R.drawable.ic_pens,true);

        LinearLayout stats=new LinearLayout(ui);
        stats.setBaselineAligned(false);
        LinearLayout.LayoutParams rowSpacing=new LinearLayout.LayoutParams(-1,-2);
        rowSpacing.topMargin=ui.dp(10);
        c.addView(stats,rowSpacing);
        stat(stats,"Pens with cameras",cameras,R.drawable.ic_pen_camera,false);
        stat(stats,"Pens with sensors",sensors,R.drawable.ic_devices,false);

        ui.directory(directory, true);
    }
    private void stat(LinearLayout parent,String label,int value,int icon,boolean primary){
        android.view.View tile=ui.getLayoutInflater().inflate(R.layout.pen_summary_stat,parent,false);
        ((android.widget.TextView)tile.findViewById(R.id.stat_value)).setText(Integer.toString(value));
        ((android.widget.TextView)tile.findViewById(R.id.stat_label)).setText(label);
        android.widget.ImageView symbol=tile.findViewById(R.id.stat_icon);
        symbol.setImageResource(icon);
        symbol.setColorFilter(primary?0xffD6EBDD:MainActivity.GREEN);
        if(primary){
            tile.setBackgroundResource(R.drawable.profile_hero);
            ((android.widget.TextView)tile.findViewById(R.id.stat_value)).setTextColor(0xffFFFFFF);
            ((android.widget.TextView)tile.findViewById(R.id.stat_label)).setTextColor(0xffD6EBDD);
        }
        LinearLayout.LayoutParams spacing=(LinearLayout.LayoutParams)tile.getLayoutParams();
        spacing.setMarginStart(parent.getChildCount()==0?0:ui.dp(8));
        parent.addView(tile);
    }
}




