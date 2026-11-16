package com.example.hogwatch;

import android.graphics.drawable.GradientDrawable;

final class AlertAppearance {
    static GradientDrawable background(MainActivity ui,String severity,boolean resolved){
        int fill=0xffFFFFFF,border=0xffE2EAE5;
        if(!resolved&&severity.equals("Warning")){fill=0xffFFF7DC;border=0xffEAD59A;}
        if(!resolved&&severity.equals("Critical")){fill=0xffFFF0ED;border=0xffE8B5AB;}
        GradientDrawable bg=new GradientDrawable();bg.setColor(fill);bg.setCornerRadius(ui.dp(20));bg.setStroke(ui.dp(1),border);return bg;
    }
}
