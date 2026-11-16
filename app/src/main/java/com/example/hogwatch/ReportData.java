package com.example.hogwatch;

import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;

final class ReportData {
    static String day(long time){return new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date(time));}
    static String today(){return day(System.currentTimeMillis());}
    static JSONArray snapshots(MainActivity ui){
        JSONArray data;
        try{data=new JSONArray(ui.pref("report_snapshots","[]"));}catch(JSONException e){data=new JSONArray();}
        String today=today();
        if(!today.equals(ui.pref("report_capture_day",""))){
            long now=System.currentTimeMillis();
            for(int p=0;p<ui.count();p++)if(ui.hasReading(p)){
                try{
                    JSONObject row=new JSONObject().put("time",now).put("pen",ui.penNumber(p)).put("name",ui.name(p));
                    for(int m=0;m<4;m++)row.put("m"+m,ui.sample(p,m));
                    data.put(row);
                }catch(JSONException e){throw new IllegalStateException(e);}
            }
            ui.prefs.edit().putString("report_snapshots",data.toString()).putString("report_capture_day",today).apply();
        }
        return data;
    }
    static List<JSONObject> readings(MainActivity ui,String from,String to,int penNumber){
        JSONArray data=snapshots(ui);List<JSONObject> result=new ArrayList<>();
        for(int i=0;i<data.length();i++){JSONObject row=data.optJSONObject(i);String date=day(row.optLong("time"));
            if(date.compareTo(from)>=0&&date.compareTo(to)<=0&&(penNumber<0||row.optInt("pen")==penNumber))result.add(row);
        }return result;
    }
    static List<JSONObject> activity(MainActivity ui,String from,String to,int penNumber){
        List<JSONObject> result=new ArrayList<>();
        for(int i=0;i<4;i++){
            int pen=ui.alertPen(i);
            if(penNumber>=0&&(pen<0||ui.penNumber(pen)!=penNumber))continue;
            for(String type:new String[]{"acknowledged","resolved"}){
                long time=ui.prefs.getLong(type.equals("resolved")?"resolution_time"+i:"acknowledged_time"+i,0);
                if(time==0||day(time).compareTo(from)<0||day(time).compareTo(to)>0)continue;
                try{result.add(new JSONObject().put("time",time).put("name",pen<0?"Deleted pen":ui.name(pen)).put("event",type.equals("resolved")?"Resolved":"Acknowledged").put("detail",type.equals("resolved")?ui.pref("resolution_action"+i,""):MainActivity.LEVELS[i]+" alert").put("notes",type.equals("resolved")?ui.pref("resolution_notes"+i,""):""));}catch(JSONException e){throw new IllegalStateException(e);}
            }
        }
        result.sort((a,b)->Long.compare(b.optLong("time"),a.optLong("time")));return result;
    }
}
