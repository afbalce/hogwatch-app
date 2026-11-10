package com.example.hogwatch;

import android.app.AlertDialog;
import android.view.View;
import android.widget.*;
import org.json.*;
import java.util.*;

final class AlertLevels {
    private final MainActivity ui;
    private final JSONArray rules;

    AlertLevels(MainActivity ui) {
        this.ui = ui;
        String saved = ui.pref("alert_levels", "");
        try {
            rules = saved.isEmpty() ? new JSONArray()
                    .put(rule("Warning", "Warning", "All sensors", 28, 20, "Triggered pen camera", true, false, 3, 5))
                    .put(rule("Critical", "Critical", "All sensors", 32, ui.prefs.getInt("limit",25), "All cameras", true, true, 5, 2))
                    : new JSONArray(saved);
        } catch (JSONException e) { throw new IllegalStateException(e); }
    }

    private static JSONObject rule(String name,String severity,String sensor,double temp,int gas,String camera,
                                   boolean snapshot,boolean record,int count,int spacing) throws JSONException {
        return new JSONObject().put("name",name).put("severity",severity).put("sensor",sensor)
                .put("temp",temp).put("gas",gas).put("camera",camera).put("snapshot",snapshot)
                .put("record",record).put("count",count).put("spacing",spacing);
    }

    String status(int pen,double temp,double ammonia) {
        String result="OK";
        for(int i=0;i<rules.length();i++) {
            JSONObject r=rules.optJSONObject(i);
            String target=r.optString("sensor");
            if (!target.equals("All sensors") && !ui.equipment.names(EquipmentRegister.SENSOR,pen).contains(target)) continue;
            if (temp<=r.optDouble("temp") && ammonia<r.optInt("gas")) continue;
            if ("Critical".equals(r.optString("severity"))) return "Critical";
            result="Warning";
        }
        return result;
    }

    void show(LinearLayout list) {
        list.removeAllViews();
        for (int i=0;i<rules.length();i++) {
            int index=i;
            JSONObject r=rules.optJSONObject(i);
            TextView title=ui.text(list,r.optString("name"),17,ui.tint(r.optString("severity")),true);
            title.setPadding(0,ui.dp(18),0,ui.dp(4));
            ui.text(list,r.optString("sensor")+" · "+r.optString("severity"),14,MainActivity.MUTED,false);
            ui.text(list,"Temperature > "+r.optString("temp")+" °C or ammonia ≥ "+r.optInt("gas")+" ppm",14,MainActivity.INK,false);
            ui.text(list,r.optString("camera"),14,MainActivity.MUTED,false);
            if (r.optBoolean("snapshot")) ui.text(list,r.optInt("count")+" snapshots · "+r.optInt("spacing")+" s apart",14,MainActivity.INK,false);
            if (r.optBoolean("record")) ui.text(list,"Start recording",14,MainActivity.INK,false);
            ui.action(list,"Edit level",()->edit(index,list));
            ui.action(list,"Delete level",()->new AlertDialog.Builder(ui).setTitle("Delete "+r.optString("name")+"?")
                    .setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{rules.remove(index);save(list);}).show());
        }
        if(rules.length()==0) ui.text(list,"No alert levels configured",14,MainActivity.MUTED,false);
    }

    private void save(LinearLayout list) {
        ui.put("alert_levels",rules.toString());
        show(list);
    }

    void edit(int index,LinearLayout list) {
        JSONObject r=index<0?new JSONObject():rules.optJSONObject(index);
        View form=ui.getLayoutInflater().inflate(R.layout.dialog_alert_level,null);
        EditText name=form.findViewById(R.id.level_name), temp=form.findViewById(R.id.temp_limit),
                gas=form.findViewById(R.id.gas_limit), count=form.findViewById(R.id.shot_count), spacing=form.findViewById(R.id.shot_spacing);
        name.setText(r.optString("name"));temp.setText(r.optString("temp","28"));gas.setText(r.optString("gas","20"));
        count.setText(r.optString("count","3")); spacing.setText(r.optString("spacing","5"));
        Spinner severity=form.findViewById(R.id.severity), sensor=form.findViewById(R.id.sensor_target), camera=form.findViewById(R.id.camera_target);
        SettingsScreen.options(ui,severity,Arrays.asList("Warning","Critical"),r.optString("severity","Warning"));
        List<String> sensors=new ArrayList<>(Arrays.asList("All sensors"));
        sensors.addAll(ui.equipment.names(EquipmentRegister.SENSOR,-1));
        List<String> cameras=new ArrayList<>(Arrays.asList("Triggered pen camera","All cameras"));
        cameras.addAll(ui.equipment.names(EquipmentRegister.CAMERA,-1));
        SettingsScreen.options(ui,sensor,sensors,r.optString("sensor","All sensors"));
        SettingsScreen.options(ui,camera,cameras,r.optString("camera","Triggered pen camera"));
        Switch shot=form.findViewById(R.id.take_snapshot), video=form.findViewById(R.id.record_video);
        shot.setChecked(r.optBoolean("snapshot",true));video.setChecked(r.optBoolean("record"));
        AlertDialog dialog=new AlertDialog.Builder(ui).setTitle(index<0?"Add alert level":"Edit alert level").setView(form)
                .setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        dialog.show();
        dialog.getButton(-1).setOnClickListener(v->{
            String label=name.getText().toString().trim();
            if(label.isEmpty()){name.setError("Enter a level name");return;}
            try {
                double temperature=Double.parseDouble(temp.getText().toString());
                int ammonia=Integer.parseInt(gas.getText().toString()), shots=Integer.parseInt(count.getText().toString()), seconds=Integer.parseInt(spacing.getText().toString());
                if(!Double.isFinite(temperature)||ammonia<1||shots<1||seconds<1) throw new NumberFormatException();
                rules.put(index<0?rules.length():index,rule(label,severity.getSelectedItem().toString(),sensor.getSelectedItem().toString(),temperature,ammonia,
                        camera.getSelectedItem().toString(),shot.isChecked(),video.isChecked(),shots,seconds));
                save(list);dialog.dismiss();
            } catch(NumberFormatException e){Toast.makeText(ui,"Enter a temperature and positive whole numbers for ammonia, snapshots and spacing.",Toast.LENGTH_LONG).show();}
              catch(JSONException e){throw new IllegalStateException(e);}
        });
    }
}
