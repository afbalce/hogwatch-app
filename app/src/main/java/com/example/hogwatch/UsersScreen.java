package com.example.hogwatch;

import android.app.AlertDialog;
import android.view.View;
import android.widget.*;
import android.text.*;
import org.json.*;
import java.util.*;

final class UsersScreen {
    private final MainActivity ui;
    UsersScreen(MainActivity ui){this.ui=ui;}

    void show(){
        ui.getLayoutInflater().inflate(R.layout.screen_users,ui.content,true);
        LinearLayout summary=ui.findViewById(R.id.farm_team);
        int active=0;
        for(int i=0;i<ui.members.length();i++)if(ui.members.optJSONArray(i).optString(3).equals("Active"))active++;
        ui.pair(summary,"Total users",""+ui.members.length());ui.pair(summary,"Active users",""+active);
        ui.action(summary,"+ Add user",()->editMember(-1));
        ui.getLayoutInflater().inflate(R.layout.user_directory_filters,summary,true);
        EditText search=summary.findViewById(R.id.member_search);
        Spinner role=summary.findViewById(R.id.member_filter);
        SettingsScreen.options(ui,role,Arrays.asList("All roles","Owner","Manager","Viewer"),"All roles");
        role.setVisibility(View.GONE);
        String[] roles={"All roles","Owner","Manager","Viewer"};
        Button filters=ui.action(summary,"Filters",()->new AlertDialog.Builder(ui).setTitle("Filter by role")
            .setSingleChoiceItems(roles,role.getSelectedItemPosition(),(dialog,index)->{role.setSelection(index);dialog.dismiss();}).setNegativeButton("Cancel",null).show());
        Runnable update=()->{
            filters.setText(role.getSelectedItemPosition()==0?"Filters":"Filters (1)");
            LinearLayout directory=ui.findViewById(R.id.team_members);directory.removeAllViews();
            String query=search.getText().toString().trim().toLowerCase(Locale.ROOT);
            for(int i=0;i<ui.members.length();i++){
                JSONArray user=ui.members.optJSONArray(i);final int id=i;
                if(!(user.optString(0)+" "+user.optString(1)).toLowerCase(Locale.ROOT).contains(query))continue;
                if(role.getSelectedItemPosition()>0&&!role.getSelectedItem().equals(user.optString(2)))continue;
                LinearLayout item=ui.card(directory,user.optString(0));
                ui.text(item,user.optString(1),14,MainActivity.MUTED,false);
                ui.pair(item,"Role",user.optString(2));ui.pair(item,"Status",user.optString(3));
                ui.action(item,"Edit member",()->editMember(id));
            }
            if(directory.getChildCount()==0)ui.text(directory,"No users match your search. Try another name or role.",14,MainActivity.MUTED,false);
        };
        SearchControls.attach(ui,summary,search,update,()->role.setSelection(0));
        search.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){update.run();}
            public void afterTextChanged(Editable e){}
        });
        role.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(AdapterView<?> p){}
            public void onItemSelected(AdapterView<?> p,View v,int pos,long id){update.run();}
        });
        update.run();
    }

    void editMember(int id){
        JSONArray user=id<0?new JSONArray():ui.members.optJSONArray(id);
        View form=ui.getLayoutInflater().inflate(R.layout.dialog_member,null);
        EditText name=form.findViewById(R.id.member_name),email=form.findViewById(R.id.member_email);
        Spinner role=form.findViewById(R.id.member_role),status=form.findViewById(R.id.member_state);
        name.setText(user.optString(0));email.setText(user.optString(1));
        List<String> roles=new ArrayList<>(Arrays.asList("Owner","Manager","Viewer"));
        if(id>=0&&!roles.contains(user.optString(2)))roles.add(user.optString(2));
        SettingsScreen.options(ui,role,roles,user.optString(2,"Viewer"));
        SettingsScreen.options(ui,status,Arrays.asList("Active","Inactive","Pending"),user.optString(3,"Active"));
        AlertDialog dialog=new AlertDialog.Builder(ui).setTitle(id<0?"Add user":"Edit member").setView(form)
                .setNegativeButton("Cancel",null).setPositiveButton("Save user",null).create();
        dialog.show();
        dialog.getButton(-1).setOnClickListener(v->{
            String n=name.getText().toString().trim(),e=email.getText().toString().trim();
            if(n.isEmpty()){name.setError("Enter a name");return;}
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches()){email.setError("Enter a valid email address");return;}
            for(int i=0;i<ui.members.length();i++)if(i!=id&&e.equalsIgnoreCase(ui.members.optJSONArray(i).optString(1))){email.setError("This email is already registered");return;}
            try{
                ui.members.put(id<0?ui.members.length():id,new JSONArray().put(n).put(e).put(role.getSelectedItem()).put(status.getSelectedItem()));
                ui.put("members",ui.members.toString());
            }catch(JSONException error){throw new IllegalStateException(error);}
            dialog.dismiss();ui.render();Toast.makeText(ui,"User saved",Toast.LENGTH_SHORT).show();
        });
    }
}
