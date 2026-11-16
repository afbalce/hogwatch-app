package com.example.hogwatch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.widget.*;
import java.io.*;

final class ProfilePhoto {
    static final int REQUEST=412;
    static void pick(MainActivity ui){
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE);
        try{ui.startActivityForResult(intent,REQUEST);}catch(android.content.ActivityNotFoundException e){Toast.makeText(ui,"No photo picker is available",Toast.LENGTH_SHORT).show();}
    }
    static void show(MainActivity ui,ImageView image){
        image.setClipToOutline(true);image.setBackgroundResource(R.drawable.avatar);
        File file=new File(ui.getFilesDir(),"profile-photo.png");
        Bitmap photo=BitmapFactory.decodeFile(file.getAbsolutePath());
        image.setImageTintList(null);
        if(photo!=null){image.setPadding(0,0,0,0);image.setImageBitmap(photo);}
        else{image.setPadding(ui.dp(12),ui.dp(12),ui.dp(12),ui.dp(12));image.setImageResource(R.drawable.ic_users);image.setImageTintList(android.content.res.ColorStateList.valueOf(0xffffffff));}
    }
    static void finish(MainActivity ui,int result,Intent data){
        if(result!=Activity.RESULT_OK||data==null||data.getData()==null)return;
        File temp=new File(ui.getFilesDir(),"profile-photo.tmp");
        try{
            BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;
            try(InputStream stream=ui.getContentResolver().openInputStream(data.getData())){BitmapFactory.decodeStream(stream,null,options);}
            if(options.outWidth<=0||options.outHeight<=0)throw new IOException("Invalid image");
            options.inSampleSize=1;while(Math.max(options.outWidth,options.outHeight)/options.inSampleSize>768)options.inSampleSize*=2;
            options.inJustDecodeBounds=false;
            Bitmap image;
            try(InputStream stream=ui.getContentResolver().openInputStream(data.getData())){image=BitmapFactory.decodeStream(stream,null,options);}
            if(image==null)throw new IOException("Invalid image");
            try(OutputStream stream=new FileOutputStream(temp)){if(!image.compress(Bitmap.CompressFormat.PNG,100,stream))throw new IOException("Cannot save image");}
            if(!temp.renameTo(new File(ui.getFilesDir(),"profile-photo.png")))throw new IOException("Cannot save photo");
            ImageView view=ui.findViewById(R.id.profile_photo);if(view!=null)show(ui,view);
            Toast.makeText(ui,"Profile picture updated",Toast.LENGTH_SHORT).show();
        }catch(Exception e){temp.delete();Toast.makeText(ui,"Could not use this photo. Please choose another image.",Toast.LENGTH_LONG).show();}
    }
}
