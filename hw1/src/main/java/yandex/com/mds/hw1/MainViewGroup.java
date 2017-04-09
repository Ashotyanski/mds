package yandex.com.mds.hw1;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

// Custom ViewGroup for tracking view addition and removal

public class MainViewGroup extends LinearLayout {

    private static final String VIEW_GROUP_TAG = "VIEWGROUP";

    public MainViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        initListeners();
    }

    private void initListeners() {
        addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                Log.d(VIEW_GROUP_TAG, "View attached");
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                Log.d(VIEW_GROUP_TAG, "View detached");
            }
        });
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        Log.d(VIEW_GROUP_TAG, "View added");
        Toast.makeText(getContext(), "View added", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        Log.d(VIEW_GROUP_TAG, "View removed");
        Toast.makeText(getContext(), "View removed", Toast.LENGTH_SHORT).show();
    }
}
