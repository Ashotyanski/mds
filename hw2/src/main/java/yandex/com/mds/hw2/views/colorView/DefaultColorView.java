package yandex.com.mds.hw2.views.colorView;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * A ColorView that has a default color
 */
public class DefaultColorView extends ColorView {
    // cannot be final because of the "constructors hell"
    private int defaultColor = Color.WHITE;

    public DefaultColorView(Context context) {
        super(context);
    }

    public DefaultColorView(Context context, int defaultColor) {
        super(context);
        this.defaultColor = defaultColor;
        setColor(defaultColor);
    }

    public DefaultColorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DefaultColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public void setColorToDefault() {
        setColor(defaultColor);
    }
}