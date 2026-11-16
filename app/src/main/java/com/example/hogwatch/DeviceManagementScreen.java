package com.example.hogwatch;

import android.view.View;
import android.widget.*;
import android.text.*;

final class DeviceManagementScreen {
    static final int ALL=0, CAMERAS=1, SENSORS=2, CONTROLS=3, GATEWAY=4;
    private final MainActivity ui;
    DeviceManagementScreen(MainActivity ui){this.ui=ui;}

    void show(){
        ui.getLayoutInflater().inflate(R.layout.screen_device_management,ui.content,true);
        LinearLayout gateway=ui.findViewById(R.id.gateway),controls=ui.findViewById(R.id.controls),register=ui.findViewById(R.id.register);
        if(ui.tab==ALL)ui.tab=GATEWAY;
        gateway.setVisibility(ui.tab==GATEWAY?View.VISIBLE:View.GONE);
        controls.setVisibility(ui.tab==CONTROLS?View.VISIBLE:View.GONE);
        boolean showRegister=ui.tab==CAMERAS||ui.tab==SENSORS;
        register.setVisibility(showRegister?View.VISIBLE:View.GONE);
        if(ui.tab==GATEWAY)new GatewayScreen(ui).show(gateway);
        if(ui.tab==CONTROLS)new DeviceDirectoryScreen(ui).show(controls,EquipmentRegister.CONTROL);
        if(showRegister)new DeviceDirectoryScreen(ui).show(register,ui.tab==CAMERAS?EquipmentRegister.CAMERA:EquipmentRegister.SENSOR);
    }
}
