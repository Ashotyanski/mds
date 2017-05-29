package yandex.com.mds.hw;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class MainDrawerLayout extends DrawerLayout {
    private boolean m_disallowIntercept;

    public MainDrawerLayout(final Context context) {
        super(context);
    }

    public MainDrawerLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public MainDrawerLayout(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        return !m_disallowIntercept && super.onInterceptTouchEvent(ev);
    }

    @Override
    public void setDrawerLockMode(int lockMode) {
        super.setDrawerLockMode(lockMode);
        m_disallowIntercept = (lockMode == LOCK_MODE_LOCKED_OPEN);
    }
}
