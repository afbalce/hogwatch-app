package com.example.hogwatch;

import android.view.View;
import android.widget.*;

final class OverviewTiles {
    static void add(MainActivity ui,LinearLayout parent,String[] labels,int[] values){
        LinearLayout row=new LinearLayout(ui);row.setBaselineAligned(false);
        parent.addView(row,new LinearLayout.LayoutParams(-1,-2));
        for(int i=0;i<labels.length;i++){
            View tile=ui.getLayoutInflater().inflate(R.layout.pen_summary_stat,row,false);
            tile.findViewById(R.id.stat_icon).setVisibility(View.GONE);
            ((TextView)tile.findViewById(R.id.stat_value)).setText(""+values[i]);
            ((TextView)tile.findViewById(R.id.stat_label)).setText(labels[i]);
            LinearLayout.LayoutParams p=(LinearLayout.LayoutParams)tile.getLayoutParams();p.setMarginStart(i==0?0:ui.dp(8));row.addView(tile);
        }
    }
}
