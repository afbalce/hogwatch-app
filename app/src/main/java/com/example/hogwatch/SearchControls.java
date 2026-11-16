package com.example.hogwatch;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

/** Shared mobile search actions, including keyboard submission and filter reset. */
final class SearchControls {
    static void attach(MainActivity ui, LinearLayout parent, EditText input, Runnable search, Runnable reset) {
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        input.setBackgroundResource(R.drawable.action_surface);
        input.setPadding(ui.dp(14),ui.dp(12),ui.dp(14),ui.dp(12));
        input.setTextColor(MainActivity.INK);
        input.setHintTextColor(MainActivity.MUTED);
        View actions=ui.getLayoutInflater().inflate(R.layout.search_actions,parent,false);
        parent.addView(actions);
        Runnable submit=()->{
            search.run();input.clearFocus();
            InputMethodManager keyboard=(InputMethodManager)ui.getSystemService(MainActivity.INPUT_METHOD_SERVICE);
            if(keyboard!=null)keyboard.hideSoftInputFromWindow(input.getWindowToken(),0);
        };
        actions.findViewById(R.id.search_submit).setOnClickListener(v->submit.run());
        actions.findViewById(R.id.search_reset).setOnClickListener(v->{input.setText("");reset.run();submit.run();});
        input.setOnEditorActionListener((v,action,event)->{if(action==EditorInfo.IME_ACTION_SEARCH){submit.run();return true;}return false;});
    }
}
