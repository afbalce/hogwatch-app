package com.example.hogwatch;

import android.app.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final String[] PAGES = {"Dashboard","Pen monitoring","Pen management","Alert management","Analytics & reports","Users","Device management","Settings"};
    static final String[] METRICS = {"Temperature","Humidity","Ammonia"};
    private static final String[] UNITS = {" °C"," %"," ppm"};
    private static final double[][] READINGS = {{26.8,65,11},{29.1,68,15},{27,62,13},{28.4,74,27},{26.2,61,9}};
    private static final String[] ALERTS = {"Ammonia above limit","Temperature rising","Sensor S-09 battery low","Sensor offline"};
    private static final String[] DETAILS = {"27 ppm · threshold 25 ppm","29.1 °C · target 22–28 °C","14% remaining · threshold 15%","No readings for 2 hours"};
    static final int[] IMPACTED = {3,1,3,5};
    static final String[] LEVELS = {"Critical","Warning","Warning","Offline"};
    static final int GREEN = 0xff205b3f, INK = 0xff183a2b, MUTED = 0xff587062;
    LinearLayout content, nav;
    android.content.SharedPreferences prefs;
    int page, selected=3, metric, tab, filter;
    private String query="";
    private boolean sorted;
    private final ArrayDeque<Integer> history=new ArrayDeque<>();
    JSONArray members;
    EquipmentRegister equipment;
    AlertLevels levels;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        prefs=getSharedPreferences("farm",MODE_PRIVATE);
        equipment = new EquipmentRegister(this);
        levels = new AlertLevels(this);
        findViewById(R.id.account).setOnClickListener(this::showProfileMenu);
        findViewById(R.id.add).setOnClickListener(v -> addMenu());
        try { members=new JSONArray(prefs.getString("members","[[\"Maria Santos\",\"maria.s@example.com\",\"Owner\",\"Active\"],[\"Jun Reyes\",\"jun.reyes@example.com\",\"Manager\",\"Active\"]]")); }
        catch(JSONException e){ members=new JSONArray(); }
        if(state!=null){page=state.getInt("page");selected=state.getInt("selected");metric=state.getInt("metric");tab=state.getInt("tab");}
        if(Build.VERSION.SDK_INT>=30){
            getWindow().setDecorFitsSystemWindows(false);
            findViewById(R.id.main).setOnApplyWindowInsetsListener((v,insets)->{
                android.graphics.Insets b=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());
                v.setPadding(b.left,b.top,b.right,b.bottom);return insets;
            });
        }
        content=findViewById(R.id.content);nav=findViewById(R.id.navigation);
        render();
    }
    @Override protected void onSaveInstanceState(Bundle s){super.onSaveInstanceState(s);s.putInt("page",page);s.putInt("selected",selected);s.putInt("metric",metric);s.putInt("tab",tab);}
    int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
    String pref(String k,String def){return prefs.getString(k,def);}
    void put(String k,String v){prefs.edit().putString(k,v).apply();}
    int count(){return prefs.getInt("count",7);}
    int penNumber(int id){return prefs.getInt("pen_number"+id,id+1);}
    String name(int i){return pref("pen"+i,"Pen "+(i+1));}
    String sensor(int i){return equipment.assigned(EquipmentRegister.SENSOR,i);}
    String camera(int i){return equipment.assigned(EquipmentRegister.CAMERA,i);}
    String status(int i){if(i>=READINGS.length)return "Offline";return levels.status(i,READINGS[i][0],READINGS[i][2]);}
    String value(double n,int m){return (m==0?String.format(Locale.US,"%.1f",n):String.valueOf(Math.round(n)))+UNITS[m];}
    double sample(int pen,int metric){return READINGS[pen][metric];}
    String metricStatus(int pen,int metric){
        if(pen>=READINGS.length)return "Offline";
        double value=sample(pen,metric);
        if(metric==1)return value<50 || value>70?"Warning":"OK";
        return levels.status(pen,metric==0?value:Double.NEGATIVE_INFINITY,metric==2?value:Double.NEGATIVE_INFINITY);
    }
    String reading(int i,int m){return i<READINGS.length?value(READINGS[i][m],m):"—";}
    double mean(int m){double sum=0;for(double[] v:READINGS)sum+=v[m];return sum/READINGS.length;}
    String workflow(int i){return pref("alert"+i,"Open");}
    int active(String severity){int n=0;for(int i=0;i<3;i++)if(!workflow(i).equals("Resolved")&&(severity.isEmpty()||LEVELS[i].equals(severity)))n++;return n;}
    int tint(String s){return s.equals("Critical")?0xffaf4936:s.equals("Warning")?0xff9b651d:s.equals("Offline")?MUTED:GREEN;}
    void go(int n){if(n!=page)history.push(page);navigate(n);}
    private void navigate(int n){page=n;tab=0;filter=0;query="";render();((ScrollView)findViewById(R.id.scroll)).scrollTo(0,0);}
    @Override public void onBackPressed(){if(!history.isEmpty())navigate(history.pop());else if(page!=0)navigate(0);else super.onBackPressed();}
    private void showProfileMenu(View anchor) {
        View panel = getLayoutInflater().inflate(R.layout.profile_menu, null);
        PopupWindow popup = new PopupWindow(panel, Math.min(dp(248), content.getWidth()),
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        popup.setElevation(dp(8));
        popup.setOutsideTouchable(true);
        int[] buttons = {R.id.manage_pens, R.id.manage_users, R.id.manage_devices, R.id.open_settings};
        int[] destinations = {2, 5, 6, 7};
        for (int i = 0; i < buttons.length; i++) {
            int destination = destinations[i];
            panel.findViewById(buttons[i]).setOnClickListener(v -> {
                popup.dismiss();
                go(destination);
            });
        }
        popup.showAsDropDown(anchor, 0, dp(4), Gravity.END);
    }

    private void addMenu() {
        String[] choices = {"Pen", "User", "Camera", "Sensor", "Environmental control"};
        new AlertDialog.Builder(this).setTitle("Add to farm").setItems(choices, (dialog,index) -> {
            if (index == 0) { go(2); editPen(count()); }
            else if (index == 1) { go(5); new UsersScreen(this).editMember(-1); }
            else { go(6); tab = index - 2; render(); equipment.add(choices[index],-1); }
        }).show();
    }
    TextView text(LinearLayout parent,String s,int size,int color,boolean bold){
        TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);parent.addView(v);return v;
    }
    private LinearLayout row(LinearLayout parent){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);parent.addView(r);return r;}
    LinearLayout card(LinearLayout parent, String title){
        LinearLayout c=(LinearLayout)getLayoutInflater().inflate(R.layout.section_card,parent,false);
        ((TextView)c.findViewById(R.id.cardTitle)).setText(title);parent.addView(c);return c;
    }
    void pair(LinearLayout c,String label,String val){LinearLayout r=row(c);r.setMinimumHeight(dp(44));TextView a=text(r,label,14,MUTED,false);a.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));TextView b=text(r,val,15,INK,true);b.setGravity(Gravity.END);b.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));}
    Button action(LinearLayout parent,String label,Runnable fn){
        Button b = new Button(this);
        boolean primary = label.startsWith("+");
        b.setText(label); b.setAllCaps(false); b.setTextSize(14);
        b.setTextColor(primary ? android.graphics.Color.WHITE : GREEN);
        b.setBackgroundResource(primary ? R.drawable.action_primary : R.drawable.action_surface);
        b.setStateListAnimator(null); b.setMinimumHeight(dp(48));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1,-2);
        params.topMargin=dp(6); b.setPadding(dp(12),dp(8),dp(12),dp(8));
        b.setOnClickListener(v -> fn.run()); parent.addView(b,params); return b;
    }
    interface Pick {void accept(int n);}
    void picker(LinearLayout parent,String title,String[] options,int current,Pick pick){
        action(parent,title+"   ·   "+options[current]+"  ▾",()->new AlertDialog.Builder(this).setTitle(title).setSingleChoiceItems(options,current,(d,i)->{d.dismiss();pick.accept(i);}).show());
    }
    String[] penNames(){String[] a=new String[count()];for(int i=0;i<a.length;i++)a[i]=name(i);return a;}
    void render(){
        content.removeAllViews();nav.removeAllViews();
        String user=pref("name","John Doe").trim();String[] words=user.split("\\s+");
        ((TextView)findViewById(R.id.account)).setText((user.substring(0,1)+(words.length>1?words[words.length-1].substring(0,1):"")).toUpperCase(Locale.ROOT));
        String[] labels = {"Home", "Pen Monitoring", "Alerts", "Analytics & Reports"};
        int[] destinations = {0, 1, 3, 4};
        int[] icons = {R.drawable.ic_overview, R.drawable.ic_monitor, R.drawable.ic_alerts, R.drawable.ic_reports};
        for (int i = 0; i < destinations.length; i++) {
            int destination = destinations[i];
            boolean selectedTab = page == destination;
            View item = getLayoutInflater().inflate(R.layout.bottom_nav_item, nav, false);
            ImageView icon = item.findViewById(R.id.nav_icon);
            icon.setImageResource(icons[i]);
            icon.setColorFilter(selectedTab ? 0xff163e33 : 0xffc3d8cb);
            if (selectedTab) icon.setBackgroundResource(R.drawable.nav_indicator);
            TextView label = item.findViewById(R.id.nav_label);
            label.setText(labels[i]);
            label.setTextColor(selectedTab ? 0xffe1f7bc : 0xffd4e3d9);
            if (selectedTab) label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            item.setContentDescription(labels[i]);
            item.setSelected(selectedTab);
            item.setOnClickListener(v -> go(destination));
            nav.addView(item);
        }
        View h=getLayoutInflater().inflate(R.layout.farm_header,content,false);content.addView(h);
        h.findViewById(R.id.header_contours).setVisibility(page==0?View.GONE:View.VISIBLE);
        if(page==0)h.findViewById(R.id.header_text).setBackgroundResource(R.drawable.header_scrim);
        h.findViewById(R.id.farmArt).setVisibility(page==0?View.VISIBLE:View.GONE);
        ((TextView)h.findViewById(R.id.screenTitle)).setText(page==0?"Farm overview":PAGES[page]);
        String[] subtitles={"Farm conditions · demo readings","Environmental readings by pen","Sensors and camera assignments","Environmental and device alerts","Last 24 hours · sample history","Farm team and access","Gateway, sensors and controls","Account and farm preferences"};
        ((TextView)h.findViewById(R.id.subtitle)).setText(subtitles[page]);
        switch (page) {
            case 0: new DashboardScreen(this).show(); break;
            case 1: new PenMonitoringScreen(this).show(); break;
            case 2: new PenManagementScreen(this).show(); break;
            case 3: new AlertManagementScreen(this).show(); break;
            case 4: new AnalyticsReportsScreen(this).show(); break;
            case 5: new UsersScreen(this).show(); break;
            case 6: new DeviceManagementScreen(this).show(); break;
            case 7: new SettingsScreen(this).show(); break;
        }
    }
    private void directoryFilters(LinearLayout parent, String[] states, Runnable update) {
        View fields = getLayoutInflater().inflate(R.layout.directory_filters,parent,false);
        parent.addView(fields);
        EditText input = fields.findViewById(R.id.pen_query);
        input.setText(query);
        Button status = fields.findViewById(R.id.status_filter);
        status.setText(states[filter] + " ▾");
        status.setContentDescription("Filter by status: " + states[filter]);
        status.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Pen status")
                .setSingleChoiceItems(states,filter,(dialog,which)->{filter=which; dialog.dismiss();render();}).show());
        input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int n,int after){}public void afterTextChanged(Editable s){}public void onTextChanged(CharSequence s,int start,int before,int n){query=s.toString().toLowerCase(Locale.ROOT);update.run();}});
    }
    void directory(LinearLayout c,boolean manage){
        String[] states={"All statuses","OK","Warning","Critical","Offline"};
        LinearLayout list=new LinearLayout(this);list.setOrientation(1);
        Runnable update=()->{
            list.removeAllViews();List<Integer> ids=new ArrayList<>();for(int i=0;i<count();i++)ids.add(i);
            if(sorted&&!manage)Collections.sort(ids,(a,b)->Double.compare(b<5?READINGS[b][metric]:-1,a<5?READINGS[a][metric]:-1));
            for(int id:ids){
                if(!(name(id)+" "+sensor(id)+" "+camera(id)).toLowerCase(Locale.ROOT).contains(query)||(filter!=0&&!states[filter].equals(status(id))))continue;
                View v=getLayoutInflater().inflate(R.layout.pen_row,list,false);list.addView(v);
                ((TextView)v.findViewById(R.id.penName)).setText(name(id));((TextView)v.findViewById(R.id.reading)).setText(manage?"Edit":reading(id,metric));
                TextView badge=v.findViewById(R.id.badge);badge.setText(status(id));badge.setTextColor(tint(status(id)));
                GradientDrawable bg=new GradientDrawable();bg.setCornerRadius(dp(12));bg.setColor((tint(status(id))&0xffffff)|0x1c000000);badge.setBackground(bg);
                ProgressBar bar=v.findViewById(R.id.bar);bar.setProgress(id<5?(int)(READINGS[id][metric]*100/new int[]{32,100,35}[metric]):0);bar.setProgressTintList(ColorStateList.valueOf(tint(status(id))));if(manage)bar.setVisibility(View.GONE);
                v.setFocusable(true);v.setOnClickListener(x->{if(manage)penOptions(id);else{selected=id;go(1);}});
                if(manage)text(list,(sensor(id).isEmpty()?"No sensor":sensor(id))+" · "+(camera(id).isEmpty()?"No camera":camera(id)),13,MUTED,false);
            }
            if(list.getChildCount()==0)text(list,"No matching pens",14,MUTED,false);
        };
        directoryFilters(c,states,update);c.addView(list);update.run();
        if(!manage)action(c,sorted?"Sort by pen number":"Highest reading first",()->{sorted=!sorted;render();});
    }
    interface Submit{void save(String[] v);}
    void form(String title,String[] labels,String[] values,int required,Submit submit){
        LinearLayout box=new LinearLayout(this);box.setPadding(dp(22),dp(12),dp(22),0);box.setOrientation(1);EditText[] edits=new EditText[labels.length];
        for(int i=0;i<labels.length;i++){text(box,labels[i],13,MUTED,true);EditText e=new EditText(this);e.setSingleLine(true);e.setText(values[i]);e.setContentDescription(labels[i]);box.addView(e);edits[i]=e;}
        ScrollView scroll=new ScrollView(this);scroll.addView(box);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(title).setView(scroll).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        dialog.show();dialog.getButton(-1).setOnClickListener(v->{String[] data=new String[edits.length];for(int i=0;i<data.length;i++){data[i]=edits[i].getText().toString().trim();if(i<required&&data[i].isEmpty()){edits[i].setError("Required");return;}}try { submit.save(data); } catch(IllegalArgumentException error) { Toast.makeText(this,error.getMessage(),Toast.LENGTH_LONG).show();return; } dialog.dismiss();render();});
    }
    void editPen(int id) {
        if (id == count()) { new AddPenDialog(this).show(); return; }
        form("Rename pen", new String[]{"Pen name"}, new String[]{name(id)}, 1, data -> {
            for (int i=0;i<count();i++) if(i!=id && name(i).equalsIgnoreCase(data[0])) throw new IllegalArgumentException("Choose a unique pen name");
            prefs.edit().putString("pen"+id,data[0]).putInt("count",Math.max(count(),id+1)).apply();
            Toast.makeText(this,"Pen saved",Toast.LENGTH_SHORT).show();
        });
    }
    private void penOptions(int id) {
        String[] choices={"Rename pen","Monitor pen","Add sensor","Add camera","Add environmental control"};
        new AlertDialog.Builder(this).setTitle(name(id)).setItems(choices,(dialog,option)->{
            switch(option) {
                case 0: editPen(id); break;
                case 1: selected=id;go(1);break;
                case 2: equipment.add(EquipmentRegister.SENSOR,id);break;
                case 3: equipment.add(EquipmentRegister.CAMERA,id);break;
                case 4: equipment.add(EquipmentRegister.CONTROL,id);break;
            }
        }).show();
    }
    void alertItem(LinearLayout c,int i){
        LinearLayout head=row(c);head.setPadding(0,dp(14),0,dp(6));
        TextView pen=text(head,name(IMPACTED[i]),13,MUTED,true);
        pen.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));
        TextView severity=text(head,LEVELS[i],12,tint(LEVELS[i]),true);
        GradientDrawable pill=new GradientDrawable();pill.setColor((tint(LEVELS[i])&0xffffff)|0x18000000);pill.setCornerRadius(dp(12));
        severity.setBackground(pill);severity.setPadding(dp(9),dp(3),dp(9),dp(3));
        text(c,ALERTS[i],17,INK,true);text(c,DETAILS[i],14,MUTED,false);
        String state=workflow(i);pair(c,"Status",state);
        if(!state.equals("Resolved"))action(c,state.equals("Open")?"Acknowledge":"Resolve",()->{put("alert"+i,state.equals("Open")?"Acknowledged":"Resolved");render();});
    }
    void chart(LinearLayout c,int id,int m){
        if(id>=5){text(c,"No readings available",14,MUTED,false);return;}
        pair(c,"Latest reading",id<0?value(mean(m),m):reading(id,m));
        View graph=new View(this){
            final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
            protected void onDraw(Canvas canvas){super.onDraw(canvas);int w=getWidth(),h=getHeight();paint.setColor(0xffdfe8df);paint.setStrokeWidth(dp(1));for(int j=1;j<=3;j++)canvas.drawLine(0,j*h/4f,w,j*h/4f,paint);
                Path line=new Path();for(int x=0;x<=48;x++){float y=(float)(h*(.5+.15*Math.sin(x*.2+m)+.08*Math.cos(x*.39+id)));if(x==0)line.moveTo(0,y);else line.lineTo(w*x/48f,y);}
                Path area=new Path(line);area.lineTo(w,h);area.lineTo(0,h);area.close();paint.setStyle(Paint.Style.FILL);paint.setColor(0x443f9c79);canvas.drawPath(area,paint);
                paint.setColor(GREEN);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(2));canvas.drawPath(line,paint);paint.setStyle(Paint.Style.FILL);
            }};
        graph.setContentDescription(METRICS[m]+" sample history");c.addView(graph,new LinearLayout.LayoutParams(-1,dp(105)));pair(c,"24 hours ago","Now");
    }
}




