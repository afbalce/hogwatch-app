package com.example.hogwatch;

import android.view.View;
import android.widget.*;
import android.content.res.ColorStateList;
import java.util.*;

final class GatewayOverview {
    private final MainActivity ui;
    GatewayOverview(MainActivity ui){this.ui=ui;}
    void show(LinearLayout parent){power(parent);network(parent);sms(parent);}
    void power(LinearLayout parent){
        LinearLayout power=card(parent,"Power details","Solar supply and configured equipment load",1);
        LinearLayout graph=power.findViewById(R.id.gateway_summary_graph);
        double load=0;int missing=0;Set<String> seen=new HashSet<>();
        for(int i=0;i<ui.equipment.size();i++){
            if(!seen.add(ui.equipment.attribute(i,"kind")+"|"+ui.equipment.attribute(i,"name")))continue;
            String watts=ui.equipment.attribute(i,"watts");
            try{double n=Double.parseDouble(watts);if(!Double.isFinite(n)||n<0)missing++;else load+=n;}catch(NumberFormatException e){missing++;}
        }
        double scale=Math.max(15.4,load);
        bar(graph,"Solar · sample","15.4 W",15.4/scale,0xffD19A36);
        bar(graph,"Device load",(missing>0?"≥ ":"")+String.format(Locale.US,"%.2f W",load),load/scale,0xff527DA5);
        bar(graph,"Battery · sample","85%",.85,0xff287C63);
        note(power,missing>0?missing+" devices need wattage values. Net power unavailable.":"Net power: "+String.format(Locale.US,"%.2f W",15.4-load)+" · sample solar input");

    }
    void network(LinearLayout parent){
        LinearLayout network=card(parent,"Network","Sample bandwidth · shared 0–2 Mbps scale",2);
        LinearLayout graph=network.findViewById(R.id.gateway_summary_graph);
        bar(graph,"Download","1.2 Mbps",.6,0xff527DA5);
        bar(graph,"Upload","0.3 Mbps",.15,0xff8B78B5);
        note(network,ui.pref("wifi","").isEmpty()?(ui.gatewayTab==0?"Wi-Fi not configured · Tap to set up":"Wi-Fi not configured · Set up below"):ui.pref("wifi","")+" · Saved network");

    }
    void sms(LinearLayout parent){
        LinearLayout sms=card(parent,"SIM & SMS","Dummy SIM · sample signal strength",3);
        LinearLayout graph=sms.findViewById(R.id.gateway_summary_graph);
        graph.addView(new GatewaySignalView(ui),new LinearLayout.LayoutParams(-1,ui.dp(160)));
        note(sms,ui.pref("carrier","").isEmpty()?(ui.gatewayTab==0?"SMS not configured · Tap to set up":"SMS not configured · Set up below"):ui.pref("carrier","")+" · Delivery telemetry unavailable");
    }
    private LinearLayout card(LinearLayout parent,String title,String caption,int tab){
        LinearLayout card=(LinearLayout)ui.getLayoutInflater().inflate(R.layout.gateway_summary_card,parent,false);
        ((TextView)card.findViewById(R.id.gateway_summary_title)).setText(title);
        ((TextView)card.findViewById(R.id.gateway_summary_caption)).setText(caption);
        card.setOnClickListener(v->{ui.gatewayTab=tab;ui.render();((ScrollView)ui.findViewById(R.id.scroll)).smoothScrollTo(0,0);});
        if(ui.gatewayTab!=0){
            card.setOnClickListener(null);card.setClickable(false);card.setFocusable(false);card.setForeground(null);
            card.findViewById(R.id.gateway_summary_link).setVisibility(View.GONE);
        }
        parent.addView(card);return card;
    }
    private void note(LinearLayout card,String text){((TextView)card.findViewById(R.id.gateway_summary_note)).setText(text);}
    private void bar(LinearLayout parent,String label,String value,double fraction,int color){
        View row=ui.getLayoutInflater().inflate(R.layout.gateway_summary_bar,parent,false);
        ((TextView)row.findViewById(R.id.gateway_bar_label)).setText(label);
        ((TextView)row.findViewById(R.id.gateway_bar_value)).setText(value);
        ProgressBar bar=row.findViewById(R.id.gateway_bar);bar.setProgress((int)(1000*fraction));bar.setProgressTintList(ColorStateList.valueOf(color));
        parent.addView(row);
    }
}

