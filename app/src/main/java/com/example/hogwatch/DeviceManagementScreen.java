package com.example.hogwatch;

import android.widget.LinearLayout;

final class DeviceManagementScreen {
    private final MainActivity ui;

    DeviceManagementScreen(MainActivity ui) {
        this.ui = ui;
    }

    void show() {
        ui.getLayoutInflater().inflate(R.layout.screen_device_management, ui.content, true);
        LinearLayout c = ui.findViewById(R.id.gateway);
        LinearLayout controls = ui.findViewById(R.id.controls);
        LinearLayout reg = ui.findViewById(R.id.register);
        String[] sections = {"Cameras","Sensors","Environmental controls","Gateway"};
        LinearLayout switcher = ui.findViewById(R.id.equipment_sections);
        ui.picker(switcher,"Manage",sections,ui.tab,i->{ui.tab=i;ui.render();});
        reg.setVisibility(ui.tab < 2 ? android.view.View.VISIBLE : android.view.View.GONE);
        controls.setVisibility(ui.tab == 2 ? android.view.View.VISIBLE : android.view.View.GONE);
        c.setVisibility(ui.tab == 3 ? android.view.View.VISIBLE : android.view.View.GONE);
        if (ui.tab < 2) {
            String kind = ui.tab == 0 ? EquipmentRegister.CAMERA : EquipmentRegister.SENSOR;
            ((android.widget.TextView)reg.getChildAt(0)).setText(sections[ui.tab]);
            ui.action(reg,"+ Add "+kind.toLowerCase(java.util.Locale.ROOT),()->ui.equipment.add(kind,-1));
            ui.equipment.list(reg,kind,-1);
            return;
        }
        ui.pair(c, "Status", "Online \u00b7 demo");
        ui.pair(c, "Solar generation", "15.4 W");
        ui.pair(c, "Gateway battery", "85%");
        ui.pair(c, "Latency", "42 ms");
        ui.pair(c, "Wi-Fi network", ui.pref("wifi", "Unconfigured"));
        ui.pair(c, "SMS carrier", ui.pref("carrier", "Unconfigured"));
        ui.action(c, "Configure gateway", ()->ui.form("Local gateway settings", new String[]{"Wi-Fi network", "SMS carrier"}, new String[]{ui.pref("wifi", ""), ui.pref("carrier", "")}, 0, (v)->{
            ui.put("wifi", v[0]);
            ui.put("carrier", v[1]);
        }));
        ui.action(controls,"+ Add environmental control",()->ui.equipment.add(EquipmentRegister.CONTROL,-1));
        ui.equipment.list(controls,EquipmentRegister.CONTROL,-1);
        ui.text(controls, "Local simulation", 13, MainActivity.MUTED, false);
        boolean fan = ui.prefs.getBoolean("fan", false);
        ui.pair(controls, "Ventilation fan", fan ? "Running" : "Off");
        ui.action(controls, fan ? "Stop fan" : "Start fan", ()->{
            ui.prefs.edit().putBoolean("fan", !fan).apply();
            ui.render();
        });
        int water = ui.prefs.getInt("water", 75);
        ui.pair(controls, "Sprinkler reservoir", water + " / 100 L");
        ui.action(controls, "Simulate 1 minute spray", ()->{
            ui.prefs.edit().putInt("water", water - 5).apply();
            ui.render();
        }).setEnabled(water >= 5);
        ui.action(controls, "Refill reservoir", ()->{
            ui.prefs.edit().putInt("water", 100).apply();
            ui.render();
        });
    }
}
