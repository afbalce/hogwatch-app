package com.example.hogwatch;

import android.view.*;
import android.widget.*;

final class NotificationSummary {
    private final MainActivity ui;
    private PopupWindow popup;
    void dismiss(){if(popup!=null)popup.dismiss();}
    NotificationSummary(MainActivity ui){this.ui=ui;}
    private void action(LinearLayout parent,String title,boolean primary,Runnable click){
        Button button=new Button(ui);button.setText(title);button.setAllCaps(false);button.setTextSize(13);
        button.setMinWidth(0);button.setMinimumWidth(0);button.setMinHeight(ui.dp(48));button.setPadding(ui.dp(6),ui.dp(8),ui.dp(6),ui.dp(8));
        button.setBackgroundResource(primary?R.drawable.action_primary:R.drawable.action_surface);
        button.setTextColor(primary?0xffFFFFFF:MainActivity.GREEN);button.setStateListAnimator(null);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,-2,1);if(parent.getChildCount()>0)params.setMarginStart(ui.dp(8));
        parent.addView(button,params);button.setOnClickListener(v->click.run());
    }
    void show(View anchor){
        View panel=ui.getLayoutInflater().inflate(R.layout.popup_notifications,null);
        int width=Math.min(ui.dp(360),ui.getResources().getDisplayMetrics().widthPixels-ui.dp(32));
        popup=new PopupWindow(panel,width,-2,true);
        popup.setBackgroundDrawable(ui.getDrawable(R.drawable.gateway_card_surface));
        popup.setElevation(ui.dp(12));popup.setOutsideTouchable(true);
        panel.findViewById(R.id.notification_close).setOnClickListener(v->popup.dismiss());
        LinearLayout list=panel.findViewById(R.id.notification_items);
        String[] titles={"Ammonia above limit","Temperature rising","Sensor battery low","Sensor offline"};
        int active=0;
        for(int i=0;i<titles.length;i++){
            if(ui.workflow(i).equals("Resolved")||ui.alertPen(i)<0)continue;
            active++;
            LinearLayout row=new LinearLayout(ui);row.setOrientation(1);row.setPadding(ui.dp(12),ui.dp(10),ui.dp(12),ui.dp(10));row.setBackgroundResource(R.drawable.pen_stat_surface);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=ui.dp(8);list.addView(row,p);
            ui.text(row,MainActivity.LEVELS[i]+" · "+ui.name(ui.alertPen(i)),12,ui.tint(MainActivity.LEVELS[i]),true);
            ui.text(row,titles[i],15,MainActivity.INK,true);
            row.setBackground(AlertAppearance.background(ui,MainActivity.LEVELS[i],false));
            boolean reminder=ui.prefs.getBoolean("alert_reminder"+i,false);
            ui.text(row,reminder?"Reminder · Still unresolved":ui.workflow(i),12,MainActivity.MUTED,false);
            final int id=i;
            row.setClickable(true);row.setFocusable(true);
            android.util.TypedValue ripple=new android.util.TypedValue();
            ui.getTheme().resolveAttribute(android.R.attr.selectableItemBackground,ripple,true);
            if(ripple.resourceId!=0)row.setForeground(ui.getDrawable(ripple.resourceId));
            row.setOnClickListener(v->{popup.dismiss();ui.openAlert(id);});
            LinearLayout actions=new LinearLayout(ui);
            LinearLayout.LayoutParams spacing=new LinearLayout.LayoutParams(-1,-2);spacing.topMargin=ui.dp(10);row.addView(actions,spacing);
            if(ui.workflow(i).equals("Open")){
                action(actions,"Acknowledge",false,()->{ui.acknowledgeAlert(id);popup.dismiss();ui.render();});
            }
            action(actions,"Resolve",true,()->{popup.dismiss();ResolveAlertDialog.show(ui,id,ui.name(ui.alertPen(id))+" · "+titles[id]);});
        }
        ((TextView)panel.findViewById(R.id.notification_count)).setText(active==0?"You're all caught up":active+(active==1?" active notification":" active notifications"));
        if(active==0)ui.text(list,"No active alerts to review.",14,MainActivity.MUTED,false);
        int[] position=new int[2];anchor.getLocationOnScreen(position);
        int available=ui.getResources().getDisplayMetrics().heightPixels-position[1]-anchor.getHeight()-ui.dp(160);
        panel.findViewById(R.id.notification_scroll).getLayoutParams().height=Math.min(ui.dp(300),Math.max(ui.dp(100),available));
        popup.showAsDropDown(anchor,anchor.getWidth()-width,ui.dp(8));
    }
}



