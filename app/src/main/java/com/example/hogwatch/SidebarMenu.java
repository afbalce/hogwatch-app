package com.example.hogwatch;

import android.app.*;
import android.view.*;
import android.widget.*;

final class SidebarMenu {
    private final MainActivity ui;
    private Dialog dialog;
    private final java.util.List<View> menuItems=new java.util.ArrayList<>();
    SidebarMenu(MainActivity ui){this.ui=ui;}
    void show(){
        if(dialog!=null&&dialog.isShowing()){dialog.dismiss();return;}
        dialog=new SlidingDialog(ui);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View root=ui.getLayoutInflater().inflate(R.layout.sidebar_menu,null);dialog.setContentView(root);
        menuItems.clear();
        menuItems.add(root.findViewById(R.id.sidebar_profile));
        LinearLayout body=root.findViewById(R.id.sidebar_body);
        ProfilePhoto.show(ui,root.findViewById(R.id.sidebar_photo));
        ((TextView)root.findViewById(R.id.sidebar_name)).setText(ui.pref("name","John Doe"));
        ((TextView)root.findViewById(R.id.sidebar_username)).setText("@"+ui.pref("username","john.doe"));
        root.findViewById(R.id.sidebar_profile).setSelected(ui.page==8);
        root.findViewById(R.id.sidebar_profile).setContentDescription("Edit profile for "+ui.pref("name","John Doe"));
        root.findViewById(R.id.sidebar_profile).setOnClickListener(v->{selectItem(v);open(8,0);});
        root.findViewById(R.id.sidebar_close).setOnClickListener(v->dialog.dismiss());
        item(body,"Pen Management",()->open(2,0),false);
        LinearLayout devices=group(body,"Device Management",()->open(6,0));
        item(devices,"Gateway",()->open(6,DeviceManagementScreen.GATEWAY),true);
        item(devices,"Sensors",()->open(6,DeviceManagementScreen.SENSORS),true);
        item(devices,"Cameras",()->open(6,DeviceManagementScreen.CAMERAS),true);
        item(devices,"External Devices",()->open(6,DeviceManagementScreen.CONTROLS),true);
        LinearLayout settings=group(body,"Settings",()->open(7,0));
        item(settings,"Security",()->open(7,0),true);
        item(settings,"Alerts",()->open(7,1),true);
        item(settings,"Recording",()->open(7,2),true);
        item(settings,"Notifications",()->open(7,3),true);
        item(body,"About App",()->new AlertDialog.Builder(ui).setTitle("About HogWatch")
            .setMessage("HogWatch · Farm Sentinels\nVersion 1.0\n\nMonitor pens, manage devices and review environmental readings.\n\nThis build uses local sample readings and stores preferences on this device. A live gateway and account service are not connected.")
            .setPositiveButton("Close",null).show(),false);
        Button logout=item(body,"Log out",()->new AlertDialog.Builder(ui).setTitle("Log out?").setMessage("End this local session? Your saved farm data will stay on this device.")
            .setNegativeButton("Cancel",null).setPositiveButton("Log out",(d,w)->{dialog.dismiss();ui.prefs.edit().putBoolean("logged_out",true).apply();ui.render();}).show(),false);
        logout.setTextColor(0xffA64734);
        dialog.setCanceledOnTouchOutside(true);dialog.show();
        Window window=dialog.getWindow();window.setBackgroundDrawableResource(android.R.color.transparent);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams attrs=window.getAttributes();attrs.width=Math.min(ui.dp(320),ui.getResources().getDisplayMetrics().widthPixels-ui.dp(48));attrs.height=-1;attrs.gravity=Gravity.LEFT|Gravity.TOP;attrs.dimAmount=.35f;window.setAttributes(attrs);
        root.setTranslationX(-attrs.width);root.animate().translationX(0).setDuration(200).start();
    }
    private void open(int page,int tab){dialog.dismiss();ui.go(page);ui.tab=tab;ui.render();}
    private LinearLayout group(LinearLayout parent,String title,Runnable overview){
        LinearLayout children=new LinearLayout(ui);children.setOrientation(1);children.setVisibility(View.GONE);children.setBackgroundResource(R.drawable.chip);children.setPadding(ui.dp(8),ui.dp(4),ui.dp(8),ui.dp(4));
        children.setTag(title);
        Button button=item(parent,title,()->{},false);
        boolean active=title.equals("Device Management")?ui.page==6:ui.page==7;
        children.setVisibility(active?View.VISIBLE:View.GONE);
        button.setContentDescription(title+(active?", expanded":", collapsed"));
        button.setOnClickListener(v->{boolean expanded=children.getVisibility()==View.VISIBLE;children.setVisibility(expanded?View.GONE:View.VISIBLE);selectItem(button);button.setContentDescription(title+(expanded?", collapsed":", expanded"));});
        parent.addView(children);return children;
    }
    private Button item(LinearLayout parent,String text,Runnable action,boolean child){
        Button b=new Button(ui);b.setText(text);b.setAllCaps(false);b.setTextSize(child?14:15);b.setTextColor(MainActivity.INK);b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);b.setPadding(ui.dp(child?28:12),0,ui.dp(8),0);b.setMinHeight(ui.dp(52));b.setBackgroundResource(R.drawable.sidebar_item_background);b.setSelected(selected(text,parent));b.setStateListAnimator(null);b.setElevation(0);menuItems.add(b);b.setOnClickListener(v->{selectItem(b);action.run();});        if(!child){
            int icon=text.startsWith("Pen")?R.drawable.ic_pens:text.startsWith("Device")?R.drawable.ic_devices:text.startsWith("Settings")?R.drawable.ic_settings:text.startsWith("About")?R.drawable.ic_reports:R.drawable.ic_more;
            android.graphics.drawable.Drawable symbol=ui.getDrawable(icon).mutate();symbol.setTint(MainActivity.GREEN);symbol.setBounds(0,0,ui.dp(20),ui.dp(20));b.setCompoundDrawables(symbol,null,null,null);b.setCompoundDrawablePadding(ui.dp(12));
        }
        LinearLayout.LayoutParams spacing=new LinearLayout.LayoutParams(-1,-2);spacing.topMargin=ui.dp(child?2:8);parent.addView(b,spacing);return b;
    }
    private void selectItem(View selected){
        for(View item:menuItems)item.setSelected(item==selected);
    }
    private boolean selected(String text,LinearLayout parent){
        if(text.equals("Pen Management"))return ui.page==2;
        if(text.equals("Device Management"))return false;
        if(text.equals("Settings"))return false;
        if("Device Management".equals(parent.getTag())&&ui.page==6){
            return text.equals("External Devices")&&ui.tab==3||text.equals("Gateway")&&ui.tab==4||text.equals("Sensors")&&ui.tab==2||text.equals("Cameras")&&ui.tab==1;
        }
        if("Settings".equals(parent.getTag())&&ui.page==7){
            return text.equals("Security")&&ui.tab==0||text.equals("Alerts")&&ui.tab==1||text.equals("Recording")&&ui.tab==2||text.equals("Notifications")&&ui.tab==3;
        }
        return false;
    }    private static final class SlidingDialog extends Dialog {
        private float startX,startY;
        private boolean swiping,closing;
        SlidingDialog(MainActivity ui){super(ui);}
        @Override public boolean dispatchTouchEvent(MotionEvent event){
            float density=getContext().getResources().getDisplayMetrics().density;
            if(event.getActionMasked()==MotionEvent.ACTION_DOWN){startX=event.getX();startY=event.getY();swiping=false;}
            float dx=event.getX()-startX,dy=event.getY()-startY;
            if(event.getActionMasked()==MotionEvent.ACTION_MOVE&&!swiping&&dx< -24*density&&Math.abs(dx)>Math.abs(dy)*1.5f){
                swiping=true;MotionEvent cancel=MotionEvent.obtain(event);cancel.setAction(MotionEvent.ACTION_CANCEL);super.dispatchTouchEvent(cancel);cancel.recycle();
            }
            if(swiping){
                View panel=findViewById(android.R.id.content);
                if(event.getActionMasked()==MotionEvent.ACTION_MOVE)panel.setTranslationX(Math.min(0,dx));
                if(event.getActionMasked()==MotionEvent.ACTION_UP||event.getActionMasked()==MotionEvent.ACTION_CANCEL){
                    if(event.getActionMasked()==MotionEvent.ACTION_UP&&dx< -64*density)dismiss();
                    else panel.animate().translationX(0).setDuration(160).start();
                    swiping=false;
                }
                return true;
            }
            return super.dispatchTouchEvent(event);
        }
        @Override public void cancel(){dismiss();}
        @Override public void dismiss(){
            if(closing||!isShowing())return;
            closing=true;View panel=findViewById(android.R.id.content);
            panel.animate().translationX(-getWindow().getDecorView().getWidth()).alpha(.7f).setDuration(180)
                .withEndAction(()->{super.dismiss();closing=false;}).start();
        }
    }}



