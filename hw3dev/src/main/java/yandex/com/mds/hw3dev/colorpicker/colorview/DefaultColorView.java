package yandex.com.mds.hw3dev.colorpicker.colorview;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * A ColorView that has a default color
 */
public class DefaultColorView extends ColorView {
    private int defaultColor = Color.WHITE;

    public DefaultColorView(Context context) {
        super(context);
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

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
        setColorToDefault();
    }

    public void setColorToDefault() {
        setColor(defaultColor);
    }
}