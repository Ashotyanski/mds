package yandex.com.mds.hw.colors.query.presenters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.colorpicker.ColorPickerDialog;
import yandex.com.mds.hw.colorpicker.colorview.CircleColorView;
import yandex.com.mds.hw.colors.query.clauses.ColorFilter;

public class ColorFilterPresenter {
    private Context mContext;
    private ViewGroup root;

    private CircleColorView colorView;
    private Switch colorSwitch;

    public ColorFilterPresenter(final Context context, ViewGroup root) {
        mContext = context;
        this.root = root;
        colorView = (CircleColorView) root.findViewById(R.id.filter_color);
        colorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog dialog = new ColorPickerDialog(mContext, colorView.getColor());
                dialog.setOnColorSavedListener(new ColorPickerDialog.OnColorSavedListener() {
                    @Override
                    public void onColorSave(int color) {
                        colorView.setColor(color);
                    }
                });
                dialog.show();
            }
        });

        colorSwitch = (Switch) root.findViewById(R.id.switch_color);
        colorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleColorFilter(isChecked);
            }
        });
        colorSwitch.setChecked(false);
        toggleColorFilter(false);
    }

    public ColorFilter getColorFilter() {
        ColorFilter filter = null;
        if (colorSwitch.isChecked()) {
            filter = new ColorFilter(colorView.getColor());
        }
        return filter;
    }

    private void toggleColorFilter(boolean isEnable) {
        colorView.setClickable(isEnable);
    }
}
