package com.example.hogwatch;

import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

/** Groups adjacent actions without moving controls past their related content. */
final class ActionSlider {
    static void append(MainActivity ui,LinearLayout parent,Button button,LinearLayout.LayoutParams params){
        if(parent.getOrientation()==LinearLayout.HORIZONTAL){
            style(ui,button,params);parent.addView(button,params);return;
        }
        View last=parent.getChildCount()==0?null:parent.getChildAt(parent.getChildCount()-1);
        HorizontalScrollView slider=null;
        if(last instanceof HorizontalScrollView && "action_slider".equals(last.getTag())){
            slider=(HorizontalScrollView)last;
        }else if(last instanceof Button && "slider_action".equals(last.getTag())){
            parent.removeView(last);
            slider=(HorizontalScrollView)ui.getLayoutInflater().inflate(R.layout.action_slider,parent,false);
            parent.addView(slider);
            LinearLayout.LayoutParams previous=(LinearLayout.LayoutParams)last.getLayoutParams();
            style(ui,(Button)last,previous);
            ((LinearLayout)slider.getChildAt(0)).addView(last,previous);
        }
        button.setTag("slider_action");
        if(slider==null){parent.addView(button,params);return;}
        style(ui,button,params);
        ((LinearLayout)slider.getChildAt(0)).addView(button,params);
    }
    private static void style(MainActivity ui,Button button,LinearLayout.LayoutParams params){
        String label=button.getText().toString();
        SegmentStyle.apply(ui,button,false,label.startsWith("Remove")||label.startsWith("Delete"));
        if(label.startsWith("+")){button.setBackgroundResource(R.drawable.action_primary);button.setTextColor(android.graphics.Color.WHITE);}
        params.topMargin=0;params.setMarginEnd(0);
    }}
