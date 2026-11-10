package com.example.hogwatch;

import android.widget.LinearLayout;
import org.json.JSONArray;
import org.json.JSONException;

final class UsersScreen {
    private final MainActivity ui;

    UsersScreen(MainActivity ui) {
        this.ui = ui;
    }

    void show() {
        ui.getLayoutInflater().inflate(R.layout.screen_users, ui.content, true);
        LinearLayout c = ui.findViewById(R.id.farm_team);
        int pending = 0;
        for (int i = 0; i < ui.members.length(); i++) if (ui.members.optJSONArray(i).optString(3).equals("Pending")) pending++;
        ui.pair(c, "Active users", "" + (ui.members.length() - pending));
        ui.pair(c, "Pending invitations", "" + pending);
        ui.action(c, "+ Add user", ()->editMember(-1));
        for (int i = 0; i < ui.members.length(); i++) {
            final int id = i;
            JSONArray u = ui.members.optJSONArray(i);
            LinearLayout item = ui.card(ui.findViewById(R.id.team_members), u.optString(0));
            ui.text(item, u.optString(1), 14, MainActivity.MUTED, false);
            ui.pair(item, "Role", u.optString(2));
            ui.pair(item, "Status", u.optString(3));
            ui.action(item, "Edit member", ()->editMember(id));
        }
    }

    void editMember(int id) {
        JSONArray u = id < 0 ? new JSONArray() : ui.members.optJSONArray(id);
        ui.form(id < 0 ? "Add user" : "Edit member", new String[]{"Name", "Email address", "Role"}, new String[]{u.optString(0), u.optString(1), u.optString(2, "Viewer")}, 3, (v)->{
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(v[1]).matches()) throw new IllegalArgumentException("Enter a valid email address");
            for (int i=0;i<ui.members.length();i++) {
                if (i!=id && v[1].equalsIgnoreCase(ui.members.optJSONArray(i).optString(1))) throw new IllegalArgumentException("A team member already uses this email");
            }
            JSONArray item = new JSONArray().put(v[0]).put(v[1]).put(v[2]).put(u.optString(3, "Active"));
            try {
                ui.members.put(id < 0 ? ui.members.length() : id, item);
                ui.put("members", ui.members.toString());
            } catch (JSONException e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
