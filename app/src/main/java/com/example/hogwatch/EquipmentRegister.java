package com.example.hogwatch;

import android.app.AlertDialog;
import android.view.View;
import android.widget.*;
import org.json.*;
import java.util.ArrayList;
import java.util.List;

final class EquipmentRegister {
    static final String CAMERA = "Camera", SENSOR = "Sensor", CONTROL = "Environmental control";
    private final MainActivity ui;
    private JSONArray entries;
    private static final String KEY = "equipment_v1";

    EquipmentRegister(MainActivity ui) {
        this.ui = ui;
        String saved = ui.prefs.getString(KEY, null);
        if (saved != null) {
            try { entries = new JSONArray(saved); }
            catch (JSONException e) { throw new IllegalStateException("Cannot read equipment register", e); }
        } else {
            entries = new JSONArray();
            for (int pen = 0; pen < ui.count(); pen++) {
                String sensor = ui.pref("sensor" + pen, pen == 0 ? "S-01" : pen == 1 ? "S-03" : pen == 3 ? "S-09" : pen == 5 ? "S-11" : "");
                String camera = ui.pref("camera" + pen, pen == 0 || pen == 1 || pen == 6 ? "Cam-01 Overhead" : "");
                if (!sensor.isEmpty()) entries.put(record(SENSOR, sensor, "Environmental", pen, false));
                if (!camera.isEmpty()) entries.put(record(CAMERA, camera, "Overhead", pen, false));
            }
            persist();
        }
    }

    private JSONObject record(String kind, String name, String type, int pen, boolean enabled) {
        try { return new JSONObject().put("kind",kind).put("name",name).put("type",type).put("pen",pen).put("enabled",enabled); }
        catch (JSONException e) { throw new IllegalStateException(e); }
    }

    private void persist() { ui.put(KEY, entries.toString()); }

    String assigned(String kind, int pen) {
        return android.text.TextUtils.join(", ", names(kind,pen));
    }

