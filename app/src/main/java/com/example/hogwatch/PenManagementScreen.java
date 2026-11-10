package com.example.hogwatch;

import android.widget.LinearLayout;

final class PenManagementScreen {
    private final MainActivity ui;

    PenManagementScreen(MainActivity ui) {
        this.ui = ui;
    }

    void show() {
        ui.getLayoutInflater().inflate(R.layout.screen_pen_management, ui.content, true);
        LinearLayout c = ui.findViewById(R.id.pen_overview);
        int sensors = 0;
        int cameras = 0;
        for (int i = 0; i < ui.count(); i++) {
            if (!ui.sensor(i).isEmpty()) sensors++;
            if (!ui.camera(i).isEmpty()) cameras++;
        }
        ui.pair(c, "Total pens", "" + ui.count());
        ui.pair(c, "Pens with sensors", "" + sensors);
        ui.pair(c, "Pens with cameras", "" + cameras);
        ui.action(c, "+ Add pen", ()->ui.editPen(ui.count()));
        ui.directory(ui.findViewById(R.id.pen_directory), true);
    }
}
