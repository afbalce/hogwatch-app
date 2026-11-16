package com.example.hogwatch;

import android.app.AlertDialog;
import android.view.View;
import android.widget.*;
import android.text.*;
import java.util.Locale;

final class DeviceDirectoryScreen {
    private final MainActivity ui;
    private int filter;
    DeviceDirectoryScreen(MainActivity ui){this.ui=ui;}
    void show(LinearLayout root,String kind){
        root.removeAllViews();root.setBackgroundColor(android.graphics.Color.TRANSPARENT);root.setPadding(0,0,0,0);
        boolean external=kind.equals(EquipmentRegister.CONTROL);
        String plural=external?"External Devices":kind.equals(EquipmentRegister.CAMERA)?"Cameras":"Sensors";
        int total=0,assigned=0;
        for(int i=0;i<ui.equipment.size();i++)if(kind.equals(ui.equipment.attribute(i,"kind"))){total++;if(ui.equipment.assignedPen(i)>=0)assigned++;}
        int demos=external?(!ui.prefs.getBoolean("hide_fan",false)?1:0)+(!ui.prefs.getBoolean("hide_sprinkler",false)?1:0):0;
        total+=demos;
        LinearLayout summary=ui.card(root,plural+" overview");
        LinearLayout top=new LinearLayout(ui);summary.addView(top);stat(top,"Total "+plural.toLowerCase(Locale.ROOT),total,true);
        LinearLayout bottom=new LinearLayout(ui);LinearLayout.LayoutParams margin=new LinearLayout.LayoutParams(-1,-2);margin.topMargin=ui.dp(10);summary.addView(bottom,margin);
        stat(bottom,"Assigned to pens",assigned,false);stat(bottom,"Unassigned",total-assigned,false);
        View search=ui.getLayoutInflater().inflate(R.layout.managed_pen_filters,root,false);root.addView(search);
        EditText query=search.findViewById(R.id.pen_query);query.setHint("Search devices, IDs or pens");query.setContentDescription("Search "+plural);
        View clear=search.findViewById(R.id.clear_pen_search);clear.setVisibility(View.GONE);
        Button filters=search.findViewById(R.id.status_filter);
        LinearLayout list=new LinearLayout(ui);list.setOrientation(1);root.addView(list);
        Runnable update=()->{
            list.removeAllViews();String q=query.getText().toString().trim().toLowerCase(Locale.ROOT);
            clear.setVisibility(q.isEmpty()?View.GONE:View.VISIBLE);
            for(int i=0;i<ui.equipment.size();i++){
                if(!kind.equals(ui.equipment.attribute(i,"kind")))continue;
                int pen=ui.equipment.assignedPen(i);
                if(filter==1&&pen<0||filter==2&&pen>=0)continue;
                String location=pen<0?"Unassigned":ui.name(pen);
                String name=ui.equipment.attribute(i,"name"),type=ui.equipment.attribute(i,"type"),id=ui.equipment.attribute(i,"deviceId");
                if(!(name+" "+type+" "+id+" "+location).toLowerCase(Locale.ROOT).contains(q))continue;
                final int index=i;
                View card=ui.getLayoutInflater().inflate(R.layout.managed_device_card,list,false);
                ((ImageView)card.findViewById(R.id.device_card_icon)).setImageResource(kind.equals(EquipmentRegister.CAMERA)?R.drawable.ic_pen_camera:R.drawable.ic_devices);
                ((TextView)card.findViewById(R.id.device_card_name)).setText(name);
                ((TextView)card.findViewById(R.id.device_card_badge)).setText("Registered");
                ((TextView)card.findViewById(R.id.device_card_type)).setText(type+(id.isEmpty()?"":" · "+id));
                ((TextView)card.findViewById(R.id.device_card_assignment)).setText("Pen · "+location);
                String watts=ui.equipment.attribute(i,"watts");
                ((TextView)card.findViewById(R.id.device_card_details)).setText((watts.isEmpty()?"Power not set":watts+" W")+" · Awaiting telemetry");
                card.setOnClickListener(v->ui.equipment.edit(kind,index,pen));list.addView(card);
            }
            if(external&&filter!=1)new EnvironmentalControlsScreen(ui).showSimulations(list,q);
            if(list.getChildCount()==0)ui.text(list,"No matching devices. Try another search or filter.",14,MainActivity.MUTED,false);
        };
        filters.setOnClickListener(v->new AlertDialog.Builder(ui).setTitle("Device assignments").setSingleChoiceItems(new String[]{"All devices","Assigned to pens","Unassigned"},filter,(d,n)->{filter=n;filters.setText(n==0?"Filters":"Filters (1)");d.dismiss();update.run();}).setNegativeButton("Cancel",null).show());
        clear.setOnClickListener(v->query.setText(""));
        query.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void afterTextChanged(Editable s){}public void onTextChanged(CharSequence s,int a,int b,int c){update.run();}});
        query.setOnEditorActionListener((v,a,e)->{if(a!=android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH)return false;query.clearFocus();((android.view.inputmethod.InputMethodManager)ui.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(query.getWindowToken(),0);return true;});
        update.run();
    }
    private void stat(LinearLayout parent,String label,int value,boolean primary){
        View tile=ui.getLayoutInflater().inflate(R.layout.pen_summary_stat,parent,false);
        ((TextView)tile.findViewById(R.id.stat_value)).setText(""+value);
        ((TextView)tile.findViewById(R.id.stat_label)).setText(label);
        ((ImageView)tile.findViewById(R.id.stat_icon)).setImageResource(R.drawable.ic_devices);
        if(primary){tile.setBackgroundResource(R.drawable.profile_hero);((TextView)tile.findViewById(R.id.stat_value)).setTextColor(-1);((TextView)tile.findViewById(R.id.stat_label)).setTextColor(0xffD6EBDD);((ImageView)tile.findViewById(R.id.stat_icon)).setColorFilter(0xffD6EBDD);}
        LinearLayout.LayoutParams p=(LinearLayout.LayoutParams)tile.getLayoutParams();p.setMarginStart(parent.getChildCount()==0?0:ui.dp(8));parent.addView(tile);
    }
}
