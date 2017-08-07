package org.apache.cordova.statusbar;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class ActivityAssistant {

    // Original: http://stackoverflow.com/a/19494006/5317401
    // Additionally: fixed a bug where on some devices it shows the gap (blank screen) between keyboard and layout.
    // For more information, see https://code.google.com/p/android/issues/detail?id=5497
    // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

    private static ActivityAssistant _instance;

    public static ActivityAssistant getInstance(){
        if (_instance == null)
            _instance = new ActivityAssistant();

        return _instance;
    }


    public void assistActivity(Activity activity) {
        this.activity = activity;
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        this.mChildOfContent = content.getChildAt(0);
        this.frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();

    }

    private Activity activity;
    private View mChildOfContent;
    private int usableHeightPrevious;
    @SuppressWarnings("FieldCanBeLocal")
    private FrameLayout.LayoutParams frameLayoutParams;
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;
    private boolean layoutListenerApplied;

    private ActivityAssistant() {
        this.onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                possiblyResizeChildOfContent();
            }
        };
        this.layoutListenerApplied = false;
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != this.usableHeightPrevious) {
            this.frameLayoutParams.height = usableHeightNow;
            this.mChildOfContent.setLayoutParams(this.frameLayoutParams);
            this.mChildOfContent.requestLayout();
            this.usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        this.mChildOfContent.getWindowVisibleDisplayFrame(r);

        boolean fullScreen = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        boolean translucentStatusBar = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) != 0;

        int usableHeight = r.bottom - r.top;

        if(translucentStatusBar || fullScreen){
            usableHeight = r.bottom;
        }

        return usableHeight;
    }

    public void applyGlobalLayoutListener(){
        if(this.onGlobalLayoutListener == null){
            this.onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    possiblyResizeChildOfContent();
                }
            };

            this.layoutListenerApplied = false;
        }

        if(!this.layoutListenerApplied){
            this.mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(this.onGlobalLayoutListener);
            this.layoutListenerApplied = true;
        }
    }

    public void removeGlobalLayoutListener(){
        if(this.layoutListenerApplied) {
            this.mChildOfContent.getViewTreeObserver().removeOnGlobalLayoutListener(this.onGlobalLayoutListener);
            this.layoutListenerApplied = false;
        }
    }
}
