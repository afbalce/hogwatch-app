package com.example.hogwatch;

import android.app.Dialog;
import android.content.ContentValues;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

final class PenCameraDemo {
    private final MainActivity ui;
    private final LinearLayout container;
    private final int pen;
    private boolean demo=true, playing=false, started=false;
    private long elapsed, since;
    private Preview preview;
    private TextView caption;
    private Button play, snapshot, full;

    PenCameraDemo(MainActivity ui,LinearLayout container,int pen){
        this.ui=ui;this.container=container;this.pen=pen;
    }

    void show(){
        List<String> sources=new ArrayList<>();
        sources.add("Demo · Overhead pig activity");
        sources.addAll(ui.equipment.names(EquipmentRegister.CAMERA,pen));
        Spinner source=new Spinner(ui);
        source.setContentDescription("Camera source");source.setMinimumHeight(ui.dp(48));
        ArrayAdapter<String> adapter=new ArrayAdapter<>(ui,android.R.layout.simple_spinner_item,sources);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        source.setAdapter(adapter);container.addView(source);
        preview=new Preview();container.addView(preview,new LinearLayout.LayoutParams(-1,ui.dp(158)));
        caption=ui.text(container,"",13,MainActivity.MUTED,false);
        play=ui.action(container,"Play demo",()->{
            if(playing)elapsed=time();else since=SystemClock.uptimeMillis();
            started=true;playing=!playing;refresh();
        });
        snapshot=ui.action(container,"Snapshot",this::snapshot);
        full=ui.action(container,"Full screen",this::fullScreen);
        ui.action(container,"Manage cameras",()->ui.go(6));
        source.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(AdapterView<?> p){}
            public void onItemSelected(AdapterView<?> p,View v,int i,long id){
                demo=i==0;started=false;playing=false;elapsed=0;refresh();
            }
        });
        refresh();
    }

    private long time(){return elapsed+(playing?SystemClock.uptimeMillis()-since:0);}

    private void refresh(){
        caption.setText(demo?"Demo loop · Resting, feeding and walking":"No live stream connected to this camera");
        play.setText(playing?"Pause demo":started?"Resume demo":"Play demo");
        play.setVisibility(demo?View.VISIBLE:View.GONE);
        full.setEnabled(demo && started);snapshot.setEnabled(demo && started);
        preview.invalidate();
    }

    private final class Preview extends View {
        final Paint brush=new Paint(Paint.ANTI_ALIAS_FLAG);
        Preview(){super(ui);setContentDescription("Illustrated overhead pen demo");}
        private void oval(Canvas c,int color,float l,float t,float r,float b){
            brush.setColor(color);c.drawOval(l,t,r,b,brush);
        }
        private void pig(Canvas c,float x,float y,float angle,int color){
            c.save();c.translate(x,y);c.rotate(angle);
            oval(c,0x40212e21,-19,-9,20,14);
            oval(c,color,-20,-12,13,12);oval(c,color,6,-10,22,10);
            oval(c,0xffbb806f,8,-13,15,-5);oval(c,0xffbb806f,8,5,15,13);
            oval(c,0xffcf9581,17,-5,26,5);oval(c,0xff413d32,16,-6,18,-4);
            c.restore();
        }
        protected void onDraw(Canvas c){
            c.drawColor(0xff173c2d);
            if(!demo||!started){
                brush.setTextAlign(Paint.Align.CENTER);brush.setColor(0xffecf5e9);brush.setTextSize(ui.dp(16));
                c.drawText(demo?"Overhead activity demo":"Camera offline",getWidth()/2f,getHeight()/2f,brush);return;
            }
            c.save();c.scale(getWidth()/320f,getHeight()/160f);
            brush.setColor(0xffcbbd9d);c.drawRoundRect(9,9,311,151,7,7,brush);
            brush.setColor(0xffa89c80);c.drawRoundRect(19,18,30,142,3,3,brush);
            double phase=time()/1500.0;
            pig(c,87,109,16,0xffefbdae);
            pig(c,54+(float)Math.sin(phase*2),58,178,0xffe7b4a4);
            pig(c,211+(float)Math.sin(phase)*26,89+(float)Math.cos(phase)*17,(float)(-phase*180/Math.PI),0xffedcbb9);
            c.restore();
            if(playing && isShown())postInvalidateDelayed(60);
        }
    }

    private void fullScreen(){
        Dialog dialog=new Dialog(ui,android.R.style.Theme_Material_NoActionBar_Fullscreen);
        LinearLayout body=new LinearLayout(ui);body.setOrientation(LinearLayout.VERTICAL);body.setBackgroundColor(0xff173c2d);
        Preview large=new Preview();body.addView(large,new LinearLayout.LayoutParams(-1,0,1));
        ui.action(body,"Close full screen",dialog::dismiss);
        dialog.setContentView(body);dialog.show();
    }

    private void snapshot(){
        Bitmap image=Bitmap.createBitmap(preview.getWidth(),preview.getHeight(),Bitmap.Config.ARGB_8888);
        preview.draw(new Canvas(image));
        String name="HogWatch-pen-"+ui.penNumber(pen)+"-"+System.currentTimeMillis()+".png";
        Uri uri=null;
        try{
            OutputStream stream;
            if(Build.VERSION.SDK_INT>=29){
                ContentValues values=new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME,name);
                values.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/HogWatch");
                values.put(MediaStore.Images.Media.IS_PENDING,1);
                uri=ui.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
                if(uri==null)throw new IOException();
                stream=ui.getContentResolver().openOutputStream(uri);
            }else{
                File dir=ui.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if(dir==null)throw new IOException();
                stream=new FileOutputStream(new File(dir,name));
            }
            try(OutputStream out=stream){if(out==null || !image.compress(Bitmap.CompressFormat.PNG,100,out))throw new IOException();}
            if(uri!=null){
                ContentValues values=new ContentValues();values.put(MediaStore.Images.Media.IS_PENDING,0);
                ui.getContentResolver().update(uri,values,null,null);
            }
            Toast.makeText(ui,uri!=null?"Snapshot saved to Pictures/HogWatch":"Snapshot saved to app pictures",Toast.LENGTH_SHORT).show();
        }catch(IOException | RuntimeException e){
            if(uri!=null)ui.getContentResolver().delete(uri,null,null);
            Toast.makeText(ui,"Could not save snapshot",Toast.LENGTH_LONG).show();
        }finally{image.recycle();}
    }
}
