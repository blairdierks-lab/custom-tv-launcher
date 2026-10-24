package com.cleanyourtv;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class LauncherActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Pitch black background
        View root = new View(this);
        root.setBackgroundColor(0xFF000000);
        
        // Simple text to confirm it's running
        TextView tv = new TextView(this);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(24);
        tv.setText("Apple TV Launcher Ready");
        tv.setPadding(50, 50, 50, 50);
        
        root.addView(tv);
        setContentView(root);
    }
}
