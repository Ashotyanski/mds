package yandex.com.mds.hw3dev.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import yandex.com.mds.hw3dev.R;
import yandex.com.mds.hw3dev.colorpicker.colorview.ColorView;

public class ColorActivityFragment extends Fragment {
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String COLOR = "color";

    private OnFragmentInteractionListener mListener;
    private EditText titleView;
    private EditText descriptionView;
    private ColorView colorView;

    public ColorActivityFragment() {
    }

    public static ColorActivityFragment newInstance(int id) {
        ColorActivityFragment fragment = new ColorActivityFragment();
        Bundle args = new Bundle();
        args.putInt(ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_color, container, false);
        titleView = (EditText) v.findViewById(R.id.text);
        descriptionView = (EditText) v.findViewById(R.id.description);
        colorView = (ColorView) v.findViewById(R.id.color);

        if (getArguments() != null) {
            int id = getArguments().getInt(ID);
            titleView.setText(getArguments().getString(TITLE));
            descriptionView.setText(getArguments().getString(DESCRIPTION));
            colorView.setColor(getArguments().getInt(COLOR));
        }

        colorView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch a colorpicker
                        ColorPickerDialog colorPicker = ColorPickerDialog.newInstance(colorView.getColor(), 0);
                        colorPicker.show(getActivity().getSupportFragmentManager(), "COLOR_PICKER");
                    }
                });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TITLE, String.valueOf(titleView.getText()));
        outState.putString(DESCRIPTION, String.valueOf(descriptionView.getText()));
        outState.putInt(COLOR, colorView.getColor());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            titleView.setText(savedInstanceState.getString(TITLE));
            descriptionView.setText(savedInstanceState.getString(DESCRIPTION));
            colorView.setColor(savedInstanceState.getInt(COLOR));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
