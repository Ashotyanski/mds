package yandex.com.mds.hw3dev.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import static yandex.com.mds.hw3dev.MainApplication.getContext;

public class GradientUtils {
    private static Drawable gradient;

    public static Drawable getGradient() {
        if (gradient == null)
            gradient = createGradient();

        return gradient;
    }

    /**
     * Creates a drawable with HSV based gradient.
     */
    private static Drawable createGradient() {
//        Drawable drawable = getContext().getResources().getDrawable(R.drawable.rgb_hsv);

        // Generate a bitmap with gradient
        Bitmap bitmap = Bitmap.createBitmap(720, 100, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < bitmap.getWidth(); i++)
            for (int j = 0; j < bitmap.getHeight(); j++)
                // saturation 0.5 just to make ColorViews more contrast (in edit mode too)
                bitmap.setPixel(i, j, Color.HSVToColor(new float[]{i / 2, 0.5f, 1}));
        return new BitmapDrawable(getContext().getResources(), bitmap);
    }
}
