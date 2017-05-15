package yandex.com.mds.hw.colorpicker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.colorpicker.colorview.ColorView;

/**
 * Dialog that shows a colorpicker
 */
public class ColorPickerDialog extends AlertDialog {
    private static final String COLOR = "COLOR";

    private ColorPickerView colorPickerView;
    private ColorView resultColor;
    private Button pickButton;

    private OnColorSavedListener mColorSavedListener;

    public ColorPickerDialog(@NonNull Context context, int color) {
        this(context, 0, color, null);
    }

    public ColorPickerDialog(@NonNull Context context, @StyleRes int themeResId, int color, OnColorSavedListener colorSavedListener) {
        super(context, themeResId);
        mColorSavedListener = colorSavedListener;
        View root = LayoutInflater.from(getContext()).inflate(R.layout.fragment_color_picker_dialog, null);
        setView(root);
        colorPickerView = (ColorPickerView) root.findViewById(R.id.color_picker_view);
        resultColor = (ColorView) root.findViewById(R.id.color_view);
        pickButton = (Button) root.findViewById(R.id.pick_button);

        resultColor.setColor(color);
        colorPickerView.setOnPickListener(new ColorPickerView.OnPickListener() {
            @Override
            public void onPick(int color) {
                resultColor.setColor(color);
            }
        });
        pickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColorSavedListener.onColorSave(resultColor.getColor());
                dismiss();
            }
        });
    }

    public void setOnColorSavedListener(OnColorSavedListener colorSavedListener) {
        mColorSavedListener = colorSavedListener;
    }

    /**
     * Interface definition for a callback to be invoked when a color is saved.
     */
    public interface OnColorSavedListener {
        /**
         * Called when the color has been saved.
         *
         * @param color saved color.
         */
        void onColorSave(int color);
    }
}
