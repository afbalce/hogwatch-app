package com.example.hogwatch;

import android.view.View;
import android.widget.*;

final class ProfileScreen {
    private final MainActivity ui;
    ProfileScreen(MainActivity ui){this.ui=ui;}
    void show(){
        ui.getLayoutInflater().inflate(R.layout.screen_profile,ui.content,true);
        bindProfile();
    }
    private <T extends View> T view(int id){return ui.content.findViewById(id);}
    private void bindProfile(){
        EditText name=view(R.id.full_name),email=view(R.id.email_address),phone=view(R.id.mobile_number);
        EditText username=view(R.id.profile_username);username.setText(ui.pref("username","john.doe"));
        name.setText(ui.pref("name","John Doe"));email.setText(ui.pref("email","john.doe@hogwatch.com"));phone.setText(ui.pref("phone","+1 (555) 123-4567"));
        ((TextView)view(R.id.profile_name)).setText(name.getText());
        ((TextView)view(R.id.profile_handle)).setText("@"+username.getText());
        ProfilePhoto.show(ui,view(R.id.profile_photo));
        view(R.id.profile_photo).setOnClickListener(v->ProfilePhoto.pick(ui));
        view(R.id.change_profile_photo).setOnClickListener(v->ProfilePhoto.pick(ui));
        view(R.id.save_profile).setOnClickListener(v->{
            String n=name.getText().toString().trim(), e=email.getText().toString().trim(), p=phone.getText().toString().trim();
            if(n.isEmpty()){name.setError("Enter your name");return;}
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches()){email.setError("Enter a valid email address");return;}
            if(!android.util.Patterns.PHONE.matcher(p).matches()){phone.setError("Enter a valid phone number");return;}
            String handle=username.getText().toString().trim();
            if(!handle.matches("[A-Za-z0-9_.-]{3,40}")){username.setError("Use 3–40 letters, numbers, dots, underscores or hyphens");return;}
            ui.prefs.edit().putString("username",handle).putString("name",n).putString("email",e).putString("phone",p).apply();
            Toast.makeText(ui,"Profile updated on this device",Toast.LENGTH_SHORT).show();ui.render();
        });
    }

}

