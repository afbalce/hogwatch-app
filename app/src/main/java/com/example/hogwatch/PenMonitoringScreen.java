package com.example.hogwatch;

import android.view.View;
import android.widget.*;
import android.text.*;
import java.util.Locale;

final class PenMonitoringScreen {
    private final MainActivity ui;
    PenMonitoringScreen(MainActivity ui){this.ui=ui;}
    void show(){
        ui.getLayoutInflater().inflate(R.layout.screen_monitor_directory,ui.content,true);
        EditText search=ui.findViewById(R.id.monitor_search);
        LinearLayout list=ui.findViewById(R.id.monitor_cards);
        Runnable update=()->{
            list.removeAllViews();String q=search.getText().toString().trim().toLowerCase(Locale.ROOT);
            for(int p=0;p<ui.count();p++){
                if(!(ui.name(p)+" "+ui.camera(p)).toLowerCase(Locale.ROOT).contains(q))continue;
                final int pen=p;
                View card=ui.getLayoutInflater().inflate(R.layout.monitor_camera_card,list,false);list.addView(card);
                ((TextView)card.findViewById(R.id.monitor_card_title)).setText(ui.name(p));
                String camera=ui.camera(p);
                ((PenCameraView)card.findViewById(R.id.monitor_preview)).setDemo(camera.isEmpty());
                ((TextView)card.findViewById(R.id.monitor_source)).setText(camera.isEmpty()?"Illustrative preview · No camera assigned":camera+" · Awaiting stream");
                LinearLayout readings=card.findViewById(R.id.monitor_readings);
                for(int m=0;m<4;m++){
                    LinearLayout row=new LinearLayout(ui);row.setPadding(0,ui.dp(7),0,ui.dp(7));readings.addView(row);
                    TextView label=ui.text(row,MainActivity.METRICS[m],13,MainActivity.MUTED,false);label.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));
                    TextView value=ui.text(row,ui.reading(p,m),14,MainActivity.INK,true);value.setGravity(android.view.Gravity.END);
                }
                Runnable open=()->PenCameraActivity.open(ui,pen);
                card.setOnClickListener(v->open.run());card.findViewById(R.id.monitor_fullscreen).setOnClickListener(v->open.run());
            }
            if(list.getChildCount()==0)ui.text(list,ui.count()==0?"No pens yet. Add a pen in Pen Management.":"No matching pens.",14,MainActivity.MUTED,false);
        };
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void afterTextChanged(Editable s){}public void onTextChanged(CharSequence s,int a,int b,int c){update.run();}});
        search.setOnEditorActionListener((v,a,e)->{if(a!=android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH)return false;search.clearFocus();((android.view.inputmethod.InputMethodManager)ui.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(),0);return true;});
        update.run();
    }
}
