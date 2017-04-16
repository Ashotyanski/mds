package yandex.com.mds.hw2.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import yandex.com.mds.hw2.R;
import yandex.com.mds.hw2.views.colorView.EditableColorView;

public class ColorPickerView extends HorizontalScrollView {
    private LinearLayout linearLayout;
    private OnPickListener onPickListener;// = (color) -> Toast.makeText(getContext(), String.valueOf(color), Toast.LENGTH_SHORT).show();

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context c) {
        View v = inflate(c, R.layout.color_picker_view, this);
        linearLayout = (LinearLayout) v.findViewById(R.id.colors_layout);
        Drawable gradient = createGradient();
        linearLayout.setBackground(gradient);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initColorsBar();
    }

    private void initColorsBar() {
        int colorViewSize = (int) getResources().getDimension(R.dimen.default_color_size);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(colorViewSize, colorViewSize);
        int MARGIN = colorViewSize / 4;
        params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);

        for (int i = 0; i < 16; i++) {
            int currentHue = 360 / 16 / 2 + 360 / 16 * i;
            EditableColorView colorView = new EditableColorView(getContext());
            colorView.setDefaultColor(Color.HSVToColor(new float[]{currentHue, 1, 1}));
            colorView.setOnPickListener(onPickListener);
            colorView.setLayoutParams(params);
            linearLayout.addView(colorView);
        }
    }

    /**
     * Creates a drawable with HSV based gradient.
     */
    private Drawable createGradient() {
//        Drawable drawable = getContext().getResources().getDrawable(R.drawable.rgb_hsv);

        // Generate a bitmap with gradient
        Bitmap bitmap = Bitmap.createBitmap(720, 100, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < bitmap.getWidth(); i++)
            for (int j = 0; j < bitmap.getHeight(); j++)
                // saturation 0.5 just to make ColorViews more contrast (in edit mode too)
                bitmap.setPixel(i, j, Color.HSVToColor(new float[]{i / 2, 0.5f, 1}));
        return new BitmapDrawable(getResources(), bitmap);
    }

    public void setOnPickListener(OnPickListener onPickListener) {
        this.onPickListener = onPickListener;
    }

    /**
     * Interface definition for a callback to be invoked when a color is picked.
     */
    public interface OnPickListener {
        /**
         * Called when a ColorView has been clicked.
         *
         * @param color picked color.
         */
        void onPick(int color);
    }
}