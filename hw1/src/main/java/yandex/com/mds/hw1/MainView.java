package yandex.com.mds.hw1;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;


// A custom view with loggers for listening its lifecycle events.
// Note that overridden methods are protected, so this class had to be created anyway.

public class MainView extends TextView {

    private static final String TAG = "VIEW";

    public MainView(Context context) {
        super(context);
    }

    public MainView(Context context, String text) {
        super(context);
        setText(text);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "Attached to window");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "Detached from window");
    }

    //	Called when this view should assign a size and position to all of its children.
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d(TAG, "Positioned");
    }

    //  Called to determine the size requirements for this view and all of its children.
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(TAG, "Measured");
    }
}
