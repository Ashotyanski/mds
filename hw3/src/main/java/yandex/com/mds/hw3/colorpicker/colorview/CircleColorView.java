package yandex.com.mds.hw3.colorpicker.colorview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class CircleColorView extends ColorView {
    Paint paint = new Paint();

    @Override
    public void setColor(int color) {
        super.setColor(color);
        paint.setColor(color);
    }

    public CircleColorView(Context context) {
        super(context);
    }

    public CircleColorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = canvas.getHeight();
        int width = canvas.getWidth();
        canvas.drawCircle(width / 2, height / 2, height < width ? height / 2 : width / 2, paint);
    }
}
