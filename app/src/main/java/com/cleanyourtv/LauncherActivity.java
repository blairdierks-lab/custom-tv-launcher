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
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View; // ADDED
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar; // ADDED
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
        
        // Make sure window is not transparent
        getWindow().setBackgroundDrawableResource(android.R.color.black);
        
        ui = new AppleTVUI(this);
        setContentView(ui);
        
        // Start clock update
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
        if (handler != null && clockUpdater != null) {
            handler.removeCallbacks(clockUpdater);
        }
    }
}

// Custom UI for Apple TV style launcher
class AppleTVUI extends SurfaceView implements View.OnClickListener, View.OnFocusChangeListener {
    private static final String TAG = "AppleTVUI";
    private Paint paint;
    private Paint highlightPaint;
    private Paint textPaint;
    private Paint dockPaint;
    private Paint topBarPaint;
    private float screenWidth;
    private float screenHeight;
    private float dockHeight;
    private float topBarHeight;
    private float gridSpacing;
    private float tileSize;
    private int gridColumns;
    private int gridRows;
    private int currentFocusRow;
    private int currentFocusCol;
    private boolean isGridFocused;
    private List<AppItem> apps;
    private AppItem focusedDockItem;
    private AppItem focusedGridItem;
    private float dockY;
    private float gridY;
    private int gridStartRow;
    private int gridEndRow;
    private int focusedGridIndex;
    private String wifiName;
    private Paint glassHighlightPaint;
    private boolean isDarkMode;
    private TextView clockText;
    private TextView dateText;
    private String lastFocusedKey;
    
    public AppleTVUI(Context context) {
        super(context);
        init(context);
    }
    
    private void init(Context context) {
        isDarkMode = true;
        apps = new ArrayList<>();
        gridStartRow = 0;
        gridEndRow = gridRows - 1;
        
        // Initialize paint objects
        paint = new Paint();
        paint.setColor(Color.parseColor("#FF000000"));
        paint.setAntiAlias(true);
        
        highlightPaint = new Paint();
        highlightPaint.setColor(Color.parseColor("#FF1E90FF"));
        highlightPaint.setAntiAlias(true);
        
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(18);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        dockPaint = new Paint();
        dockPaint.setColor(Color.parseColor("#1A1A1A"));
        dockPaint.setAntiAlias(true);
        
        topBarPaint = new Paint();
        topBarPaint.setColor(Color.parseColor("#1A1A1A"));
        topBarPaint.setAntiAlias(true);
        
        glassHighlightPaint = new Paint();
        glassHighlightPaint.setColor(Color.parseColor("#20FFFFFF"));
        glassHighlightPaint.setAntiAlias(true);
        
        // Initialize app list
        loadApps(context);
    }
    
