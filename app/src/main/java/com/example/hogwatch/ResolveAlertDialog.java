package com.example.hogwatch;

import android.app.Dialog;
import android.view.*;
import android.widget.*;
import java.util.Arrays;

final class ResolveAlertDialog {
    static void show(MainActivity ui,int id,String subject){
        Dialog dialog=new Dialog(ui);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View form=ui.getLayoutInflater().inflate(R.layout.dialog_resolve_alert,null);dialog.setContentView(form);
        ((TextView)form.findViewById(R.id.resolve_subject)).setText(subject);
        Spinner action=form.findViewById(R.id.resolve_action);
        SettingsScreen.options(ui,action,Arrays.asList("Select response","Adjusted ventilation","Adjusted cooling / sprinklers","Repaired or replaced sensor","Restored device connection","Inspected and verified readings","Other action"),"Select response");
        form.findViewById(R.id.resolve_close).setOnClickListener(v->dialog.dismiss());
        form.findViewById(R.id.resolve_cancel).setOnClickListener(v->dialog.dismiss());
        form.findViewById(R.id.resolve_confirm).setOnClickListener(v->{
            TextView error=form.findViewById(R.id.resolve_error);
            boolean inspected=((CheckBox)form.findViewById(R.id.resolve_inspected)).isChecked();
            boolean hardware=((CheckBox)form.findViewById(R.id.resolve_hardware)).isChecked();
            boolean stable=((CheckBox)form.findViewById(R.id.resolve_stable)).isChecked();
            if(!inspected||!hardware||!stable||action.getSelectedItemPosition()==0){
                error.setText("Complete the checklist and select the action taken.");error.setVisibility(View.VISIBLE);return;
            }
            String notes=((EditText)form.findViewById(R.id.resolve_notes)).getText().toString().trim();
            ui.prefs.edit().putString("alert"+id,"Resolved").remove("alert_remind_at"+id).putBoolean("alert_reminder"+id,false).putString("resolution_action"+id,action.getSelectedItem().toString())
                .putString("resolution_notes"+id,notes).putLong("resolution_time"+id,System.currentTimeMillis())
                .putBoolean("resolution_inspected"+id,inspected).putBoolean("resolution_hardware"+id,hardware).putBoolean("resolution_stable"+id,stable).apply();
            dialog.dismiss();ui.render();Toast.makeText(ui,"Alert resolved",Toast.LENGTH_SHORT).show();
        });
        dialog.show();Window window=dialog.getWindow();window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setLayout(Math.min(ui.dp(480),ui.getResources().getDisplayMetrics().widthPixels-ui.dp(24)),(int)(ui.getResources().getDisplayMetrics().heightPixels*.88f));
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}

