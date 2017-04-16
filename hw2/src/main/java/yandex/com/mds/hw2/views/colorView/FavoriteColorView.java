package yandex.com.mds.hw2.views.colorView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;

import yandex.com.mds.hw2.ColorPickerDialog;

/**
 * A view that stores users favorite color.
 */
public class FavoriteColorView extends ColorView {
    private int id;

    public FavoriteColorView(Context context) {
        super(context);
    }

    public FavoriteColorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FavoriteColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch a colorpicker
                        ColorPickerDialog colorPicker = ColorPickerDialog.newInstance(getColor(), id);
                        colorPicker.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "COLOR_PICKER");
                    }
                });

    }


    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec)
        );
    }
}
