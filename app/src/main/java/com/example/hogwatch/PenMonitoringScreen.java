package com.example.hogwatch;

import android.graphics.*;
import android.view.View;
import android.widget.*;
import android.graphics.drawable.GradientDrawable;

final class PenMonitoringScreen {
    private final MainActivity ui;
    PenMonitoringScreen(MainActivity ui) { this.ui=ui; }

    void show() {
        ui.getLayoutInflater().inflate(R.layout.screen_pen_monitoring,ui.content,true);
        int pen=ui.selected;
        boolean offline="Offline".equals(ui.status(pen));
        Spinner pens=ui.findViewById(R.id.monitor_pen);
        ArrayAdapter<String> adapter=new ArrayAdapter<>(ui,android.R.layout.simple_spinner_item,ui.penNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pens.setAdapter(adapter);
        pens.setSelection(pen);
        pens.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> parent) {}
            public void onItemSelected(AdapterView<?> parent,View v,int position,long id) {
                if(pens.isAttachedToWindow() && position!=ui.selected){ui.selected=position;ui.render();}
            }
        });
        TextView age=ui.findViewById(R.id.sample_age);
        age.setText(offline?"Offline · No recent readings":ui.status(pen)+" · Demo readings · 2 min ago");
        age.setTextColor(ui.tint(ui.status(pen)));
        LinearLayout metrics=ui.findViewById(R.id.pen_metrics);
        String[] targets={"22–28 °C","50–70%","up to 20 ppm"};
        int[] icons={R.drawable.ic_temperature,R.drawable.ic_humidity,R.drawable.ic_ammonia};
        for(int m=0;m<3;m++){
            View row=ui.getLayoutInflater().inflate(R.layout.monitor_metric,metrics,false);
            ((ImageView)row.findViewById(R.id.metric_symbol)).setImageResource(icons[m]);
            ((TextView)row.findViewById(R.id.metric_name)).setText(MainActivity.METRICS[m]);
            ((TextView)row.findViewById(R.id.metric_target)).setText("Target "+targets[m]);
            ((TextView)row.findViewById(R.id.metric_readout)).setText(ui.reading(pen,m));
            String state=ui.metricStatus(pen,m);
            TextView badge=row.findViewById(R.id.metric_severity);
            badge.setText(state);badge.setTextColor(ui.tint(state));
            GradientDrawable pill=new GradientDrawable();
            pill.setCornerRadius(ui.dp(10));pill.setColor((ui.tint(state)&0xffffff)|0x18000000);
            badge.setBackground(pill);
            metrics.addView(row);
        }
        String sensors=ui.sensor(pen);
        ((TextView)ui.findViewById(R.id.sensor_footer)).setText("Sensors · "+(sensors.isEmpty()?"Unassigned":sensors));
        ui.action(ui.findViewById(R.id.pen_details),"Manage pens",()->ui.go(2));

        LinearLayout alerts=ui.findViewById(R.id.monitor_alert_list);
        int count=0;
        for(int i=0;i<MainActivity.IMPACTED.length;i++){
            if(MainActivity.IMPACTED[i]!=pen || ui.workflow(i).equals("Resolved"))continue;
            ui.alertItem(alerts,i);count++;
        }
        ((TextView)ui.findViewById(R.id.pen_alert_count)).setText("Active alerts · "+count);
        if(count==0)ui.text(alerts,"No active alerts for this pen",14,MainActivity.MUTED,false);
        ui.action(alerts,"Manage alerts",()->ui.go(3));

        LinearLayout trend=ui.findViewById(R.id.trend_body);
        disclosure(R.id.trend_heading,trend,"Temperature trend");
        if(offline)ui.text(trend,"No temperature history available",14,MainActivity.MUTED,false);
        else {
            ui.text(trend,"24-hour sample history · 30 °C reference line",13,MainActivity.MUTED,false);
            addTrend(trend,pen);
            ui.pair(trend,"24 hours ago","Now · "+ui.reading(pen,0));
        }
        ui.action(trend,"Open analytics",()->{ui.go(4);ui.tab=pen+1;ui.render();});
        LinearLayout camera=ui.findViewById(R.id.camera_body);
        disclosure(R.id.camera_heading,camera,"Camera feed");
        new PenCameraDemo(ui,camera,pen).show();
    }

    private void disclosure(int id,LinearLayout body,String title){
        TextView heading=ui.findViewById(id);
        heading.setMinHeight(ui.dp(48));heading.setGravity(android.view.Gravity.CENTER_VERTICAL);
        heading.setFocusable(true);heading.setText(title+"  ▾");
        heading.setOnClickListener(v->{
            boolean expand=body.getVisibility()!=View.VISIBLE;
            body.setVisibility(expand?View.VISIBLE:View.GONE);heading.setText(title+(expand?"  ▾":"  ▸"));
        });
    }

    private void addTrend(LinearLayout parent,int pen){
        double latest=ui.sample(pen,0);
        View plot=new View(ui){
            final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
            float y(double value){return (float)((32-value)/10*getHeight());}
            protected void onDraw(Canvas c){
                float w=getWidth()-ui.dp(6),h=getHeight();
                c.drawColor(0xfff4f9f3);
                paint.setColor(0xffbd6954);paint.setStrokeWidth(ui.dp(1));
                paint.setPathEffect(new DashPathEffect(new float[]{ui.dp(4),ui.dp(3)},0));
                c.drawLine(0,y(30),w,y(30),paint);paint.setPathEffect(null);
                Path line=new Path();
                for(int i=0;i<=96;i++){
                    double phase=(i-96)/10.0;
                    float py=y(latest+Math.sin(phase)*1.1+Math.sin(phase*.5)*.8);
                    if(i==0)line.moveTo(0,py);else line.lineTo(i*w/96,py);
                }
                Path fill=new Path(line);fill.lineTo(w,h);fill.lineTo(0,h);fill.close();
                paint.setColor(0x443e9b71);c.drawPath(fill,paint);
                paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(ui.dp(2));paint.setColor(MainActivity.GREEN);
                c.drawPath(line,paint);paint.setStyle(Paint.Style.FILL);c.drawCircle(w,y(latest),ui.dp(3),paint);
            }
        };
        plot.setContentDescription("Sample temperature trend ending at "+ui.reading(pen,0));
        parent.addView(plot,new LinearLayout.LayoutParams(-1,ui.dp(106)));
    }
}
