package yandex.com.mds.hw2.views.colorView;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import yandex.com.mds.hw2.ColorPickerDialog;

/**
 * A view that stores users favorite color.
 */
public class FavoriteColorView extends ColorView {
    private int id;

    public FavoriteColorView(Context context, int color, int id) {
        this(context);
        setColor(color);
        this.id = id;
    }

    public FavoriteColorView(Context context) {
        super(context);
        setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(), "Color " + getColor(), Toast.LENGTH_SHORT).show();
                        // Launch a colorpicker
                        ColorPickerDialog colorPicker = ColorPickerDialog.newInstance(getColor(), id);
                        colorPicker.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "COLOR_PICKER");
                    }
                });
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
