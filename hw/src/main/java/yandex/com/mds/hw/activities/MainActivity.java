package yandex.com.mds.hw.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import yandex.com.mds.hw.ColorDatabaseHelper;
import yandex.com.mds.hw.ColorListAdapter;
import yandex.com.mds.hw.R;

public class MainActivity extends AppCompatActivity {
    public static final int COLOR_REQUEST_CODE = 1;
    private static final String CURRENT_POSITION = "currentPosition";
    private static final String CURRENT_SORT = "currentSort";

    private enum Sort {
        DateOfCreation, Alphabetic
    }

    private Sort currentSort = Sort.DateOfCreation;
    ColorDatabaseHelper dbHelper = new ColorDatabaseHelper(this);
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Cursor c = dbHelper.getColors(currentSort == Sort.Alphabetic);
        CursorAdapter adapter = new ColorListAdapter(this, c);

        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int colorId = ((ColorListAdapter.ViewHolder) view.getTag()).getId();
                showColorActivity(colorId);
            }
        });
    }

    private void showColorActivity(int colorId) {
        startActivityForResult(ColorActivity.getInstance(this, colorId), COLOR_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COLOR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Cursor c = dbHelper.getColors(currentSort == Sort.Alphabetic);
                ((ColorListAdapter) listView.getAdapter()).changeCursor(c);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.getItem(0);
        initSortButton(item, currentSort);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add: {
                showColorActivity(-1);
                return true;
            }
            case R.id.action_sort: {
                currentSort = switchSort(currentSort);
                Cursor c = dbHelper.getColors(currentSort == Sort.Alphabetic);
                ((ColorListAdapter) listView.getAdapter()).changeCursor(c);
                initSortButton(item, currentSort);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private Sort switchSort(Sort currentSort) {
        if (currentSort == Sort.Alphabetic)
            return Sort.DateOfCreation;
        return Sort.Alphabetic;
    }

    private void initSortButton(MenuItem item, Sort currentSort) {
        item.setTitle(currentSort == Sort.Alphabetic ? R.string.sort_date : R.string.sort_alphabetically);
        item.setIcon(currentSort == Sort.Alphabetic ? R.drawable.ic_sort : R.drawable.ic_sort_by_alpha);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        listView.smoothScrollToPosition(savedInstanceState.getInt(CURRENT_POSITION));

        currentSort = Sort.valueOf(savedInstanceState.getString(CURRENT_SORT));
        Cursor c = dbHelper.getColors(currentSort == Sort.Alphabetic);
        ((ColorListAdapter) listView.getAdapter()).changeCursor(c);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_POSITION, listView.getFirstVisiblePosition());
        outState.putString(CURRENT_SORT, currentSort.name());
    }
}