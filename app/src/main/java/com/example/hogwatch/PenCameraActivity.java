package com.example.hogwatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.util.*;

public final class PenCameraActivity extends Activity {
    static void open(MainActivity ui,int pen){
        Intent intent=new Intent(ui,PenCameraActivity.class);
        intent.putExtra("name",ui.name(pen));intent.putExtra("cameras",ui.equipment.names(EquipmentRegister.CAMERA,pen).toArray(new String[0]));
        String[] values=new String[4];for(int m=0;m<4;m++)values[m]=ui.reading(pen,m);
        intent.putExtra("values",values);ui.startActivity(intent);
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pen_camera);
        ((TextView)findViewById(R.id.camera_title)).setText(getIntent().getStringExtra("name"));
        findViewById(R.id.camera_close).setOnClickListener(v->finish());
        String[] values=getIntent().getStringArrayExtra("values");LinearLayout overlay=findViewById(R.id.camera_overlay);
        for(int m=0;m<4;m++){
            LinearLayout tile=new LinearLayout(this);tile.setOrientation(1);tile.setPadding(12,4,12,4);
            TextView title=new TextView(this);title.setText(MainActivity.METRICS[m]);title.setTextColor(0xffCBDDCF);title.setTextSize(12);tile.addView(title);
            TextView value=new TextView(this);value.setText(values==null?"—":values[m]);value.setTextColor(-1);value.setTextSize(21);value.setTypeface(null,1);tile.addView(value);
            overlay.addView(tile,new LinearLayout.LayoutParams(0,-2,1));
        }
        List<String> sources=new ArrayList<>();String[] cameras=getIntent().getStringArrayExtra("cameras");if(cameras!=null)Collections.addAll(sources,cameras);
        sources.add("Illustrative preview");
        Spinner source=findViewById(R.id.camera_source);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,sources);adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);source.setAdapter(adapter);
        source.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(AdapterView<?> p){}
            public void onItemSelected(AdapterView<?> p,View v,int index,long id){
                boolean demo=index==sources.size()-1;((PenCameraView)findViewById(R.id.full_camera)).setDemo(demo);
                ((TextView)findViewById(R.id.camera_note)).setText((demo?"Illustrative camera preview":"No live stream connected")+" · Sensor readings captured when opened");
            }
        });
    }
}
