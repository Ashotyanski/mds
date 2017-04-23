package yandex.com.mds.hw3dev.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import yandex.com.mds.hw3dev.ColorDatabaseHelper;
import yandex.com.mds.hw3dev.ColorListAdapter;
import yandex.com.mds.hw3dev.R;
import yandex.com.mds.hw3dev.activities.ColorActivity;

public class MainActivityFragment extends Fragment {
    private static final int COLOR_REQUEST_CODE = 1;
    ColorDatabaseHelper dbHelper = new ColorDatabaseHelper(getContext());
    private ListView listView;
    private boolean isDualPane;

    public MainActivityFragment() {
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Cursor c = dbHelper.getColors();
        CursorAdapter adapter = new ColorListAdapter(getContext(), c, false);

        listView = (ListView) getActivity().findViewById(R.id.list);
        listView.setAdapter(adapter);

        View colorFrame = getActivity().findViewById(R.id.color_frame);

        isDualPane = colorFrame != null && colorFrame.getVisibility() == View.VISIBLE;
        if (isDualPane) {
            showColor(0);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showColor(position);
            }
        });
    }

    private void showColor(int id) {
        if (isDualPane) {
            ColorActivityFragment colorFragment = ColorActivityFragment.newInstance(id);
            FragmentTransaction ft = getFragmentManager().beginTransaction().replace(R.id.color_frame, colorFragment);
            ft.commit();
        } else {
            Intent intent = new Intent(getActivity(), ColorActivity.class);
            intent.putExtra("id", id);
            startActivityForResult(intent, COLOR_REQUEST_CODE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // TODO: 23.04.2017 set menu
        if (id == R.id.action_settings) {
            showColor(-1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COLOR_REQUEST_CODE) {
            // TODO: 23.04.2017 update database
        }
    }
}