    private void loadApps(Context context) {
        try {
            apps.add(new AppItem("Netflix", "com.netflix.ninja", "#E50914"));
            apps.add(new AppItem("YouTube", "com.google.android.youtube.tv", "#FF0000"));
            apps.add(new AppItem("Disney+", "com.disney.disneyplus", "#0063E5"));
            apps.add(new AppItem("Prime Video", "com.amazon.amazonvideo.livingroom", "#00A8E1"));
            apps.add(new AppItem("HBO Max", "com.hbo.hbonow", "#9900FF"));
            apps.add(new AppItem("Pluto TV", "tv.pluto.android", "#00FF7F"));
        } catch (Exception e) {
            Log.e(TAG, "Error loading apps: " + e.getMessage());
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;
        
        // Calculate layout
        topBarHeight = screenHeight * 0.05f;
        dockHeight = screenHeight * 0.08f;
        dockY = screenHeight - dockHeight;
        gridY = topBarHeight + (screenHeight - topBarHeight - dockHeight) * 0.5f;
        
        // Calculate tile size and spacing
        gridColumns = 6;
        gridRows = 6;
        gridSpacing = screenWidth * 0.02f;
        tileSize = (screenWidth - gridSpacing * (gridColumns + 1)) / gridColumns;
        
        // Calculate grid rows
        int gridAvailableHeight = (int)(screenHeight - topBarHeight - dockHeight);
        int usableGridRows = (int)(gridAvailableHeight / (tileSize + gridSpacing));
        gridEndRow = Math.min(usableGridRows - 1, gridRows - 1);
        
        // Set up clock
        updateClock();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw pitch black background
        canvas.drawColor(Color.BLACK);
        
        // Draw top bar
        canvas.drawRect(0, 0, screenWidth, topBarHeight, topBarPaint);
        drawTopBarContent(canvas);
        
        // Draw dock
        canvas.drawRect(0, dockY, screenWidth, screenHeight, dockPaint);
        drawDockContent(canvas);
        
        // Draw grid
        drawGridContent(canvas);
    }
    
    private void drawTopBarContent(Canvas canvas) {
        // Draw greeting and clock
        textPaint.setTextSize(16);
        String greeting = "Good " + getTimeOfDay();
        String time = getCurrentTime();
        String date = getCurrentDate();
        
        float greetingX = screenWidth * 0.05f;
        float clockX = screenWidth * 0.5f;
        float settingsX = screenWidth * 0.95f;
        
        canvas.drawText(greeting, greetingX, topBarHeight * 0.7f, textPaint);
        canvas.drawText(time, clockX, topBarHeight * 0.7f, textPaint);
        canvas.drawText(date, clockX, topBarHeight * 0.95f, textPaint);
        
        // Draw settings button
        textPaint.setTextSize(14);
        canvas.drawText("Settings", settingsX, topBarHeight * 0.7f, textPaint);
    }
    
    private void drawDockContent(Canvas canvas) {
        // Draw dock apps
        float dockAppWidth = (screenWidth - gridSpacing * 5) / 6;
        
        for (int i = 0; i < Math.min(6, apps.size()); i++) {
            AppItem app = apps.get(i);
            float appX = gridSpacing + i * (dockAppWidth + gridSpacing);
            float appY = dockY + (dockHeight - tileSize) / 2;
            
            // Draw app icon background
            highlightPaint.setColor(Color.parseColor(app.color));
            canvas.drawRoundRect(new RectF(appX, appY, appX + dockAppWidth, appY + dockHeight), 
                                15, 15, highlightPaint);
            
            // Draw app name
            textPaint.setTextSize(12);
            canvas.drawText(app.name, appX + dockAppWidth / 2, 
                           dockY + dockHeight - 10, textPaint);
        }
    }
    
    private void drawGridContent(Canvas canvas) {
        // Draw grid background
        int gridTop = (int) (gridY - (tileSize / 2)); // FIXED CASTING
        int gridBottom = (int)dockY;
        canvas.drawRect(gridSpacing, gridTop, screenWidth - gridSpacing, gridBottom, topBarPaint);
        
        // Draw grid items
        for (int row = gridStartRow; row <= gridEndRow; row++) {
            for (int col = 0; col < gridColumns; col++) {
                int appIndex = (row - gridStartRow) * gridColumns + col;
                if (appIndex < apps.size()) {
                    AppItem app = apps.get(appIndex);
                    float appX = gridSpacing + col * (tileSize + gridSpacing);
                    float appY = gridY + (row - gridStartRow) * (tileSize + gridSpacing);
                    
                    // Check if focused
                    if (isGridFocused && appIndex == focusedGridIndex) {
                        // Draw glass highlight
                        canvas.drawRoundRect(new RectF(appX - 5, appY - 5, 
                                                      appX + tileSize + 5, appY + tileSize + 5), 
                                            10, 10, glassHighlightPaint);
                    }
                    
                    // Draw app icon
                    highlightPaint.setColor(Color.parseColor(app.color));
                    canvas.drawRoundRect(new RectF(appX, appY, appX + tileSize, appY + tileSize), 
                                        10, 10, highlightPaint);
                    
                    // Draw app name
                    textPaint.setTextSize(14);
                    canvas.drawText(app.name, appX + tileSize / 2, 
                                   appY + tileSize + 20, textPaint);
                }
            }
        }
    }
    
    private String getTimeOfDay() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); // FIXED
        if (hour < 12) return "morning";
        else if (hour < 18) return "afternoon";
        else return "evening";
    }
    
    private String getCurrentTime() {
        return String.format(Locale.getDefault(), "%02d:%02d", 
                            Calendar.getInstance().get(Calendar.HOUR_OF_DAY), // FIXED
                            Calendar.getInstance().get(Calendar.MINUTE)); // FIXED
    }
    
    private String getCurrentDate() {
        return Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) + ", " + // FIXED
               Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + " " + // FIXED
               Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()); // FIXED
    }
    
    public void updateClock() {
        invalidate();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (isGridFocused && currentFocusRow > 0) {
                    currentFocusRow--;
                    invalidate();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (isGridFocused && currentFocusRow < gridEndRow) {
                    currentFocusRow++;
                    invalidate();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (isGridFocused && currentFocusCol > 0) {
                    currentFocusCol--;
                    invalidate();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (isGridFocused && currentFocusCol < gridColumns - 1) {
                    currentFocusCol++;
                    invalidate();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (isGridFocused) {
                    int appIndex = (currentFocusRow - gridStartRow) * gridColumns + currentFocusCol;
                    if (appIndex < apps.size()) {
                        Toast.makeText(getContext(), "Launching " + apps.get(appIndex).name, 
                                      Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void onClick(View v) {
        // Handle clicks
    }
    
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // Handle focus changes
    }
    
    // Helper class for app items
    private class AppItem {
        String name;
        String packageName;
        String color;
        
        public AppItem(String name, String packageName, String color) {
            this.name = name;
            this.packageName = packageName;
            this.color = color;
        }
    }
}
