package yandex.com.mds.hw2;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import yandex.com.mds.hw2.views.ColorPickerView;
import yandex.com.mds.hw2.views.colorView.ColorView;

/**
 * Dialog that shows a colorpicker
 */
public class ColorPickerDialog extends DialogFragment {
    private static final String COLOR = "COLOR";
    private static final String ID = "ID";

    private ColorPickerView colorPickerView;
    private ColorView resultColor;
    private Button pickButton;

    private int currentId;

    private OnColorSavedListener colorSavedListener;

    public ColorPickerDialog() {
    }

    public static ColorPickerDialog newInstance(int color, int id) {
        ColorPickerDialog fragment = new ColorPickerDialog();
        Bundle args = new Bundle();
        args.putInt(COLOR, color);
        args.putInt(ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_color_picker_dialog, container, false);
        colorPickerView = (ColorPickerView) root.findViewById(R.id.color_picker_view);
        resultColor = (ColorView) root.findViewById(R.id.color_view);
        pickButton = (Button) root.findViewById(R.id.pick_button);

        if (getArguments() != null) {
            resultColor.setColor(getArguments().getInt(COLOR));
            currentId = getArguments().getInt(ID);
        }

        colorPickerView.setOnPickListener(new ColorPickerView.OnPickListener() {
            @Override
            public void onPick(int color) {
                resultColor.setColor(color);
            }
        });

        pickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorSavedListener.onColorSave(resultColor.getColor(), currentId);
                dismiss();
            }
        });

        getDialog().setTitle("Pick a color");
        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnColorSavedListener) {
            colorSavedListener = (OnColorSavedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnColorSavedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        colorSavedListener = null;
    }

    /**
     * Interface definition for a callback to be invoked when a color is saved.
     */
    interface OnColorSavedListener {
        /**
         * Called when the color has been saved.
         *
         * @param color  saved color.
         * @param viewId id of the view, which stores the color.
         */
        void onColorSave(int color, int viewId);
    }
}
