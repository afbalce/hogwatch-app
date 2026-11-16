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
    private static final String[] PAGES = {"Dashboard","Pen monitoring","Pen management","Alert management","Analytics & reports","Users","Device management","Settings","Profile"};
    static final String[] METRICS = {"Temperature","Humidity","Ammonia","CO₂"};
    private static final String[] UNITS = {" °C"," %"," ppm"," ppm"};
    private static final double[][] READINGS = {{26.8,65,11,680},{29.1,68,15,920},{27,62,13,740},{28.4,74,27,1180},{26.2,61,9,630}};
    private static final String[] ALERTS = {"Ammonia above limit","Temperature rising","Sensor S-09 battery low","Sensor offline"};
    private static final String[] DETAILS = {"27 ppm · threshold 25 ppm","29.1 °C · target 22–28 °C","14% remaining · threshold 15%","No readings for 2 hours"};
    static final int[] IMPACTED = {3,1,3,5};
    static final String[] LEVELS = {"Critical","Warning","Warning","Offline"};
    static final int GREEN = 0xff205b3f, INK = 0xff183a2b, MUTED = 0xff587062;
    LinearLayout content, nav;
    android.content.SharedPreferences prefs;
    int page, selected=3, metric, tab, filter, gatewayTab;
    private String query="";
    private boolean sorted;
    private final ArrayDeque<Integer> history=new ArrayDeque<>();
    JSONArray members;
    EquipmentRegister equipment;
    private AlertReminders alertReminders;
    private NotificationSummary notificationSummary;
    AlertLevels levels;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        prefs=getSharedPreferences("farm",MODE_PRIVATE);
        equipment = new EquipmentRegister(this);
        levels = new AlertLevels(this);
        findViewById(R.id.menu_button).setOnClickListener(this::showProfileMenu);
        findViewById(R.id.notifications).setOnClickListener(v->showNotifications());
        findViewById(R.id.export_report).setOnClickListener(v->new AnalyticsReportsScreen(this).export());
        findViewById(R.id.add).setOnClickListener(v -> {if(page==2)editPen(count());else if(page==6)equipment.add(tab==DeviceManagementScreen.CAMERAS?EquipmentRegister.CAMERA:tab==DeviceManagementScreen.SENSORS?EquipmentRegister.SENSOR:EquipmentRegister.CONTROL,-1);});
        try { members=new JSONArray(prefs.getString("members","[[\"Maria Santos\",\"maria.s@example.com\",\"Owner\",\"Active\"],[\"Jun Reyes\",\"jun.reyes@example.com\",\"Manager\",\"Active\"]]")); }
        catch(JSONException e){ members=new JSONArray(); }
        if(state!=null){page=state.getInt("page");selected=state.getInt("selected");metric=state.getInt("metric");tab=state.getInt("tab");gatewayTab=state.getInt("gatewayTab");}
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
    void showNotifications(){
        if(notificationSummary!=null)notificationSummary.dismiss();
        notificationSummary=new NotificationSummary(this);
        notificationSummary.show(findViewById(R.id.notifications));
    }
    void acknowledgeAlert(int id){
        prefs.edit().putString("alert"+id,"Acknowledged").putLong("acknowledged_time"+id,System.currentTimeMillis()).putLong("alert_remind_at"+id,System.currentTimeMillis()+AlertReminders.DELAY).putBoolean("alert_reminder"+id,false).apply();
    }
    @Override protected void onResume(){
        super.onResume();
        if(prefs!=null){if(alertReminders==null)alertReminders=new AlertReminders(this);alertReminders.start();}
    }
    @Override protected void onPause(){
        if(alertReminders!=null)alertReminders.stop();
        if(notificationSummary!=null)notificationSummary.dismiss();
        super.onPause();
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==ProfilePhoto.REQUEST)ProfilePhoto.finish(this,result,data);
        if(request==ReportExport.REQUEST)new ReportExport(this).finish(result,data);
    }
    @Override protected void onSaveInstanceState(Bundle s){super.onSaveInstanceState(s);s.putInt("page",page);s.putInt("selected",selected);s.putInt("metric",metric);s.putInt("tab",tab);s.putInt("gatewayTab",gatewayTab);}
    int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
    String pref(String k,String def){return prefs.getString(k,def);}
    void put(String k,String v){prefs.edit().putString(k,v).apply();}
    int count(){return prefs.getInt("count",7);}
    int alertPen(int index){return prefs.getInt("alert_pen"+index,IMPACTED[index]);}
    boolean hasReading(int id){int source=prefs.getInt("sample_source"+id,id);return id>=0&&id<count()&&source>=0&&source<READINGS.length;}
    int penNumber(int id){return prefs.getInt("pen_number"+id,id+1);}
    String name(int i){return pref("pen"+i,"Pen "+(i+1));}
    String sensor(int i){return equipment.assigned(EquipmentRegister.SENSOR,i);}
    String camera(int i){return equipment.assigned(EquipmentRegister.CAMERA,i);}
    String status(int i){if(!hasReading(i))return "Offline";return levels.status(i,sample(i,0),sample(i,2));}
    String value(double n,int m){return (m==0?String.format(Locale.US,"%.1f",n):String.valueOf(Math.round(n)))+UNITS[m];}
    double sample(int pen,int metric){return hasReading(pen)?READINGS[prefs.getInt("sample_source"+pen,pen)][metric]:0;}
    String metricStatus(int pen,int metric){
        if(!hasReading(pen))return "Offline";
        double value=sample(pen,metric);
        if(metric==1)return value<50 || value>70?"Warning":"OK";
        return levels.status(pen,metric==0?value:Double.NEGATIVE_INFINITY,metric==2?value:Double.NEGATIVE_INFINITY);
    }
    String reading(int i,int m){return hasReading(i)?value(sample(i,m),m):"—";}
    double mean(int m){double sum=0;int n=0;for(int i=0;i<count();i++)if(hasReading(i)){sum+=sample(i,m);n++;}return n==0?0:sum/n;}
    String workflow(int i){return pref("alert"+i,"Open");}
    int active(String severity){int n=0;for(int i=0;i<3;i++)if(!workflow(i).equals("Resolved")&&(severity.isEmpty()||LEVELS[i].equals(severity)))n++;return n;}
    int tint(String s){return s.equals("Critical")?0xffaf4936:s.equals("Warning")?0xff9b651d:s.equals("Offline")?MUTED:GREEN;}
    void openAlert(int id){
        go(3);
        ScrollView scroll=findViewById(R.id.scroll);
        scroll.post(()->{
            View card=content.findViewWithTag("alert-card-"+id);
            if(card==null)return;
            android.graphics.Rect bounds=new android.graphics.Rect();
            card.getDrawingRect(bounds);
            scroll.offsetDescendantRectToMyCoords(card,bounds);
            scroll.smoothScrollTo(0,Math.max(0,bounds.top+scroll.getScrollY()-dp(12)));
            card.sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_FOCUSED);
        });
    }
    void go(int n){if(n!=page)history.push(page);navigate(n);}
    private void navigate(int n){page=n;tab=0;gatewayTab=0;filter=0;query="";render();((ScrollView)findViewById(R.id.scroll)).scrollTo(0,0);}
    @Override public void onBackPressed(){if(!history.isEmpty())navigate(history.pop());else if(page!=0)navigate(0);else super.onBackPressed();}
    private float edgeStartX,edgeStartY;
    private boolean edgeCandidate;
    @Override public boolean dispatchTouchEvent(MotionEvent event){
        if(event.getActionMasked()==MotionEvent.ACTION_DOWN){
            edgeStartX=event.getX();edgeStartY=event.getY();
            edgeCandidate=edgeStartX<dp(28)&&prefs!=null&&!prefs.getBoolean("logged_out",false);
        }
        if(event.getActionMasked()==MotionEvent.ACTION_MOVE&&edgeCandidate){
            float dx=event.getX()-edgeStartX,dy=event.getY()-edgeStartY;
            if(Math.abs(dy)>dp(24)&&Math.abs(dy)>Math.abs(dx))edgeCandidate=false;
            else if(dx>dp(64)&&dx>Math.abs(dy)*1.5f){
                edgeCandidate=false;MotionEvent cancel=MotionEvent.obtain(event);cancel.setAction(MotionEvent.ACTION_CANCEL);super.dispatchTouchEvent(cancel);cancel.recycle();
                showProfileMenu(findViewById(R.id.menu_button));return true;
            }
        }
        if(event.getActionMasked()==MotionEvent.ACTION_UP||event.getActionMasked()==MotionEvent.ACTION_CANCEL)edgeCandidate=false;
        return super.dispatchTouchEvent(event);
    }
    private void showProfileMenu(View anchor) {
        new SidebarMenu(this).show();
    }

    private void addMenu() {
        String[] choices = {"Pen", "User", "Camera", "Sensor", "Environmental control"};
        new AlertDialog.Builder(this).setTitle("Add to farm").setItems(choices, (dialog,index) -> {
            if (index == 0) { go(2); editPen(count()); }
            else if (index == 1) { go(5); new UsersScreen(this).editMember(-1); }
            else { go(6); tab = index - 1; render(); equipment.add(choices[index],-1); }
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
        boolean destructive = label.startsWith("Remove") || label.startsWith("Delete");
        int foreground = primary ? android.graphics.Color.WHITE : destructive ? 0xffa64734 : GREEN;
        b.setTextColor(new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{}},new int[]{0xff89928b,foreground}));
        b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        b.setBackgroundResource(primary ? R.drawable.action_primary : destructive ? R.drawable.action_danger : R.drawable.action_surface);
        b.setStateListAnimator(null); b.setMinimumHeight(dp(48));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2,-2);
        params.gravity=Gravity.START;
        params.topMargin=dp(8); b.setPadding(dp(16),dp(10),dp(16),dp(10));
        b.setOnClickListener(v -> fn.run()); ActionSlider.append(this,parent,b,params); return b;
    }
    interface Pick {void accept(int n);}
    void picker(LinearLayout parent,String title,String[] options,int current,Pick pick){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams spacing=new LinearLayout.LayoutParams(-1,-2);spacing.setMargins(0,dp(8),0,dp(10));parent.addView(box,spacing);
        if(!title.isEmpty())text(box,title,12,MUTED,true);
        HorizontalScrollView scroll=(HorizontalScrollView)getLayoutInflater().inflate(R.layout.action_slider,box,false);box.addView(scroll);
        LinearLayout strip=(LinearLayout)scroll.getChildAt(0);
        for(int i=0;i<options.length;i++){
            final int index=i;Button option=new Button(this);option.setText(options[i]);option.setAllCaps(false);
            SegmentStyle.apply(this,option,i==current,false);option.setContentDescription(title+": "+options[i]);
            strip.addView(option,new LinearLayout.LayoutParams(-2,-2));
            option.setOnClickListener(v->{for(int j=0;j<strip.getChildCount();j++)strip.getChildAt(j).setSelected(j==index);pick.accept(index);});
            if(i==current)scroll.post(()->scroll.scrollTo(Math.max(0,option.getLeft()-(scroll.getWidth()-option.getWidth())/2),0));
        }
    }
    String[] penNames(){String[] a=new String[count()];for(int i=0;i<a.length;i++)a[i]=name(i);return a;}
    private void updateNotificationBadge(){
        int count=0;
        for(int i=0;i<4;i++)if(!workflow(i).equals("Resolved")&&alertPen(i)>=0)count++;
        TextView badge=findViewById(R.id.notification_badge);
        badge.setText(Integer.toString(count));
        badge.setVisibility(count>0?View.VISIBLE:View.GONE);
        findViewById(R.id.notifications).setContentDescription(count==0?"Open notifications":"Open notifications, "+count+" active");
    }
    void render(){
        findViewById(R.id.export_report).setVisibility(page==4&&!prefs.getBoolean("logged_out",false)?View.VISIBLE:View.GONE);
        if(prefs.getBoolean("logged_out",false)){
            content.removeAllViews();nav.removeAllViews();findViewById(R.id.menu_button).setVisibility(View.GONE);findViewById(R.id.notification_container).setVisibility(View.GONE);findViewById(R.id.add).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.app_title)).setText("HogWatch");
            LinearLayout signedOut=card(content,"You're logged out");
            text(signedOut,"Your local farm data is saved. Continue to reopen this local profile; online authentication is not connected.",14,MUTED,false);
            action(signedOut,"Continue with local profile",()->{prefs.edit().putBoolean("logged_out",false).apply();go(0);});return;
        }
        findViewById(R.id.menu_button).setVisibility(View.VISIBLE);findViewById(R.id.notification_container).setVisibility(View.VISIBLE);updateNotificationBadge();
        View addPen=findViewById(R.id.add);
        boolean deviceAdd=page==6&&(tab==DeviceManagementScreen.CAMERAS||tab==DeviceManagementScreen.SENSORS||tab==DeviceManagementScreen.CONTROLS);
        addPen.setVisibility(page==2||deviceAdd?View.VISIBLE:View.GONE);
        addPen.setContentDescription(page==2?"Add pen":tab==DeviceManagementScreen.CAMERAS?"Add camera":tab==DeviceManagementScreen.SENSORS?"Add sensor":"Add external device");
        content.removeAllViews();nav.removeAllViews();
        String user=pref("name","John Doe").trim();String[] words=user.split("\\s+");
        String[] labels = {"Home", "Pen Monitoring", "Alerts", "Analytics & Reports"};
        int[] destinations = {0, 1, 3, 4};
        int[] icons = {R.drawable.ic_overview, R.drawable.ic_monitor, R.drawable.ic_alerts, R.drawable.ic_reports};
        for (int i = 0; i < destinations.length; i++) {
            int destination = destinations[i];
            boolean selectedTab = page == destination;
            View item = getLayoutInflater().inflate(R.layout.bottom_nav_item, nav, false);
            ImageView icon = item.findViewById(R.id.nav_icon);
            icon.setImageResource(icons[i]);
            icon.setColorFilter(selectedTab ? 0xff205b3f : MUTED);
            if (selectedTab) icon.setBackgroundResource(R.drawable.nav_indicator);
            TextView label = item.findViewById(R.id.nav_label);
            label.setText(new String[]{"Home","Monitoring","Alerts","Reports"}[i]);
            label.setTextColor(selectedTab ? GREEN : MUTED);
            if (selectedTab) label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            item.setContentDescription(labels[i]);
            item.setSelected(selectedTab);
            item.setOnClickListener(v -> go(destination));
            nav.addView(item);
        }
        String[] titles={"HogWatch","Pen monitoring","Pen management","Alerts","Reports","Team","Devices","Settings","Profile"};
        ((TextView)findViewById(R.id.app_title)).setText(titles[page]);
        View h=getLayoutInflater().inflate(R.layout.farm_header,content,false);content.addView(h);
        if(page==0||page==1||page==2||page==3||page==4||page==6||page==7||page==8)h.setVisibility(View.GONE);
        ((TextView)h.findViewById(R.id.screenTitle)).setText(page==0?"Hello, "+user+"!":"Piggery A");
        String[] subtitles={"Piggery A · Demo readings","Readings, alerts and camera feeds","Organize pens and device assignments","Review issues and track your response","Sensor trends and downloadable readings","Manage your farm team","Connections, sensors and controls","Security and farm preferences","Your photo and personal information"};
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
            case 8: new ProfileScreen(this).show(); break;
        }
    }
    private void directoryFilters(LinearLayout parent, String[] states, Runnable update, boolean manage) {
        View fields = getLayoutInflater().inflate(manage?R.layout.managed_pen_filters:R.layout.directory_filters,parent,false);
        parent.addView(fields);
        EditText input = fields.findViewById(R.id.pen_query);
        input.setText(query);
        Button status = fields.findViewById(R.id.status_filter);
        status.setText(filter==0?"Filters":"Filters (1)");
        status.setContentDescription("Filter by status: " + states[filter]);
        status.setOnClickListener(v -> PenStatusDialog.show(this,states,filter,which->{filter=which;render();}));
        if(!manage)SearchControls.attach(this,(LinearLayout)fields,input,update,()->{filter=0;query="";status.setText("Filters");status.setContentDescription("Filter by status: "+states[0]);});
        if(manage){
            View clear=fields.findViewById(R.id.clear_pen_search);
            clear.setVisibility(query.isEmpty()?View.GONE:View.VISIBLE);
            clear.setOnClickListener(v->input.setText(""));
            input.addTextChangedListener(new TextWatcher(){
                public void beforeTextChanged(CharSequence s,int start,int count,int after){}
                public void onTextChanged(CharSequence s,int start,int before,int count){clear.setVisibility(s.length()==0?View.GONE:View.VISIBLE);}
                public void afterTextChanged(Editable s){}
            });
            input.setOnEditorActionListener((v,action,event)->{
                if(action!=android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH)return false;
                update.run();input.clearFocus();
                android.view.inputmethod.InputMethodManager keyboard=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                if(keyboard!=null)keyboard.hideSoftInputFromWindow(input.getWindowToken(),0);
                return true;
            });
        }
        input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int n,int after){}public void afterTextChanged(Editable s){}public void onTextChanged(CharSequence s,int start,int before,int n){query=s.toString().toLowerCase(Locale.ROOT);update.run();}});
    }
    void directory(LinearLayout c,boolean manage){
        String[] states={"All statuses","OK","Warning","Critical","Offline"};
        LinearLayout list=new LinearLayout(this);list.setOrientation(1);
        Runnable update=()->{
            list.removeAllViews();List<Integer> ids=new ArrayList<>();for(int i=0;i<count();i++)ids.add(i);
            if(sorted&&!manage)Collections.sort(ids,(a,b)->Double.compare(hasReading(b)?sample(b,metric):-1,hasReading(a)?sample(a,metric):-1));
            for(int id:ids){
                if(!(name(id)+" "+sensor(id)+" "+camera(id)).toLowerCase(Locale.ROOT).contains(query)||(filter!=0&&!states[filter].equals(status(id))))continue;
                View v=getLayoutInflater().inflate(manage?R.layout.managed_pen_card:R.layout.pen_row,list,false);list.addView(v);
                ((TextView)v.findViewById(R.id.penName)).setText(name(id));((TextView)v.findViewById(R.id.reading)).setText(manage?"›":reading(id,metric));
                TextView badge=v.findViewById(R.id.badge);badge.setText(status(id));badge.setTextColor(tint(status(id)));
                GradientDrawable bg=new GradientDrawable();bg.setCornerRadius(dp(12));bg.setColor((tint(status(id))&0xffffff)|0x1c000000);badge.setBackground(bg);
                ProgressBar bar=v.findViewById(R.id.bar);bar.setProgress(hasReading(id)?(int)(sample(id,metric)*100/new int[]{32,100,35,1500}[metric]):0);bar.setProgressTintList(ColorStateList.valueOf(tint(status(id))));if(manage)bar.setVisibility(View.GONE);
                v.setFocusable(true);v.setOnClickListener(x->{if(manage)penOptions(id);else{selected=id;go(1);}});
                if(manage){
                    v.setContentDescription("Manage "+name(id)+", "+status(id));
                    ((TextView)v.findViewById(R.id.pen_sensor_summary)).setText((sensor(id).isEmpty()?"Not assigned":sensor(id)));
                    ((TextView)v.findViewById(R.id.pen_camera_summary)).setText((camera(id).isEmpty()?"Not assigned":camera(id)));
                }
            }
            if(list.getChildCount()==0)text(list,"No matching pens",14,MUTED,false);
        };
        directoryFilters(c,states,update,manage);c.addView(list);update.run();
        if(!manage)action(c,sorted?"Sort by pen number":"Highest reading first",()->{sorted=!sorted;render();});
    }
    interface Submit{void save(String[] v);}
    void form(String title,String[] labels,String[] values,int required,Submit submit){
        ScrollView scroll=(ScrollView)getLayoutInflater().inflate(R.layout.dialog_form,null);
        LinearLayout box=scroll.findViewById(R.id.form_fields);EditText[] edits=new EditText[labels.length];
        for(int i=0;i<labels.length;i++){
            View field=getLayoutInflater().inflate(R.layout.form_field,box,false);
            TextView label=field.findViewById(R.id.field_label);
            EditText e=field.findViewById(R.id.field_input);e.setId(View.generateViewId());
            label.setText(new String[]{"Home","Monitoring","Alerts","Reports"}[i]);label.setLabelFor(e.getId());e.setText(values[i]);e.setContentDescription(labels[i]);
            box.addView(field);edits[i]=e;
        }

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
    private void penOptions(int id) { new AddPenDialog(this).show(id); }
    String alertSearchText(int i){return (ALERTS[i]+" "+DETAILS[i]+" "+(alertPen(i)<0?"Deleted pen":name(alertPen(i)))+" "+LEVELS[i]+" "+workflow(i)).toLowerCase(Locale.ROOT);}
    void alertItem(LinearLayout c,int i){
        View card=getLayoutInflater().inflate(R.layout.alert_card,c,false);card.setTag("alert-card-"+i);c.addView(card);
        String pen=alertPen(i)<0?"Deleted pen":name(alertPen(i)),state=workflow(i);
        card.setBackground(AlertAppearance.background(this,LEVELS[i],state.equals("Resolved")));
        ((TextView)card.findViewById(R.id.alert_pen_label)).setText(pen);
        TextView severity=card.findViewById(R.id.alert_severity_label);severity.setText(LEVELS[i]);severity.setTextColor(tint(LEVELS[i]));
        GradientDrawable pill=new GradientDrawable();pill.setColor((tint(LEVELS[i])&0xffffff)|0x18000000);pill.setCornerRadius(dp(10));severity.setBackground(pill);
        ((TextView)card.findViewById(R.id.alert_title)).setText(ALERTS[i]);
        ((TextView)card.findViewById(R.id.alert_detail)).setText(DETAILS[i]);
        ((TextView)card.findViewById(R.id.alert_workflow)).setText(state.equals("Resolved")?"✓ Resolved":state.equals("Acknowledged")?"Acknowledged · Reminder in 5 minutes":prefs.getBoolean("alert_reminder"+i,false)?"Reminder · Still unresolved":"Open · Action needed");
        if(state.equals("Resolved")){
            card.findViewById(R.id.alert_actions).setVisibility(View.GONE);
            String action=pref("resolution_action"+i,""),notes=pref("resolution_notes"+i,"");
            if(!action.isEmpty()){
                TextView resolution=card.findViewById(R.id.alert_resolution);resolution.setVisibility(View.VISIBLE);
                resolution.setText(action+(notes.isEmpty()?"":"\n"+notes));
            }
        }else{
            View acknowledge=card.findViewById(R.id.alert_acknowledge);
            acknowledge.setVisibility(state.equals("Open")?View.VISIBLE:View.GONE);
            acknowledge.setOnClickListener(v->{acknowledgeAlert(i);render();});
            card.findViewById(R.id.alert_resolve).setOnClickListener(v->ResolveAlertDialog.show(this,i,pen+" · "+ALERTS[i]));
        }
    }
    void chart(LinearLayout c,int id,int m){
        if(id>=0&&!hasReading(id)){text(c,"No readings available",14,MUTED,false);return;}
        pair(c,"Latest reading",id<0?value(mean(m),m):reading(id,m));
        text(c,"Illustrative history · "+UNITS[m].trim(),12,MUTED,false);
        c.addView(new SensorTrendView(this,id,m),new LinearLayout.LayoutParams(-1,dp(170)));
    }
}


















