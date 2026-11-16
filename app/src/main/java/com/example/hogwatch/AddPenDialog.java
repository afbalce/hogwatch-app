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

    void show() { show(-1); }
    void show(int pen) {
        View form = ui.getLayoutInflater().inflate(R.layout.dialog_add_pen, null);
        EditText number = form.findViewById(R.id.pen_number);
        EditText alias = form.findViewById(R.id.pen_alias);
        Set<Integer> used = new HashSet<>();
        for (int i = 0; i < ui.count(); i++) if(i!=pen)used.add(ui.penNumber(i));
        int next = ui.count() + 1;
        while (used.contains(next)) next++;
        number.setText(String.valueOf(pen<0?next:ui.penNumber(pen)));
        if(pen>=0){String label=ui.name(pen),prefix="Pen "+ui.penNumber(pen);alias.setText(label.equals(prefix)?"":label.startsWith(prefix+" · ")?label.substring(prefix.length()+3):label);}
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
        if(pen>=0){for(int i=0;i<ui.equipment.size();i++)if(ui.equipment.assignedPen(i)==pen&&ui.equipment.attribute(i,"kind").equals(EquipmentRegister.SENSOR))sensors.add(i);cameras.addAll(ui.equipment.names(EquipmentRegister.CAMERA,pen));}
        Map<String,List<String>> cameraPens = new LinkedHashMap<>();
        for (int i = 0; i < ui.equipment.size(); i++) {
            String kind = ui.equipment.attribute(i,"kind");
            String label = ui.equipment.attribute(i,"name");
            int assigned = ui.equipment.assignedPen(i);
            String location = assigned < 0 ? "Unassigned" : ui.name(assigned);
            if (kind.equals(EquipmentRegister.SENSOR)) {
                option(sensorList,sensorTitle,"Sensors",sensors,i,label,ui.equipment.attribute(i,"type")+" · "+location,assigned<0||assigned==pen);
                available |= assigned<0||assigned==pen;
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

        android.app.Dialog dialog = new android.app.Dialog(ui);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(form);
        ((TextView)form.findViewById(R.id.pen_editor_title)).setText(pen<0?"Add pen":"Edit "+ui.name(pen));
        form.findViewById(R.id.pen_editor_close).setOnClickListener(v->dialog.dismiss());
        form.findViewById(R.id.pen_editor_cancel).setOnClickListener(v->dialog.dismiss());
        form.findViewById(R.id.manage_pen_devices).setOnClickListener(v->{dialog.dismiss();ui.go(6);});
        Button delete=form.findViewById(R.id.delete_pen);
        delete.setVisibility(pen<0?View.GONE:View.VISIBLE);
        delete.setOnClickListener(v->new AlertDialog.Builder(ui).setTitle("Delete "+ui.name(pen)+"?")
            .setMessage("This removes the pen. Its sensors, cameras and controls will remain registered and become unassigned.")
            .setNegativeButton("Cancel",null).setPositiveButton("Delete pen",(d,w)->{
                ui.equipment.deletePen(pen);dialog.dismiss();ui.render();
                Toast.makeText(ui,"Pen deleted",Toast.LENGTH_SHORT).show();
            }).show());
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.filter_panel);
        android.view.Window window=dialog.getWindow();
        window.setLayout(Math.min(ui.dp(480),ui.getResources().getDisplayMetrics().widthPixels-ui.dp(24)),(int)(ui.getResources().getDisplayMetrics().heightPixels*.9f));
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        form.findViewById(R.id.pen_editor_save).setOnClickListener(v->{
            int n;
            try { n=Integer.parseInt(number.getText().toString()); }
            catch (NumberFormatException e) { number.setError("Enter a positive pen number");return; }
            if(n<=0 || used.contains(n)){number.setError(n<=0?"Enter a positive pen number":"This pen number is already in use");return;}
            String custom = alias.getText().toString().trim();
            String label = "Pen "+n+(custom.isEmpty()?"":" · "+custom);
            if(pen<0)ui.equipment.createPen(n,label,sensors,cameras);else ui.equipment.updatePen(pen,n,label,sensors,cameras);
            dialog.dismiss();
            ui.render();
            Toast.makeText(ui,label+(pen<0?" added":" saved"),Toast.LENGTH_SHORT).show();
        });
    }

    private <T> void option(LinearLayout parent,TextView title,String group,Set<T> selected,T key,String label,String detail,boolean enabled) {
        CheckBox box = new CheckBox(ui);
        android.text.SpannableString text=new android.text.SpannableString(label+"\n"+detail);
        text.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),0,label.length(),0);
        text.setSpan(new android.text.style.AbsoluteSizeSpan(12,true),label.length()+1,text.length(),0);
        text.setSpan(new android.text.style.ForegroundColorSpan(MainActivity.MUTED),label.length()+1,text.length(),0);
        box.setText(text);
        box.setMinHeight(ui.dp(64));box.setEnabled(enabled);
        box.setChecked(selected.contains(key));
        box.setPadding(ui.dp(8),ui.dp(8),ui.dp(8),ui.dp(8));box.setTextSize(14);box.setTextColor(MainActivity.INK);box.setBackgroundResource(R.drawable.pen_assignment_surface);box.setSelected(box.isChecked());
        title.setText(group+" · "+selected.size()+" selected");
        box.setButtonTintList(android.content.res.ColorStateList.valueOf(MainActivity.GREEN));box.setAlpha(enabled?1f:.5f);
        LinearLayout.LayoutParams spacing=new LinearLayout.LayoutParams(-1,-2);spacing.topMargin=ui.dp(6);parent.addView(box,spacing);
        box.setOnCheckedChangeListener((v,on)->{
            box.setSelected(on);
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


