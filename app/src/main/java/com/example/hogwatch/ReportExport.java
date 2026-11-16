package com.example.hogwatch;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import java.io.*;
import java.nio.charset.StandardCharsets;

final class ReportExport {
    static final int REQUEST=410;
    static final String PENDING="pending_report_csv";
    private final MainActivity ui;
    ReportExport(MainActivity ui){this.ui=ui;}

    void start(int selectedPen){
        StringBuilder csv=new StringBuilder("Pen,Temperature (C),Humidity (%),Ammonia (ppm),CO2 (ppm),Status,Source\r\n");
        for(int pen=0;pen<ui.count();pen++){
            if(selectedPen>=0 && pen!=selectedPen)continue;
            csv.append(cell(ui.name(pen)));
            boolean offline=ui.status(pen).equals("Offline");
            for(int metric=0;metric<MainActivity.METRICS.length;metric++)csv.append(',').append(offline?"":Double.toString(ui.sample(pen,metric)));
            csv.append(',').append(cell(ui.status(pen))).append(",Demo readings\r\n");
        }
        ui.put(PENDING,csv.toString());
        Intent save=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                .setType("text/csv").putExtra(Intent.EXTRA_TITLE,"HogWatch-readings.csv");
        try{ui.startActivityForResult(save,REQUEST);}
        catch(android.content.ActivityNotFoundException e){
            ui.prefs.edit().remove(PENDING).apply();
            Toast.makeText(ui,"A document provider is needed to save the report.",Toast.LENGTH_LONG).show();
        }
    }

    void startReport(int tab,String from,String to,int pen){
        StringBuilder csv=new StringBuilder("Record type,Date,Pen,Metric or event,Value,Unit,Notes,Source,Filter from,Filter to\r\n");
        int[] metrics=tab==0?new int[]{3,2,0,1}:tab==5?new int[]{}:new int[]{new int[]{-1,3,2,0,1}[tab]};
        if(tab!=5)for(org.json.JSONObject row:ReportData.readings(ui,from,to,pen))for(int m:metrics){
            csv.append("Reading,").append(cell(ReportData.day(row.optLong("time")))).append(',').append(cell(row.optString("name"))).append(',').append(cell(MainActivity.METRICS[m])).append(',').append(row.optDouble("m"+m)).append(',').append(cell(m==0?"C":m==1?"%":"ppm")).append(",,Sample snapshot,").append(cell(from)).append(',').append(cell(to)).append("\r\n");
        }
        if(tab==0||tab==5)for(org.json.JSONObject e:ReportData.activity(ui,from,to,pen)){
            csv.append("Activity,").append(cell(ReportData.day(e.optLong("time")))).append(',').append(cell(e.optString("name"))).append(',').append(cell(e.optString("event"))).append(',').append(cell(e.optString("detail"))).append(",,").append(cell(e.optString("notes"))).append(",Local activity,").append(cell(from)).append(',').append(cell(to)).append("\r\n");
        }
        ui.put(PENDING,csv.toString());
        Intent save=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("text/csv").putExtra(Intent.EXTRA_TITLE,"HogWatch-"+new String[]{"All","CO2","Ammonia","Temperature","Humidity","Activity"}[tab]+"-"+from+"-to-"+to+".csv");
        try{ui.startActivityForResult(save,REQUEST);}catch(android.content.ActivityNotFoundException e){ui.prefs.edit().remove(PENDING).apply();Toast.makeText(ui,"A document provider is needed to save reports.",Toast.LENGTH_LONG).show();}
    }

    private String cell(String value){
        if(!value.isEmpty() && "=+-@\t\r".indexOf(value.charAt(0))>=0)value="'"+value;
        return "\""+value.replace("\"","\"\"")+"\"";
    }

    void finish(int result,Intent data){
        String csv=ui.pref(PENDING,"");
        if(result!=Activity.RESULT_OK||data==null||data.getData()==null){ui.prefs.edit().remove(PENDING).apply();return;}
        Uri target=data.getData();
        try(OutputStream stream=ui.getContentResolver().openOutputStream(target,"wt")){
            if(stream==null)throw new IOException();
            stream.write(csv.getBytes(StandardCharsets.UTF_8));
            Toast.makeText(ui,"CSV report saved",Toast.LENGTH_SHORT).show();
        }catch(IOException|SecurityException e){Toast.makeText(ui,"Could not save report. Please try again.",Toast.LENGTH_LONG).show();}
        finally{ui.prefs.edit().remove(PENDING).apply();}
    }
}

