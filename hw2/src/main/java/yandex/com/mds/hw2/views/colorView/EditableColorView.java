package yandex.com.mds.hw2.views.colorView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import yandex.com.mds.hw2.views.ColorPickerView;
import yandex.com.mds.hw2.R;

import static android.content.Context.VIBRATOR_SERVICE;

public class EditableColorView extends DefaultColorView {
    private GestureDetector detector;
    private Rect rect = new Rect();
    boolean isEditing = false;

    public EditableColorView(Context context) {
        super(context);
    }

    public EditableColorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EditableColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EditableColorView(Context context, int defaultColor, final ColorPickerView.OnPickListener listener) {
        super(context, defaultColor);
        float size = getContext().getResources().getDimension(R.dimen.default_color_size);
        rect = new Rect(0, 0, (int) size, (int) size);

        detector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                setColorToDefault();
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                listener.onPick(getColor());
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                isEditing = true;
                getParent().requestDisallowInterceptTouchEvent(true);
                vibrate(VIBRATE_SHORT);
                super.onLongPress(e);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                isEditing = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            case MotionEvent.ACTION_MOVE:
                if (isEditing) {
                    if (rect.contains((int) event.getX(), (int) event.getY())) {
                        float deltaX = event.getX() - rect.width() / 2;
                        float deltaY = event.getY();
                        editColor(deltaX, deltaY);
                    } else {
                        vibrate(VIBRATE_LONG);
                    }
                }
        }
        return true;
    }

    private void editColor(float deltaX, float deltaY) {
        float[] hsv = new float[3];
        Color.colorToHSV(getDefaultColor(), hsv);

        //edit hue
        float deltaHue = deltaX / 16;
        hsv[0] += deltaHue;

        //edit value
        float deltaValue = deltaY / rect.height();
        hsv[2] -= deltaValue;

        setColor(Color.HSVToColor(hsv));
        invalidate();
    }

    private static final int TIMEOUT_VIBRATE = 1000;
    private static final int VIBRATE_LONG = 50;
    private static final int VIBRATE_SHORT = 10;

    private long lastVibrate = 0;

    private void vibrate(int duration) {
        if (System.currentTimeMillis() - lastVibrate > TIMEOUT_VIBRATE) {
            Log.i("VIBRATION", "Vibrated for " + duration);
            ((Vibrator) getContext().getSystemService(VIBRATOR_SERVICE)).vibrate(duration);
            lastVibrate = System.currentTimeMillis();
        }
    }
}
