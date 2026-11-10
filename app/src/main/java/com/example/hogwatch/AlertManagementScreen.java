package com.example.hogwatch;

import android.widget.LinearLayout;

final class AlertManagementScreen {
    private final MainActivity ui;

    AlertManagementScreen(MainActivity ui) {
        this.ui = ui;
    }

    void show() {
        ui.getLayoutInflater().inflate(R.layout.screen_alert_management, ui.content, true);
        LinearLayout c = ui.findViewById(R.id.alert_overview);
        ui.pair(c, "Active environmental alerts", "" + ui.active(""));
        int pending = 0;
        int resolved = 0;
        for (int i = 0; i < 4; i++) {
            if (ui.workflow(i).equals("Open")) pending++;
            if (ui.workflow(i).equals("Resolved")) resolved++;
        }
        ui.pair(c, "Pending action", "" + pending);
        ui.pair(c, "Resolved", "" + resolved);
        ui.action(c, "Acknowledge all", ()->{
            for (int i = 0; i < 4; i++) if (ui.workflow(i).equals("Open")) ui.put("alert" + i, "Acknowledged");
            ui.render();
        });
        LinearLayout list = ui.findViewById(R.id.alert_directory);
        String[] opts = {"All alerts", "Critical", "Warning", "Offline", "Acknowledged", "Resolved"};
        ui.picker(list, "Filter", opts, ui.filter, (i)->{
            ui.filter = i;
            ui.render();
        });
        boolean any = false;
        for (int i = 0; i < 4; i++) if (ui.filter == 0 || opts[ui.filter].equals(MainActivity.LEVELS[i]) || opts[ui.filter].equals(ui.workflow(i))) {
            ui.alertItem(list, i);
            any = true;
        }
        if (!any) ui.text(list, "No matching alerts", 14, MainActivity.MUTED, false);
    }
}
