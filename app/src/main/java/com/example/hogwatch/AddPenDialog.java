package com.example.hogwatch;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.camera2.*;
import android.view.View;
import android.widget.*;
import java.util.*;

final class AddPenDialog {
    private final MainActivity ui;

    AddPenDialog(MainActivity ui) { this.ui = ui; }

    void show() {
        View form = ui.getLayoutInflater().inflate(R.layout.dialog_add_pen, null);
        EditText number = form.findViewById(R.id.pen_number);
        EditText alias = form.findViewById(R.id.pen_alias);
        Set<Integer> used = new HashSet<>();
        for (int i = 0; i < ui.count(); i++) used.add(ui.penNumber(i));
        int next = ui.count() + 1;
        while (used.contains(next)) next++;
        number.setText(String.valueOf(next));
        number.setSelectAllOnFocus(true);

        Set<Integer> sensors = new LinkedHashSet<>();
        Set<String> cameras = new LinkedHashSet<>();
        LinearLayout sensorList = form.findViewById(R.id.sensors_options);
        LinearLayout cameraList = form.findViewById(R.id.cameras_options);
        TextView sensorTitle = form.findViewById(R.id.sensors_caption);
        TextView cameraTitle = form.findViewById(R.id.cameras_caption);
        sensorTitle.setText("Sensors · 0 selected");
        cameraTitle.setText("Cameras · 0 selected");
        boolean available = false;
        Map<String,List<String>> cameraPens = new LinkedHashMap<>();
        for (int i = 0; i < ui.equipment.size(); i++) {
            String kind = ui.equipment.attribute(i,"kind");
            String label = ui.equipment.attribute(i,"name");
            int assigned = ui.equipment.assignedPen(i);
            String location = assigned < 0 ? "Unassigned" : ui.name(assigned);
            if (kind.equals(EquipmentRegister.SENSOR)) {
                option(sensorList,sensorTitle,"Sensors",sensors,i,label,ui.equipment.attribute(i,"type")+" · "+location,assigned<0);
                available |= assigned<0;
            } else if (kind.equals(EquipmentRegister.CAMERA)) {
                List<String> places = cameraPens.get(label);
                if (places == null) { places = new ArrayList<>(); cameraPens.put(label,places); }
                if (!places.contains(location)) places.add(location);
            }
        }
        if (!available) ui.text(sensorList,"Register an unassigned sensor in Device Management to select it here.",13,MainActivity.MUTED,false);
        addLocalCameras(cameraPens);
        for (Map.Entry<String,List<String>> entry : cameraPens.entrySet()) {
            option(cameraList,cameraTitle,"Cameras",cameras,entry.getKey(),entry.getKey(),android.text.TextUtils.join(" · ",entry.getValue()),true);
        }
        if (cameraPens.isEmpty()) ui.text(cameraList,"Register a camera in Device Management.",13,MainActivity.MUTED,false);

        AlertDialog dialog = new AlertDialog.Builder(ui).setTitle("Add pen").setView(form)
                .setNegativeButton("Cancel",null).setPositiveButton("Save pen",null).create();
        form.findViewById(R.id.manage_pen_devices).setOnClickListener(v->{dialog.dismiss();ui.go(6);});
        dialog.show();
        dialog.getButton(-1).setOnClickListener(v->{
            int n;
            try { n=Integer.parseInt(number.getText().toString()); }
            catch (NumberFormatException e) { number.setError("Enter a positive pen number");return; }
            if(n<=0 || used.contains(n)){number.setError(n<=0?"Enter a positive pen number":"This pen number is already in use");return;}
            String custom = alias.getText().toString().trim();
            String label = "Pen "+n+(custom.isEmpty()?"":" · "+custom);
            ui.equipment.createPen(n,label,sensors,cameras);
            dialog.dismiss();
            ui.render();
            Toast.makeText(ui,label+" added",Toast.LENGTH_SHORT).show();
        });
    }

    private <T> void option(LinearLayout parent,TextView title,String group,Set<T> selected,T key,String label,String detail,boolean enabled) {
        CheckBox box = new CheckBox(ui);
        box.setText(label+"\n"+detail);
        box.setMinHeight(ui.dp(52));box.setEnabled(enabled);
        parent.addView(box);
        box.setOnCheckedChangeListener((v,on)->{
            if(on) selected.add(key); else selected.remove(key);
            title.setText(group+" · "+selected.size()+" selected");
        });
    }

    private void addLocalCameras(Map<String,List<String>> cameras) {
        CameraManager manager = (CameraManager) ui.getSystemService(Context.CAMERA_SERVICE);
        if(manager==null)return;
        try {
            for(String id:manager.getCameraIdList()) {
                String name="Local camera "+id;
                if(!cameras.containsKey(name)) cameras.put(name,Collections.singletonList("This device"));
            }
        } catch(CameraAccessException | SecurityException ignored) {
            // Registered farm cameras are available even if local discovery is restricted.
        }
    }
}
