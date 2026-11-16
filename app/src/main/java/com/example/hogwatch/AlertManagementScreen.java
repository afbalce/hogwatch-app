package com.example.hogwatch;

import android.widget.LinearLayout;

final class AlertManagementScreen {
    private final MainActivity ui;

    AlertManagementScreen(MainActivity ui) {
        this.ui = ui;
    }

    void show() {
        ui.getLayoutInflater().inflate(R.layout.screen_alert_management, ui.content, true);
        LinearLayout c = ui.findViewById(R.id.alert_overview);

        int pending = 0;
        int resolved = 0;
        for (int i = 0; i < 4; i++) {
            if (ui.workflow(i).equals("Open")) pending++;
            if (ui.workflow(i).equals("Resolved")) resolved++;
        }
        OverviewTiles.add(ui,c,new String[]{"Active","Pending","Resolved"},new int[]{4-resolved,pending,resolved});

        for(int n=0;n<c.getChildCount();n++){android.view.View child=c.getChildAt(n);if(child instanceof LinearLayout){LinearLayout tiles=(LinearLayout)child;for(int t=0;t<tiles.getChildCount();t++)tiles.getChildAt(t).setBackgroundResource(R.drawable.card);}}
        LinearLayout list = ui.findViewById(R.id.alert_directory);
        String[] opts = {"All alerts", "Critical", "Warning", "Offline", "Acknowledged", "Resolved"};
        android.view.View fields=ui.getLayoutInflater().inflate(R.layout.managed_pen_filters,list,false);list.addView(fields);
        android.widget.EditText search=fields.findViewById(R.id.pen_query);search.setHint("Search alerts or pens");search.setContentDescription("Search alerts");
        android.view.View clear=fields.findViewById(R.id.clear_pen_search);clear.setVisibility(android.view.View.GONE);clear.setOnClickListener(v->search.setText(""));
        android.widget.Button status=fields.findViewById(R.id.status_filter);
        LinearLayout results=new LinearLayout(ui);results.setOrientation(LinearLayout.VERTICAL);
        Runnable update=()->{
            results.removeAllViews();clear.setVisibility(search.length()==0?android.view.View.GONE:android.view.View.VISIBLE);
            String query=search.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
            int count=0;
            for(int i=0;i<4;i++){
                if(ui.filter!=0&&!opts[ui.filter].equals(MainActivity.LEVELS[i])&&!opts[ui.filter].equals(ui.workflow(i)))continue;
                if(!ui.alertSearchText(i).contains(query))continue;
                ui.alertItem(results,i);count++;
            }
            if(count==0)ui.text(results,"No matching alerts. Try another search or reset your filters.",14,MainActivity.MUTED,false);
        };
        status.setOnClickListener(v->PenStatusDialog.show(ui,opts,ui.filter,index->{ui.filter=index;status.setText(index==0?"Filters":"Filters (1)");update.run();},"Filter alerts","Choose severity or resolution status."));

        list.addView(results);
        search.addTextChangedListener(new android.text.TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void afterTextChanged(android.text.Editable e){}
            public void onTextChanged(CharSequence s,int start,int before,int count){update.run();}
        });
        update.run();
    }
}



