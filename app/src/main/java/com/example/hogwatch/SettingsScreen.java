package com.example.hogwatch;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

final class SettingsScreen {
    private static final String[] SECTIONS={"Account","Security","Alerts","Cameras","Notifications"};
    private static final int[] PANELS={R.layout.settings_account,R.layout.settings_security,R.layout.settings_alerts,R.layout.settings_cameras,R.layout.settings_notifications};
    private static final String[] INTERVALS={"Off","Every 15 minutes","Every 30 minutes","Every hour"};
    private final MainActivity ui;
    private LinearLayout panel;

    SettingsScreen(MainActivity ui){this.ui=ui;}

    void show(){
        ui.getLayoutInflater().inflate(R.layout.screen_settings,ui.content,true);
        LinearLayout tabs=ui.findViewById(R.id.settings_tabs);
        HorizontalScrollView scroller=ui.findViewById(R.id.settings_scroll);
        for(int i=0;i<SECTIONS.length;i++){
            final int index=i;
            Button item=new Button(ui);
            item.setText(SECTIONS[i]);item.setAllCaps(false);item.setTextSize(14);
            item.setMinWidth(0);item.setMinimumWidth(0);item.setMinHeight(ui.dp(48));
            item.setPadding(ui.dp(14),0,ui.dp(14),0);item.setStateListAnimator(null);
            item.setSelected(ui.tab==i);item.setTextColor(item.isSelected()?Color.WHITE:MainActivity.GREEN);
            if(item.isSelected())item.setBackgroundResource(R.drawable.action_primary);else item.setBackgroundColor(Color.TRANSPARENT);
            tabs.addView(item);
            item.setOnClickListener(v->{if(ui.tab!=index){ui.tab=index;ui.render();}});
            if(item.isSelected())scroller.post(()->scroller.scrollTo(item.getLeft()-(scroller.getWidth()-item.getWidth())/2,0));
        }
        panel=ui.findViewById(R.id.settings_panel);
        panel.setFocusableInTouchMode(true);
        ui.getLayoutInflater().inflate(PANELS[ui.tab],panel,true);
        switch(ui.tab){
            case 0:account();break;
            case 1:security();break;
            case 2:alerts();break;
            case 3:cameras();break;
            case 4:notifications();break;
        }
    }

    private <T extends View> T view(int id){return panel.findViewById(id);}
    private void saved(){Toast.makeText(ui,"Settings saved on this device",Toast.LENGTH_SHORT).show();}

    private void account(){
        EditText name=view(R.id.full_name),email=view(R.id.email_address),phone=view(R.id.mobile_number);
        name.setText(ui.pref("name","John Doe"));email.setText(ui.pref("email","john.doe@hogwatch.com"));phone.setText(ui.pref("phone","+1 (555) 123-4567"));
        ((TextView)view(R.id.profile_name)).setText(name.getText());
        ((TextView)view(R.id.profile_initials)).setText(((TextView)ui.findViewById(R.id.account)).getText());
        view(R.id.save_profile).setOnClickListener(v->{
            String n=name.getText().toString().trim(), e=email.getText().toString().trim(), p=phone.getText().toString().trim();
            if(n.isEmpty()){name.setError("Enter your name");return;}
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches()){email.setError("Enter a valid email address");return;}
            if(!android.util.Patterns.PHONE.matcher(p).matches()){phone.setError("Enter a valid phone number");return;}
            ui.prefs.edit().putString("name",n).putString("email",e).putString("phone",p).apply();
            saved();ui.render();
        });
    }

    private void security(){
        // The demo has no authenticated session or remote account provider.
        for(int id:new int[]{R.id.two_factor,R.id.current_password,R.id.new_password,R.id.confirm_password,R.id.change_password})
            view(id).setEnabled(false);
    }

    static void options(MainActivity ui,Spinner spinner,List<String> values,String selected){
        ArrayAdapter<String> adapter=new ArrayAdapter<>(ui,android.R.layout.simple_spinner_item,values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(Math.max(0,values.indexOf(selected)));
    }

    private Spinner preference(int id,String key,String def,String... values){
        Spinner spinner=view(id);
        options(ui,spinner,Arrays.asList(values),ui.pref(key,def));
        return spinner;
    }

    private void alerts(){
        EditText delay=view(R.id.alert_delay);
        delay.setText(Integer.toString(ui.prefs.getInt("alert_delay",5)));
        Spinner snapshot=preference(R.id.alert_snapshot,"snapshot","Every 30 minutes",INTERVALS);
        view(R.id.save_global).setOnClickListener(v->{
            try{
                int minutes=Integer.parseInt(delay.getText().toString());
                if(minutes<0)throw new NumberFormatException();
                ui.prefs.edit().putInt("alert_delay",minutes).putString("snapshot",snapshot.getSelectedItem().toString()).apply();
                saved();
            }catch(NumberFormatException e){delay.setError("Enter minutes as a nonnegative whole number");}
        });
        LinearLayout list=view(R.id.rule_list);
        ui.levels.show(list);
        view(R.id.add_level).setOnClickListener(v->ui.levels.edit(-1,list));
    }

    private void cameras(){
        Spinner quality=preference(R.id.stream_quality,"quality","1080p HD","720p HD","1080p HD","4K");
        Spinner retention=preference(R.id.retention,"retention","14 days","7 days","14 days","30 days");
        Spinner snapshot=preference(R.id.camera_snapshot,"snapshot","Every 30 minutes",INTERVALS);
        Switch continuous=view(R.id.continuous), event=view(R.id.event_recording);
        continuous.setChecked(ui.prefs.getBoolean("continuous",false));
        event.setChecked(ui.prefs.getBoolean("event_recording",true));
        view(R.id.save_cameras).setOnClickListener(v->{
            ui.prefs.edit().putString("quality",quality.getSelectedItem().toString())
                    .putString("retention",retention.getSelectedItem().toString()).putString("snapshot",snapshot.getSelectedItem().toString())
                    .putBoolean("continuous",continuous.isChecked()).putBoolean("event_recording",event.isChecked()).apply();
            saved();
        });
    }

    private void notifications(){
        int[] ids={R.id.sms_alerts,R.id.email_digest,R.id.offline_alerts,R.id.low_battery};
        String[] keys={"notify_sms","notify_digest","notify_offline","notify_battery"};
        for(int i=0;i<ids.length;i++){
            Switch setting=view(ids[i]);String key=keys[i];
            setting.setChecked(ui.prefs.getBoolean(key,i<3));
            setting.setOnCheckedChangeListener((v,on)->ui.prefs.edit().putBoolean(key,on).apply());
        }
    }
}
