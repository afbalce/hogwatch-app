package com.example.hogwatch;

import android.app.*;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;
import java.util.*;

final class AnalyticsReportsScreen {
    private final MainActivity ui;
    private final String[] tabs={"All","CO₂","Ammonia","Temp","Humidity","Activity"};
    private final int[] metrics={-1,3,2,0,1,-1};
    AnalyticsReportsScreen(MainActivity ui){this.ui=ui;}
    void show(){
        ui.getLayoutInflater().inflate(R.layout.screen_analytics_reports,ui.content,true);
        ui.tab=Math.max(0,Math.min(5,ui.tab));
        LinearLayout params=ui.findViewById(R.id.report_parameters);
        params.removeAllViews();
        LinearLayout strip=ui.findViewById(R.id.report_tab_strip);
        HorizontalScrollView scroll=(HorizontalScrollView)strip.getParent();
        for(int i=0;i<tabs.length;i++){
            final int index=i;
            Button button=new Button(ui);button.setText(tabs[i]);button.setAllCaps(false);
            SegmentStyle.apply(ui,button,i==ui.tab,false);
            button.setTextSize(12);button.setMinHeight(ui.dp(56));button.setMinimumHeight(ui.dp(56));
            button.setPadding(ui.dp(8),ui.dp(8),ui.dp(8),ui.dp(8));
            button.setTypeface(android.graphics.Typeface.create("sans-serif-medium",0));
            LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(ui.dp(88),-2,1);
            size.setMarginStart(ui.dp(2));size.setMarginEnd(ui.dp(2));strip.addView(button,size);
            button.setOnClickListener(v->{ui.tab=index;ui.render();});
            if(i==ui.tab)scroll.post(()->scroll.smoothScrollTo(Math.max(0,button.getLeft()-(scroll.getWidth()-button.getWidth())/2),0));
        }
        String from=ui.pref("report_from",ReportData.today()),to=ui.pref("report_to",ReportData.today());
        int pen=ui.prefs.getInt("report_pen",-1);
        inlineFilters(params);

        LinearLayout body=ui.findViewById(R.id.trend_charts);
        List<JSONObject> rows=ReportData.readings(ui,from,to,pen);
        if(ui.tab==0){for(int m:new int[]{3,2,0,1})sensor(body,rows,m,false);activity(body,from,to,pen);}
        else if(ui.tab==5)activity(body,from,to,pen);
        else sensor(body,rows,metrics[ui.tab],true);
    }
    private void sensor(LinearLayout parent,List<JSONObject> rows,int metric,boolean breakdown){
        LinearLayout card=ui.card(parent,MainActivity.METRICS[metric]);
        if(rows.isEmpty()){ui.text(card,"No saved readings for these dates and pens.",14,MainActivity.MUTED,false);return;}
        double total=0,min=Double.MAX_VALUE,max=-Double.MAX_VALUE;
        Map<String,double[]> pens=new LinkedHashMap<>();
        for(JSONObject r:rows){double v=r.optDouble("m"+metric);total+=v;min=Math.min(min,v);max=Math.max(max,v);double[] a=pens.get(r.optString("name"));if(a==null){a=new double[2];pens.put(r.optString("name"),a);}a[0]+=v;a[1]++;}
        ui.text(card,ui.value(total/rows.size(),metric),26,MainActivity.INK,true);
        ui.text(card,"Average · "+rows.size()+" saved readings",12,MainActivity.MUTED,false);
        for(Map.Entry<String,double[]> e:pens.entrySet()){
            double value=e.getValue()[0]/e.getValue()[1];
            View row=ui.getLayoutInflater().inflate(R.layout.gateway_summary_bar,card,false);
            ((TextView)row.findViewById(R.id.gateway_bar_label)).setText(e.getKey());
            ((TextView)row.findViewById(R.id.gateway_bar_value)).setText(ui.value(value,metric));
            ProgressBar bar=row.findViewById(R.id.gateway_bar);bar.setProgress((int)(1000*value/Math.max(1,max)));bar.setProgressTintList(android.content.res.ColorStateList.valueOf(new int[]{0xff287C63,0xff527DA5,0xffB38132,0xff8870AA}[metric]));card.addView(row);
        }
        ui.text(card,"Bars show average by pen · scale 0–"+ui.value(max,metric),11,MainActivity.MUTED,false);
        if(breakdown){
            LinearLayout details=ui.card(parent,"Reading breakdown");
            ui.pair(details,"Lowest",ui.value(min,metric));ui.pair(details,"Highest",ui.value(max,metric));
            for(JSONObject r:rows){ui.pair(details,r.optString("name")+" · "+ReportData.day(r.optLong("time")),ui.value(r.optDouble("m"+metric),metric));}
        }
    }
    private void activity(LinearLayout parent,String from,String to,int pen){
        List<JSONObject> events=ReportData.activity(ui,from,to,pen);
        LinearLayout card=ui.card(parent,"Activity");
        int resolved=0;for(JSONObject e:events)if(e.optString("event").equals("Resolved"))resolved++;
        OverviewTiles.add(ui,card,new String[]{"Acknowledged","Resolved"},new int[]{events.size()-resolved,resolved});
        LinearLayout list=ui.card(parent,"Activity breakdown");
        if(events.isEmpty())ui.text(list,"No recorded activity for this date range.",14,MainActivity.MUTED,false);
        for(JSONObject e:events){ui.text(list,e.optString("name")+" · "+e.optString("event"),15,MainActivity.INK,true);ui.text(list,ReportData.day(e.optLong("time"))+" · "+e.optString("detail"),13,MainActivity.MUTED,false);if(!e.optString("notes").isEmpty())ui.text(list,e.optString("notes"),13,MainActivity.MUTED,false);}
    }
    void export(){filters(true);}
    private void inlineFilters(LinearLayout parent){
        View fields=ui.getLayoutInflater().inflate(R.layout.report_inline_filters,parent,false);parent.addView(fields);
        EditText pen=fields.findViewById(R.id.report_inline_pen);
        int saved=ui.prefs.getInt("report_pen",-1);if(saved>=0)pen.setText(""+saved);
        Button from=fields.findViewById(R.id.report_inline_from),to=fields.findViewById(R.id.report_inline_to);
        from.setText(ui.pref("report_from","Select date"));to.setText(ui.pref("report_to","Select date"));
        from.setOnClickListener(v->date(from));to.setOnClickListener(v->date(to));
        fields.findViewById(R.id.report_inline_cancel).setOnClickListener(v->{
            ui.prefs.edit().remove("report_from").remove("report_to").remove("report_pen").apply();ui.render();
        });
        fields.findViewById(R.id.report_inline_apply).setOnClickListener(v->{
            TextView error=fields.findViewById(R.id.report_inline_error);
            String start=from.getText().toString(),end=to.getText().toString();
            if(start.equals("Select date"))start=ReportData.today();if(end.equals("Select date"))end=ReportData.today();
            int number=-1;
            try{
                if(pen.length()>0){number=Integer.parseInt(pen.getText().toString());boolean exists=false;for(int i=0;i<ui.count();i++)if(ui.penNumber(i)==number)exists=true;if(!exists)throw new IllegalArgumentException();}
            }catch(IllegalArgumentException e){pen.setError("Enter an existing pen number, or leave blank for all");return;}
            if(start.compareTo(end)>0){error.setText("From date must be on or before To date.");error.setVisibility(View.VISIBLE);return;}
            ui.prefs.edit().putString("report_from",start).putString("report_to",end).putInt("report_pen",number).apply();ui.render();
        });
    }
    private void filters(boolean export){
        View form=ui.getLayoutInflater().inflate(R.layout.dialog_report_dates,null);
        Button start=form.findViewById(R.id.report_from),end=form.findViewById(R.id.report_to);
        start.setText(ui.pref("report_from",ReportData.today()));end.setText(ui.pref("report_to",ReportData.today()));
        start.setOnClickListener(v->date(start));end.setOnClickListener(v->date(end));
        List<String> names=new ArrayList<>();names.add("All pens");for(int i=0;i<ui.count();i++)names.add(ui.name(i));
        Spinner pen=form.findViewById(R.id.report_pen);
        int selected=0;for(int i=0;i<ui.count();i++)if(ui.penNumber(i)==ui.prefs.getInt("report_pen",-1))selected=i+1;
        SettingsScreen.options(ui,pen,names,names.get(selected));
        AlertDialog dialog=new AlertDialog.Builder(ui).setTitle(export?"Export "+tabs[ui.tab]+" report":"Report filters").setView(form).setNegativeButton("Cancel",null).setNeutralButton("Reset",(d,w)->{ui.prefs.edit().remove("report_from").remove("report_to").remove("report_pen").apply();ui.render();}).setPositiveButton(export?"Export CSV":"Apply",null).create();
        dialog.show();
        dialog.getButton(-1).setOnClickListener(v->{
            String from=start.getText().toString(),to=end.getText().toString();
            if(from.compareTo(to)>0){TextView error=form.findViewById(R.id.report_filter_error);error.setText("From date must be on or before To date.");error.setVisibility(View.VISIBLE);return;}
            int number=pen.getSelectedItemPosition()==0?-1:ui.penNumber(pen.getSelectedItemPosition()-1);
            if(export)new ReportExport(ui).startReport(ui.tab,from,to,number);
            else{ui.prefs.edit().putString("report_from",from).putString("report_to",to).putInt("report_pen",number).apply();ui.render();}
            dialog.dismiss();
        });
    }
    private void date(Button button){
        String value=button.getText().toString();
        String[] parts=(value.equals("Select date")?ReportData.today():value).split("-");
        new DatePickerDialog(ui,(v,y,m,d)->button.setText(String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d)),Integer.parseInt(parts[0]),Integer.parseInt(parts[1])-1,Integer.parseInt(parts[2])).show();
    }
}


