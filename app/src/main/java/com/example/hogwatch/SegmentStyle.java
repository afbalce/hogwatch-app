package com.example.hogwatch;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.widget.TextView;

final class SegmentStyle {
    static void apply(MainActivity ui,TextView view,boolean selected,boolean destructive){
        view.setBackgroundResource(R.drawable.segment_background);
        view.setSelected(selected);
        view.setTextColor(new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{android.R.attr.state_selected},new int[]{}},new int[]{0xff89928b,0xffffffff,destructive?0xffa64734:MainActivity.MUTED}));
        view.setTextSize(14);view.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
        view.setMinHeight(ui.dp(48));view.setMinimumHeight(ui.dp(48));
        view.setMinWidth(0);view.setMinimumWidth(0);view.setSingleLine(true);
        view.setPadding(ui.dp(14),0,ui.dp(14),0);
        view.setGravity(android.view.Gravity.CENTER);view.setStateListAnimator(null);view.setElevation(0);
    }
}
