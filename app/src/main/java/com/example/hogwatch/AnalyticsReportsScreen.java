package com.example.hogwatch;

final class AnalyticsReportsScreen {
    private final MainActivity ui;

    AnalyticsReportsScreen(MainActivity ui) {
        this.ui = ui;
    }

    void show() {
        ui.getLayoutInflater().inflate(R.layout.screen_analytics_reports, ui.content, true);
        String[] pens = new String[ui.count() + 1];
        pens[0] = "All pens";
        System.arraycopy(ui.penNames(), 0, pens, 1, ui.count());
        ui.picker(ui.findViewById(R.id.report_parameters), "Target pens", pens, ui.tab, (i)->{
            ui.tab = i;
            ui.render();
        });
        for (int m = 0; m < 3; m++) ui.chart(ui.card(ui.findViewById(R.id.trend_charts), MainActivity.METRICS[m] + " trend"), ui.tab - 1, m);
    }
}
