package com.cleanyourtv;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class LauncherActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pitch black background
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF000000);

        // Simple text to confirm it's running
        TextView tv = new TextView(this);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(24);
        tv.setGravity(Gravity.CENTER);
        tv.setText("Apple TV Launcher Ready");

        // Set layout params so it fills the screen
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        );
        tv.setLayoutParams(params);

        root.addView(tv);
        setContentView(root);
    }
}
