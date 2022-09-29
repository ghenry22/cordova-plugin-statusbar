package org.apache.cordova.statusbar;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

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


    private Activity activity;
    private int usableHeightPrevious;
    private boolean layoutListenerApplied;

    public void assistActivity(Activity activity) {
        this.activity = activity;
    }

    private void possiblyResizeChildOfContent(View childOfContent) {
        int usableHeightNow = computeUsableHeight(childOfContent);
        if (usableHeightNow != this.usableHeightPrevious) {
            ViewGroup.LayoutParams params = childOfContent.getLayoutParams();
            params.height = usableHeightNow;
            childOfContent.setLayoutParams(params);
            childOfContent.requestLayout();
            this.usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight(View childOfContent) {
        Rect r = new Rect();
        childOfContent.getWindowVisibleDisplayFrame(r);

        boolean fullScreen = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        boolean translucentStatusBar = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) != 0
                || (activity.getWindow().getAttributes().flags & (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)) != 0;

        int usableHeight = r.bottom - r.top;

        if(translucentStatusBar || fullScreen){
            usableHeight = r.bottom;
        }

        return usableHeight;
    }

    public void applyGlobalLayoutListener() {
        if (layoutListenerApplied) {
            return;
        }

        activity.getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                // This should only run when the view hierarchy is ready
                // We're still going to be safe here, just in case
                ViewGroup content = activity.findViewById(android.R.id.content);
                if (content == null) {
                    return;
                }

                final View childOfContent = content.getChildAt(0);
                if (childOfContent == null) {
                    return;
                }

                layoutListenerApplied = true;
                childOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        possiblyResizeChildOfContent(childOfContent);
                    }
                });
            }
        });
    }
}
