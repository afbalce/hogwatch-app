package com.example.hogwatch;

import android.app.AlertDialog;
import android.widget.*;

final class EnvironmentalControlsScreen {
    private final MainActivity ui;
    EnvironmentalControlsScreen(MainActivity ui){this.ui=ui;}

    void show(LinearLayout root){
        ui.text(root,"Local simulation · Water estimates use the configured flow rate.",13,MainActivity.MUTED,false);
        ui.action(root,"+ Add controller",()->ui.equipment.add(EquipmentRegister.CONTROL,-1));
        ui.equipment.list(root,EquipmentRegister.CONTROL,-1);
        showSimulations(root,"");
    }
    void showSimulations(LinearLayout root,String query){
        if(!ui.prefs.getBoolean("hide_fan",false)&&"ventilation fan simulation".contains(query))fan(simulationCard(root,"Ventilation fan"));
        if(!ui.prefs.getBoolean("hide_sprinkler",false)&&"sprinkler simulation".contains(query))sprinkler(simulationCard(root,"Sprinkler"));
        if(ui.prefs.getBoolean("hide_fan",false)||ui.prefs.getBoolean("hide_sprinkler",false))
            ui.action(root,"Restore demo controllers",()->{ui.prefs.edit().putBoolean("hide_fan",false).putBoolean("hide_sprinkler",false).apply();ui.render();});
    }

    private LinearLayout simulationCard(LinearLayout parent,String title){
        LinearLayout shell=ui.card(parent,title+"  ›");
        ui.text(shell,"Unassigned · Local simulation",12,MainActivity.MUTED,false);
        LinearLayout details=new LinearLayout(ui);details.setOrientation(1);details.setVisibility(android.view.View.GONE);shell.addView(details);
        android.view.View heading=shell.getChildAt(0);
        heading.setMinimumHeight(ui.dp(48));heading.setFocusable(true);
        heading.setContentDescription(title+", expand controls");
        heading.setOnClickListener(v->{boolean open=details.getVisibility()==android.view.View.VISIBLE;details.setVisibility(open?android.view.View.GONE:android.view.View.VISIBLE);heading.setContentDescription(title+(open?", expand controls":", collapse controls"));});
        return details;
    }

    private void fan(LinearLayout card){
        boolean on=ui.prefs.getBoolean("fan",false);
        ui.pair(card,"Status",on?"Running · simulation":"Off");
        ui.pair(card,"Power draw",on?ui.pref("fan_watts","25")+" W":"0 W");
        ui.action(card,on?"Stop fan":"Start fan",()->{ui.prefs.edit().putBoolean("fan",!on).apply();ui.render();});
        ui.action(card,"Configure fan",()->ui.form("Fan configuration",new String[]{"Power draw (W)"},new String[]{ui.pref("fan_watts","25")},1,v->{positive(v[0]);ui.put("fan_watts",v[0]);}));
        remove(card,"fan");
    }

    private double positive(String value){
        try{double n=Double.parseDouble(value);if(!Double.isFinite(n)||n<=0)throw new NumberFormatException();return n;}
        catch(NumberFormatException e){throw new IllegalArgumentException("Enter a positive number");}
    }

    private void sprinkler(LinearLayout card){
        double capacity=Double.parseDouble(ui.pref("tank_capacity","100"));
        double flow=Double.parseDouble(ui.pref("flow_rate","5"));
        double water=ui.prefs.contains("water_litres")?Double.parseDouble(ui.pref("water_litres","75")):ui.prefs.getInt("water",75);
        ui.pair(card,"Water remaining",String.format(java.util.Locale.US,"%.1f / %.1f L",water,capacity));
        ProgressBar level=new ProgressBar(ui,null,android.R.attr.progressBarStyleHorizontal);
        level.setProgress((int)(water/capacity*100));card.addView(level);
        ui.text(card,String.format(java.util.Locale.US,"About %.1f minutes at %.1f L/min",water/flow,flow),13,MainActivity.MUTED,false);
        ui.text(card,"Spray duration (seconds)",14,MainActivity.INK,true);
        EditText duration=new EditText(ui);duration.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);duration.setText("60");duration.setContentDescription("Spray duration in seconds");card.addView(duration);
        ui.action(card,"Run simulated spray",()->{
            try{
                double seconds=positive(duration.getText().toString()),required=seconds/60*flow;
                if(required>water){duration.setError("Not enough water for this duration");return;}
                ui.put("water_litres",Double.toString(Math.max(0,water-required)));ui.render();
                Toast.makeText(ui,"Simulated spray complete",Toast.LENGTH_SHORT).show();
            }catch(IllegalArgumentException e){duration.setError(e.getMessage());}
        }).setEnabled(water>0);
        ui.action(card,"Configure sprinkler",()->ui.form("Sprinkler configuration",new String[]{"Tank capacity (L)","Flow rate (L/min)"},new String[]{Double.toString(capacity),Double.toString(flow)},2,v->{
            double cap=positive(v[0]);positive(v[1]);
            ui.prefs.edit().putString("tank_capacity",v[0]).putString("flow_rate",v[1]).putString("water_litres",Double.toString(Math.min(cap,water))).apply();
        }));
        ui.action(card,"Refill tank",()->{ui.put("water_litres",Double.toString(capacity));ui.render();});
        remove(card,"sprinkler");
    }

    private void remove(LinearLayout card,String type){
        ui.action(card,"Remove "+type,()->new AlertDialog.Builder(ui).setTitle("Remove "+type+"?")
                .setNegativeButton("Cancel",null).setPositiveButton("Remove",(d,w)->{
                    ui.prefs.edit().putBoolean("hide_"+type,true).putBoolean("fan",type.equals("fan")?false:ui.prefs.getBoolean("fan",false)).apply();ui.render();
                }).show());
    }
}


