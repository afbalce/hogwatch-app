package com.example.hogwatch;

import android.os.Handler;
import android.os.Looper;

/** Durable in-app reminders; overdue reminders surface when the app resumes. */
final class AlertReminders {
    static final long DELAY=5*60*1000L;
    private final MainActivity ui;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable tick=new Runnable(){public void run(){check();handler.postDelayed(this,15000);}};
    AlertReminders(MainActivity ui){this.ui=ui;}
    void start(){stop();handler.post(tick);}
    void stop(){handler.removeCallbacks(tick);}
    void check(){
        if(ui.prefs.getBoolean("logged_out",false))return;
        long now=System.currentTimeMillis();boolean due=false;
        android.content.SharedPreferences.Editor edit=ui.prefs.edit();
        for(int i=0;i<4;i++){
            if(!ui.workflow(i).equals("Acknowledged")||ui.alertPen(i)<0)continue;
            long next=ui.prefs.getLong("alert_remind_at"+i,0);
            if(next==0){edit.putLong("alert_remind_at"+i,now+DELAY);continue;}
            if(now>=next){edit.putString("alert"+i,"Open").putBoolean("alert_reminder"+i,true).remove("alert_remind_at"+i);due=true;}
        }
        edit.apply();
        if(due){
            ui.render();
            ui.findViewById(R.id.notifications).post(()->ui.showNotifications());
        }
    }
}
