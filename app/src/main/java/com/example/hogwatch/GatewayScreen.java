package com.example.hogwatch;

import android.widget.LinearLayout;

final class GatewayScreen {
    private final MainActivity ui;
    GatewayScreen(MainActivity ui){this.ui=ui;}
    void show(LinearLayout card){
        card.removeAllViews();
        card.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        card.setPadding(0,0,0,0);
        ui.gatewayTab=Math.max(0,Math.min(3,ui.gatewayTab));
        android.view.View tabs=ui.getLayoutInflater().inflate(R.layout.gateway_tabs,card,false);
        card.addView(tabs);
        int[] ids={R.id.gateway_tab_overview,R.id.gateway_tab_power,R.id.gateway_tab_network,R.id.gateway_tab_sms};
        for(int i=0;i<ids.length;i++){
            final int index=i;
            android.widget.Button button=tabs.findViewById(ids[i]);
            button.setSelected(i==ui.gatewayTab);
            button.setTextColor(new android.content.res.ColorStateList(new int[][]{new int[]{android.R.attr.state_selected},new int[]{}},new int[]{0xffFFFFFF,MainActivity.MUTED}));
            button.setOnClickListener(v->{ui.gatewayTab=index;ui.render();});
        }
        if(ui.gatewayTab==0){new GatewayOverview(ui).show(card);return;}
        if(ui.gatewayTab==1)power(card);
        if(ui.gatewayTab==2)network(card);
        if(ui.gatewayTab==3)sms(card);
    }

    private LinearLayout settingsCard(LinearLayout parent,String title){
        LinearLayout card=ui.card(parent,title);
        card.setBackgroundResource(R.drawable.gateway_card_surface);
        card.setPadding(ui.dp(20),ui.dp(20),ui.dp(20),ui.dp(20));
        LinearLayout.LayoutParams params=(LinearLayout.LayoutParams)card.getLayoutParams();
        params.topMargin=ui.dp(16);card.setLayoutParams(params);
        return card;
    }

    private void power(LinearLayout card){
        android.view.View summary=ui.getLayoutInflater().inflate(R.layout.dashboard_power,card,false);
        card.addView(summary);
        ((SolarFlowView)summary.findViewById(R.id.solar_flow)).setSolar(15.4f);
        ((android.widget.TextView)summary.findViewById(R.id.sim_summary)).setText("SIM · Dummy SIM\n4G LTE · −75 dBm");
        new GatewayOverview(ui).power(card);
        LinearLayout power=settingsCard(card,"Power monitoring");
        ui.text(power,"Solar and battery values are illustrative. Device load uses saved wattages.",13,MainActivity.MUTED,false);
        ui.action(power,"Refresh data",()->android.widget.Toast.makeText(ui,"Demo values refreshed. Connect a gateway for live telemetry.",android.widget.Toast.LENGTH_LONG).show());
    }

    private void network(LinearLayout card){
        new GatewayOverview(ui).network(card);
        LinearLayout wifi=settingsCard(card,"Connection settings");


        ui.text(wifi,"Live network telemetry is not connected.",12,MainActivity.MUTED,false);
        ui.pair(wifi,"Network",ui.pref("wifi","").isEmpty()?"Not configured":ui.pref("wifi",""));
        ui.action(wifi,"Configure Wi-Fi",()->ui.form("Gateway Wi-Fi settings",new String[]{"Network name (SSID)"},new String[]{ui.pref("wifi","")},1,v->ui.put("wifi",v[0])));
    }

    private void sms(LinearLayout card){
        new GatewayOverview(ui).sms(card);
        LinearLayout sim=settingsCard(card,"SMS settings");
        ui.pair(sim,"SIM","Dummy SIM");

        ui.pair(sim,"Modem",ui.pref("modem","").isEmpty()?"Not configured":ui.pref("modem",""));
        ui.pair(sim,"Carrier",ui.pref("carrier","").isEmpty()?"Not configured":ui.pref("carrier",""));
        ui.action(sim,"Configure SMS",()->ui.form("Gateway SMS settings",new String[]{"Mobile carrier","Modem identifier (optional)"},new String[]{ui.pref("carrier",""),ui.pref("modem","")},1,v->{ui.put("carrier",v[0]);ui.put("modem",v[1]);}));
        ui.action(sim,"SMS alert settings",()->{ui.go(7);ui.tab=3;ui.render();});
    }
}







