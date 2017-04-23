package yandex.com.mds.hw3.activities;

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

import yandex.com.mds.hw3.ColorDatabaseHelper;
import yandex.com.mds.hw3.ColorListAdapter;
import yandex.com.mds.hw3.R;

public class MainActivity extends AppCompatActivity {
    public static final int COLOR_REQUEST_CODE = 1;
    private static final String CURRENT_POSITION = "currentPosition";

    ColorDatabaseHelper dbHelper = new ColorDatabaseHelper(this);
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Cursor c = dbHelper.getColors();
        CursorAdapter adapter = new ColorListAdapter(this, c);

        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showColorActivity(position);
            }
        });
    }

    private void showColorActivity(int position) {
        Intent intent = new Intent(this, ColorActivity.class);
        if (position >= 0)
            intent.putExtra("id", position);
        startActivityForResult(intent, COLOR_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COLOR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Cursor c = dbHelper.getColors();
                ColorListAdapter adapter = new ColorListAdapter(this, c);
                listView.setAdapter(adapter);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            showColorActivity(-1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        listView.smoothScrollToPosition(savedInstanceState.getInt(CURRENT_POSITION));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_POSITION, listView.getFirstVisiblePosition());
    }
}