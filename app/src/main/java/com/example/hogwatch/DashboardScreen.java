package com.example.hogwatch;

import android.widget.LinearLayout;
import android.view.View;
import android.widget.TextView;
import android.widget.ScrollView;

final class DashboardScreen {
    private final MainActivity ui;

    DashboardScreen(MainActivity ui) {
        this.ui = ui;
    }

    void show() {
        ui.getLayoutInflater().inflate(R.layout.screen_dashboard, ui.content, true);
        LinearLayout r = ui.findViewById(R.id.farm_counts);
        tile(r, R.drawable.ic_pens, "Pens monitored", "" + ui.count(), "5 online \u00b7 " + (ui.count() - 5) + " offline", ()->ui.go(2));
        tile(r, R.drawable.ic_alerts, "Active alerts", "" + ui.active(""), ui.active("Critical") + " critical \u00b7 " + ui.active("Warning") + " warning", ()->ui.go(3));
        r = ui.findViewById(R.id.farm_averages);
        tile(r, R.drawable.ic_temperature, "Avg temperature", ui.value(ui.mean(0), 0), "Compare pens \u2197", ()->compare(0));
        tile(r, R.drawable.ic_ammonia, "Avg ammonia", ui.value(ui.mean(2), 2), "Compare pens \u2197", ()->compare(2));
        LinearLayout c = ui.findViewById(R.id.compare_pens);
        LinearLayout tabs = c.findViewById(R.id.reading_tabs);
        for (int i = 0; i < MainActivity.METRICS.length; i++) {
            final int index = i;
            TextView tab = new TextView(ui);
            tab.setText(MainActivity.METRICS[i]);
            tab.setTextSize(13);
            tab.setGravity(android.view.Gravity.CENTER);
            tab.setMinHeight(ui.dp(44));
            tab.setPadding(ui.dp(8),0,ui.dp(8),0);
            tab.setSingleLine(true);
            tab.setSelected(i == ui.metric);
            tab.setTextColor(tab.isSelected() ? android.graphics.Color.WHITE : MainActivity.GREEN);
            if (tab.isSelected()) tab.setBackgroundResource(R.drawable.action_primary);
            tab.setFocusable(true);
            tab.setOnClickListener(v -> {ui.metric = index; ui.render();});
            tabs.addView(tab,new LinearLayout.LayoutParams(-2,-2,1));
        }
        ui.directory(c, false);
    }

    private void tile(LinearLayout r, int icon, String label, String val, String hint, Runnable fn) {
        View v = ui.getLayoutInflater().inflate(R.layout.metric_tile, r, false);
        r.addView(v);
        ((android.widget.ImageView)v.findViewById(R.id.metric_icon)).setImageResource(icon);
        ((TextView)v.findViewById(R.id.metricLabel)).setText(label);
        TextView t = v.findViewById(R.id.metricValue);
        t.setText(val);
        if (label.equals("Active alerts")) t.setTextColor(ui.tint("Critical"));
        ((TextView)v.findViewById(R.id.metricHint)).setText(hint);
        v.setOnClickListener((x)->fn.run());
        v.setFocusable(true);
    }

    private void compare(int m) {
        ui.metric = m;
        ui.render();
        ScrollView s = ui.findViewById(R.id.scroll);
        s.post(()->s.smoothScrollTo(0, ui.findViewById(R.id.compare_pens).getTop()));
    }
}
