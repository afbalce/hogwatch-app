package com.example.hogwatch;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;

final class PenStatusDialog {
    static void show(MainActivity ui,String[] states,int current,MainActivity.Pick apply){
        show(ui,states,current,apply,"Pen status","Choose which pens to show.");
    }
    static void show(MainActivity ui,String[] states,int current,MainActivity.Pick apply,String title,String hint){
        Dialog dialog=new Dialog(ui);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View root=ui.getLayoutInflater().inflate(R.layout.dialog_pen_status,null);
        dialog.setContentView(root);
        ((TextView)root.findViewById(R.id.filter_dialog_title)).setText(title);
        ((TextView)root.findViewById(R.id.filter_dialog_hint)).setText(hint);
        LinearLayout options=root.findViewById(R.id.filter_options);
        int[] choice={current};
        RadioButton[] buttons=new RadioButton[states.length];
        View[] rows=new View[states.length];
        for(int i=0;i<states.length;i++){
            final int index=i;
            LinearLayout row=new LinearLayout(ui);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(ui.dp(14),0,ui.dp(8),0);
            row.setBackgroundResource(R.drawable.sidebar_item_background);
            View dot=new View(ui);
            GradientDrawable circle=new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(i==0?MainActivity.GREEN:ui.tint(states[i]));
            dot.setBackground(circle);
            row.addView(dot,new LinearLayout.LayoutParams(ui.dp(9),ui.dp(9)));
            RadioButton button=new RadioButton(ui);
            button.setText(states[i]);
            button.setTextSize(15);
            button.setTextColor(MainActivity.INK);
            button.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            button.setTextDirection(View.TEXT_DIRECTION_LTR);
            button.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
            button.setPadding(ui.dp(8),0,ui.dp(12),0);
            button.setButtonTintList(ColorStateList.valueOf(MainActivity.GREEN));
            button.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            row.addView(button,new LinearLayout.LayoutParams(-1,ui.dp(54)));
            LinearLayout.LayoutParams spacing=new LinearLayout.LayoutParams(-1,-2);
            spacing.topMargin=ui.dp(5);
            options.addView(row,spacing);
            buttons[i]=button;rows[i]=row;
            Runnable select=()->{
                choice[0]=index;
                for(int n=0;n<buttons.length;n++){buttons[n].setChecked(n==index);rows[n].setSelected(n==index);}
            };
            button.setOnClickListener(v->select.run());
            row.setOnClickListener(v->select.run());
            button.setChecked(i==current);row.setSelected(i==current);
        }
        root.findViewById(R.id.filter_close).setOnClickListener(v->dialog.dismiss());
        root.findViewById(R.id.filter_reset).setOnClickListener(v->{
            choice[0]=0;
            for(int n=0;n<buttons.length;n++){buttons[n].setChecked(n==0);rows[n].setSelected(n==0);}
        });
        root.findViewById(R.id.filter_apply).setOnClickListener(v->{dialog.dismiss();apply.accept(choice[0]);});
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        Window window=dialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setLayout(Math.min(ui.dp(420),ui.getResources().getDisplayMetrics().widthPixels-ui.dp(32)),-2);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams attrs=window.getAttributes();attrs.dimAmount=.4f;window.setAttributes(attrs);
    }
}


