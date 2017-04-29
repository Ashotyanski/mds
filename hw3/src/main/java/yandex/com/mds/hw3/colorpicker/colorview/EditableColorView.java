package yandex.com.mds.hw3.colorpicker.colorview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import yandex.com.mds.hw3.colorpicker.ColorPickerView;
import yandex.com.mds.hw3.utils.VibrationUtils;

/**
 * An editable ColorView
 */
public class EditableColorView extends DefaultColorView {
    private Rect boundingRect = new Rect();
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
            if (onPickListener != null)
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        boundingRect.set(0, 0, getWidth(), getHeight());
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
                    if (boundingRect.contains((int) event.getX(), (int) event.getY())) {
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

        float deltaValue = (deltaY - boundingRect.height() / 2) / (boundingRect.height() / 2);
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
