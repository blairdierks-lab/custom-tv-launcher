package com.cleanyourtv;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LauncherActivity extends Activity {
    private static final String TAG = "LauncherActivity";
    private AppleTVUI ui;
    private Handler handler;
    private Runnable clockUpdater;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = new AppleTVUI(this);
        setContentView(ui);
        
        handler = new Handler(Looper.getMainLooper());
        clockUpdater = new Runnable() {
            @Override
            public void run() {
                ui.updateClock();
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(clockUpdater, 1000);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) handler.removeCallbacks(clockUpdater);
    }
}

class AppleTVUI extends FrameLayout {
    private static final String TAG = "AppleTVUI";
    private Paint bgPaint, highlightPaint, textPaint, glassPaint, topBarBg, dockBg;
    private float screenWidth, screenHeight, topBarHeight, dockHeight, gridSpacing, tileSize;
    private int gridColumns = 6, gridRows = 6, gridStartRow = 0, gridEndRow = 5;
    private int focusRow = 0, focusCol = 0;
    private boolean gridFocused = false;
    private List<AppItem> apps;
    
    public AppleTVUI(Context context) {
        super(context);
        init();
    }
    
    private void init() {
        setBackgroundColor(Color.BLACK);
        apps = new ArrayList<>();
        apps.add(new AppItem("Netflix", "com.netflix.ninja", "#E50914"));
        apps.add(new AppItem("YouTube", "com.google.android.youtube.tv", "#FF0000"));
        apps.add(new AppItem("Disney+", "com.disney.disneyplus", "#0063E5"));
        apps.add(new AppItem("Prime", "com.amazon.amazonvideo.livingroom", "#00A8E1"));
        apps.add(new AppItem("HBO Max", "com.hbo.hbonow", "#9900FF"));
        apps.add(new AppItem("Pluto TV", "tv.pluto.android", "#00FF7F"));
        
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        glassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glassPaint.setColor(Color.parseColor("#20FFFFFF"));
        topBarBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        topBarBg.setColor(Color.parseColor("#1A1A1A"));
        dockBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        dockBg.setColor(Color.parseColor("#1A1A1A"));
        requestFocus();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;
        topBarHeight = h * 0.06f;
        dockHeight = h * 0.1f;
        gridSpacing = w * 0.02f;
        tileSize = (w - gridSpacing * 7) / 6;
        gridEndRow = Math.min(5, (int)((h - topBarHeight - dockHeight) / (tileSize + gridSpacing)) - 1);
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        drawTopBar(canvas);
        drawGrid(canvas);
        drawDock(canvas);
    }
    
    private void drawTopBar(Canvas canvas) {
        canvas.drawRect(0, 0, screenWidth, topBarHeight, topBarBg);
        textPaint.setTextSize(16);
        String greeting = "Good " + getTimeOfDay();
        String time = getCurrentTime();
        String date = getCurrentDate();
        canvas.drawText(greeting, screenWidth * 0.05f, topBarHeight * 0.7f, textPaint);
        canvas.drawText(time, screenWidth * 0.5f, topBarHeight * 0.7f, textPaint);
        canvas.drawText(date, screenWidth * 0.5f, topBarHeight * 0.95f, textPaint);
        textPaint.setTextSize(14);
        canvas.drawText("Settings", screenWidth * 0.95f, topBarHeight * 0.7f, textPaint);
    }
    
    private void drawGrid(Canvas canvas) {
        int gridTop = (int)(topBarHeight);
        int gridBottom = (int)(screenHeight - dockHeight);
        canvas.drawRect(gridSpacing, gridTop, screenWidth - gridSpacing, gridBottom, topBarBg);
        
        for (int row = gridStartRow; row <= gridEndRow; row++) {
            for (int col = 0; col < gridColumns; col++) {
                int appIndex = (row - gridStartRow) * gridColumns + col;
                if (appIndex < apps.size()) {
                    AppItem app = apps.get(appIndex);
                    float appX = gridSpacing + col * (tileSize + gridSpacing);
                    float appY = gridTop + (row - gridStartRow) * (tileSize + gridSpacing);
                    
                    if (gridFocused && row == focusRow && col == focusCol) {
                        canvas.drawRoundRect(new RectF(appX - 4, appY - 4, appX + tileSize + 4, appY + tileSize + 4), 12, 12, glassPaint);
                    }
                    
                    highlightPaint.setColor(Color.parseColor(app.color));
                    canvas.drawRoundRect(new RectF(appX, appY, appX + tileSize, appY + tileSize), 10, 10, highlightPaint);
                    
                    textPaint.setTextSize(14);
                    canvas.drawText(app.name, appX + tileSize / 2, appY + tileSize + 20, textPaint);
                }
            }
        }
    }
    
    private void drawDock(Canvas canvas) {
        canvas.drawRect(0, screenHeight - dockHeight, screenWidth, screenHeight, dockBg);
        float dockAppWidth = (screenWidth - gridSpacing * 5) / 6;
        for (int i = 0; i < Math.min(6, apps.size()); i++) {
            AppItem app = apps.get(i);
            float appX = gridSpacing + i * (dockAppWidth + gridSpacing);
            float appY = screenHeight - dockHeight + (dockHeight - tileSize) / 2;
            
            highlightPaint.setColor(Color.parseColor(app.color));
            canvas.drawRoundRect(new RectF(appX, appY, appX + dockAppWidth, appY + dockHeight), 15, 15, highlightPaint);
            
            textPaint.setTextSize(12);
            canvas.drawText(app.name, appX + dockAppWidth / 2, screenHeight - 12, textPaint);
        }
    }
    
    private String getTimeOfDay() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "morning";
        else if (hour < 18) return "afternoon";
        else return "evening";
    }
    
    private String getCurrentTime() {
        return String.format(Locale.getDefault(), "%02d:%02d", 
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY), 
            Calendar.getInstance().get(Calendar.MINUTE));
    }
    
    private String getCurrentDate() {
        return Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) + ", " +
               Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + " " +
               Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
    }
    
    public void updateClock() { invalidate(); }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (gridFocused && focusRow > 0) { focusRow--; invalidate(); return true; }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (gridFocused && focusRow < gridEndRow) { focusRow++; invalidate(); return true; }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (gridFocused && focusCol > 0) { focusCol--; invalidate(); return true; }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (gridFocused && focusCol < gridColumns - 1) { focusCol++; invalidate(); return true; }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (gridFocused) {
                    int idx = (focusRow - gridStartRow) * gridColumns + focusCol;
                    if (idx < apps.size()) Toast.makeText(getContext(), "Launching " + apps.get(idx).name, Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        gridFocused = gainFocus;
        invalidate();
    }
    
    private class AppItem {
        String name, packageName, color;
        AppItem(String n, String p, String c) { name = n; packageName = p; color = c; }
    }
}
