package yandex.com.mds.hw2.views.colorView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import yandex.com.mds.hw2.R;
import yandex.com.mds.hw2.utils.VibrationUtils;
import yandex.com.mds.hw2.views.ColorPickerView;

/**
 * An editable ColorView
 */
public class EditableColorView extends DefaultColorView {
    private float size = getContext().getResources().getDimension(R.dimen.default_color_size);
    private float lastX, lastY;
    private Rect rect = new Rect(0, 0, (int) size, (int) size);
    private boolean isEditing = false;

    private ColorPickerView.OnPickListener onPickListener;
    private GestureDetector detector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            setColorToDefault();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            onPickListener.onPick(getColor());
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            isEditing = true;
            getParent().requestDisallowInterceptTouchEvent(true);
            VibrationUtils.vibrate(VibrationUtils.VIBRATE_SHORT);
            super.onLongPress(e);
        }
    });

    public EditableColorView(Context context) {
        super(context);
    }

    public EditableColorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EditableColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
                        editColor(event.getX(), event.getY());
                    } else {
                        VibrationUtils.vibrate(VibrationUtils.VIBRATE_LONG);
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

        float deltaValue = (deltaY - rect.height() / 2) / (rect.height() / 2);
        if (deltaValue < 0)
            //edit saturation
            hsv[1] += deltaValue;
        else
            //edit value
            hsv[2] -= deltaValue;

        setColor(Color.HSVToColor(hsv));
        invalidate();
    }


    public void setOnPickListener(ColorPickerView.OnPickListener onPickListener) {
        this.onPickListener = onPickListener;
    }
}