    List<String> names(String kind, int pen) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < entries.length(); i++) {
            JSONObject item = entries.optJSONObject(i);
            if (kind.equals(item.optString("kind")) && (pen < 0 || pen == item.optInt("pen")) && !names.contains(item.optString("name"))) names.add(item.optString("name"));
        }
        return names;
    }

    int size() { return entries.length(); }
    String attribute(int index,String key) { return entries.optJSONObject(index).optString(key); }
    int assignedPen(int index) { return entries.optJSONObject(index).optInt("pen",-1); }

    void createPen(int number,String label,java.util.Set<Integer> sensors,java.util.Set<String> cameras) {
        int pen = ui.count();
        try {
            JSONArray updated = new JSONArray(entries.toString());
            for (int index : sensors) updated.getJSONObject(index).put("pen",pen);
            for (String camera : cameras) {
                JSONObject free = null;
                String type = "Local camera";
                for (int i=0;i<entries.length();i++) {
                    JSONObject item = updated.getJSONObject(i);
                    if (!CAMERA.equals(item.optString("kind")) || !camera.equals(item.optString("name"))) continue;
                    type = item.optString("type");
                    if (item.optInt("pen")==-1) { free=item;break; }
                }
                if (free!=null) free.put("pen",pen);
                else updated.put(record(CAMERA,camera,type,pen,false));
            }
            ui.prefs.edit().putInt("count",pen+1).putInt("sample_source"+pen,1000000).putInt("pen_number"+pen,number)
                    .putString("pen"+pen,label).putString(KEY,updated.toString()).apply();
            entries=updated;
        } catch (JSONException e) { throw new IllegalStateException(e); }
    }

    void updatePen(int pen,int number,String label,java.util.Set<Integer> sensors,java.util.Set<String> cameras){
        try{
            JSONArray updated=new JSONArray(entries.toString());
            for(int i=0;i<updated.length();i++){
                JSONObject item=updated.getJSONObject(i);
                String kind=item.optString("kind");
                if(SENSOR.equals(kind)){
                    if(sensors.contains(i))item.put("pen",pen);
                    else if(item.optInt("pen")==pen)item.put("pen",-1);
                }else if(CAMERA.equals(kind)&&item.optInt("pen")==pen&&!cameras.contains(item.optString("name")))item.put("pen",-1);
            }
            for(String camera:cameras){
                boolean assigned=false;JSONObject free=null,source=null;
                for(int i=0;i<updated.length();i++){
                    JSONObject item=updated.getJSONObject(i);
                    if(!CAMERA.equals(item.optString("kind"))||!camera.equals(item.optString("name")))continue;
                    source=item;
                    if(item.optInt("pen")==pen)assigned=true;
                    if(item.optInt("pen")==-1)free=item;
                }
                if(!assigned){
                    if(free!=null)free.put("pen",pen);
                    else if(source!=null)updated.put(new JSONObject(source.toString()).put("pen",pen));
                    else updated.put(record(CAMERA,camera,"Local camera",pen,false));
                }
            }
            ui.prefs.edit().putInt("pen_number"+pen,number).putString("pen"+pen,label).putString(KEY,updated.toString()).apply();
            entries=updated;
        }catch(JSONException e){throw new IllegalStateException(e);}
    }

    void deletePen(int pen){
        try{
            JSONArray updated=new JSONArray(entries.toString());
            for(int i=0;i<updated.length();i++){
                JSONObject item=updated.getJSONObject(i);int assigned=item.optInt("pen",-1);
                if(assigned==pen)item.put("pen",-1).put("enabled",false);
                else if(assigned>pen)item.put("pen",assigned-1);
            }
            android.content.SharedPreferences.Editor editor=ui.prefs.edit();
            int count=ui.count();
            for(int i=pen;i<count-1;i++){
                editor.putString("pen"+i,ui.name(i+1)).putInt("pen_number"+i,ui.penNumber(i+1))
                    .putInt("sample_source"+i,ui.prefs.getInt("sample_source"+(i+1),i+1));
            }
            editor.remove("pen"+(count-1)).remove("pen_number"+(count-1)).remove("sample_source"+(count-1));
            for(int i=0;i<MainActivity.IMPACTED.length;i++){
                int affected=ui.alertPen(i);
                editor.putInt("alert_pen"+i,affected==pen?-1:affected>pen?affected-1:affected);
                if(affected==pen)editor.putString("alert"+i,"Resolved");
            }
            editor.putInt("count",count-1).putString(KEY,updated.toString()).apply();
            entries=updated;
            ui.selected=Math.max(0,Math.min(ui.selected>pen?ui.selected-1:ui.selected,count-2));
        }catch(JSONException e){throw new IllegalStateException(e);}
    }
    void list(LinearLayout parent, String kind, int penFilter) { list(parent,kind,penFilter,""); }

    void list(LinearLayout parent, String kind, int penFilter,String query) {
        int shown=0;
        for (int i = 0; i < entries.length(); i++) {
            JSONObject item = entries.optJSONObject(i);
            int pen = item.optInt("pen");
            if ((kind!=null && !kind.equals(item.optString("kind"))) || (kind==null && CONTROL.equals(item.optString("kind"))) || (penFilter >= 0 && penFilter != pen)) continue;
            String searchable=item.optString("name")+" "+item.optString("deviceId")+" "+(pen<0?"Unassigned":ui.name(pen));
            if(!searchable.toLowerCase(java.util.Locale.ROOT).contains(query.toLowerCase(java.util.Locale.ROOT)))continue;
            shown++;
            final int index = i;
            LinearLayout block = new LinearLayout(ui);
            block.setOrientation(LinearLayout.VERTICAL);
            block.setPadding(0,ui.dp(10),0,ui.dp(6));
            parent.addView(block);
            ui.text(block,item.optString("name"),16,MainActivity.INK,true);
            ui.text(block,item.optString("type")+" · "+(pen < 0 ? "Unassigned" : ui.name(pen)),13,MainActivity.MUTED,false);
            if (CONTROL.equals(kind)) {
                Switch toggle = new Switch(ui);
                toggle.setText("Enabled in simulation");
                toggle.setMinHeight(ui.dp(48));
                toggle.setChecked(item.optBoolean("enabled"));
                block.addView(toggle);
                toggle.setOnCheckedChangeListener((button, checked) -> {
                    try { item.put("enabled", checked); } catch(JSONException e) { throw new IllegalStateException(e); }
                    persist();
                });
            }
            ui.pair(block,"Device ID",item.optString("deviceId","Not set"));
            ui.pair(block,"Power draw",item.has("watts")?item.optString("watts")+" W":"Not set");
            ui.pair(block,"Connection",item.optString("connection","Not configured"));
            ui.pair(block,"Status","Registered · awaiting telemetry");
            ui.action(block,"Edit / assign",()->edit(item.optString("kind"),index,pen));
            ui.action(block,"Remove",()->new AlertDialog.Builder(ui).setTitle("Remove "+item.optString("name")+"?")
                    .setMessage("This removes its saved pen assignment.").setNegativeButton("Cancel",null)
                    .setPositiveButton("Remove",(d,w)->{entries.remove(index);persist();ui.render();}).show());
        }
        if(shown==0)ui.text(parent,"No matching devices. Add a device or adjust your search.",14,MainActivity.MUTED,false);
    }

    void add(String kind, int pen) { edit(kind,-1,pen); }

    void edit(String kind, int index, int initialPen) {
        JSONObject original = index < 0 ? null : entries.optJSONObject(index);
        View form = ui.getLayoutInflater().inflate(R.layout.dialog_equipment,null);
        EditText name = form.findViewById(R.id.equipment_name);
        EditText deviceId=form.findViewById(R.id.device_id),watts=form.findViewById(R.id.device_watts),connection=form.findViewById(R.id.device_connection);
        Spinner type = form.findViewById(R.id.equipment_type);
        Spinner pen = form.findViewById(R.id.equipment_pen);
        String[] types = kind.equals(CONTROL) ? new String[]{"Ventilation","Cooling","Heating","Misting","Lighting"}
                : kind.equals(CAMERA) ? new String[]{"Overhead","Fixed","Pan and tilt"}
                : new String[]{"Environmental","Temperature","Humidity","Ammonia"};
        options(type,types);
        String[] pens = new String[ui.count()+1];
        pens[0]="Unassigned";
        System.arraycopy(ui.penNames(),0,pens,1,ui.count());
        options(pen,pens);
        pen.setSelection(initialPen+1);
        if (original != null) {
            deviceId.setText(original.optString("deviceId"));watts.setText(original.optString("watts"));connection.setText(original.optString("connection"));
            name.setText(original.optString("name"));
            type.setSelection(Math.max(0, java.util.Arrays.asList(types).indexOf(original.optString("type"))));
        }
        AlertDialog dialog = new AlertDialog.Builder(ui).setTitle((index<0?"Add ":"Edit ")+kind.toLowerCase(java.util.Locale.ROOT))
                .setView(form).setNegativeButton("Cancel",null).setNeutralButton(index<0?"": "Delete device",null).setPositiveButton("Save",null).create();
        dialog.show();
        if(index<0)dialog.getButton(-3).setVisibility(View.GONE);
        else dialog.getButton(-3).setOnClickListener(v->new AlertDialog.Builder(ui).setTitle("Delete device?")
            .setMessage("Remove this device registration and its pen assignment?")
            .setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{entries.remove(index);persist();dialog.dismiss();ui.render();}).show());
        dialog.getButton(-1).setOnClickListener(v -> {
            String label=name.getText().toString().trim();
            if (label.isEmpty()) { name.setError("Enter a name"); return; }
            String power=watts.getText().toString().trim();
            double draw=0;
            if(!power.isEmpty())try{draw=Double.parseDouble(power);if(!Double.isFinite(draw)||draw<0)throw new NumberFormatException();}
                catch(NumberFormatException error){watts.setError("Enter a nonnegative wattage");return;}
            int target = pen.getSelectedItemPosition()-1;
            for (int i=0;i<entries.length();i++) {
                JSONObject other=entries.optJSONObject(i);
                if (i!=index && kind.equals(other.optString("kind")) && other.optInt("pen")==target && label.equalsIgnoreCase(other.optString("name"))) {
                    name.setError("This name is already registered here"); return;
                }
            }
            JSONObject item=record(kind,label,type.getSelectedItem().toString(),target,original!=null && original.optBoolean("enabled"));
            try {
                item.put("deviceId",deviceId.getText().toString().trim());
                item.put("connection",connection.getText().toString().trim());
                if(!power.isEmpty())item.put("watts",draw);
                entries.put(index<0?entries.length():index,item);
            } catch(JSONException e) { throw new IllegalStateException(e); }
            persist();
            dialog.dismiss();
            ui.render();
            Toast.makeText(ui,kind+" saved",Toast.LENGTH_SHORT).show();
        });
    }

    private void options(Spinner spinner,String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ui,android.R.layout.simple_spinner_item,items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}




